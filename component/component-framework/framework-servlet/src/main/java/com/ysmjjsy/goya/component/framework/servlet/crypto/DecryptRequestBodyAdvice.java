package com.ysmjjsy.goya.component.framework.servlet.crypto;

import com.ysmjjsy.goya.component.cache.multilevel.crypto.CryptoProcessor;
import com.ysmjjsy.goya.component.core.exception.CommonException;
import com.ysmjjsy.goya.component.core.utils.GoyaByteUtils;
import com.ysmjjsy.goya.component.core.utils.GoyaIoUtils;
import com.ysmjjsy.goya.component.framework.json.GoyaJson;
import com.ysmjjsy.goya.component.web.annotation.Crypto;
import com.ysmjjsy.goya.component.web.utils.WebUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeType;
import tools.jackson.databind.node.ObjectNode;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/10/9 16:28
 */
@RestControllerAdvice
public class DecryptRequestBodyAdvice implements RequestBodyAdvice {

    private static final Logger log = LoggerFactory.getLogger(DecryptRequestBodyAdvice.class);

    private CryptoProcessor cryptoProcessor;

    public void setInterfaceCryptoProcessor(CryptoProcessor httpCryptoProcessor) {
        this.cryptoProcessor = httpCryptoProcessor;
    }

    @Override
    public boolean supports(MethodParameter methodParameter, @NonNull Type targetType, @NonNull Class<? extends HttpMessageConverter<?>> converterType) {

        String methodName = methodParameter.getMethod().getName();
        Crypto crypto = methodParameter.getMethodAnnotation(Crypto.class);

        boolean isSupports = ObjectUtils.isNotEmpty(crypto) && crypto.requestDecrypt();

        log.trace("[Goya] |- Is DecryptRequestBodyAdvice supports method [{}] ? Status is [{}].", methodName, isSupports);
        return isSupports;
    }

    @Override
    @NullMarked
    public HttpInputMessage beforeBodyRead(HttpInputMessage httpInputMessage, MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) throws IOException {

        String requestId = WebUtils.getRequestId(httpInputMessage);

        if (WebUtils.isCryptoEnabled(httpInputMessage, requestId)) {

            log.info("[Goya] |- DecryptRequestBodyAdvice begin decrypt data.");

            String methodName = methodParameter.getMethod().getName();
            String className = methodParameter.getDeclaringClass().getName();

            String content = GoyaIoUtils.read(httpInputMessage.getBody());

            if (StringUtils.isNotBlank(content)) {
                String data = cryptoProcessor.decrypt(requestId, content);
                if (Strings.CS.equals(data, content)) {
                    data = decrypt(requestId, content);
                }
                log.debug("[Goya] |- Decrypt request body for rest method [{}] in [{}] finished.", methodName, className);
                return new DecryptHttpInputMessage(httpInputMessage, GoyaByteUtils.toUtf8Bytes(data));
            } else {
                return httpInputMessage;
            }
        } else {
            log.warn("[Goya] |- Cannot find Goya Cloud custom session header. Use interface crypto founction need add X_GOYA_REQUEST_ID to request header.");
            return httpInputMessage;
        }
    }

    private String decrypt(String sessionKey, String content) throws CommonException {
        JsonNode jsonNode = GoyaJson.toJsonNode(content);
        if (ObjectUtils.isNotEmpty(jsonNode)) {
            decrypt(sessionKey, jsonNode);
            return GoyaJson.toJson(jsonNode);
        }

        return content;
    }

    private void decrypt(String sessionKey, JsonNode node) throws CommonException {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return;
        }
        JsonNodeType type = node.getNodeType();
        switch (type) {
            case OBJECT -> {
                ObjectNode objectNode = (ObjectNode) node;

                for (String fieldName : node.propertyNames()) {
                    JsonNode child = objectNode.get(fieldName);

                    if (child == null || child.isMissingNode()) {
                        continue;
                    }

                    if (child.getNodeType() == JsonNodeType.STRING) {
                        String decrypted =
                                cryptoProcessor.decrypt(sessionKey, child.stringValue());

                        objectNode.put(fieldName, decrypted);
                    } else {
                        decrypt(sessionKey, child);
                    }
                }
            }
            case ARRAY -> {
                ArrayNode arrayNode = (ArrayNode) node;

                for (int i = 0; i < arrayNode.size(); i++) {
                    JsonNode element = arrayNode.get(i);
                    decrypt(sessionKey, element);
                }
            }
            default -> {
            }
        }
    }

    @Override
    @NullMarked
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }

    @Override
    public Object handleEmptyBody(Object body, @NonNull HttpInputMessage inputMessage, @NonNull MethodParameter parameter, @NonNull Type targetType, @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }

    public static class DecryptHttpInputMessage implements HttpInputMessage {

        private final HttpInputMessage httpInputMessage;
        private final byte[] data;

        public DecryptHttpInputMessage(HttpInputMessage httpInputMessage, byte[] data) {
            this.httpInputMessage = httpInputMessage;
            this.data = data;
        }

        @Override
        @NullMarked
        public InputStream getBody() throws IOException {
            return new ByteArrayInputStream(this.data);
        }

        @Override
        @NullMarked
        public HttpHeaders getHeaders() {
            return this.httpInputMessage.getHeaders();
        }
    }
}