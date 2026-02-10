# Module Dependency Rules

## 聚合模块（仅聚合，不可被依赖）
- `goya`
- `component`
- `component-framework`
- `ai`
- `platform`

## 允许直接依赖的典型模块
- `framework-boot-starter`
- `framework-common`
- `framework-core`
- `component-service`
- `component-redis`
- `component-kafka`
- `component-mybatisplus`

## 自动配置注册文件
- 路径：
  `component/component-framework/**/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
