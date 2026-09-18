package com.budaos.system.service.impl;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.budaos.system.common.enums.AccountTypeEnum;
import com.budaos.common.core.code.UserErrorCodes;
import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.CurrentUser;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.common.core.constant.RedisKeyConstants;
import com.budaos.common.core.util.IpRegionUtil;
import com.budaos.common.redis.util.RedisUtil;
import com.budaos.common.security.encrypt.ApiCipherUtil;
import com.budaos.system.constant.LoginRecordResultEnum;
import com.budaos.system.constant.SystemConstant;
import com.budaos.system.dao.StaffDao;
import com.budaos.system.dao.LoginRecordDao;
import com.budaos.system.dao.NavMenuDao;
import com.budaos.system.dao.AuthRoleStaffDao;
import com.budaos.system.dao.AuthRoleMenuDao;
import com.budaos.system.domain.form.AuthLoginForm;
import com.budaos.system.domain.form.OnlineUserQueryForm;
import com.budaos.system.domain.vo.AuthLoginVO;
import com.budaos.system.domain.SessionEmployee;
import com.budaos.system.domain.entity.StaffEntity;
import com.budaos.system.domain.entity.LoginRecordEntity;
import com.budaos.system.domain.entity.NavMenuEntity;
import com.budaos.system.domain.entity.AuthRoleStaffEntity;
import com.budaos.system.domain.entity.AuthRoleMenuEntity;
import com.budaos.system.domain.vo.VerifyCodeVO;
import com.budaos.system.domain.vo.OrganizationVO;
import com.budaos.system.domain.vo.NavMenuVO;
import com.budaos.system.domain.vo.OnlineUserVO;
import com.budaos.system.service.OrganizationService;
import com.budaos.system.service.StaffService;
import com.budaos.system.service.AuthService;
import com.budaos.system.service.AuthRoleMenuService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;

/**
 * 登录服务实现
 *
 * @author budaos
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService, StpInterface {

    @Lazy
    @Resource
    private StaffService employeeService;

    @Lazy
    @Resource
    private OrganizationService departmentService;

    @Resource
    private StaffDao employeeDao;

    @Resource
    private AuthRoleStaffDao roleEmployeeDao;

    @Resource
    private AuthRoleMenuDao roleMenuDao;

    @Resource
    private NavMenuDao menuDao;

    @Resource
    private LoginRecordDao loginLogDao;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private AuthRoleMenuService roleMenuService;

    @Resource
    private com.budaos.system.service.SecureLoginService securityLoginService;

    @Resource
    private com.budaos.system.manager.LoginCacheManager loginCacheManager;


    @Override
    public ApiResult<AuthLoginVO> login(AuthLoginForm loginForm, String ip, String userAgent) {
        log.info("========== 开始登录流程 ==========");
        log.info("登录信息 - loginName: {}, IP: {}", loginForm.getLoginName(), ip);

        // 验证登录名格式
        if (loginForm.getLoginName() == null || loginForm.getLoginName().trim().isEmpty()) {
            log.warn("登录名为空");
            return ApiResult.userErrorParam("登录名不能为空！");
        }

        // 验证密码
        if (loginForm.getPassword() == null || loginForm.getPassword().trim().isEmpty()) {
            log.warn("密码为空");
            return ApiResult.userErrorParam("密码不能为空！");
        }

        // 查询用户
        StaffEntity employeeEntity = employeeService.getByLoginNameOrPhone(loginForm.getLoginName());
        log.info("用户查询结果 - 是否为空: {}", employeeEntity == null);

        if (employeeEntity == null) {
            log.warn("用户不存在 - loginNameOrPhone: {}", loginForm.getLoginName());
            recordLoginLog(null, loginForm.getLoginName(), ip, userAgent, LoginRecordResultEnum.LOGIN_FAIL.getValue(), "用户名或密码错误");
            return ApiResult.userErrorParam("登录名或密码错误！");
        }

        // 验证账号状态
        if (employeeEntity.getDeletedFlag()) {
            log.warn("账号已删除 - employeeId: {}", employeeEntity.getEmployeeId());
            recordLoginLog(employeeEntity, ip, userAgent, LoginRecordResultEnum.LOGIN_FAIL.getValue(), "账号已被删除");
            return ApiResult.userErrorParam("您的账号已被删除,请联系工作人员！");
        }

        if (employeeEntity.getDisabledFlag()) {
            log.warn("账号已禁用 - employeeId: {}", employeeEntity.getEmployeeId());
            recordLoginLog(employeeEntity, ip, userAgent, LoginRecordResultEnum.LOGIN_FAIL.getValue(), "账号已被禁用");
            return ApiResult.userErrorParam("您的账号已被禁用,请联系工作人员！");
        }

        // 验证密码（使用 Argon2 加密）
        // 1. 先解密前端加密的密码（使用Common模块的加密工具）
        String requestPassword = ApiCipherUtil.decrypt(loginForm.getPassword());
        log.info("解密后的密码长度: {}, 是否为空: {}",
                requestPassword != null ? requestPassword.length() : "null",
                requestPassword == null || requestPassword.isEmpty());

        // 按照等保登录要求，进行登录失败次数校验
        com.budaos.common.core.domain.ApiResult<com.budaos.system.domain.entity.LoginAttemptEntity> loginFailEntityResponseDTO =
            securityLoginService.checkLogin(employeeEntity.getEmployeeId(), AccountTypeEnum.ADMIN_EMPLOYEE.getValue());
        if (!loginFailEntityResponseDTO.getOk()) {
            log.info("登录失败次数超限，用户ID: {}, 用户名: {}",
                    employeeEntity.getEmployeeId(), employeeEntity.getLoginName());
            recordLoginLog(employeeEntity, ip, userAgent, 0, "登录失败次数超限");
            return com.budaos.common.core.domain.ApiResult.error(loginFailEntityResponseDTO);
        }

        // 2. 生成加盐密码：password_UID大写_UID小写
        String saltPassword = employeeService.generateSaltPassword(requestPassword, employeeEntity.getEmployeeUid());
        log.info("加盐密码: {}", saltPassword);

        // 3. 使用 Argon2 验证密码
        boolean passwordMatch = com.budaos.system.util.PasswordUtil.matches(saltPassword, employeeEntity.getLoginPwd());

        log.info("密码验证 - 是否匹配: {}", passwordMatch);

        if (!passwordMatch) {
            // 记录登录失败
            log.info("密码错误，用户ID: {}, 用户名: {}, IP: {}",
                    employeeEntity.getEmployeeId(), employeeEntity.getLoginName(), ip);
            recordLoginLog(employeeEntity, ip, userAgent, LoginRecordResultEnum.LOGIN_FAIL.getValue(), "用户名或密码错误");
            // 记录等级保护次数
            com.budaos.system.domain.entity.LoginAttemptEntity loginFailEntity =
                securityLoginService.getLoginFailEntity(employeeEntity.getEmployeeId(), AccountTypeEnum.ADMIN_EMPLOYEE.getValue());
            String msg = securityLoginService.recordLoginFail(employeeEntity.getEmployeeId(), AccountTypeEnum.ADMIN_EMPLOYEE.getValue(), employeeEntity.getLoginName(), loginFailEntity);

            return msg == null ? ApiResult.userErrorParam("登录名或密码错误！") : ApiResult.error(UserErrorCodes.LOGIN_FAIL_WILL_LOCK, msg);
        }

        // 登录
        String saTokenLoginId = AccountTypeEnum.ADMIN_EMPLOYEE.getValue() + SystemConstant.COLON + employeeEntity.getEmployeeId();
        StpUtil.login(saTokenLoginId);

        // 获取员工信息 - 使用缓存管理器，这样会将登录信息缓存起来
        SessionEmployee requestEmployee = loginCacheManager.loadLoginInfo(employeeEntity);

        // 移除登录失败
        securityLoginService.removeLoginFail(employeeEntity.getEmployeeId(), AccountTypeEnum.ADMIN_EMPLOYEE.getValue());

        // 将用户基本信息存入 Session（仅存储简单类型）
        StpUtil.getSession().set("userName", employeeEntity.getActualName());
        StpUtil.getSession().set("loginName", employeeEntity.getLoginName());

        // 获取登录结果信息
        String token = StpUtil.getTokenValue();
        AuthLoginVO loginResultVO = getLoginResult(requestEmployee, token);

        // 设置 token
        loginResultVO.setToken(token);

        // 预加载权限到缓存，避免首次访问接口时权限检查失败
        getPermissionList(saTokenLoginId, null);
        getRoleList(saTokenLoginId, null);

        // 记录登录成功日志
        recordLoginLog(employeeEntity, ip, userAgent, LoginRecordResultEnum.LOGIN_SUCCESS.getValue(), null);

        // 登录成功日志
        log.info("用户登录成功，用户ID: {}, 用户名: {}, IP: {}",
                employeeEntity.getEmployeeId(), employeeEntity.getLoginName(), ip);

        return ApiResult.ok(loginResultVO);
    }

    @Override
    public ApiResult<AuthLoginVO> getLoginInfo() {
        String tokenValue = StpUtil.getTokenValue();
        String loginId = StpUtil.getLoginIdAsString();
        SessionEmployee requestEmployee = getLoginEmployee(loginId);
        AuthLoginVO loginResult = getLoginResult(requestEmployee, tokenValue);
        loginResult.setToken(tokenValue);
        return ApiResult.ok(loginResult);
    }

    @Override
    public ApiResult<String> logout(CurrentUser requestUser) {
        try {
            // 获取员工信息
            Long employeeId = getEmployeeIdByLoginId(StpUtil.getLoginIdAsString());
            StaffEntity employeeEntity = employeeService.getById(employeeId);

            // 记录退出日志
            if (employeeEntity != null) {
                recordLogoutLog(employeeEntity, requestUser.getIp(), requestUser.getUserAgent());
            }

            // sa token 登出
            StpUtil.logout();

            // 清除用户登录信息缓存和权限信息
            this.clearLoginEmployeeCache(requestUser.getUserId());

            log.info("用户退出登录，用户ID: {}, 用户名: {}, IP: {}",
                    requestUser.getUserId(), requestUser.getUserName(), requestUser.getIp());
        } catch (Exception e) {
            log.error("退出登录失败", e);
        }

        return ApiResult.ok();
    }

    @Override
    public ApiResult<VerifyCodeVO> getCaptcha() {
        // 生成验证码ID
        String captchaId = RandomUtil.randomString(32);

        // 生成4位数字验证码
        String captchaCode = RandomUtil.randomNumbers(4);

        // 生成验证码图片
        String captchaImage = generateCaptchaImage(captchaCode);

        // 存储到Redis，5分钟有效期
        String redisKey = RedisKeyConstants.CAPTCHA_KEY + captchaId;
        redisUtil.set(redisKey, captchaCode.toLowerCase(), 300);

        VerifyCodeVO captchaVO = new VerifyCodeVO();
        captchaVO.setCaptchaUuid(captchaId);
        captchaVO.setCaptchaBase64Image(captchaImage);
        captchaVO.setExpireSeconds(300L);

        log.info("生成验证码 - captchaId: {}, code: {}", captchaId, captchaCode);

        return ApiResult.ok(captchaVO);
    }

    @Override
    public boolean verifyCaptcha(String captchaId, String captcha) {
        if (captchaId == null || captcha == null || captcha.isEmpty()) {
            return false;
        }

        String redisKey = RedisKeyConstants.CAPTCHA_KEY + captchaId;
        String captchaCode = (String) redisUtil.get(redisKey);

        if (captchaCode == null) {
            log.warn("验证码已过期 - captchaId: {}", captchaId);
            return false;
        }

        // 验证后删除验证码
        redisUtil.delete(redisKey);

        // 不区分大小写比较
        boolean isValid = captchaCode.equalsIgnoreCase(captcha);

        if (!isValid) {
            log.warn("验证码错误 - captchaId: {}, input: {}, actual: {}",
                    captchaId, captcha, captchaCode);
        }

        return isValid;
    }

    /**
     * 生成验证码图片
     */
    private String generateCaptchaImage(String code) {
        int width = 120;
        int height = 40;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 填充背景色
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        // 设置字体
        g.setFont(new Font("Arial", Font.BOLD, 20));

        // 生成随机颜色
        Random random = new Random();
        g.setColor(new Color(random.nextInt(150), random.nextInt(150), random.nextInt(150)));

        // 添加噪点
        for (int i = 0; i < 50; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            g.setColor(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
            g.fillRect(x, y, 1, 1);
        }

        // 绘制验证码
        for (int i = 0; i < code.length(); i++) {
            g.setColor(new Color(random.nextInt(100), random.nextInt(100), random.nextInt(100)));
            g.drawString(String.valueOf(code.charAt(i)), 20 + i * 25, 25);
        }

        g.dispose();

        // 转换为Base64
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);
            byte[] imageBytes = baos.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            log.error("生成验证码图片失败", e);
            throw new RuntimeException("生成验证码失败");
        }
    }

    @Override
    public SessionEmployee getLoginEmployee(String loginId) {
        if (loginId == null) {
            return null;
        }

        Long requestEmployeeId = getEmployeeIdByLoginId(loginId);
        if (requestEmployeeId == null) {
            return null;
        }

        // 使用缓存管理器获取用户信息
        return loginCacheManager.getRequestEmployee(requestEmployeeId);
    }

    @Override
    public Long getEmployeeIdByLoginId(String loginId) {
        if (loginId == null) {
            return null;
        }

        try {
            // 如果是万能密码登录的用户
            String employeeIdStr = null;
            if (loginId.startsWith(SystemConstant.SUPER_PASSWORD_LOGIN_ID_PREFIX)) {
                employeeIdStr = loginId.split(SystemConstant.COLON)[2];
            } else {
                employeeIdStr = loginId.substring(2);
            }

            return Long.parseLong(employeeIdStr);
        } catch (Exception e) {
            log.error("loginId parse error , loginId : {}", loginId, e);
            return null;
        }
    }

    @Override
    public void clearLoginEmployeeCache(Long employeeId) {
        // 清除用户登录信息缓存和权限信息
        loginCacheManager.clearUserLoginInfo(employeeId);
        loginCacheManager.clearUserPermission(employeeId);
        // 清除角色缓存
        loginCacheManager.clearUserRoleCache(employeeId);
    }

    /**
     * 构建请求员工信息
     */
    private SessionEmployee buildRequestEmployee(StaffEntity employeeEntity) {
        if (employeeEntity == null) {
            return null;
        }

        SessionEmployee requestEmployee = new SessionEmployee();
        requestEmployee.setEmployeeId(employeeEntity.getEmployeeId());
        requestEmployee.setLoginName(employeeEntity.getLoginName());
        requestEmployee.setActualName(employeeEntity.getActualName());
        requestEmployee.setAvatar(employeeEntity.getAvatar());
        requestEmployee.setPhone(employeeEntity.getPhone());
        requestEmployee.setEmail(employeeEntity.getEmail());
        requestEmployee.setAdministratorFlag(employeeEntity.getAdministratorFlag());
        return requestEmployee;
    }

    /**
     * 获取登录结果信息
     */
    private AuthLoginVO getLoginResult(SessionEmployee requestEmployee, String token) {
        // 基础信息
        AuthLoginVO loginResultVO = new AuthLoginVO();
        loginResultVO.setEmployeeId(requestEmployee.getEmployeeId());
        loginResultVO.setLoginName(requestEmployee.getLoginName());
        loginResultVO.setActualName(requestEmployee.getActualName());
        loginResultVO.setAvatar(requestEmployee.getAvatar());
        loginResultVO.setAdministratorFlag(requestEmployee.getAdministratorFlag());

        // 前端菜单和功能点清单 - 与原项目保持一致，使用RoleMenuService获取菜单
        LambdaQueryWrapper<AuthRoleStaffEntity> roleEmployeeWrapper = new LambdaQueryWrapper<>();
        roleEmployeeWrapper.eq(AuthRoleStaffEntity::getEmployeeId, requestEmployee.getEmployeeId());
        List<AuthRoleStaffEntity> roleEmployeeList = roleEmployeeDao.selectList(roleEmployeeWrapper);

        List<Long> roleIdList = roleEmployeeList.stream()
                .map(AuthRoleStaffEntity::getRoleId)
                .distinct()
                .collect(Collectors.toList());

        List<NavMenuVO> menuAndPointsList = roleMenuService.getMenuList(roleIdList, requestEmployee.getAdministratorFlag());
        loginResultVO.setMenuList(menuAndPointsList);

        // 默认不需要强制修改密码
        loginResultVO.setNeedUpdatePwdFlag(false);

        return loginResultVO;
    }

    /**
     * 获取菜单列表
     */
    private List<NavMenuVO> getMenuList(Long employeeId) {
        // 如果是管理员，返回所有菜单
        StaffEntity employeeEntity = employeeService.getById(employeeId);
        if (employeeEntity != null && employeeEntity.getAdministratorFlag()) {
            return queryAllMenus();
        }

        // 查询用户角色
        LambdaQueryWrapper<AuthRoleStaffEntity> roleEmployeeWrapper = new LambdaQueryWrapper<>();
        roleEmployeeWrapper.eq(AuthRoleStaffEntity::getEmployeeId, employeeId);
        List<AuthRoleStaffEntity> roleEmployeeList = roleEmployeeDao.selectList(roleEmployeeWrapper);

        if (roleEmployeeList.isEmpty()) {
            return List.of();
        }

        List<Long> roleIdList = roleEmployeeList.stream()
                .map(AuthRoleStaffEntity::getRoleId)
                .collect(Collectors.toList());

        // 查询角色菜单关联
        LambdaQueryWrapper<AuthRoleMenuEntity> roleMenuWrapper = new LambdaQueryWrapper<>();
        roleMenuWrapper.in(AuthRoleMenuEntity::getRoleId, roleIdList);
        List<AuthRoleMenuEntity> roleMenuList = roleMenuDao.selectList(roleMenuWrapper);

        if (roleMenuList.isEmpty()) {
            return List.of();
        }

        List<Long> menuIdList = roleMenuList.stream()
                .map(AuthRoleMenuEntity::getMenuId)
                .distinct()
                .collect(Collectors.toList());

        // 查询菜单列表
        LambdaQueryWrapper<NavMenuEntity> menuWrapper = new LambdaQueryWrapper<>();
        menuWrapper.in(NavMenuEntity::getMenuId, menuIdList)
                .eq(NavMenuEntity::getVisibleFlag, true)
                .eq(NavMenuEntity::getDeletedFlag, false)
                .orderByAsc(NavMenuEntity::getSort);
        List<NavMenuEntity> menuEntityList = menuDao.selectList(menuWrapper);

        return convertToMenuVOList(menuEntityList);
    }

    /**
     * 查询所有菜单
     */
    private List<NavMenuVO> queryAllMenus() {
        LambdaQueryWrapper<NavMenuEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NavMenuEntity::getVisibleFlag, true)
                .eq(NavMenuEntity::getDeletedFlag, false)
                .orderByAsc(NavMenuEntity::getSort);
        List<NavMenuEntity> menuEntityList = menuDao.selectList(wrapper);
        return convertToMenuVOList(menuEntityList);
    }

    /**
     * 转换为MenuVO列表
     */
    private List<NavMenuVO> convertToMenuVOList(List<NavMenuEntity> menuEntityList) {
        if (menuEntityList == null || menuEntityList.isEmpty()) {
            return List.of();
        }

        // 转换为MenuVO
        List<NavMenuVO> menuVOList = new ArrayList<>();
        Map<Long, NavMenuVO> menuMap = new HashMap<>();

        // 先转换所有菜单
        for (NavMenuEntity entity : menuEntityList) {
            NavMenuVO vo = new NavMenuVO();
            vo.setMenuId(entity.getMenuId());
            vo.setMenuName(entity.getMenuName());
            vo.setMenuType(entity.getMenuType());
            vo.setParentId(entity.getParentId());
            vo.setSort(entity.getSort());
            vo.setPath(entity.getPath());
            vo.setComponent(entity.getComponent());
            vo.setFrameFlag(entity.getFrameFlag());
            vo.setFrameUrl(entity.getFrameUrl());
            vo.setCacheFlag(entity.getCacheFlag());
            vo.setVisibleFlag(entity.getVisibleFlag());
            vo.setDisabledFlag(entity.getDisabledFlag());
            vo.setApiPerms(entity.getApiPerms());
            vo.setPermsType(entity.getPermsType());
            vo.setWebPerms(entity.getWebPerms());
            vo.setIcon(entity.getIcon());
            vo.setContextMenuId(entity.getContextMenuId());
            vo.setChildren(new ArrayList<>());
            menuVOList.add(vo);
            menuMap.put(entity.getMenuId(), vo);
        }

        // 构建树形结构
        List<NavMenuVO> result = new ArrayList<>();
        for (NavMenuVO menu : menuVOList) {
            if (menu.getParentId() == null || menu.getParentId() == 0) {
                result.add(menu);
            } else {
                NavMenuVO parent = menuMap.get(menu.getParentId());
                if (parent != null) {
                    parent.getChildren().add(menu);
                }
            }
        }

        return result;
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        Long employeeId = this.getEmployeeIdByLoginId((String) loginId);
        if (employeeId == null) {
            return List.of();
        }

        // 如果是管理员，返回所有权限
        StaffEntity employeeEntity = employeeService.getById(employeeId);
        if (employeeEntity != null && employeeEntity.getAdministratorFlag()) {
            LambdaQueryWrapper<NavMenuEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.isNotNull(NavMenuEntity::getApiPerms)
                    .ne(NavMenuEntity::getApiPerms, "")
                    .eq(NavMenuEntity::getDeletedFlag, false);
            List<NavMenuEntity> menuList = menuDao.selectList(wrapper);
            return menuList.stream()
                    .map(NavMenuEntity::getApiPerms)
                    .filter(perms -> perms != null && !perms.isEmpty())
                    .collect(Collectors.toList());
        }

        // 使用缓存管理器获取权限列表
        return loginCacheManager.getUserPermissionList(employeeId);
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long employeeId = this.getEmployeeIdByLoginId((String) loginId);
        if (employeeId == null) {
            return List.of();
        }

        // 如果是管理员，返回管理员角色标识
        StaffEntity employeeEntity = employeeService.getById(employeeId);
        if (employeeEntity != null && employeeEntity.getAdministratorFlag()) {
            return List.of("ADMIN");
        }

        // 使用缓存管理器获取角色列表
        return loginCacheManager.getUserRoleList(employeeId);
    }

    /**
     * 记录登录日志
     */
    private void recordLoginLog(StaffEntity employeeEntity, String ip, String userAgent,
                                 Integer loginStatus, String failReason) {
        try {
            LoginRecordEntity loginLog = new LoginRecordEntity();

            if (employeeEntity != null) {
                loginLog.setUserId(employeeEntity.getEmployeeId());
                loginLog.setUserType(AccountTypeEnum.ADMIN_EMPLOYEE.getValue());
                loginLog.setUserName(employeeEntity.getActualName() != null ? employeeEntity.getActualName() : employeeEntity.getLoginName());
            }

            loginLog.setLoginIp(ip);
            loginLog.setUserAgent(userAgent);
            loginLog.setLoginResult(loginStatus);
            loginLog.setRemark(failReason);
            loginLog.setCreateTime(LocalDateTime.now());
            loginLog.setUpdateTime(LocalDateTime.now());

            // 解析User-Agent获取设备信息
            UserAgent ua = UserAgentUtil.parse(userAgent);
            loginLog.setLoginDevice(ua.getOs().getName());

            // 解析IP地理位置
            try {
                loginLog.setLoginIpRegion(IpRegionUtil.getRegion(ip));
            } catch (Exception e) {
                log.warn("解析IP地理位置失败: {}", ip, e);
            }

            loginLogDao.insert(loginLog);
        } catch (Exception e) {
            log.error("记录登录日志失败", e);
        }
    }

    /**
     * 记录登录日志（用户不存在的情况）
     */
    private void recordLoginLog(Long employeeId, String loginName, String ip, String userAgent,
                                 Integer loginStatus, String failReason) {
        try {
            LoginRecordEntity loginLog = new LoginRecordEntity();
            loginLog.setUserId(employeeId);
            loginLog.setUserType(AccountTypeEnum.ADMIN_EMPLOYEE.getValue());
            loginLog.setUserName(loginName);
            loginLog.setLoginIp(ip);
            loginLog.setUserAgent(userAgent);
            loginLog.setLoginResult(loginStatus);
            loginLog.setRemark(failReason);
            loginLog.setCreateTime(LocalDateTime.now());
            loginLog.setUpdateTime(LocalDateTime.now());

            // 解析User-Agent获取设备信息
            UserAgent ua = UserAgentUtil.parse(userAgent);
            loginLog.setLoginDevice(ua.getOs().getName());

            // 解析IP地理位置
            try {
                loginLog.setLoginIpRegion(IpRegionUtil.getRegion(ip));
            } catch (Exception e) {
                log.warn("解析IP地理位置失败: {}", ip, e);
            }

            loginLogDao.insert(loginLog);
        } catch (Exception e) {
            log.error("记录登录日志失败", e);
        }
    }

    /**
     * 记录退出日志
     */
    private void recordLogoutLog(StaffEntity employeeEntity, String ip, String userAgent) {
        try {
            LoginRecordEntity loginLog = new LoginRecordEntity();
            loginLog.setUserId(employeeEntity.getEmployeeId());
            loginLog.setUserType(AccountTypeEnum.ADMIN_EMPLOYEE.getValue());
            loginLog.setUserName(employeeEntity.getActualName() != null ? employeeEntity.getActualName() : employeeEntity.getLoginName());
            loginLog.setLoginIp(ip);
            loginLog.setUserAgent(userAgent);
            loginLog.setLoginResult(LoginRecordResultEnum.LOGIN_OUT.getValue());
            loginLog.setRemark("用户退出登录");
            loginLog.setCreateTime(LocalDateTime.now());
            loginLog.setUpdateTime(LocalDateTime.now());

            // 解析User-Agent获取设备信息
            UserAgent ua = UserAgentUtil.parse(userAgent);
            loginLog.setLoginDevice(ua.getOs().getName());

            // 解析IP地理位置
            try {
                loginLog.setLoginIpRegion(IpRegionUtil.getRegion(ip));
            } catch (Exception e) {
                log.warn("解析IP地理位置失败: {}", ip, e);
            }

            loginLogDao.insert(loginLog);
        } catch (Exception e) {
            log.error("记录退出日志失败", e);
        }
    }

    // ==================== 在线用户管理 ====================

    private static final String LOGIN_ID_PREFIX = "1:"; // AccountTypeEnum.ADMIN_EMPLOYEE.getValue() = 1

    @Override
    public ApiResult<PageResponse<OnlineUserVO>> queryOnlineUserPage(OnlineUserQueryForm queryForm) {
        List<OnlineUserVO> allOnlineUsers = queryAllOnlineUsers();

        List<OnlineUserVO> filteredList = allOnlineUsers.stream()
                .filter(vo -> {
                    if (StringUtils.hasText(queryForm.getKeywords())) {
                        String keywords = queryForm.getKeywords().toLowerCase();
                        boolean match = (vo.getUserName() != null && vo.getUserName().toLowerCase().contains(keywords))
                                || (vo.getLoginName() != null && vo.getLoginName().toLowerCase().contains(keywords));
                        if (!match) return false;
                    }
                    if (queryForm.getDepartmentId() != null && !queryForm.getDepartmentId().equals(vo.getDepartmentId())) {
                        return false;
                    }
                    if (queryForm.getUserType() != null && !queryForm.getUserType().equals(vo.getUserType())) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        LocalDateTime now = LocalDateTime.now();
        filteredList.forEach(vo -> {
            if (vo.getLoginTime() != null) {
                Duration duration = Duration.between(vo.getLoginTime(), now);
                vo.setOnlineDuration(duration.getSeconds());
                vo.setOnlineDurationDesc(formatDuration(duration));
            }
        });

        int total = filteredList.size();
        int pageNum = queryForm.getPageNum() != null ? queryForm.getPageNum().intValue() : 1;
        int pageSize = queryForm.getPageSize() != null ? queryForm.getPageSize().intValue() : 10;
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);

        List<OnlineUserVO> pageList = fromIndex < total 
                ? filteredList.subList(fromIndex, toIndex) 
                : new ArrayList<>();

        return ApiResult.ok(new PageResponse<>((long) pageNum, (long) pageSize, (long) total, pageList));
    }

    @Override
    public List<OnlineUserVO> queryAllOnlineUsers() {
        List<OnlineUserVO> result = new ArrayList<>();

        try {
            List<String> tokenKeyList = StpUtil.searchTokenValue("", 0, -1, false);
            
            for (String tokenKey : tokenKeyList) {
                try {
                    String token = tokenKey;
                    if (tokenKey.contains(":login:token:")) {
                        token = tokenKey.substring(tokenKey.lastIndexOf(":") + 1);
                    }
                    
                    Object loginIdObj = StpUtil.getLoginIdByToken(token);
                    if (loginIdObj == null) {
                        continue;
                    }
                    String loginId = loginIdObj.toString();
                    
                    OnlineUserVO vo = buildOnlineUserVO(loginId, token);
                    if (vo != null) {
                        result.add(vo);
                    }
                } catch (Exception e) {
                    log.debug("token 处理异常: {}, error: {}", tokenKey, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("获取在线用户列表失败", e);
        }

        return result;
    }

    @Override
    public long getOnlineUserCount() {
        try {
            List<String> tokenKeyList = StpUtil.searchTokenValue("", 0, -1, false);
            long count = 0;
            for (String tokenKey : tokenKeyList) {
                try {
                    String token = tokenKey;
                    if (tokenKey.contains(":login:token:")) {
                        token = tokenKey.substring(tokenKey.lastIndexOf(":") + 1);
                    }
                    
                    Object loginIdObj = StpUtil.getLoginIdByToken(token);
                    if (loginIdObj != null) {
                        count++;
                    }
                } catch (Exception e) {
                    // token 无效或已过期，忽略
                }
            }
            return count;
        } catch (Exception e) {
            log.error("获取在线用户数量失败", e);
            return 0;
        }
    }

    @Override
    public ApiResult<String> forceLogout(Long userId, Integer userType) {
        if (userId == null) {
            return ApiResult.userErrorParam("用户ID不能为空");
        }

        try {
            String saTokenLoginId = userType + ":" + userId;
            StpUtil.kickout(saTokenLoginId);

            log.info("强制下线成功，用户ID: {}, 用户类型: {}", userId, userType);
            return ApiResult.ok("强制下线成功");
        } catch (Exception e) {
            log.error("强制下线失败", e);
            return ApiResult.paramError("强制下线失败：" + e.getMessage());
        }
    }

    @Override
    public ApiResult<String> batchForceLogout(List<Long> userIds, Integer userType) {
        if (userIds == null || userIds.isEmpty()) {
            return ApiResult.userErrorParam("用户ID列表不能为空");
        }

        int successCount = 0;
        for (Long userId : userIds) {
            try {
                String saTokenLoginId = userType + ":" + userId;
                StpUtil.kickout(saTokenLoginId);
                successCount++;
            } catch (Exception e) {
                log.warn("批量强制下线失败，用户ID: {}", userId, e);
            }
        }

        log.info("批量强制下线完成，成功: {}/{}", successCount, userIds.size());
        return ApiResult.ok("批量强制下线完成，成功: " + successCount + "个");
    }

    private OnlineUserVO buildOnlineUserVO(String loginId, String token) {
        try {
            String[] parts = loginId.split(":");
            if (parts.length != 2) {
                return null;
            }
            
            Integer userType = Integer.parseInt(parts[0]);
            Long userId = Long.parseLong(parts[1]);

            OnlineUserVO vo = new OnlineUserVO();
            vo.setLoginId(loginId);
            vo.setUserId(userId);
            vo.setUserType(userType);
            vo.setToken(token);

            try {
                Object userName = StpUtil.getSessionByLoginId(loginId).get("userName");
                Object loginName = StpUtil.getSessionByLoginId(loginId).get("loginName");
                vo.setUserName(userName != null ? userName.toString() : null);
                vo.setLoginName(loginName != null ? loginName.toString() : null);
            } catch (Exception e) {
                log.debug("获取Session信息失败: {}", loginId);
            }

            try {
                StaffEntity employee = employeeService.getById(userId);
                if (employee != null) {
                    vo.setUserName(employee.getActualName());
                    vo.setLoginName(employee.getLoginName());
                    vo.setDepartmentId(employee.getDepartmentId());
                    // 获取部门名称
                    if (employee.getDepartmentId() != null) {
                        try {
                            OrganizationVO department = departmentService.getDepartmentById(employee.getDepartmentId());
                            if (department != null) {
                                vo.setDepartmentName(department.getDepartmentName());
                            }
                        } catch (Exception e) {
                            log.debug("获取部门信息失败: departmentId={}", employee.getDepartmentId());
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("获取员工信息失败: userId={}, error={}", userId, e.getMessage());
            }

            try {
                long tokenCreateTime = StpUtil.getTokenSessionByToken(token).getCreateTime();
                vo.setLoginTime(LocalDateTime.ofEpochSecond(tokenCreateTime / 1000, 0, ZoneOffset.ofHours(8)));
            } catch (Exception e) {
                log.debug("获取Token创建时间失败: {}", token);
            }

            return vo;
        } catch (Exception e) {
            log.warn("解析loginId失败: {}", loginId, e);
            return null;
        }
    }

    private String formatDuration(Duration duration) {
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("天");
        }
        if (hours > 0) {
            sb.append(hours).append("小时");
        }
        if (minutes > 0) {
            sb.append(minutes).append("分钟");
        }
        if (sb.length() == 0 && seconds > 0) {
            sb.append(seconds).append("秒");
        }
        if (sb.length() == 0) {
            sb.append("刚上线");
        }
        return sb.toString();
    }
}
