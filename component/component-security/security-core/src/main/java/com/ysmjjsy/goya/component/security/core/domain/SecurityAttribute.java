package com.ysmjjsy.goya.component.security.core.domain;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.ysmjjsy.goya.component.common.enums.NetworkAccessTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/10/10 11:43
 */
@Getter
@Setter
public class SecurityAttribute implements Serializable {

    @Serial
    private static final long serialVersionUID = -3490780533806400433L;

    @Schema(description = "属性")
    private String attribute;

    @Schema(description = "接口访问网络限制类型")
    private NetworkAccessTypeEnum networkAccessType;

    @Schema(description = "允许访问的ip")
    private String allowIp;

    @Schema(description = "限制访问的ip")
    private String denyIp;


    public SecurityAttribute(String attribute) {
        Assert.hasText(attribute, "You must provide a configuration attribute");
        this.attribute = attribute;
    }

    public static SecurityAttribute create(String attribute) {
        Assert.notNull(attribute, "You must supply an array of attribute names");
        return new SecurityAttribute(attribute.trim());
    }

    public static List<SecurityAttribute> createListFromCommaDelimitedString(String access) {
        return createList(StringUtils.commaDelimitedListToStringArray(access));
    }

    public static List<SecurityAttribute> createList(String... attributeNames) {
        Assert.notNull(attributeNames, "You must supply an array of attribute names");
        List<SecurityAttribute> attributes = new ArrayList<>(attributeNames.length);
        for (String attribute : attributeNames) {
            attributes.add(new SecurityAttribute(attribute.trim()));
        }
        return attributes;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SecurityAttribute that = (SecurityAttribute) o;
        return Objects.equal(attribute, that.attribute);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(attribute);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("attrib", attribute)
                .toString();
    }
}
