# Goya 组件脚手架生成器

## 描述
快速生成符合 Goya 规范的组件模块脚手架，包括目录结构、pom.xml、自动配置类等。

## 使用场景
- 创建新的 component 模块
- 创建新的 starter 模块
- 生成标准的模块结构

## 使用方法

只需说明您要创建的组件名称和功能，例如：
- "创建一个名为 component-notification 的组件，用于消息通知"
- "生成 sms-boot-starter 短信服务模块"

## 生成内容

### 1. 目录结构
```
component-xxx/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/ysmjjsy/goya/component/xxx/
│   │   │   ├── configuration/
│   │   │   │   └── XxxAutoConfiguration.java
│   │   │   ├── properties/
│   │   │   │   └── XxxProperties.java
│   │   │   └── service/
│   │   │       └── XxxService.java
│   │   └── resources/
│   │       └── META-INF/spring/
│   │           └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
│   └── test/
│       └── java/
└── README.md
```

### 2. pom.xml
- 正确的父模块引用
- 基础依赖配置
- 描述信息

### 3. 自动配置类
- @AutoConfiguration 注解
- 条件注解 (@ConditionalOnClass 等)
- Properties 配置类绑定
- Bean 注册
- 日志输出

### 4. 配置属性类
- @ConfigurationProperties 注解
- 配置前缀 `goya.xxx`
- 属性字段和注释

### 5. 自动配置注册
- META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports

## 规范要求

- 包名：`com.ysmjjsy.goya.component.{moduleName}`
- 配置前缀：`goya.{moduleName}`
- 配置类命名：`{ModuleName}AutoConfiguration`
- 日志格式：`[Goya] |- component [{moduleName}] XxxAutoConfiguration auto configure.`
- 所有类必须添加 `@Slf4j` 注解
- 所有类必须有完整的中文注释

## 示例输出

当用户请求"创建 component-sms 短信组件"时，生成：

1. **目录结构**
2. **pom.xml**
3. **SmsAutoConfiguration.java**
4. **SmsProperties.java**
5. **SmsService.java**
6. **AutoConfiguration.imports**
7. **README.md**

## 注意事项

- 自动更新父 pom.xml 的 modules 配置
- 遵循 Goya 项目的代码规范
- 生成后提示用户运行 `mvn clean install`
