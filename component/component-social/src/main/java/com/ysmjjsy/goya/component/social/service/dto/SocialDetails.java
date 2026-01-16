package com.ysmjjsy.goya.component.social.service.dto;

import com.ysmjjsy.goya.component.core.pojo.DTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import me.zhyd.oauth.enums.AuthUserGender;

import java.io.Serial;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/6 09:47
 */
@Data
public class SocialDetails implements DTO {

    @Serial
    private static final long serialVersionUID = 1793977806432185493L;
    
    /**
     * JustAuth中的关键词
     * 以下内容了解后，将会使你更容易地上手JustAuth。
     * <p>
     * source JustAuth支持的第三方平台，比如：GITHUB、GITEE等
     * uuid 一般为第三方平台的用户ID。以下几个平台需特别注意：
     * 钉钉、抖音：uuid 为用户的 unionid
     * 微信公众平台登录、京东、酷家乐、美团：uuid 为用户的 openId
     * 微信开放平台登录、QQ：uuid 为用户的 openId，平台支持获取unionid， unionid 在 AuthToken 中（如果支持），在登录完成后，可以通过 response.getData().getToken().getUnionId() 获取
     * Google：uuid 为用户的 sub，sub为Google的所有账户体系中用户唯一的身份标识符，详见：OpenID Connect (opens new window)
     * 注：建议通过uuid + source的方式唯一确定一个用户，这样可以解决用户身份归属的问题。因为 单个用户ID 在某一平台中是唯一的，但不能保证在所有平台中都是唯一的。
     */
    @Schema(name = "用户第三方系统的唯一id", description = "在调用方集成该组件时，可以用uuid + source唯一确定一个用")
    private String thirdPartId;

    @Schema(name = "用户名")
    private String username;

    @Schema(name = "用户昵称")
    private String nickname;

    @Schema(name = "用户头像")
    private String avatar;

    @Schema(name = "用户网址")
    private String blog;

    @Schema(name = "所在公司")
    private String company;

    @Schema(name = "位置")
    private String location;

    @Schema(name = "用户邮箱")
    private String email;

    @Schema(name = "用户邮箱")
    private String remark;
    /**
     * 性别
     */
    @Schema(name = "性别")
    private AuthUserGender gender;

    @Schema(name = "第三方用户来源")
    private String source;

    @Schema(name = "用户的授权令牌")
    private String accessToken;

    @Schema(name = "第三方用户的授权令牌的有效期", description = "部分平台可能没有")
    private Integer expireIn;

    @Schema(name = "刷新令牌", description = "部分平台可能没有")
    private String refreshToken;

    @Schema(name = "第三方用户的刷新令牌的有效期", description = "部分平台可能没有")
    private Integer refreshTokenExpireIn;

    @Schema(name = "第三方用户授予的权限", description = "部分平台可能没有")
    private String scope;

    @Schema(name = "个别平台的授权信息", description = "部分平台可能没有")
    private String tokenType;

    @Schema(name = "第三方用户的 ID", description = "部分平台可能没有")
    private String uid;

    @Schema(name = "第三方用户的 open id", description = "部分平台可能没有")
    private String openId;

    @Schema(name = "个别平台的授权信息", description = "部分平台可能没有")
    private String accessCode;

    @Schema(name = "第三方用户的 union id", description = "部分平台可能没有")
    private String unionId;

    @Schema(name = "小程序Appid", description = "部分平台可能没有")
    private String appId;

    @Schema(name = "手机号码", description = "部分平台可能没有")
    private String phoneNumber;
}
