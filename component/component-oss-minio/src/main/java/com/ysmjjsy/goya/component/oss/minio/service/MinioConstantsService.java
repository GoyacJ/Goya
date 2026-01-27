package com.ysmjjsy.goya.component.oss.minio.service;

import com.google.common.collect.Maps;
import com.ysmjjsy.goya.component.oss.minio.enums.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * <p> 常量接口 </p>
 *
 * @author goya
 * @since 2023/6/5 22:41
 */
@Service
public class MinioConstantsService {

    private static final List<Map<String, Object>> POLICY_ENUM = PolicyEnums.getJsonStruct();
    private static final List<Map<String, Object>> RETENTION_UNIT_ENUM = RetentionUnitEnums.getJsonStruct();
    private static final List<Map<String, Object>> RETENTION_MODE_ENUM = RetentionModeEnums.getJsonStruct();
    private static final List<Map<String, Object>> SSE_CONFIGURATION_ENUM = SseConfigurationEnums.getJsonStruct();
    private static final List<Map<String, Object>> QUOTA_UNIT_ENUMS = QuotaUnitEnums.getJsonStruct();
    private static final List<Map<String, Object>> VERSIONING_STATUS_ENUMS = VersioningStatusEnums.getJsonStruct();

    public Map<String, Object> getAllEnums() {
        Map<String, Object> map = Maps.newHashMap();
        map.put("policy", POLICY_ENUM);
        map.put("retentionUnit", RETENTION_UNIT_ENUM);
        map.put("retentionMode", RETENTION_MODE_ENUM);
        map.put("sseConfiguration", SSE_CONFIGURATION_ENUM);
        map.put("quotaUnit", QUOTA_UNIT_ENUMS);
        map.put("versioningStatus", VERSIONING_STATUS_ENUMS);
        return map;
    }
}
