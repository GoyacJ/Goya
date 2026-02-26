package com.ysmjjsy.goya.component.admin.support;

import com.ysmjjsy.goya.component.admin.error.AdminErrorCode;
import com.ysmjjsy.goya.component.framework.common.exception.BizException;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * <p>树结构辅助</p>
 *
 * @author goya
 * @since 2026/2/26
 */
public class AdminTreeSupport {

    public String normalizeParentId(String parentId) {
        if (StringUtils.isBlank(parentId)) {
            return "0";
        }
        return parentId.trim();
    }

    public String resolveParentIds(String parentId, Function<String, String> parentIdsLookup) {
        String normalized = normalizeParentId(parentId);
        if ("0".equals(normalized)) {
            return "0";
        }
        String parentIds = parentIdsLookup.apply(normalized);
        if (StringUtils.isBlank(parentIds)) {
            return "0," + normalized;
        }
        if ("0".equals(parentIds)) {
            return "0," + normalized;
        }
        return parentIds + "," + normalized;
    }

    public void validateNoCycle(String currentId, String parentId, Function<String, String> parentLookup) {
        String normalizedParent = normalizeParentId(parentId);
        if (StringUtils.isBlank(currentId) || "0".equals(normalizedParent)) {
            return;
        }
        if (StringUtils.equals(currentId, normalizedParent)) {
            throw new BizException(AdminErrorCode.ADMIN_TREE_CYCLE, "父节点不能是当前节点");
        }

        String cursor = normalizedParent;
        int guard = 0;
        while (StringUtils.isNotBlank(cursor) && !"0".equals(cursor) && guard < 256) {
            if (StringUtils.equals(cursor, currentId)) {
                throw new BizException(AdminErrorCode.ADMIN_TREE_CYCLE, "检测到循环父子关系");
            }
            cursor = parentLookup.apply(cursor);
            guard++;
        }
    }

    public <N extends TreeNode<N>> List<N> buildTree(List<N> flatNodes) {
        Map<String, N> index = new LinkedHashMap<>();
        List<N> roots = new ArrayList<>();
        for (N node : flatNodes) {
            if (node == null || StringUtils.isBlank(node.nodeId())) {
                continue;
            }
            index.put(node.nodeId(), node);
            node.children().clear();
        }
        for (N node : flatNodes) {
            if (node == null || StringUtils.isBlank(node.nodeId())) {
                continue;
            }
            String parentId = normalizeParentId(node.parentId());
            if ("0".equals(parentId)) {
                roots.add(node);
                continue;
            }
            N parent = index.get(parentId);
            if (parent == null) {
                roots.add(node);
                continue;
            }
            parent.children().add(node);
        }
        return roots;
    }

    public interface TreeNode<N> {

        String nodeId();

        String parentId();

        List<N> children();
    }
}
