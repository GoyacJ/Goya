package com.ysmjjsy.goya.component.ddd.definition;

import com.ysmjjsy.goya.component.common.definition.pojo.IEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/19 22:37
 */
@Getter
@Setter
public abstract class AbstractEntity<T extends Serializable> implements IEntity<T> {

    @Serial
    private static final long serialVersionUID = 6786773144623669452L;

    @Schema(description = "id")
    protected T id;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AbstractEntity<?> that)) return false;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
