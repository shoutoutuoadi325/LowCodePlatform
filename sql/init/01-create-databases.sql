-- 创建设备数据库
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'devicedb')
BEGIN
    CREATE DATABASE devicedb;
END
GO

-- 创建场景数据库
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'scenedb')
BEGIN
    CREATE DATABASE scenedb;
END
GO

-- 使用设备数据库
USE devicedb;
GO

-- 创建设备服务用户（可选）
IF NOT EXISTS (SELECT name FROM sys.server_principals WHERE name = 'device_user')
BEGIN
    CREATE LOGIN device_user WITH PASSWORD = 'Device@123456';
    CREATE USER device_user FOR LOGIN device_user;
    ALTER ROLE db_datareader ADD MEMBER device_user;
    ALTER ROLE db_datawriter ADD MEMBER device_user;
    ALTER ROLE db_ddladmin ADD MEMBER device_user;
END
GO

-- 使用场景数据库
USE scenedb;
GO

-- 创建场景服务用户（可选）
IF NOT EXISTS (SELECT name FROM sys.server_principals WHERE name = 'scene_user')
BEGIN
    CREATE LOGIN scene_user WITH PASSWORD = 'Scene@123456';
    CREATE USER scene_user FOR LOGIN scene_user;
    ALTER ROLE db_datareader ADD MEMBER scene_user;
    ALTER ROLE db_datawriter ADD MEMBER scene_user;
    ALTER ROLE db_ddladmin ADD MEMBER scene_user;
END
GO