package com.formssi.generator.util;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * @author lizhangyu
 * @version 1.0
 * @date 2025/1/8 11:16
 */
public class DecodeURIUtil {

    public static String decodeURIComponent(String encodedURI) {
        // 检查输入是否为空
        if (encodedURI == null || encodedURI.isEmpty()) {
            // 返回空字符串
            return "";
        }

        try {
            // 使用UTF-8字符集进行解码
            return URLDecoder.decode(encodedURI, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // 捕获异常并打印错误信息
            System.out.println("解码错误: " + e.getMessage());
            return null; // 返回null表示解码失败
        }
    }

}
