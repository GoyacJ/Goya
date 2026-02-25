# bom

`bom` 模块说明文档。

## 模块定位
- 类型：Maven 聚合模块（`packaging=pom`）
- 描述：Spring Boot 4 Enterprise Framework Parent BOM
- 作用：管理子模块编译顺序与聚合构建，不直接提供运行时代码。

## 依赖治理
- 外部依赖版本统一由 `bom/pom.xml` 的 `dependencyManagement` 管理。
- 模块内新增依赖时，需先在 BOM 中声明后再引用。

## 构建校验
```bash
mvn -pl bom -am -DskipTests validate
```
