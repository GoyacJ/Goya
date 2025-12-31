package com.ysmjjsy.goya.component.web.advice;

import com.ysmjjsy.goya.component.common.definition.exception.CommonException;
import com.ysmjjsy.goya.component.common.utils.ByteUtils;
import com.ysmjjsy.goya.component.common.utils.IoUtils;
import com.ysmjjsy.goya.component.common.utils.JsonUtils;
import com.ysmjjsy.goya.component.web.annotation.Crypto;
import com.ysmjjsy.goya.component.web.processor.HttpCryptoProcessor;
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

    private HttpCryptoProcessor httpCryptoProcessor;

    public void setInterfaceCryptoProcessor(HttpCryptoProcessor httpCryptoProcessor) {
        this.httpCryptoProcessor = httpCryptoProcessor;
    }

    @Override
    public boolean supports(MethodParameter methodParameter, @NonNull Type targetType, @NonNull Class<? extends HttpMessageConverter<?>> converterType) {

        String methodName = methodParameter.getMethod().getName();
        Crypto crypto = methodParameter.getMethodAnnotation(Crypto.class);

        boolean isSupports = ObjectUtils.isNotEmpty(crypto) && crypto.requestDecrypt();

        log.trace("[GOYA] |- Is DecryptRequestBodyAdvice supports method [{}] ? Status is [{}].", methodName, isSupports);
        return isSupports;
    }

    @Override
    @NullMarked
    public HttpInputMessage beforeBodyRead(HttpInputMessage httpInputMessage, MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) throws IOException {

        String requestId = WebUtils.getRequestId(httpInputMessage);

        if (WebUtils.isCryptoEnabled(httpInputMessage, requestId)) {

            log.info("[GOYA] |- DecryptRequestBodyAdvice begin decrypt data.");

            String methodName = methodParameter.getMethod().getName();
            String className = methodParameter.getDeclaringClass().getName();

            String content = IoUtils.read(httpInputMessage.getBody());

            if (StringUtils.isNotBlank(content)) {
                String data = httpCryptoProcessor.decrypt(requestId, content);
                if (Strings.CS.equals(data, content)) {
                    data = decrypt(requestId, content);
                }
                log.debug("[GOYA] |- Decrypt request body for rest method [{}] in [{}] finished.", methodName, className);
                return new DecryptHttpInputMessage(httpInputMessage, ByteUtils.toUtf8Bytes(data));
            } else {
                return httpInputMessage;
            }
        } else {
            log.warn("[GOYA] |- Cannot find Goya Cloud custom session header. Use interface crypto founction need add X_GOYA_REQUEST_ID to request header.");
            return httpInputMessage;
        }
    }

    private String decrypt(String sessionKey, String content) throws CommonException {
        JsonNode jsonNode = JsonUtils.toJsonNode(content);
        if (ObjectUtils.isNotEmpty(jsonNode)) {
            decrypt(sessionKey, jsonNode);
            return JsonUtils.toJson(jsonNode);
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
                                httpCryptoProcessor.decrypt(sessionKey, child.stringValue());

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