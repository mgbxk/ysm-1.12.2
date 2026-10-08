package com.elfmcys.yesstevemodel.util;

import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.StringUtils;

/**
 * {@link ResourceLocation} 的辅助方法。验证移植自 1.16.5。
 */
public final class ResourceUtil {
    public static boolean isValidResourceLocation(String resourceName) {
        String[] decompose = ResourceLocation.splitObjectName(resourceName);
        return isValidNamespace(StringUtils.isEmpty(decompose[0]) ? "minecraft" : decompose[0]) && isValidPath(decompose[1]);
    }

    private static boolean isValidPath(String path) {
        for (int i = 0; i < path.length(); ++i) {
            if (!validPathChar(path.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean validPathChar(char charValue) {
        return validNamespaceChar(charValue) || charValue == '/';
    }

    private static boolean isValidNamespace(String namespace) {
        for (int i = 0; i < namespace.length(); ++i) {
            if (!validNamespaceChar(namespace.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean validNamespaceChar(char charValue) {
        return charValue == '_' || charValue == '-' || charValue >= 'a' && charValue <= 'z' || charValue >= '0' && charValue <= '9' || charValue == '.';
    }
}
