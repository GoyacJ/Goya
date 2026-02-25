# framework-log

`framework-log` 模块说明文档。

## 模块定位
- 类型：Maven 运行时模块（`packaging=jar`）
- 描述：未单独声明 description
- 作用：提供 `framework-log` 相关能力。

## 依赖治理
- 外部依赖版本统一由 `bom/pom.xml` 的 `dependencyManagement` 管理。
- 模块内新增依赖时，需先在 BOM 中声明后再引用。

## 构建校验
```bash
mvn -pl component/component-framework/framework-log -am -DskipTests validate
```
