package com.ysmjjsy.goya.component.framework.servlet.scan;

import com.ysmjjsy.goya.component.framework.servlet.enums.NetworkAccessTypeEnum;
import com.ysmjjsy.goya.component.framework.servlet.enums.RequestMethodEnum;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/29 19:36
 */

@Data
public class RestMapping implements Serializable {

    @Serial
    private static final long serialVersionUID = 4952284287852160748L;

    /**
     * 请求方法
     */
    private Set<RequestMethodEnum> requestMethod;

    /**
     * 服务 ID
     */
    private String serviceId;

    /**
     * 基础 URL
     */
    private String baseUrl;

    /**
     * 全路径请求 URL
     */
    private String url;

    /**
     * 接口 ID
     */
    private String mappingId;

    /**
     * 接口代码
     */
    private String mappingCode;

    /**
     * 接口所在类
     */
    private String className;

    /**
     * 接口所在类标签
     */
    private String tag;

    /**
     * 接口对应方法
     */
    private String methodName;

    /**
     * 简要说明
     */
    private String summary;

    /**
     * 备注
     */
    private String description;

    /**
     * 组件忽略
     */
    private Boolean elementIgnore;

    /**
     * web 表达式
     */
    private String webExpression;

    /**
     * 权限
     */
    private String permissions;

    /**
     * 接口访问网络限制类型
     */
    private NetworkAccessTypeEnum networkAccessType;

    /**
     * 允许访问的 ip
     */
    private String allowIp;

    /**
     * 限制访问的 ip
     */
    private String denyIp;
}
