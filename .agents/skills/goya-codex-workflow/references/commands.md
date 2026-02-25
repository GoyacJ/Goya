# Commands

## 常用构建命令
- 全仓编译（跳过测试）：
  `mvn -DskipTests compile`
- 按模块校验（跳过测试）：
  `mvn -pl platform/platform-monolith -am -DskipTests validate`
- AI 聚合模块校验：
  `mvn -pl ai -am -DskipTests validate`

## 排障命令
- 查看模块树：
  `mvn -q help:effective-pom -Doutput=.flattened-pom.xml`
- 快速定位依赖声明：
  `rg "<artifactId>.*</artifactId>" **/pom.xml`
