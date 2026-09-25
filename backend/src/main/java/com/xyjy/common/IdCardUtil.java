package com.xyjy.common;

/**
 * 中国大陆18位身份证号码校验
 */
public final class IdCardUtil {

    private static final int[] WEIGHTS = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
    private static final char[] CHECK_CODES = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};

    private IdCardUtil() {
    }

    public static String normalize(String idCard) {
        return idCard == null ? "" : idCard.trim().toUpperCase();
    }

    public static boolean isValid(String idCard) {
        String card = normalize(idCard);
        if (!card.matches("^[1-9]\\d{16}[\\dX]$")) {
            return false;
        }
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            sum += (card.charAt(i) - '0') * WEIGHTS[i];
        }
        return CHECK_CODES[sum % 11] == card.charAt(17);
    }
}
