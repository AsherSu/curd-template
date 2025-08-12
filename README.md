# CURD Template Project

这是一个基于 MyBatis-Plus 的代码生成器项目，可以根据数据库表结构自动生成相应的 CURD 代码。

## 项目结构

```
mybatis-plus-generator
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── ruoyi
│   │   │           ├── CodeGenerator.java             // 主程序代码生成器
│   │   │           ├── CodeGeneratorGUI.java          // 图形界面代码生成器
│   │   │           ├── GenerationRecord.java          // 生成记录实体类
│   │   │           ├── GenerationRecordManager.java   // 生成记录管理类
│   │   │           ├── GeneratedFile.java             // 生成文件实体类
│   │   └── resources
│   │       └── application.properties                 // 配置文件
│   └── test
│       ├── java
│       │   └── com
│       │       └── ruoyi
│       │           ├── BaseGeneratorTest.java         // 基础测试类
│       │           ├── H2CodeGeneratorTest.java       // H2数据库代码生成示例
│       │           ├── MySQLGeneratorTest.java        // MySQL数据库代码生成示例
│       │           └── InteractiveGeneratorTest.java  // 交互式代码生成器
│       └── resources
│           └── sql
│               └── init.sql
└── pom.xml
```

## 使用方法

有多种方式运行代码生成器：

### 方式一：图形界面运行（推荐）

直接运行 [CodeGeneratorGUI.java](mybatis-plus-generator/src/main/java/com/ruoyi/CodeGeneratorGUI.java) 的 main 方法：

```bash
# 在 mybatis-plus-generator 目录下执行
mvn compile exec:java -Dexec.mainClass="com.ruoyi.CodeGeneratorGUI"
```

或者在IDE中直接运行 [CodeGeneratorGUI.java](mybatis-plus-generator/src/main/java/com/ruoyi/CodeGeneratorGUI.java) 文件。

图形界面提供了直观的操作方式，包括：
- 可视化表选择
- 生成配置设置
- 实时日志显示
- 历史记录管理
- 文件恢复功能

### 方式二：命令行菜单运行

直接运行 [CodeGenerator.java](mybatis-plus-generator/src/main/java/com/ruoyi/CodeGenerator.java) 的 main 方法：

```bash
# 在 mybatis-plus-generator 目录下执行
mvn compile exec:java -Dexec.mainClass="com.ruoyi.CodeGenerator"
```

或者在IDE中直接运行 [CodeGenerator.java](mybatis-plus-generator/src/main/java/com/ruoyi/CodeGenerator.java) 文件。

运行后，程序将显示主菜单，提供生成代码和查看历史记录等功能。

### 方式三：交互式生成

运行 [InteractiveGeneratorTest.java](mybatis-plus-generator/src/test/java/com/ruoyi/InteractiveGeneratorTest.java) 中的 `testInteractive` 方法：

```bash
# 在 mybatis-plus-generator 目录下执行
mvn test -Dtest=com.ruoyi.InteractiveGeneratorTest#testInteractive
```

### 方式四：简单生成

运行 [MySQLGeneratorTest.java](mybatis-plus-generator/src/test/java/com/ruoyi/MySQLGeneratorTest.java) 中的 `testSimple` 方法：

```bash
# 在 mybatis-plus-generator 目录下执行
mvn test -Dtest=com.ruoyi.MySQLGeneratorTest#testSimple
```

## 配置说明

### 数据库配置

在 [application.properties](mybatis-plus-generator/src/main/resources/application.properties) 中修改数据库连接信息：

```properties
# 数据库配置
db.url=jdbc:mysql://服务器地址:端口/数据库名?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
db.username=用户名
db.password=密码
db.driver-class-name=com.mysql.cj.jdbc.Driver

# 代码生成配置
output.dir=${user.dir}/src/main/java
default.parent.package=com.ruoyi.project
```

### 表选择方式

在图形界面中，可以通过列表选择一个或多个表进行代码生成。

在命令行模式下，程序会自动列出数据库中的所有表，并提供以下选择方式：
- 输入序号选择单个表，如：`1`
- 输入多个序号选择多个表，用英文逗号分隔，如：`1,3,5`
- 输入 `all` 选择所有表

### 包名和模块配置

运行时可以自定义以下配置：
- 模块名：如 `system`，将成为包名的一部分
- 父包名：如 `com.example`，与模块名组合形成完整包名 `com.example.system`
- Controller生成选项：可选择是否生成Controller层代码

### 生成确认和撤销

代码生成完成后，程序会询问是否确认保留生成的代码：
- 输入 `y` 或直接回车确认保留代码
- 输入 `n` 删除刚刚生成的所有代码文件

### 生成记录管理

程序会自动保存每次代码生成的记录，包括：
- 生成时间
- 表名列表
- 模块名和包名
- 是否生成Controller
- 数据库URL
- 生成状态（成功、失败、已取消）
- 生成的文件内容（用于恢复）

在主菜单中选择"查看生成记录"可以：
- 查看最近的生成记录
- 查看记录详细信息
- 恢复记录中的文件
- 删除单条记录
- 清空所有记录

### 文件恢复功能

文件恢复功能允许您将历史生成记录中的文件恢复到项目中：
- 程序会检查目标文件是否已存在
- 如果文件不存在，则直接创建
- 如果文件存在且内容相同，则跳过
- 如果文件存在但内容不同，则提示是否覆盖

这个功能在以下场景中非常有用：
- 恢复意外删除的代码
- 比较不同配置下生成的代码
- 恢复到之前的代码版本

### 生成路径

默认生成路径为当前项目结构中：
- 实体类、Mapper、Service等Java代码：`src/main/java`
- Mapper XML文件：`src/main/resources/mapper/{moduleName}`

## 常见问题及解决方案

### 1. 数据库连接失败

如果出现以下错误：
```
获取表列表失败: Communications link failure
The last packet sent successfully to the server was 0 milliseconds ago. The driver has not received any packets from the server
```

请检查以下配置：

1. **检查数据库服务是否启动**：确保MySQL服务正在运行
2. **检查数据库连接参数**：确认 [application.properties](mybatis-plus-generator/src/main/resources/application.properties) 中的数据库地址、端口、用户名和密码正确
3. **检查网络连接**：确认可以访问数据库服务器
4. **检查防火墙设置**：确保数据库端口（默认3306）未被防火墙阻止

### 2. 无法找到表

如果提示"未找到任何数据表"：
1. 确认数据库中确实存在表
2. 检查数据库名称是否正确（从URL中正确提取）
3. 确认提供的用户具有访问相应数据库和表的权限

## 生成的代码结构

生成的代码将包括以下组件：

- Entity 实体类（带Lombok注解）
- Mapper 接口（带@Mapper注解）
- Service 服务层接口及实现
- Controller 控制器（REST风格，可选）
- Mapper XML 文件（包含基本 resultMap 和列名定义）

所有生成的代码都遵循以下规范：
- 命名规范：下划线转驼峰命名
- 逻辑删除：自动识别 deleted 字段作为逻辑删除标记
- RESTful 风格：Controller 使用 RESTful 风格接口设计