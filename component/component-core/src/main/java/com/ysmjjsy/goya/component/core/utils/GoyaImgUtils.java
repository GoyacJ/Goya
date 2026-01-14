package com.ysmjjsy.goya.component.core.utils;

import com.ysmjjsy.goya.component.core.exception.CommonException;
import lombok.experimental.UtilityClass;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * <p>图片工具类</p>
 * <p>提供图片读取、写入、格式转换、缩放、裁剪、Base64 编码/解码等功能</p>
 * <p>不依赖第三方库，使用 Java 标准库实现</p>
 *
 * @author goya
 * @since 2025/12/7 19:38
 */
@UtilityClass
public class GoyaImgUtils {

    // ==================== 常量定义 ====================

    /**
     * PNG 图片格式
     */
    public static final String IMAGE_TYPE_PNG = "png";

    /**
     * JPEG 图片格式
     */
    public static final String IMAGE_TYPE_JPEG = "jpeg";

    /**
     * JPG 图片格式
     */
    public static final String IMAGE_TYPE_JPG = "jpg";

    /**
     * GIF 图片格式
     */
    public static final String IMAGE_TYPE_GIF = "gif";

    /**
     * BMP 图片格式
     */
    public static final String IMAGE_TYPE_BMP = "bmp";

    /**
     * Base64 PNG 图片前缀
     */
    public static final String BASE64_PNG_PREFIX = "data:image/png;base64,";

    /**
     * Base64 JPEG 图片前缀
     */
    public static final String BASE64_JPEG_PREFIX = "data:image/jpeg;base64,";

    /**
     * Base64 GIF 图片前缀
     */
    public static final String BASE64_GIF_PREFIX = "data:image/gif;base64,";

    // ==================== 图片读取 ====================

    /**
     * 从文件读取图片
     *
     * @param file 图片文件
     * @return BufferedImage 对象
     * @throws CommonException 如果读取失败
     */
    public static BufferedImage read(File file) {
        try {
            return ImageIO.read(file);
        } catch (IOException e) {
            throw new CommonException("Failed to read image from file: " + file.getPath(), e);
        }
    }

    /**
     * 从文件路径读取图片
     *
     * @param filePath 图片文件路径
     * @return BufferedImage 对象
     * @throws CommonException 如果读取失败
     */
    public static BufferedImage read(String filePath) {
        return read(new File(filePath));
    }

    /**
     * 从输入流读取图片
     *
     * @param inputStream 输入流
     * @return BufferedImage 对象
     * @throws CommonException 如果读取失败
     */
    public static BufferedImage read(InputStream inputStream) {
        try {
            return ImageIO.read(inputStream);
        } catch (IOException e) {
            throw new CommonException("Failed to read image from input stream", e);
        }
    }

    /**
     * 从字节数组读取图片
     *
     * @param bytes 图片字节数组
     * @return BufferedImage 对象
     * @throws CommonException 如果读取失败
     */
    public static BufferedImage read(byte[] bytes) {
        return read(new ByteArrayInputStream(bytes));
    }

    // ==================== 图片写入 ====================

    /**
     * 将图片写入文件
     *
     * @param image      图片对象
     * @param format     图片格式（png, jpeg, jpg, gif, bmp）
     * @param outputFile 输出文件
     * @throws CommonException 如果写入失败
     */
    public static void write(BufferedImage image, String format, File outputFile) {
        try {
            // 确保父目录存在
            File parentDir = outputFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            ImageIO.write(image, format, outputFile);
        } catch (IOException e) {
            throw new CommonException("Failed to write image to file: " + outputFile.getPath(), e);
        }
    }

    /**
     * 将图片写入文件路径
     *
     * @param image      图片对象
     * @param format     图片格式
     * @param outputPath 输出文件路径
     * @throws CommonException 如果写入失败
     */
    public static void write(BufferedImage image, String format, String outputPath) {
        write(image, format, new File(outputPath));
    }

    /**
     * 将图片写入字节数组
     *
     * @param image  图片对象
     * @param format 图片格式
     * @return 图片字节数组
     * @throws CommonException 如果写入失败
     */
    public static byte[] toBytes(BufferedImage image, String format) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ImageIO.write(image, format, outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new CommonException("Failed to convert image to bytes", e);
        }
    }

    // ==================== Base64 编码/解码 ====================

    /**
     * 将图片转换为 Base64 字符串
     *
     * @param image  图片对象
     * @param format 图片格式
     * @return Base64 字符串
     */
    public static String toBase64(BufferedImage image, String format) {
        byte[] bytes = toBytes(image, format);
        return GoyaBase64Utils.encode(bytes);
    }

    /**
     * 将图片转换为 Base64 字符串（PNG 格式）
     *
     * @param image 图片对象
     * @return Base64 字符串
     */
    public static String toBase64(BufferedImage image) {
        return toBase64(image, IMAGE_TYPE_PNG);
    }

    /**
     * 将图片转换为带前缀的 Base64 字符串（用于前端显示）
     *
     * @param image  图片对象
     * @param format 图片格式
     * @return 带前缀的 Base64 字符串
     */
    public static String toBase64WithPrefix(BufferedImage image, String format) {
        String base64 = toBase64(image, format);
        String prefix = getBase64Prefix(format);
        return prefix + base64;
    }

    /**
     * 将图片转换为带前缀的 Base64 字符串（PNG 格式）
     *
     * @param image 图片对象
     * @return 带前缀的 Base64 字符串
     */
    public static String toBase64WithPrefix(BufferedImage image) {
        return toBase64WithPrefix(image, IMAGE_TYPE_PNG);
    }

    /**
     * 从 Base64 字符串解码为图片
     *
     * @param base64 Base64 字符串（可以带前缀）
     * @return BufferedImage 对象
     * @throws CommonException 如果解码失败
     */
    public static BufferedImage fromBase64(String base64) {
        // 移除可能的前缀
        String base64Data = removeBase64Prefix(base64);
        byte[] bytes = GoyaBase64Utils.decode(base64Data);
        return read(bytes);
    }

    /**
     * 从 Base64 字符串解码为图片（兼容 Hutool 的 toImage 方法）
     *
     * @param base64 Base64 字符串（可以带前缀）
     * @return BufferedImage 对象
     */
    public static BufferedImage toImage(String base64) {
        return fromBase64(base64);
    }

    /**
     * 获取 Base64 前缀
     *
     * @param format 图片格式
     * @return Base64 前缀
     */
    private static String getBase64Prefix(String format) {
        return switch (format.toLowerCase()) {
            case IMAGE_TYPE_PNG -> BASE64_PNG_PREFIX;
            case IMAGE_TYPE_JPEG, IMAGE_TYPE_JPG -> BASE64_JPEG_PREFIX;
            case IMAGE_TYPE_GIF -> BASE64_GIF_PREFIX;
            default -> BASE64_PNG_PREFIX;
        };
    }

    /**
     * 移除 Base64 前缀
     *
     * @param base64 Base64 字符串
     * @return 纯 Base64 数据
     */
    private static String removeBase64Prefix(String base64) {
        if (base64 == null) {
            return null;
        }
        if (base64.startsWith(BASE64_PNG_PREFIX)) {
            return base64.substring(BASE64_PNG_PREFIX.length());
        }
        if (base64.startsWith(BASE64_JPEG_PREFIX)) {
            return base64.substring(BASE64_JPEG_PREFIX.length());
        }
        if (base64.startsWith(BASE64_GIF_PREFIX)) {
            return base64.substring(BASE64_GIF_PREFIX.length());
        }
        return base64;
    }

    // ==================== 图片缩放 ====================

    /**
     * 缩放图片
     *
     * @param image        原图片
     * @param targetWidth  目标宽度
     * @param targetHeight 目标高度
     * @return 缩放后的图片
     */
    public static BufferedImage scale(BufferedImage image, int targetWidth, int targetHeight) {
        return scale(image, targetWidth, targetHeight, Image.SCALE_SMOOTH);
    }

    /**
     * 缩放图片（指定缩放算法）
     *
     * @param image        原图片
     * @param targetWidth  目标宽度
     * @param targetHeight 目标高度
     * @param scaleType    缩放算法（Image.SCALE_SMOOTH, Image.SCALE_FAST 等）
     * @return 缩放后的图片
     */
    public static BufferedImage scale(BufferedImage image, int targetWidth, int targetHeight, int scaleType) {
        Image scaledImage = image.getScaledInstance(targetWidth, targetHeight, scaleType);
        BufferedImage bufferedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bufferedImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(scaledImage, 0, 0, null);
        g.dispose();
        return bufferedImage;
    }

    /**
     * 按比例缩放图片
     *
     * @param image 原图片
     * @param scale 缩放比例（1.0 为原始大小，0.5 为缩小一半，2.0 为放大一倍）
     * @return 缩放后的图片
     */
    public static BufferedImage scale(BufferedImage image, double scale) {
        int targetWidth = (int) (image.getWidth() * scale);
        int targetHeight = (int) (image.getHeight() * scale);
        return scale(image, targetWidth, targetHeight);
    }

    /**
     * 按宽度等比例缩放图片
     *
     * @param image       原图片
     * @param targetWidth 目标宽度
     * @return 缩放后的图片
     */
    public static BufferedImage scaleByWidth(BufferedImage image, int targetWidth) {
        double scale = (double) targetWidth / image.getWidth();
        return scale(image, scale);
    }

    /**
     * 按高度等比例缩放图片
     *
     * @param image        原图片
     * @param targetHeight 目标高度
     * @return 缩放后的图片
     */
    public static BufferedImage scaleByHeight(BufferedImage image, int targetHeight) {
        double scale = (double) targetHeight / image.getHeight();
        return scale(image, scale);
    }

    // ==================== 图片裁剪 ====================

    /**
     * 裁剪图片
     *
     * @param image  原图片
     * @param x      起始 X 坐标
     * @param y      起始 Y 坐标
     * @param width  裁剪宽度
     * @param height 裁剪高度
     * @return 裁剪后的图片
     */
    public static BufferedImage crop(BufferedImage image, int x, int y, int width, int height) {
        // 边界检查
        x = Math.max(0, Math.min(x, image.getWidth()));
        y = Math.max(0, Math.min(y, image.getHeight()));
        width = Math.min(width, image.getWidth() - x);
        height = Math.min(height, image.getHeight() - y);

        return image.getSubimage(x, y, width, height);
    }

    /**
     * 居中裁剪图片
     *
     * @param image  原图片
     * @param width  裁剪宽度
     * @param height 裁剪高度
     * @return 裁剪后的图片
     */
    public static BufferedImage cropCenter(BufferedImage image, int width, int height) {
        int x = (image.getWidth() - width) / 2;
        int y = (image.getHeight() - height) / 2;
        return crop(image, x, y, width, height);
    }

    // ==================== 图片信息 ====================

    /**
     * 获取图片宽度
     *
     * @param image 图片对象
     * @return 宽度
     */
    public static int getWidth(BufferedImage image) {
        return image.getWidth();
    }

    /**
     * 获取图片高度
     *
     * @param image 图片对象
     * @return 高度
     */
    public static int getHeight(BufferedImage image) {
        return image.getHeight();
    }

    /**
     * 获取图片尺寸
     *
     * @param image 图片对象
     * @return 尺寸对象（宽度和高度）
     */
    public static Dimension getSize(BufferedImage image) {
        return new Dimension(image.getWidth(), image.getHeight());
    }

    /**
     * 判断是否为图片文件
     *
     * @param file 文件
     * @return 是否为图片
     */
    public static boolean isImage(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return false;
        }
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")
                || fileName.endsWith(".gif") || fileName.endsWith(".bmp");
    }

    /**
     * 判断是否为图片文件
     *
     * @param filePath 文件路径
     * @return 是否为图片
     */
    public static boolean isImage(String filePath) {
        return isImage(new File(filePath));
    }

    /**
     * 验证图片是否有效
     *
     * @param image 图片对象
     * @return 是否有效
     */
    public static boolean isValid(BufferedImage image) {
        return image != null && image.getWidth() > 0 && image.getHeight() > 0;
    }

    // ==================== 图片格式转换 ====================

    /**
     * 转换图片格式
     *
     * @param image        原图片
     * @param targetFormat 目标格式
     * @return 转换后的图片字节数组
     */
    public static byte[] convertFormat(BufferedImage image, String targetFormat) {
        return toBytes(image, targetFormat);
    }

    /**
     * 转换图片格式并保存到文件
     *
     * @param image        原图片
     * @param targetFormat 目标格式
     * @param outputFile   输出文件
     */
    public static void convertFormat(BufferedImage image, String targetFormat, File outputFile) {
        write(image, targetFormat, outputFile);
    }

    /**
     * 转换图片格式并保存到文件路径
     *
     * @param image        原图片
     * @param targetFormat 目标格式
     * @param outputPath   输出文件路径
     */
    public static void convertFormat(BufferedImage image, String targetFormat, String outputPath) {
        write(image, targetFormat, outputPath);
    }

    // ==================== 图片创建 ====================

    /**
     * 创建指定大小的空白图片
     *
     * @param width  宽度
     * @param height 高度
     * @return 空白图片
     */
    public static BufferedImage create(int width, int height) {
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    /**
     * 创建指定大小和类型的空白图片
     *
     * @param width  宽度
     * @param height 高度
     * @param type   图片类型（BufferedImage.TYPE_INT_RGB 等）
     * @return 空白图片
     */
    public static BufferedImage create(int width, int height, int type) {
        return new BufferedImage(width, height, type);
    }

    /**
     * 创建带背景色的图片
     *
     * @param width  宽度
     * @param height 高度
     * @param color  背景色
     * @return 图片
     */
    public static BufferedImage create(int width, int height, Color color) {
        BufferedImage image = create(width, height);
        Graphics2D g = image.createGraphics();
        g.setColor(color);
        g.fillRect(0, 0, width, height);
        g.dispose();
        return image;
    }
}
