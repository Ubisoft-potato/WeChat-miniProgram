package com.cust.utils;

import org.springframework.util.DigestUtils;

import java.io.File;
import java.util.UUID;

public class Token {
    //盐，用于混交md5
    private static final String slat = "&%5123***&&%%$$#@";

    public static String getMD5(String str) {
        String base = str + "/" + slat;
        return DigestUtils.md5DigestAsHex(base.getBytes());
    }

    public static String CreateToken() {
        String uid = UUID.randomUUID().toString();
        return getMD5(uid);
    }

    public static String createNewUserId() {
        String uuid = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
        return uuid + System.currentTimeMillis();
    }

    /**
     * 迭代删除文件夹
     *
     * @param path 文件夹路径
     */
    public static void deleteDir(File path) {
        if (null != path) {
            if (!path.exists())
                return;
            if (path.isFile()) {
                boolean result = path.delete();
                int tryCount = 0;
                while (!result && tryCount++ < 10) {
                    System.gc(); // 回收资源
                    result = path.delete();
                }
            }
            File[] files = path.listFiles();
            if (null != files) {
                for (File file : files) {
                    deleteDir(file);
                }
            }
            path.delete();
        }
    }


}