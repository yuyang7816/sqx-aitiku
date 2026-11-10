package com.sqx.app.vip.rules.support;

import lombok.Data;

import java.time.LocalDate;

/**
 * 开通/续费 VIP 当日配额重置快照（文档 23.3-4）
 */
@Data
public class ActivationQuotaReset {

    /** AI 今日已用次数（开通当日重置为 0） */
    private Integer aiUsedToday;

    /** OCR 今日已用次数（开通当日重置为 0） */
    private Integer ocrUsedToday;

    /** AI 配额日期（开通当日设为今天） */
    private LocalDate aiQuotaDate;

    /** OCR 配额日期（开通当日设为今天） */
    private LocalDate ocrQuotaDate;
}
