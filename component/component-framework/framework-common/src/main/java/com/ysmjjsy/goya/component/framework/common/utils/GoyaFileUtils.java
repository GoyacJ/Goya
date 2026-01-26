package com.ysmjjsy.goya.component.framework.common.utils;

import com.ysmjjsy.goya.component.framework.common.error.CommonErrorCode;
import com.ysmjjsy.goya.component.framework.common.exception.Exceptions;
import com.ysmjjsy.goya.component.framework.common.exception.GoyaException;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * <p>文件工具类</p>
 * <p>提供文件读写、复制、移动、删除、路径处理等功能</p>
 * <p>不依赖第三方库，使用 Java 标准库实现</p>
 *
 * @author goya
 * @since 2025/12/7 20:00
 */
@UtilityClass
public class GoyaFileUtils {

    // ==================== 文件读取 ====================

    /**
     * 读取文件内容为字符串（UTF-8）
     *
     * @param file 文件
     * @return 文件内容
     * @throws GoyaException 如果读取失败
     */
    public static String readString(File file) {
        return readString(file, StandardCharsets.UTF_8);
    }

    /**
     * 读取文件内容为字符串（指定编码）
     *
     * @param file    文件
     * @param charset 字符编码
     * @return 文件内容
     * @throws GoyaException 如果读取失败
     */
    public static String readString(File file, Charset charset) {
        try {
            return Files.readString(file.toPath(), charset);
        } catch (IOException e) {
            throw Exceptions.system(CommonErrorCode.SYSTEM_ERROR).cause(e).build();
        }
    }

    /**
     * 读取文件内容为字符串（UTF-8）
     *
     * @param filePath 文件路径
     * @return 文件内容
     * @throws GoyaException 如果读取失败
     */
    public static String readString(String filePath) {
        return readString(new File(filePath));
    }

    /**
     * 读取文件内容为字符串（指定编码）
     *
     * @param filePath 文件路径
     * @param charset  字符编码
     * @return 文件内容
     * @throws GoyaException 如果读取失败
     */
    public static String readString(String filePath, Charset charset) {
        return readString(new File(filePath), charset);
    }

    /**
     * 按行读取文件内容
     *
     * @param file 文件
     * @return 行列表
     * @throws GoyaException 如果读取失败
     */
    public static List<String> readLines(File file) {
        return readLines(file, StandardCharsets.UTF_8);
    }

    /**
     * 按行读取文件内容（指定编码）
     *
     * @param file    文件
     * @param charset 字符编码
     * @return 行列表
     * @throws GoyaException 如果读取失败
     */
    public static List<String> readLines(File file, Charset charset) {
        try {
            return Files.readAllLines(file.toPath(), charset);
        } catch (IOException e) {
            throw Exceptions.system(CommonErrorCode.SYSTEM_ERROR).cause(e).build();
        }
    }

    /**
     * 按行读取文件内容
     *
     * @param filePath 文件路径
     * @return 行列表
     * @throws GoyaException 如果读取失败
     */
    public static List<String> readLines(String filePath) {
        return readLines(new File(filePath));
    }

    /**
     * 读取文件内容为字节数组
     *
     * @param file 文件
     * @return 字节数组
     * @throws GoyaException 如果读取失败
     */
    public static byte[] readBytes(File file) {
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            throw Exceptions.system(CommonErrorCode.SYSTEM_ERROR).cause(e).build();
        }
    }

    /**
     * 读取文件内容为字节数组
     *
     * @param filePath 文件路径
     * @return 字节数组
     * @throws GoyaException 如果读取失败
     */
    public static byte[] readBytes(String filePath) {
        return readBytes(new File(filePath));
    }

    // ==================== 文件写入 ====================

    /**
     * 写入字符串到文件（UTF-8）
     *
     * @param file    文件
     * @param content 内容
     * @throws GoyaException 如果写入失败
     */
    public static void writeString(File file, String content) {
        writeString(file, content, StandardCharsets.UTF_8);
    }

    /**
     * 写入字符串到文件（指定编码）
     *
     * @param file    文件
     * @param content 内容
     * @param charset 字符编码
     * @throws GoyaException 如果写入失败
     */
    public static void writeString(File file, String content, Charset charset) {
        try {
            mkdirs(file.getParentFile());
            Files.writeString(file.toPath(), content, charset);
        } catch (IOException e) {
            throw Exceptions.system(CommonErrorCode.SYSTEM_ERROR).cause(e).build();
        }
    }

    /**
     * 写入字符串到文件（UTF-8）
     *
     * @param filePath 文件路径
     * @param content  内容
     * @throws GoyaException 如果写入失败
     */
    public static void writeString(String filePath, String content) {
        writeString(new File(filePath), content);
    }

    /**
     * 写入多行内容到文件（UTF-8）
     *
     * @param file  文件
     * @param lines 行列表
     * @throws GoyaException 如果写入失败
     */
    public static void writeLines(File file, List<String> lines) {
        writeLines(file, lines, StandardCharsets.UTF_8);
    }

    /**
     * 写入多行内容到文件（指定编码）
     *
     * @param file    文件
     * @param lines   行列表
     * @param charset 字符编码
     * @throws GoyaException 如果写入失败
     */
    public static void writeLines(File file, List<String> lines, Charset charset) {
        try {
            mkdirs(file.getParentFile());
            Files.write(file.toPath(), lines, charset);
        } catch (IOException e) {
            throw Exceptions.system(CommonErrorCode.SYSTEM_ERROR).cause(e).build();
        }
    }

    /**
     * 写入字节数组到文件
     *
     * @param file  文件
     * @param bytes 字节数组
     * @throws GoyaException 如果写入失败
     */
    public static void writeBytes(File file, byte[] bytes) {
        try {
            mkdirs(file.getParentFile());
            Files.write(file.toPath(), bytes);
        } catch (IOException e) {
            throw Exceptions.system(CommonErrorCode.SYSTEM_ERROR).cause(e).build();
        }
    }

    /**
     * 写入字节数组到文件
     *
     * @param filePath 文件路径
     * @param bytes    字节数组
     * @throws GoyaException 如果写入失败
     */
    public static void writeBytes(String filePath, byte[] bytes) {
        writeBytes(new File(filePath), bytes);
    }

    /**
     * 追加字符串到文件（UTF-8）
     *
     * @param file    文件
     * @param content 内容
     * @throws GoyaException 如果写入失败
     */
    public static void appendString(File file, String content) {
        appendString(file, content, StandardCharsets.UTF_8);
    }

    /**
     * 追加字符串到文件（指定编码）
     *
     * @param file    文件
     * @param content 内容
     * @param charset 字符编码
     * @throws GoyaException 如果写入失败
     */
    public static void appendString(File file, String content, Charset charset) {
        try {
            mkdirs(file.getParentFile());
            Files.writeString(file.toPath(), content, charset, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw Exceptions.system(CommonErrorCode.SYSTEM_ERROR).cause(e).build();
        }
    }

    // ==================== 文件复制 ====================

    /**
     * 复制文件
     *
     * @param source 源文件
     * @param target 目标文件
     * @throws GoyaException 如果复制失败
     */
    public static void copyFile(File source, File target) {
        if (!source.exists()) {
            throw Exceptions.system(CommonErrorCode.SYSTEM_ERROR)
                    .userMessage("source not exists").build();
        }
        if (!source.isDirectory()) {
            throw Exceptions.system(CommonErrorCode.SYSTEM_ERROR)
                    .userMessage("source is not directory").build();
        }
        try {
            mkdirs(target.getParentFile());
            Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw Exceptions.system(CommonErrorCode.SYSTEM_ERROR).cause(e).build();
        }
    }

    /**
     * 复制文件
     *
     * @param sourcePath 源文件路径
     * @param targetPath 目标文件路径
     * @throws GoyaException 如果复制失败
     */
    public static void copyFile(String sourcePath, String targetPath) {
        copyFile(new File(sourcePath), new File(targetPath));
    }

    /**
     * 复制目录
     *
     * @param source 源目录
     * @param target 目标目录
     * @throws GoyaException 如果复制失败
     */
    public static void copyDirectory(File source, File target) {
        if (!source.exists()) {
            throw Exceptions.system(CommonErrorCode.SYSTEM_ERROR)
                    .userMessage("source not exists").build();
        }
        if (!source.isDirectory()) {
            throw Exceptions.system(CommonErrorCode.SYSTEM_ERROR)
                    .userMessage("source is not directory").build();
        }
        try {
            mkdirs(target);
            Files.walkFileTree(source.toPath(), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    Path targetDir = target.toPath().resolve(source.toPath().relativize(dir));
                    Files.createDirectories(targetDir);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path targetFile = target.toPath().resolve(source.toPath().relativize(file));
                    Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw Exceptions.system(CommonErrorCode.SYSTEM_ERROR).cause(e).build();
        }
    }

    // ==================== 文件移动 ====================

    /**
     * 移动文件
     *
     * @param source 源文件
     * @param target 目标文件
     * @throws GoyaException 如果移动失败
     */
    public static void moveFile(File source, File target) {
        if (!source.exists()) {
            throw Exceptions.system(CommonErrorCode.SYSTEM_ERROR)
                    .userMessage("source not exists").build();
        }
        try {
            mkdirs(target.getParentFile());
            Files.move(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw Exceptions.system(CommonErrorCode.SYSTEM_ERROR).cause(e).build();
        }
    }

    /**
     * 移动文件
     *
     * @param sourcePath 源文件路径
     * @param targetPath 目标文件路径
     * @throws GoyaException 如果移动失败
     */
    public static void moveFile(String sourcePath, String targetPath) {
        moveFile(new File(sourcePath), new File(targetPath));
    }

    // ==================== 文件删除 ====================

    /**
     * 删除文件或目录
     *
     * @param file 文件或目录
     * @return 是否删除成功
     */
    public static boolean delete(File file) {
        if (file == null || !file.exists()) {
            return false;
        }
        if (file.isDirectory()) {
            return deleteDirectory(file);
        } else {
            return file.delete();
        }
    }

    /**
     * 删除文件或目录
     *
     * @param filePath 文件或目录路径
     * @return 是否删除成功
     */
    public static boolean delete(String filePath) {
        return delete(new File(filePath));
    }

    /**
     * 删除目录（递归删除）
     *
     * @param directory 目录
     * @return 是否删除成功
     */
    public static boolean deleteDirectory(File directory) {
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            return false;
        }
        try {
            Files.walkFileTree(directory.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
            return true;
        } catch (IOException e) {
            throw Exceptions.system(CommonErrorCode.SYSTEM_ERROR).cause(e).build();
        }
    }

    /**
     * 强制删除文件或目录（忽略错误）
     *
     * @param file 文件或目录
     * @return 是否删除成功
     */
    public static boolean deleteQuietly(File file) {
        try {
            return delete(file);
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== 目录操作 ====================

    /**
     * 创建目录（如果不存在）
     *
     * @param directory 目录
     * @return 是否创建成功
     */
    public static boolean mkdirs(File directory) {
        if (directory == null) {
            return false;
        }
        if (directory.exists()) {
            return directory.isDirectory();
        }
        return directory.mkdirs();
    }

    /**
     * 创建目录（如果不存在）
     *
     * @param dirPath 目录路径
     * @return 是否创建成功
     */
    public static boolean mkdirs(String dirPath) {
        return mkdirs(new File(dirPath));
    }

    /**
     * 获取目录下的所有文件
     *
     * @param directory 目录
     * @return 文件列表
     */
    public static List<File> listFiles(File directory) {
        List<File> files = new ArrayList<>();
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            return files;
        }
        File[] fileArray = directory.listFiles();
        if (fileArray != null) {
            for (File file : fileArray) {
                if (file.isFile()) {
                    files.add(file);
                }
            }
        }
        return files;
    }

    /**
     * 获取目录下的所有文件（递归）
     *
     * @param directory 目录
     * @return 文件列表
     */
    public static List<File> listFilesRecursively(File directory) {
        List<File> files = new ArrayList<>();
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            return files;
        }
        try (Stream<Path> paths = Files.walk(directory.toPath())) {
            paths.filter(Files::isRegularFile)
                    .forEach(path -> files.add(path.toFile()));
        } catch (IOException e) {
            throw Exceptions.system(CommonErrorCode.SYSTEM_ERROR).cause(e).build();
        }
        return files;
    }

    /**
     * 获取目录下的所有子目录
     *
     * @param directory 目录
     * @return 目录列表
     */
    public static List<File> listDirectories(File directory) {
        List<File> directories = new ArrayList<>();
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            return directories;
        }
        File[] fileArray = directory.listFiles();
        if (fileArray != null) {
            for (File file : fileArray) {
                if (file.isDirectory()) {
                    directories.add(file);
                }
            }
        }
        return directories;
    }

    // ==================== 文件信息 ====================

    /**
     * 检查文件是否存在
     *
     * @param file 文件
     * @return 是否存在
     */
    public static boolean exists(File file) {
        return file != null && file.exists();
    }

    /**
     * 检查文件是否存在
     *
     * @param filePath 文件路径
     * @return 是否存在
     */
    public static boolean exists(String filePath) {
        return exists(new File(filePath));
    }

    /**
     * 检查是否为文件
     *
     * @param file 文件
     * @return 是否为文件
     */
    public static boolean isFile(File file) {
        return file != null && file.isFile();
    }

    /**
     * 检查是否为目录
     *
     * @param file 文件
     * @return 是否为目录
     */
    public static boolean isDirectory(File file) {
        return file != null && file.isDirectory();
    }

    /**
     * 获取文件大小（字节）
     *
     * @param file 文件
     * @return 文件大小
     */
    public static long size(File file) {
        if (file == null || !file.exists()) {
            return 0;
        }
        if (file.isFile()) {
            return file.length();
        }
        // 如果是目录，计算总大小
        try (Stream<Path> paths = Files.walk(file.toPath())) {
            return paths.filter(Files::isRegularFile)
                    .mapToLong(path -> {
                        try {
                            return Files.size(path);
                        } catch (IOException e) {
                            return 0L;
                        }
                    })
                    .sum();
        } catch (IOException e) {
            return 0;
        }
    }

    /**
     * 获取文件大小（字节）
     *
     * @param filePath 文件路径
     * @return 文件大小
     */
    public static long size(String filePath) {
        return size(new File(filePath));
    }

    /**
     * 获取文件扩展名
     *
     * @param file 文件
     * @return 扩展名（不含点号）
     */
    public static String getExtension(File file) {
        if (file == null) {
            return "";
        }
        return getExtension(file.getName());
    }

    /**
     * 获取文件扩展名
     *
     * @param fileName 文件名
     * @return 扩展名（不含点号）
     */
    public static String getExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * 获取文件名（不含扩展名）
     *
     * @param file 文件
     * @return 文件名（不含扩展名）
     */
    public static String getNameWithoutExtension(File file) {
        if (file == null) {
            return "";
        }
        return getNameWithoutExtension(file.getName());
    }

    /**
     * 获取文件名（不含扩展名）
     *
     * @param fileName 文件名
     * @return 文件名（不含扩展名）
     */
    public static String getNameWithoutExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return fileName;
        }
        return fileName.substring(0, lastDotIndex);
    }

    // ==================== 路径处理 ====================

    /**
     * 获取文件名
     *
     * @param file 文件
     * @return 文件名
     */
    public static String getName(File file) {
        return file != null ? file.getName() : "";
    }

    /**
     * 获取文件路径
     *
     * @param file 文件
     * @return 文件路径
     */
    public static String getPath(File file) {
        return file != null ? file.getPath() : "";
    }

    /**
     * 获取绝对路径
     *
     * @param file 文件
     * @return 绝对路径
     */
    public static String getAbsolutePath(File file) {
        return file != null ? file.getAbsolutePath() : "";
    }

    /**
     * 获取父目录
     *
     * @param file 文件
     * @return 父目录
     */
    public static File getParent(File file) {
        return file != null ? file.getParentFile() : null;
    }

    /**
     * 获取父目录路径
     *
     * @param file 文件
     * @return 父目录路径
     */
    public static String getParentPath(File file) {
        File parent = getParent(file);
        return parent != null ? parent.getPath() : "";
    }

    /**
     * 规范化路径（去除多余的路径分隔符和 . 或 ..）
     *
     * @param path 路径
     * @return 规范化后的路径
     */
    public static String normalizePath(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }
        try {
            return Paths.get(path).normalize().toString();
        } catch (Exception e) {
            return path;
        }
    }

    /**
     * 连接路径
     *
     * @param first 第一个路径
     * @param more  更多路径
     * @return 连接后的路径
     */
    public static String joinPath(String first, String... more) {
        if (first == null) {
            first = "";
        }
        if (more == null || more.length == 0) {
            return first;
        }
        try {
            Path path = Paths.get(first);
            for (String segment : more) {
                if (segment != null) {
                    path = path.resolve(segment);
                }
            }
            return path.toString();
        } catch (Exception e) {
            // 降级处理
            StringBuilder sb = new StringBuilder(first);
            for (String segment : more) {
                if (segment != null) {
                    if (!first.endsWith(File.separator) && !segment.startsWith(File.separator)) {
                        sb.append(File.separator);
                    }
                    sb.append(segment);
                }
            }
            return sb.toString();
        }
    }

    // ==================== 临时文件 ====================

    /**
     * 创建临时文件
     *
     * @param prefix 前缀
     * @param suffix 后缀
     * @return 临时文件
     * @throws GoyaException 如果创建失败
     */
    public static File createTempFile(String prefix, String suffix) {
        try {
            return File.createTempFile(prefix, suffix);
        } catch (IOException e) {
            throw Exceptions.system(CommonErrorCode.SYSTEM_ERROR).cause(e).build();
        }
    }

    /**
     * 创建临时文件（在指定目录）
     *
     * @param prefix    前缀
     * @param suffix    后缀
     * @param directory 目录
     * @return 临时文件
     * @throws GoyaException 如果创建失败
     */
    public static File createTempFile(String prefix, String suffix, File directory) {
        try {
            return File.createTempFile(prefix, suffix, directory);
        } catch (IOException e) {
            throw Exceptions.system(CommonErrorCode.SYSTEM_ERROR).cause(e).build();
        }
    }

    /**
     * 创建临时目录
     *
     * @param prefix 前缀
     * @return 临时目录
     * @throws GoyaException 如果创建失败
     */
    public static File createTempDirectory(String prefix) {
        try {
            return Files.createTempDirectory(prefix).toFile();
        } catch (IOException e) {
            throw Exceptions.system(CommonErrorCode.SYSTEM_ERROR).cause(e).build();
        }
    }
}
