package com.formssi.generator.util;

import org.apache.commons.lang.StringUtils;

/**
 * @author thc
 */
public class StringUtil {

    /**
     * Trim leading whitespace from the given {@code String}.
     * @param str the {@code String} to check
     * @return the trimmed {@code String}
     * @see Character#isWhitespace
     */
    public static String trimLeadingWhitespace(String str) {
        if (StringUtils.isBlank(str)) {
            return str;
        }

        int beginIdx = 0;
        while (beginIdx < str.length() && Character.isWhitespace(str.charAt(beginIdx))) {
            beginIdx++;
        }
        return str.substring(beginIdx);
    }

    public static String trimLeadingCharacter(String str, char leadingCharacter) {
        if (StringUtils.isBlank(str)) {
            return str;
        }

        int beginIdx = 0;
        while (beginIdx < str.length() && leadingCharacter == str.charAt(beginIdx)) {
            beginIdx++;
        }
        return str.substring(beginIdx);
    }

    public static String trimTrailingCharacter(String str, char trailingCharacter) {
        if (StringUtils.isBlank(str)) {
            return str;
        }

        int endIdx = str.length() - 1;
        while (endIdx >= 0 && trailingCharacter == str.charAt(endIdx)) {
            endIdx--;
        }
        return str.substring(0, endIdx + 1);
    }
}
