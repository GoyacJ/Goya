package com.ysmjjsy.goya.component.common.utils;

import com.ysmjjsy.goya.component.common.definition.exception.CommonException;
import lombok.experimental.UtilityClass;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * <p>IO 工具类</p>
 * <p>提供输入流读取、转换等功能</p>
 * <p>不依赖第三方库，使用 Java 标准库实现</p>
 *
 * @author goya
 * @since 2025/12/7 22:30
 */
@UtilityClass
public class IoUtils {

    /**
     * 从输入流读取为字符串（UTF-8）
     * <p>注意：此方法会关闭输入流</p>
     *
     * @param inputStream 输入流
     * @return 字符串内容
     * @throws CommonException 如果读取失败
     */
    public static String read(InputStream inputStream) {
        return read(inputStream, StandardCharsets.UTF_8);
    }

    /**
     * 从输入流读取为字符串（指定编码）
     * <p>注意：此方法会关闭输入流</p>
     *
     * @param inputStream 输入流
     * @param charset     字符编码
     * @return 字符串内容
     * @throws CommonException 如果读取失败
     */
    public static String read(InputStream inputStream, Charset charset) {
        if (inputStream == null) {
            return null;
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
            throw new CommonException("Failed to read input stream", e);
        }
    }

    /**
     * 从输入流读取为字符串（指定编码名称）
     * <p>注意：此方法会关闭输入流</p>
     *
     * @param inputStream 输入流
     * @param charsetName 字符编码名称
     * @return 字符串内容
     * @throws CommonException 如果读取失败
     */
    public static String read(InputStream inputStream, String charsetName) {
        if (inputStream == null) {
            return null;
        }
        try {
            return read(inputStream, Charset.forName(charsetName));
        } catch (Exception e) {
            throw new CommonException("Failed to read input stream with charset: " + charsetName, e);
        }
    }

    /**
     * 从输入流读取为字节数组
     * <p>注意：此方法会关闭输入流</p>
     *
     * @param inputStream 输入流
     * @return 字节数组
     * @throws CommonException 如果读取失败
     */
    public static byte[] readBytes(InputStream inputStream) {
        if (inputStream == null) {
            return null;
        }
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new CommonException("Failed to read bytes from input stream", e);
        }
    }

    /**
     * 将字节数组写入输出流
     *
     * @param outputStream 输出流
     * @param data         字节数组
     * @throws CommonException 如果写入失败
     */
    public static void write(OutputStream outputStream, byte[] data) {
        if (outputStream == null) {
            throw new CommonException("OutputStream cannot be null");
        }
        if (data == null) {
            return;
        }
        try {
            outputStream.write(data);
            outputStream.flush();
        } catch (IOException e) {
            throw new CommonException("Failed to write bytes to output stream", e);
        }
    }

    /**
     * 将字符串写入输出流（UTF-8）
     *
     * @param outputStream 输出流
     * @param data         字符串
     * @throws CommonException 如果写入失败
     */
    public static void write(OutputStream outputStream, String data) {
        write(outputStream, data, StandardCharsets.UTF_8);
    }

    /**
     * 将字符串写入输出流（指定编码）
     *
     * @param outputStream 输出流
     * @param data         字符串
     * @param charset      字符编码
     * @throws CommonException 如果写入失败
     */
    public static void write(OutputStream outputStream, String data, Charset charset) {
        if (outputStream == null) {
            throw new CommonException("OutputStream cannot be null");
        }
        if (data == null) {
            return;
        }
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, charset))) {
            writer.write(data);
            writer.flush();
        } catch (IOException e) {
            throw new CommonException("Failed to write string to output stream", e);
        }
    }

    /**
     * 复制输入流到输出流
     *
     * @param inputStream  输入流
     * @param outputStream 输出流
     * @return 复制的字节数
     * @throws CommonException 如果复制失败
     */
    public static long copy(InputStream inputStream, OutputStream outputStream) {
        if (inputStream == null || outputStream == null) {
            throw new CommonException("InputStream and OutputStream cannot be null");
        }
        try {
            byte[] buffer = new byte[4096];
            long totalBytes = 0;
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
            }
            outputStream.flush();
            return totalBytes;
        } catch (IOException e) {
            throw new CommonException("Failed to copy input stream to output stream", e);
        }
    }

    /**
     * 关闭输入流（忽略异常）
     *
     * @param inputStream 输入流
     */
    public static void closeQuietly(InputStream inputStream) {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                // 忽略异常
            }
        }
    }

    /**
     * 关闭输出流（忽略异常）
     *
     * @param outputStream 输出流
     */
    public static void closeQuietly(OutputStream outputStream) {
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
                // 忽略异常
            }
        }
    }

    /**
     * 关闭 Reader（忽略异常）
     *
     * @param reader Reader
     */
    public static void closeQuietly(Reader reader) {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                // 忽略异常
            }
        }
    }

    /**
     * 关闭 Writer（忽略异常）
     *
     * @param writer Writer
     */
    public static void closeQuietly(Writer writer) {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                // 忽略异常
            }
        }
    }
}
