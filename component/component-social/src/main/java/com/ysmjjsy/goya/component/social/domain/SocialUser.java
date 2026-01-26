package com.ysmjjsy.goya.component.social.domain;

import com.ysmjjsy.goya.component.framework.context.GoyaUser;
import com.ysmjjsy.goya.component.social.enums.GenderEnum;
import com.ysmjjsy.goya.component.social.enums.SocialTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/20 23:19
 */
@Data
@Builder
public class SocialUser implements GoyaUser {

    @Serial
    private static final long serialVersionUID = 8792357383746684594L;

    @Schema(description = "本系统userId")
    private String userId;

    @Schema(description = "本系统username")
    private String name;

    @Schema(description = "第三方系统类型")
    private SocialTypeEnum socialTypeEnum;

    @Schema(name = "用户第三方系统的唯一id", description = "在调用方集成该组件时，可以用uuid + source唯一确定一个用")
    private String uuid;

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

    @Schema(name = "性别")
    private GenderEnum gender;

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
