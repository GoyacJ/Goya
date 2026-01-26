package com.ysmjjsy.goya.component.framework.common.utils;

import com.ysmjjsy.goya.component.framework.common.constants.SymbolConst;
import com.ysmjjsy.goya.component.framework.common.error.CommonErrorCode;
import com.ysmjjsy.goya.component.framework.common.exception.Exceptions;
import com.ysmjjsy.goya.component.framework.common.exception.GoyaException;
import lombok.experimental.UtilityClass;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * <p>资源工具类</p>
 * <p>提供类路径资源、文件系统资源、URL 资源的加载和读取功能</p>
 * <p>不依赖第三方库，使用 Java 标准库实现</p>
 *
 * @author goya
 * @since 2025/12/7 20:25
 */
@UtilityClass
public class GoyaResourceUtils {

    // ==================== 类路径资源 ====================

    /**
     * 从类路径获取资源 URL
     *
     * @param resourcePath 资源路径（如：/config/app.properties）
     * @return 资源 URL，如果不存在返回 null
     */
    public static URL getClassPathResource(String resourcePath) {
        if (resourcePath == null || resourcePath.isEmpty()) {
            return null;
        }
        // 确保路径以 / 开头
        String path = resourcePath.startsWith(SymbolConst.FORWARD_SLASH) ? resourcePath : SymbolConst.FORWARD_SLASH + resourcePath;
        return GoyaResourceUtils.class.getResource(path);
    }

    /**
     * 从类路径获取资源输入流
     *
     * @param resourcePath 资源路径
     * @return 资源输入流
     * @throws GoyaException 如果资源不存在或读取失败
     */
    public static InputStream getClassPathResourceAsStream(String resourcePath) {
        if (resourcePath == null || resourcePath.isEmpty()) {
            throw Exceptions.system(CommonErrorCode.SYSTEM_ERROR).userMessage("Resource path cannot be null or empty").build();
        }
        // 确保路径以 / 开头
        String path = resourcePath.startsWith(SymbolConst.FORWARD_SLASH) ? resourcePath : SymbolConst.FORWARD_SLASH + resourcePath;
        InputStream inputStream = GoyaResourceUtils.class.getResourceAsStream(path);
        if (inputStream == null) {
            throw Exceptions.system(CommonErrorCode.SYSTEM_ERROR).userMessage("Resource not found: " + resourcePath).build();
        }
        return inputStream;
    }

    /**
     * 检查类路径资源是否存在
     *
     * @param resourcePath 资源路径
     * @return 是否存在
     */
    public static boolean existsClassPathResource(String resourcePath) {
        return getClassPathResource(resourcePath) != null;
    }

    /**
     * 从类路径读取资源为字符串（UTF-8）
     *
     * @param resourcePath 资源路径
     * @return 资源内容
     * @throws GoyaException 如果读取失败
     */
    public static String readClassPathResourceAsString(String resourcePath) {
        return readClassPathResourceAsString(resourcePath, StandardCharsets.UTF_8);
    }

    /**
     * 从类路径读取资源为字符串（指定编码）
     *
     * @param resourcePath 资源路径
     * @param charset      字符编码
     * @return 资源内容
     * @throws GoyaException 如果读取失败
     */
    public static String readClassPathResourceAsString(String resourcePath, Charset charset) {
        try (InputStream inputStream = getClassPathResourceAsStream(resourcePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charset))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
            // 移除最后一个换行符
            if (!sb.isEmpty()) {
                sb.setLength(sb.length() - System.lineSeparator().length());
            }
            return sb.toString();
        } catch (IOException e) {
            throw Exceptions.system(CommonErrorCode.SYSTEM_ERROR).cause(e).build();
        }
    }

    /**
     * 从类路径读取资源为字节数组
     *
     * @param resourcePath 资源路径
     * @return 资源字节数组
     * @throws GoyaException 如果读取失败
     */
    public static byte[] readClassPathResourceAsBytes(String resourcePath) {
        try (InputStream inputStream = getClassPathResourceAsStream(resourcePath);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw Exceptions.system(CommonErrorCode.SYSTEM_ERROR).cause(e).build();
        }
    }

    /**
     * 从类路径读取资源为 Properties
     *
     * @param resourcePath 资源路径
     * @return Properties 对象
     * @throws GoyaException 如果读取失败
     */
    public static Properties readClassPathResourceAsProperties(String resourcePath) {
        Properties properties = new Properties();
        try (InputStream inputStream = getClassPathResourceAsStream(resourcePath)) {
            properties.load(inputStream);
            return properties;
        } catch (IOException e) {
            throw Exceptions.system(CommonErrorCode.SYSTEM_ERROR).cause(e).build();
        }
    }

    /**
     * 从类路径读取资源为行列表
     *
     * @param resourcePath 资源路径
     * @return 行列表
     * @throws GoyaException 如果读取失败
     */
    public static List<String> readClassPathResourceAsLines(String resourcePath) {
        return readClassPathResourceAsLines(resourcePath, StandardCharsets.UTF_8);
    }

    /**
     * 从类路径读取资源为行列表（指定编码）
     *
     * @param resourcePath 资源路径
     * @param charset      字符编码
     * @return 行列表
     * @throws GoyaException 如果读取失败
     */
    public static List<String> readClassPathResourceAsLines(String resourcePath, Charset charset) {
        try (InputStream inputStream = getClassPathResourceAsStream(resourcePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charset))) {
            return reader.lines().toList();
        } catch (IOException e) {
            throw Exceptions.system(CommonErrorCode.SYSTEM_ERROR).cause(e).build();
        }
    }

    /**
     * 获取所有匹配的类路径资源
     *
     * @param resourcePath 资源路径（支持通配符，如：/config/*.properties）
     * @return 资源 URL 枚举
     * @throws GoyaException 如果查找失败
     */
    public static Enumeration<URL> getClassPathResources(String resourcePath) {
        if (resourcePath == null || resourcePath.isEmpty()) {
            throw Exceptions.system(CommonErrorCode.SYSTEM_ERROR).userMessage("Resource path cannot be null or empty").build();
        }
        try {
            String path = resourcePath.startsWith(SymbolConst.FORWARD_SLASH) ? resourcePath : SymbolConst.FORWARD_SLASH + resourcePath;
            ClassLoader classLoader = GoyaResourceUtils.class.getClassLoader();
            if (classLoader == null) {
                classLoader = ClassLoader.getSystemClassLoader();
            }
            return classLoader.getResources(path);
        } catch (IOException e) {
            throw Exceptions.system(CommonErrorCode.SYSTEM_ERROR).cause(e).build();
        }
    }

    // ==================== 文件系统资源 ====================

    /**
     * 从文件系统读取资源为字符串（UTF-8）
     *
     * @param filePath 文件路径
     * @return 文件内容
     * @throws GoyaException 如果读取失败
     */
    public static String readFileAsString(String filePath) {
        return readFileAsString(filePath, StandardCharsets.UTF_8);
    }

    /**
     * 从文件系统读取资源为字符串（指定编码）
     *
     * @param filePath 文件路径
     * @param charset  字符编码
     * @return 文件内容
     * @throws GoyaException 如果读取失败
     */
    public static String readFileAsString(String filePath, Charset charset) {
        try {
            Path path = Paths.get(filePath);
            return Files.readString(path, charset);
        } catch (IOException e) {
            throw Exceptions.system(CommonErrorCode.SYSTEM_ERROR).cause(e).build();
        }
    }

    /**
     * 从文件系统读取资源为字节数组
     *
     * @param filePath 文件路径
     * @return 文件字节数组
     * @throws GoyaException 如果读取失败
     */
    public static byte[] readFileAsBytes(String filePath) {
        try {
            Path path = Paths.get(filePath);
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw Exceptions.system(CommonErrorCode.SYSTEM_ERROR).cause(e).build();
        }
    }

    /**
     * 从文件系统读取资源为行列表
     *
     * @param filePath 文件路径
     * @return 行列表
     * @throws GoyaException 如果读取失败
     */
    public static List<String> readFileAsLines(String filePath) {
        return readFileAsLines(filePath, StandardCharsets.UTF_8);
    }

    /**
     * 从文件系统读取资源为行列表（指定编码）
     *
     * @param filePath 文件路径
     * @param charset  字符编码
     * @return 行列表
     * @throws GoyaException 如果读取失败
     */
    public static List<String> readFileAsLines(String filePath, Charset charset) {
        try {
            Path path = Paths.get(filePath);
            return Files.readAllLines(path, charset);
        } catch (IOException e) {
            throw Exceptions.system(CommonErrorCode.SYSTEM_ERROR).cause(e).build();
        }
    }

    /**
     * 从文件系统读取资源为 Properties
     *
     * @param filePath 文件路径
     * @return Properties 对象
     * @throws GoyaException 如果读取失败
     */
    public static Properties readFileAsProperties(String filePath) {
        Properties properties = new Properties();
        try (FileInputStream inputStream = new FileInputStream(filePath)) {
            properties.load(inputStream);
            return properties;
        } catch (IOException e) {
            throw Exceptions.system(CommonErrorCode.SYSTEM_ERROR).cause(e).build();
        }
    }

    // ==================== URL 资源 ====================

    /**
     * 从 URL 读取资源为字符串（UTF-8）
     *
     * @param url URL 地址
     * @return 资源内容
     * @throws GoyaException 如果读取失败
     */
    public static String readUrlAsString(String url) {
        return readUrlAsString(url, StandardCharsets.UTF_8);
    }

    /**
     * 从 URL 读取资源为字符串（指定编码）
     *
     * @param url     URL 地址
     * @param charset 字符编码
     * @return 资源内容
     * @throws GoyaException 如果读取失败
     */
    public static String readUrlAsString(String url, Charset charset) {
        try {
            URL resourceUrl = new URI(url).toURL();
            try (InputStream inputStream = resourceUrl.openStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charset))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append(System.lineSeparator());
                }
                // 移除最后一个换行符
                if (!sb.isEmpty()) {
                    sb.setLength(sb.length() - System.lineSeparator().length());
                }
                return sb.toString();
            }
        } catch (IOException | URISyntaxException e) {
            throw Exceptions.system(CommonErrorCode.SYSTEM_ERROR).cause(e).build();
        }
    }

    /**
     * 从 URL 读取资源为字节数组
     *
     * @param url URL 地址
     * @return 资源字节数组
     * @throws GoyaException 如果读取失败
     */
    public static byte[] readUrlAsBytes(String url) {
        try {
            URL resourceUrl = new URI(url).toURL();
            try (InputStream inputStream = resourceUrl.openStream();
                 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                return outputStream.toByteArray();
            }
        } catch (IOException | URISyntaxException e) {
            throw Exceptions.system(CommonErrorCode.SYSTEM_ERROR).cause(e).build();
        }
    }

    // ==================== 工具方法 ====================

    /**
     * 从输入流读取为字符串（UTF-8）
     *
     * @param inputStream 输入流
     * @return 字符串内容
     * @throws GoyaException 如果读取失败
     */
    public static String readInputStreamAsString(InputStream inputStream) {
        return readInputStreamAsString(inputStream, StandardCharsets.UTF_8);
    }

    /**
     * 从输入流读取为字符串（指定编码）
     *
     * @param inputStream 输入流
     * @param charset     字符编码
     * @return 字符串内容
     * @throws GoyaException 如果读取失败
     */
    public static String readInputStreamAsString(InputStream inputStream, Charset charset) {
        if (inputStream == null) {
            throw Exceptions.system(CommonErrorCode.SYSTEM_ERROR).userMessage("InputStream cannot be null").build();
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charset))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
            // 移除最后一个换行符
            if (!sb.isEmpty()) {
                sb.setLength(sb.length() - System.lineSeparator().length());
            }
            return sb.toString();
        } catch (IOException e) {
            throw Exceptions.system(CommonErrorCode.SYSTEM_ERROR).cause(e).build();
        }
    }

    /**
     * 从输入流读取为字节数组
     *
     * @param inputStream 输入流
     * @return 字节数组
     * @throws GoyaException 如果读取失败
     */
    public static byte[] readInputStreamAsBytes(InputStream inputStream) {
        if (inputStream == null) {
            throw Exceptions.system(CommonErrorCode.SYSTEM_ERROR).userMessage("InputStream cannot be null").build();
        }
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw Exceptions.system(CommonErrorCode.SYSTEM_ERROR).cause(e).build();
        }
    }

    /**
     * 检查资源路径是否为类路径资源
     *
     * @param resourcePath 资源路径
     * @return 是否为类路径资源
     */
    public static boolean isClassPathResource(String resourcePath) {
        if (resourcePath == null || resourcePath.isEmpty()) {
            return false;
        }
        return resourcePath.startsWith("classpath:") || resourcePath.startsWith("/");
    }

    /**
     * 检查资源路径是否为文件系统路径
     *
     * @param resourcePath 资源路径
     * @return 是否为文件系统路径
     */
    public static boolean isFileSystemResource(String resourcePath) {
        if (resourcePath == null || resourcePath.isEmpty()) {
            return false;
        }
        return resourcePath.startsWith("file:") || Paths.get(resourcePath).isAbsolute();
    }

    /**
     * 检查资源路径是否为 URL
     *
     * @param resourcePath 资源路径
     * @return 是否为 URL
     */
    public static boolean isUrlResource(String resourcePath) {
        if (resourcePath == null || resourcePath.isEmpty()) {
            return false;
        }
        return resourcePath.startsWith("http://") || resourcePath.startsWith("https://") ||
                resourcePath.startsWith("ftp://") || resourcePath.startsWith("file://");
    }

    /**
     * 智能读取资源为字节数组
     * <p>根据资源路径类型自动选择读取方式：</p>
     * <ul>
     *   <li>类路径资源（classpath: 或 / 开头）：从类路径读取</li>
     *   <li>URL 资源（http://, https:// 等开头）：从 URL 读取</li>
     *   <li>文件系统路径：从文件系统读取</li>
     * </ul>
     *
     * @param resourcePath 资源路径
     * @return 资源字节数组
     * @throws GoyaException 如果读取失败
     */
    public static byte[] readBytes(String resourcePath) {
        if (resourcePath == null || resourcePath.isEmpty()) {
            throw Exceptions.system(CommonErrorCode.SYSTEM_ERROR).userMessage("Resource path cannot be null or empty").build();
        }

        // 移除 classpath: 前缀（如果存在）
        String path = resourcePath.startsWith("classpath:") 
                ? resourcePath.substring(10) 
                : resourcePath;

        // 判断资源类型并选择相应的读取方式
        if (isClassPathResource(path)) {
            return readClassPathResourceAsBytes(path);
        } else if (isUrlResource(resourcePath)) {
            return readUrlAsBytes(resourcePath);
        } else {
            // 默认为文件系统路径
            return readFileAsBytes(resourcePath);
        }
    }
}
