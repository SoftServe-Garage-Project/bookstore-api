package com.softserve.bookstoreapi.logger;

public final class LoggerUtils {

    private LoggerUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Obfuscates sensitive data by replacing 80% of the string with asterisks.
     * Only the first 20% of characters remain visible.
     *
     * @param data the string to obfuscate
     * @return obfuscated string with 80% replaced by asterisks
     */
    public static String obfuscate(String data) {
        if (data == null || data.isEmpty()) {
            return data;
        }

        int visibleLength = (int) Math.ceil(data.length() * 0.2);
        int obfuscatedLength = data.length() - visibleLength;

        return data.substring(0, visibleLength) + "*".repeat(obfuscatedLength);
    }
}
