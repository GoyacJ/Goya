package com.ysmjjsy.goya.component.core.utils;

import com.ysmjjsy.goya.component.core.exception.CommonException;
import lombok.experimental.UtilityClass;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * <p>字体工具类</p>
 * <p>提供字体加载、创建、度量等功能</p>
 * <p>不依赖第三方库，使用 Java 标准库实现</p>
 *
 * @author goya
 * @since 2025/12/7 19:57
 */
@UtilityClass
public class GoyaFontUtils {

    // ==================== 字体样式常量 ====================

    /**
     * 普通字体样式
     */
    public static final int STYLE_PLAIN = Font.PLAIN;

    /**
     * 粗体字体样式
     */
    public static final int STYLE_BOLD = Font.BOLD;

    /**
     * 斜体字体样式
     */
    public static final int STYLE_ITALIC = Font.ITALIC;

    /**
     * 粗斜体字体样式
     */
    public static final int STYLE_BOLD_ITALIC = Font.BOLD | Font.ITALIC;

    // ==================== 字体加载 ====================

    /**
     * 从文件加载字体
     *
     * @param file 字体文件
     * @return Font 对象
     * @throws CommonException 如果加载失败
     */
    public static Font loadFont(File file) {
        try {
            return Font.createFont(Font.TRUETYPE_FONT, file);
        } catch (FontFormatException | IOException e) {
            throw new CommonException("Failed to load font from file: " + file.getPath(), e);
        }
    }

    /**
     * 从文件路径加载字体
     *
     * @param filePath 字体文件路径
     * @return Font 对象
     * @throws CommonException 如果加载失败
     */
    public static Font loadFont(String filePath) {
        return loadFont(new File(filePath));
    }

    /**
     * 从输入流加载字体
     *
     * @param inputStream 输入流
     * @return Font 对象
     * @throws CommonException 如果加载失败
     */
    public static Font loadFont(InputStream inputStream) {
        try {
            return Font.createFont(Font.TRUETYPE_FONT, inputStream);
        } catch (FontFormatException | IOException e) {
            throw new CommonException("Failed to load font from input stream", e);
        }
    }

    /**
     * 从类路径资源加载字体
     *
     * @param resourcePath 资源路径（如：/fonts/custom.ttf）
     * @return Font 对象
     * @throws CommonException 如果加载失败
     */
    public static Font loadFontFromResource(String resourcePath) {
        try (InputStream inputStream = GoyaFontUtils.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new CommonException("Font resource not found: " + resourcePath);
            }
            return loadFont(inputStream);
        } catch (IOException e) {
            throw new CommonException("Failed to load font from resource: " + resourcePath, e);
        }
    }

    // ==================== 字体创建 ====================

    /**
     * 创建字体
     *
     * @param fontName 字体名称
     * @param style    字体样式（PLAIN, BOLD, ITALIC, BOLD|ITALIC）
     * @param size     字体大小
     * @return Font 对象
     */
    public static Font createFont(String fontName, int style, int size) {
        return new Font(fontName, style, size);
    }

    /**
     * 创建普通字体
     *
     * @param fontName 字体名称
     * @param size     字体大小
     * @return Font 对象
     */
    public static Font createPlainFont(String fontName, int size) {
        return createFont(fontName, STYLE_PLAIN, size);
    }

    /**
     * 创建粗体字体
     *
     * @param fontName 字体名称
     * @param size     字体大小
     * @return Font 对象
     */
    public static Font createBoldFont(String fontName, int size) {
        return createFont(fontName, STYLE_BOLD, size);
    }

    /**
     * 创建斜体字体
     *
     * @param fontName 字体名称
     * @param size     字体大小
     * @return Font 对象
     */
    public static Font createItalicFont(String fontName, int size) {
        return createFont(fontName, STYLE_ITALIC, size);
    }

    /**
     * 创建粗斜体字体
     *
     * @param fontName 字体名称
     * @param size     字体大小
     * @return Font 对象
     */
    public static Font createBoldItalicFont(String fontName, int size) {
        return createFont(fontName, STYLE_BOLD_ITALIC, size);
    }

    /**
     * 从现有字体派生新字体（修改大小）
     *
     * @param font 原始字体
     * @param size 新字体大小
     * @return 派生字体
     */
    public static Font deriveFont(Font font, int size) {
        return font.deriveFont((float) size);
    }

    /**
     * 从现有字体派生新字体（修改样式和大小）
     *
     * @param font  原始字体
     * @param style 新字体样式
     * @param size  新字体大小
     * @return 派生字体
     */
    public static Font deriveFont(Font font, int style, int size) {
        return font.deriveFont(style, (float) size);
    }

    // ==================== 系统字体 ====================

    /**
     * 获取系统所有可用字体名称
     *
     * @return 字体名称数组
     */
    public static String[] getAvailableFontNames() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        return ge.getAvailableFontFamilyNames();
    }

    /**
     * 检查字体是否可用
     *
     * @param fontName 字体名称
     * @return 是否可用
     */
    public static boolean isFontAvailable(String fontName) {
        if (fontName == null || fontName.isEmpty()) {
            return false;
        }
        String[] availableFonts = getAvailableFontNames();
        for (String font : availableFonts) {
            if (font.equalsIgnoreCase(fontName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取默认字体
     *
     * @return 默认字体
     */
    public static Font getDefaultFont() {
        return new Font(Font.DIALOG, Font.PLAIN, 12);
    }

    /**
     * 获取默认字体（指定大小）
     *
     * @param size 字体大小
     * @return 默认字体
     */
    public static Font getDefaultFont(int size) {
        return new Font(Font.DIALOG, Font.PLAIN, size);
    }

    // ==================== 字体度量 ====================

    /**
     * 获取字体度量对象
     *
     * @param font     字体
     * @param graphics 图形上下文
     * @return FontMetrics 对象
     */
    public static FontMetrics getFontMetrics(Font font, Graphics graphics) {
        return graphics.getFontMetrics(font);
    }

    /**
     * 计算文本宽度
     *
     * @param text     文本内容
     * @param font     字体
     * @param graphics 图形上下文
     * @return 文本宽度（像素）
     */
    public static int getTextWidth(String text, Font font, Graphics graphics) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        FontMetrics fm = getFontMetrics(font, graphics);
        return fm.stringWidth(text);
    }

    /**
     * 计算文本高度
     *
     * @param font     字体
     * @param graphics 图形上下文
     * @return 文本高度（像素）
     */
    public static int getTextHeight(Font font, Graphics graphics) {
        FontMetrics fm = getFontMetrics(font, graphics);
        return fm.getHeight();
    }

    /**
     * 计算文本的上升高度（ascent）
     *
     * @param font     字体
     * @param graphics 图形上下文
     * @return 上升高度（像素）
     */
    public static int getTextAscent(Font font, Graphics graphics) {
        FontMetrics fm = getFontMetrics(font, graphics);
        return fm.getAscent();
    }

    /**
     * 计算文本的下降高度（descent）
     *
     * @param font     字体
     * @param graphics 图形上下文
     * @return 下降高度（像素）
     */
    public static int getTextDescent(Font font, Graphics graphics) {
        FontMetrics fm = getFontMetrics(font, graphics);
        return fm.getDescent();
    }

    /**
     * 计算文本的上升高度（leading）
     *
     * @param font     字体
     * @param graphics 图形上下文
     * @return 行间距（像素）
     */
    public static int getTextLeading(Font font, Graphics graphics) {
        FontMetrics fm = getFontMetrics(font, graphics);
        return fm.getLeading();
    }

    /**
     * 计算文本边界矩形
     *
     * @param text     文本内容
     * @param font     字体
     * @param graphics 图形上下文
     * @return 文本边界矩形
     */
    public static Rectangle getTextBounds(String text, Font font, Graphics graphics) {
        if (text == null || text.isEmpty()) {
            return new Rectangle(0, 0, 0, 0);
        }
        FontMetrics fm = getFontMetrics(font, graphics);
        return fm.getStringBounds(text, graphics).getBounds();
    }

    // ==================== 字体信息 ====================

    /**
     * 获取字体名称
     *
     * @param font 字体
     * @return 字体名称
     */
    public static String getFontName(Font font) {
        return font != null ? font.getName() : null;
    }

    /**
     * 获取字体族名称
     *
     * @param font 字体
     * @return 字体族名称
     */
    public static String getFontFamily(Font font) {
        return font != null ? font.getFamily() : null;
    }

    /**
     * 获取字体大小
     *
     * @param font 字体
     * @return 字体大小
     */
    public static int getFontSize(Font font) {
        return font != null ? font.getSize() : 0;
    }

    /**
     * 获取字体样式
     *
     * @param font 字体
     * @return 字体样式
     */
    public static int getFontStyle(Font font) {
        return font != null ? font.getStyle() : STYLE_PLAIN;
    }

    /**
     * 检查字体是否为粗体
     *
     * @param font 字体
     * @return 是否为粗体
     */
    public static boolean isBold(Font font) {
        return font != null && font.isBold();
    }

    /**
     * 检查字体是否为斜体
     *
     * @param font 字体
     * @return 是否为斜体
     */
    public static boolean isItalic(Font font) {
        return font != null && font.isItalic();
    }

    /**
     * 检查字体是否为普通样式
     *
     * @param font 字体
     * @return 是否为普通样式
     */
    public static boolean isPlain(Font font) {
        return font != null && font.isPlain();
    }
}
