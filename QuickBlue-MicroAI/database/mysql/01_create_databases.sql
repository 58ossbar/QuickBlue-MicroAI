-- ========================================
-- 创建独立数据库和账号
-- 说明: 为四个微服务创建独立的数据库和专用账号
-- ========================================

-- 1. 创建Support服务数据库
CREATE DATABASE IF NOT EXISTS `quickblue_support`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

-- 2. 创建System服务数据库
CREATE DATABASE IF NOT EXISTS `quickblue_system`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

-- 3. 创建Business服务数据库
CREATE DATABASE IF NOT EXISTS `quickblue_business`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

-- 4. 创建AI服务数据库
CREATE DATABASE IF NOT EXISTS `quickblue_ai`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- ========================================
-- 创建服务专用账号并授权
-- ========================================

-- 5. 创建Support服务账号
DROP USER IF EXISTS 'support_user'@'%';
CREATE USER 'support_user'@'%' IDENTIFIED BY 'Support@2026';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, DROP, REFERENCES
      ON `quickblue_support`.* TO 'support_user'@'%';

-- 6. 创建System服务账号
DROP USER IF EXISTS 'system_user'@'%';
CREATE USER 'system_user'@'%' IDENTIFIED BY 'System@2026';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, DROP, REFERENCES
      ON `quickblue_system`.* TO 'system_user'@'%';

-- 7. 创建Business服务账号
DROP USER IF EXISTS 'business_user'@'%';
CREATE USER 'business_user'@'%' IDENTIFIED BY 'Business@2026';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, DROP, REFERENCES
      ON `quickblue_business`.* TO 'business_user'@'%';

-- 8. 创建AI服务账号
DROP USER IF EXISTS 'ai_user'@'%';
CREATE USER 'ai_user'@'%' IDENTIFIED BY 'Ai@2026';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, DROP, REFERENCES
      ON `quickblue_ai`.* TO 'ai_user'@'%';

-- 9. 刷新权限
FLUSH PRIVILEGES;