package com.ysmjjsy.goya.component.framework.oss.proxy;

import com.ysmjjsy.goya.component.framework.oss.configuration.properties.OssProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * <p>OSS 与签名代理</p>
 *
 * @author goya
 * @since 2025/11/3 09:37
 */
public class OssPresignedUrlProxy {

    private final Function<HttpServletRequest, String> function;
    private final RestTemplate restTemplate;

    public OssPresignedUrlProxy(OssProperties ossProperties) {
        this.function = new OssProxyAddressFunction(ossProperties);
        this.restTemplate = new RestTemplate(new SimpleClientHttpRequestFactory());
    }

    public ResponseEntity<String> delegate(HttpServletRequest request) {
        try {
            String target = function.apply(request);
            RequestEntity<byte[]> requestEntity = createRequestEntity(request, target);
            return restTemplate.exchange(requestEntity, String.class);
        } catch (Exception e) {
            return new ResponseEntity<>("Delegate ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 创建请求
     *
     * @param request 请求 {@link HttpServletRequest}
     * @param url     请求地址
     * @return 具体请求
     * @throws URISyntaxException uri语法错误
     * @throws IOException        io错误
     */
    private RequestEntity<byte[]> createRequestEntity(HttpServletRequest request, String url) throws URISyntaxException, IOException {
        String method = request.getMethod();
        HttpMethod httpMethod = method != null ? HttpMethod.valueOf(method) : null;
        HttpHeaders headers = readRequestHeader(request);
        byte[] body = readRequestBody(request);
        return new RequestEntity<>(body, headers, httpMethod, new URI(url));
    }

    /**
     * 解析请求体
     *
     * @param request 请求 {@link HttpServletRequest}
     * @return request body
     * @throws IOException io错误
     */
    private byte[] readRequestBody(HttpServletRequest request) throws IOException {
        InputStream inputStream = request.getInputStream();
        return StreamUtils.copyToByteArray(inputStream);
    }

    /**
     * 解析请求 Headers
     *
     * @param request 请求 {@link HttpServletRequest}
     * @return 请求头
     */
    private HttpHeaders readRequestHeader(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        List<String> headerNames = Collections.list(request.getHeaderNames());
        for (String headerName : headerNames) {
            List<String> headerValues = Collections.list(request.getHeaders(headerName));
            for (String headerValue : headerValues) {
                headers.add(headerName, headerValue);
            }
        }

        // TODO: 如果传递 OAuth2 Token 会导致转发上传失败。猜测是因为 Minio Server 也是采用 OAuth2 认证，体系不一致导致。
        // 目前先临时将外部传入的 Token 取消，等摸清楚 Minio 认证体系集成方式后再行完善。
        headers.remove(HttpHeaders.AUTHORIZATION);

        return headers;
    }
}
