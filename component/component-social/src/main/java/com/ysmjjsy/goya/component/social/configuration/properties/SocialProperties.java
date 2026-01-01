package com.ysmjjsy.goya.component.social.configuration.properties;

import com.ysmjjsy.goya.component.social.constants.ISocialConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.zhyd.oauth.config.AuthConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.io.Serializable;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/1 22:46
 */
@Schema(defaultValue = "社交配置")
@ConfigurationProperties(prefix = ISocialConstants.PROPERTY_SOCIAL)
public record SocialProperties(

        @Schema(defaultValue = "短信配置")
        @DefaultValue
        Sms sms,

        @Schema(defaultValue = "微信小程序配置")
        @DefaultValue
        WxMiniProgram wxMiniProgram,

        @Schema(defaultValue = "第三方配置")
        @DefaultValue
        ThirdPart thirdPart
) {

    @Schema(defaultValue = "第三方配置")
    public record ThirdPart(

            @Schema(defaultValue = "超时时间")
            @DefaultValue("PT5M")
            Duration timeout,

            @Schema(defaultValue = "第三方系统登录配置信息")
            Map<String, AuthConfig> configs,

            @Schema(defaultValue = "支付宝登录 PublicKey")
            String alipayPublicKey
    ) {

    }

    @Schema(defaultValue = "短信配置")
    public record Sms(

            @Schema(defaultValue = "沙盒模式")
            @DefaultValue("false")
            Boolean sandbox,

            @Schema(defaultValue = "测试验证码")
            @DefaultValue("123456")
            String testCode,

            @Schema(defaultValue = "验证码过期时间")
            @DefaultValue("PT5M")
            Duration expire,

            @Schema(defaultValue = "验证码长度")
            @DefaultValue("6")
            Integer length,

            @Schema(defaultValue = "模版 Id")
            @DefaultValue("VERIFICATION_CODE")
            String templateId
    ) {
    }

    public record WxMiniProgram(
            /*
             * 是否开启
             */
            Boolean enabled,

            /*
             * 默认App Id
             */
            String defaultAppId,

            /*
             * 小程序配置列表
             */
            List<Config> configs,

            /*
             * 小程序订阅消息配置列表
             */
            List<Subscribe> subscribes
    ) {
        /**
         * 小程序配置
         */
        public record Config(

                /*
                 * 设置微信小程序的appid
                 */
                String appId,

                /*
                 * 设置微信小程序的Secret
                 */
                String secret,

                /*
                 * 设置微信小程序消息服务器配置的token
                 */
                String token,

                /*
                 * 设置微信小程序消息服务器配置的EncodingAESKey
                 */
                String aesKey,

                /*
                 * 消息格式，XML或者JSON
                 */
                String messageDataFormat
        ) implements Serializable {
        }

        /**
         * 小程序订阅消息配置
         */
        public record Subscribe(

                /*
                 * 订阅消息指定的小程序跳转页面地址
                 */
                String redirectPage,

                /*
                 * 订阅消息模版ID
                 */
                String templateId,

                /*
                 * 自定义Message区分ID，用于获取不同的SubscribeMessageHandler
                 */
                String subscribeId,

                /*
                 * 小程序状态（默认：formal）
                 */
                @DefaultValue("FORMAL")
                MiniProgramStateEnum miniProgramState
        ) implements Serializable {
        }

        @Getter
        @AllArgsConstructor
        public enum MiniProgramStateEnum {

            /**
             * 开发版
             */
            DEVELOPER(0, "开发版"),

            /**
             * 体验版
             */
            TRIAL(1, "体验版"),

            /**
             * 正式版
             */
            FORMAL(2, "正式版"),

            ;


            private final Integer code;
            private final String name;
        }
    }
}
