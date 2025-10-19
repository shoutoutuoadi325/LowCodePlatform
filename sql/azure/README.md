# Azure SQL Server 数据库初始化脚本

本目录包含用于 Azure SQL Server 的数据库初始化脚本。

## 前提条件

1. 已创建 Azure SQL Server 实例
2. 已创建两个数据库：`devicedb` 和 `scenedb`
3. 具有数据库管理权限的用户账户

## 连接信息

- **服务器**: shoutoutuoadi325.database.windows.net
- **用户名**: shoutoutuoadi325
- **密码**: 1QAZ0plm@
- **端口**: 1433

## 执行顺序

### 1. 设备数据库 (devicedb)

连接到 `devicedb` 数据库，按以下顺序执行脚本：

```sql
-- 1. 创建表结构
\i 01-create-devicedb-tables.sql

-- 2. 插入示例数据
\i 03-insert-devicedb-sample-data.sql
```

### 2. 场景数据库 (scenedb)

连接到 `scenedb` 数据库，按以下顺序执行脚本：

```sql
-- 1. 创建表结构
\i 02-create-scenedb-tables.sql

-- 2. 插入示例数据
\i 04-insert-scenedb-sample-data.sql
```

## 脚本说明

- `01-create-devicedb-tables.sql`: 创建设备数据库的所有表结构
- `02-create-scenedb-tables.sql`: 创建场景数据库的所有表结构
- `03-insert-devicedb-sample-data.sql`: 向设备数据库插入示例数据
- `04-insert-scenedb-sample-data.sql`: 向场景数据库插入示例数据

## 注意事项

1. Azure SQL Server 不支持在单个连接中使用 `USE` 语句切换数据库
2. 每个脚本都需要在对应的数据库上下文中执行
3. 所有脚本都包含了 `IF NOT EXISTS` 检查，可以安全地重复执行
4. 确保网络连接允许访问 Azure SQL Server (端口 1433)

## 验证安装

执行完所有脚本后，可以运行以下查询来验证安装：

### 在 devicedb 中：
```sql
SELECT COUNT(*) as table_count FROM information_schema.tables WHERE table_type = 'BASE TABLE';
SELECT COUNT(*) as device_count FROM devices;
```

### 在 scenedb 中：
```sql
SELECT COUNT(*) as table_count FROM information_schema.tables WHERE table_type = 'BASE TABLE';
SELECT COUNT(*) as scene_count FROM scenes;
```