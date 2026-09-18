# QuickBlue Common Excel

基于 EasyExcel 的 Excel 处理公共模块，提供便捷的 Excel 导入导出功能。

## 功能特性

- ✅ 简洁易用的 API
- ✅ 支持自定义样式
- ✅ 自动列宽调整
- ✅ 水印功能
- ✅ 大文件读写
- ✅ 注解式配置
- ✅ 优秀的性能（基于 EasyExcel）

## 依赖配置

```xml
<dependency>
    <groupId>com.budaos</groupId>
    <artifactId>QuickBlue-common-excel</artifactId>
    <version>${QuickBlue.version}</version>
</dependency>
```

## 快速开始

### 1. 定义 Excel 实体类

```java
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.budaos.common.excel.domain.ExcelModel;

@ContentRowHeight(20)
@HeadRowHeight(25)
public class UserExcelVO extends ExcelModel {

    @ExcelProperty(value = "用户ID")
    private Long userId;

    @ExcelProperty(value = "用户名")
    private String username;

    @ExcelProperty(value = "手机号")
    private String phoneNumber;

    @ExcelProperty(value = "邮箱")
    private String email;

    @DateTimeFormat("yyyy-MM-dd")
    @ExcelProperty(value = "出生日期")
    private LocalDate birthDate;

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }
}
```

### 2. 导出 Excel

#### 简化版导出

```java
import com.budaos.common.excel.util.EasyExcelUtil;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/user")
public class UserController {

    @GetMapping("/export")
    public void exportUsers(HttpServletResponse response) {
        // 查询用户数据
        List<UserExcelVO> userList = userService.getUserList();

        // 导出 Excel
        EasyExcelUtil.export(response, "用户数据", "用户列表", UserExcelVO.class, userList);
    }
}
```

#### 高级版导出（使用 ExcelExportDTO）

```java
import com.budaos.common.excel.domain.ExcelExportDTO;
import com.budaos.common.excel.util.EasyExcelUtil;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/user")
public class UserController {

    @GetMapping("/export")
    public void exportUsers(HttpServletResponse response) {
        List<UserExcelVO> userList = userService.getUserList();

        ExcelExportDTO exportDTO = ExcelExportDTO.builder()
                .dataList(userList)
                .fileName("用户数据")
                .sheetName("用户列表")
                .clazz(UserExcelVO.class)
                .includeHeader(true)
                .autoWidth(true)
                .build();

        EasyExcelUtil.exportToResponse(exportDTO, response);
    }
}
```

#### 导出到本地文件

```java
import com.budaos.common.excel.util.EasyExcelUtil;

@Service
public class UserService {

    public void exportToFile() {
        List<UserExcelVO> userList = getUserList();

        // 导出到本地文件
        EasyExcelUtil.exportToFile("/tmp/users.xlsx", UserExcelVO.class, userList);
    }
}
```

### 3. 导入 Excel

#### 从 MultipartFile 导入

```java
import com.budaos.common.excel.util.EasyExcelUtil;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/user")
public class UserController {

    @PostMapping("/import")
    public ApiResult<Void> importUsers(@RequestParam("file") MultipartFile file) {
        // 导入 Excel
        List<UserExcelVO> userList = EasyExcelUtil.importExcel(file, UserExcelVO.class);

        // 处理数据
        userService.batchImport(userList);

        return ApiResult.ok();
    }
}
```

#### 从 InputStream 导入

```java
import com.budaos.common.excel.util.EasyExcelUtil;

@Service
public class UserService {

    public void importFromStream(InputStream inputStream) {
        List<UserExcelVO> userList = EasyExcelUtil.importExcel(inputStream, UserExcelVO.class);
        // 处理数据
        batchImport(userList);
    }
}
```

#### 从文件导入

```java
import com.budaos.common.excel.util.EasyExcelUtil;

@Service
public class UserService {

    public void importFromFile(String filePath) {
        List<UserExcelVO> userList = EasyExcelUtil.importExcelFromFile(filePath, UserExcelVO.class);
        // 处理数据
        batchImport(userList);
    }
}
```

### 4. 添加水印

```java
import com.budaos.common.excel.domain.ExcelExportDTO;
import com.budaos.common.excel.handler.WatermarkHandler;
import com.budaos.common.excel.util.EasyExcelUtil;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/user")
public class UserController {

    @GetMapping("/exportWithWatermark")
    public void exportUsersWithWatermark(HttpServletResponse response) {
        List<UserExcelVO> userList = userService.getUserList();

        ExcelExportDTO exportDTO = ExcelExportDTO.builder()
                .dataList(userList)
                .fileName("用户数据")
                .sheetName("用户列表")
                .clazz(UserExcelVO.class)
                .includeHeader(true)
                .autoWidth(true)
                .build();

        // 创建水印处理器
        WatermarkHandler watermarkHandler = new WatermarkHandler("QuickBlue内部资料");

        // 导出时添加水印
        EasyExcelUtil.exportToResponse(exportDTO, response,
                EasyExcelUtil.getDefaultStyleStrategy(), watermarkHandler);
    }
}
```

#### 自定义水印配置

```java
import com.budaos.common.excel.handler.WatermarkHandler;

import java.awt.*;

// 创建自定义水印
WatermarkHandler.Watermark watermark = new WatermarkHandler.Watermark(
        "QuickBlue内部资料",  // 水印内容
        new Color(200, 200, 200),  // 颜色
        new Font("Microsoft YaHei", Font.BOLD, 26),  // 字体
        30  // 倾斜角度
);

WatermarkHandler watermarkHandler = new WatermarkHandler(watermark);
```

## API 文档

### EasyExcelUtil 工具类

#### 导出方法

| 方法 | 说明 |
|-----|------|
| `export(response, fileName, sheetName, head, data)` | 简化版导出到响应 |
| `exportToResponse(exportDTO, response)` | 使用 DTO 导出到响应（默认样式） |
| `exportToResponse(exportDTO, response, styleStrategy)` | 使用 DTO 导出到响应（自定义样式） |
| `exportToFile(filePath, head, data)` | 导出到本地文件 |
| `exportToFile(filePath, sheetName, head, data)` | 导出到本地文件（指定 Sheet 名称） |

#### 导入方法

| 方法 | 说明 |
|-----|------|
| `importExcel(inputStream, head)` | 从输入流导入 |
| `importExcelFromFile(filePath, head)` | 从文件导入 |
| `importExcel(file, head)` | 从 MultipartFile 导入 |
| `importExcel(inputStream, head, listener)` | 使用自定义监听器导入 |

#### 样式方法

| 方法 | 说明 |
|-----|------|
| `defaultHeadStyle()` | 获取默认表头样式 |
| `defaultContentStyle()` | 获取默认内容样式 |
| `getDefaultStyleStrategy()` | 获取默认样式策略 |

#### 工具方法

| 方法 | 说明 |
|-----|------|
| `setExcelResponseHeader(response, fileName)` | 设置 Excel 响应头 |
| `getFileNameWithoutExtension(fileName)` | 获取无扩展名的文件名 |
| `isExcelFile(fileName)` | 判断是否为 Excel 文件 |

### ExcelExportDTO 导出配置类

| 属性 | 类型 | 默认值 | 说明 |
|-----|------|--------|------|
| dataList | List<?> | - | 导出数据列表 |
| fileName | String | - | Excel 文件名 |
| sheetName | String | "Sheet1" | Sheet 名称 |
| clazz | Class<?> | - | 数据类型 |
| includeHeader | boolean | true | 是否包含表头 |
| autoWidth | boolean | true | 是否自动调整列宽 |

### WatermarkHandler 水印处理器

#### 构造函数

```java
// 默认配置
WatermarkHandler(String content)

// 完全自定义配置
WatermarkHandler(Watermark watermark)
```

#### Watermark 配置类

| 属性 | 类型 | 默认值 | 说明 |
|-----|------|--------|------|
| content | String | - | 水印内容 |
| color | Color | Color(239,239,239) | 颜色 |
| font | Font | Font("Microsoft YaHei", BOLD, 26) | 字体 |
| angle | double | 25 | 倾斜角度（非弧度制） |

## 高级功能

### 1. 自定义样式

```java
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import org.apache.poi.ss.usermodel.*;

// 创建自定义表头样式
WriteCellStyle headStyle = new WriteCellStyle();
headStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
WriteFont headFont = new WriteFont();
headFont.setFontHeightInPoints((short) 14);
headFont.setBold(true);
headFont.setColor(IndexedColors.WHITE.getIndex());
headStyle.setWriteFont(headFont);
headStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
headStyle.setVerticalAlignment(VerticalAlignment.CENTER);

// 创建自定义内容样式
WriteCellStyle contentStyle = new WriteCellStyle();
contentStyle.setHorizontalAlignment(HorizontalAlignment.LEFT);
contentStyle.setVerticalAlignment(VerticalAlignment.CENTER);
WriteFont contentFont = new WriteFont();
contentFont.setFontHeightInPoints((short) 11);
contentStyle.setWriteFont(contentFont);

// 创建样式策略
HorizontalCellStyleStrategy styleStrategy = new HorizontalCellStyleStrategy(headStyle, contentStyle);

// 应用自定义样式
EasyExcelUtil.exportToResponse(exportDTO, response, styleStrategy);
```

### 2. 合并单元格

```java
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

public class MergeCellHandler implements CellWriteHandler {

    @Override
    public void afterCellDispose(WriteSheetHolder writeSheetHolder, 
                                WriteTableHolder writeTableHolder, 
                                List<CellData<?>> cellDataList, 
                                Cell cell, Head head, Integer relativeRowIndex, 
                                Boolean isHead) {
        if (isHead) {
            Sheet sheet = writeSheetHolder.getSheet();
            // 合并第一行的前两个单元格
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));
        }
    }
}
```

### 3. 使用自定义监听器

```java
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;

public class UserImportListener extends AnalysisEventListener<UserExcelVO> {

    private final UserService userService;
    private final List<UserExcelVO> dataList = new ArrayList<>();

    public UserImportListener(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void invoke(UserExcelVO data, AnalysisContext context) {
        dataList.add(data);

        // 每读取 1000 条数据，批量保存一次
        if (dataList.size() >= 1000) {
            userService.batchImport(dataList);
            dataList.clear();
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 保存剩余数据
        if (!dataList.isEmpty()) {
            userService.batchImport(dataList);
        }
    }
}

// 使用自定义监听器
UserImportListener listener = new UserImportListener(userService);
EasyExcelUtil.importExcel(inputStream, UserExcelVO.class, listener);
```

## 注意事项

1. **大文件处理**：EasyExcel 采用流式读写，适合处理大文件，但建议单次读取不超过 10 万条记录
2. **内存占用**：导出时 `inMemory(true)` 会占用更多内存，适合小文件
3. **日期格式**：使用 `@DateTimeFormat` 注解指定日期格式
4. **数字格式**：使用 `@NumberFormat` 注解指定数字格式
5. **列宽限制**：最大列宽为 255，超过会自动限制

## 示例代码

完整的示例代码请参考 `QuickBlue-modules` 中的使用示例。

## 版本历史

### 4.0.0 (2026-02-19)

- ✨ 初始版本发布
- ✅ 支持导出到 HttpServletResponse
- ✅ 支持从 InputStream/MultipartFile 导入
- ✅ 支持自定义样式
- ✅ 支持自动列宽调整
- ✅ 支持水印功能
- ✅ 提供便捷的工具类方法

## 许可证

基于 QuickBlue 框架许可证
