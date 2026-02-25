# component-social

`component-social` 提供短信登录、第三方登录、小程序登录相关能力，并提供默认社交绑定存储实现。

## 核心能力

- 短信验证码能力：`SmsService`
- 第三方登录能力：`ThirdPartService`
- 小程序登录能力：`WxMiniProgramService`
- 社交用户聚合管理：`SocialManager`
- 默认绑定存储：`SocialBindingStore` + `CacheSocialBindingStore`

## 默认实现

- `DefaultSocialManager` 已实现手机号、第三方、小程序账号的查询/保存/更新。
- 若未自定义绑定存储，自动装配 `CacheSocialBindingStore`。
- 绑定缓存默认过期：`P3650D`（可配置）。

## 关键配置

配置前缀：`goya.social`

- `sms.*`：短信配置
- `third-part.*`：第三方登录配置
- `wx-mini-program.*`：小程序配置
- `binding.expire`：社交绑定缓存过期时间

## 与 security-authentication 集成

`security-authentication` 提供默认 `ISocialUserService` 适配器（`@ConditionalOnMissingBean`），会优先复用本模块的 `SocialBindingStore` 进行账号绑定解析。

## 构建校验

```bash
mvn -pl component/component-social -am -DskipTests validate
```
