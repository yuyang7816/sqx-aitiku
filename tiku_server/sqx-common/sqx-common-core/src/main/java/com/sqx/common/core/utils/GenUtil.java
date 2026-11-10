package com.sqx.common.core.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * @author chengming
 * @since 2024/9/19
 */
public class GenUtil {
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

    /**
     * 生成订单号
     *
     * @return
     */
    public static String genOrderNo() {
        Date date = new Date();
        String newString = String.format("%0" + 4 + "d", (int) ((Math.random() * 9 + 1) * 1000));
        String format = sdf.format(date);
        return format + newString;
    }

    /**
     * 生成消息编号
     * @return
     */
    public static String genMsgNo() {
        return "msg_" + System.currentTimeMillis() + String.format("%08d", (int) (Math.random() * 100_000_000));
    }

    public static String genInviteCode(String phoneNo) {
// 去除非数字字符（包括 +、空格等）
        String digitsOnly = phoneNo.replaceAll("[^0-9]", "");

        if (digitsOnly.length() < 5) { // 至少需要一定长度的数字才能生成邀请码
            throw new IllegalArgumentException("手机号无效，必须包含至少5位数字");
        }

        // 对手机号数字部分进行哈希运算
        int hashCode = digitsOnly.hashCode();

        // 将哈希值转换为正数
        long positiveHash = hashCode & 0xFFFFFFFFL;

        // 定义字母表 A-Z
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        // 生成6位大写字母的邀请码
        StringBuilder inviteCode = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            // 取模运算以确保索引在字母表范围内
            int index = (int) (positiveHash % alphabet.length());
            inviteCode.append(alphabet.charAt(index));
            // 更新哈希值，避免重复字符
            positiveHash /= alphabet.length();
        }

        return inviteCode.toString();
    }

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        String generalOrder = genOrderNo();
        System.out.println("orderNo = " + generalOrder);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime; // 毫秒
        System.out.println("1 - 执行时间: " + duration + " 毫秒");
    }
}
