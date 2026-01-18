package com.ysmjjsy.goya.component.oss.service;

import com.ysmjjsy.goya.component.core.exception.CommonException;
import com.ysmjjsy.goya.component.oss.core.arguments.object.PutObjectArguments;
import com.ysmjjsy.goya.component.oss.core.core.repository.OssObjectRepository;
import com.ysmjjsy.goya.component.oss.core.domain.object.GetObjectDomain;
import com.ysmjjsy.goya.component.oss.core.domain.object.ObjectMetadataDomain;
import com.ysmjjsy.goya.component.oss.core.domain.object.PutObjectDomain;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * <p>OSS 对象流式上传、下载 Service</p>
 *
 * @author goya
 * @since 2025/11/3 09:42
 */
@Slf4j
@Service
public class OssObjectStreamService {

    private final OssObjectRepository ossObjectRepository;

    public OssObjectStreamService(OssObjectRepository ossObjectRepository) {
        this.ossObjectRepository = ossObjectRepository;
    }

    /**
     * 流式文件下载
     *
     * @param bucketName 存储桶名称
     * @param objectName 存储对象名称
     * @param isOnline   true 在线显示，false 直接下载
     * @param response   {@link HttpServletResponse}
     * @throws IOException 输入输出错误
     */
    private void stream(String bucketName, String objectName, boolean isOnline, HttpServletResponse response) throws IOException {
        ObjectMetadataDomain objectMetadata = ossObjectRepository.getObjectMetadata(bucketName, objectName);

        String type = isOnline ? "inline" : "attachment";

        response.setContentType(objectMetadata.getContentType());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, type + ";filename=" + URLEncoder.encode(objectName, StandardCharsets.UTF_8));

        GetObjectDomain domain = ossObjectRepository.getObject(bucketName, objectName);
        InputStream is = domain.getObjectContent();
        IOUtils.copy(is, response.getOutputStream());
        IOUtils.closeQuietly(is);
    }

    /**
     * 以流的方式返回响应内容，前端可直接下载
     *
     * @param bucketName 存储桶名称
     * @param objectName 存储对象名称
     * @param response   {@link HttpServletResponse}
     * @throws IOException 输入输出错误
     */
    public void download(String bucketName, String objectName, HttpServletResponse response) throws IOException {
        stream(bucketName, objectName, false, response);
    }

    /**
     * 以流的方式返回响应内容，前端可直接展示
     *
     * @param bucketName 存储桶名称
     * @param objectName 存储对象名称
     * @param response   {@link HttpServletResponse}
     * @throws IOException 输入输出错误
     */
    public void display(String bucketName, String objectName, HttpServletResponse response) throws IOException {
        stream(bucketName, objectName, true, response);
    }

    /**
     * 普通文件上传
     *
     * @param bucketName 存储桶名称
     * @param file       文件 {@link MultipartFile}
     * @return 上传结果实体 {@link PutObjectDomain}
     */
    public PutObjectDomain upload(String bucketName, MultipartFile file) {

        try {
            PutObjectArguments arguments = new PutObjectArguments();
            arguments.setContentType(file.getContentType());
            arguments.setObjectSize(file.getSize());
            arguments.setPartSize(-1L);
            arguments.setInputStream(file.getInputStream());
            arguments.setObjectName(file.getOriginalFilename());
            arguments.setBucketName(bucketName);
            return ossObjectRepository.putObject(arguments);
        } catch (IOException e) {
            log.error("[HZ-ZHG] |- Minio upload catch IOException.", e);
            throw new CommonException(e.getMessage());
        }
    }
}
