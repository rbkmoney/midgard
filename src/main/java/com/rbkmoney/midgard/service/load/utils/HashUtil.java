package com.rbkmoney.midgard.service.load.utils;

import org.springframework.util.DigestUtils;

public class HashUtil {

    public static boolean checkHashMod(String str, int div, int mod) {
        return (getIntHash(str) % div) == mod;
    }

    public static int getIntHash(String str) {
        String hexStr = DigestUtils.md5DigestAsHex(str.getBytes());
        return Integer.parseInt(hexStr.substring(0, 7), 16);
    }
}
