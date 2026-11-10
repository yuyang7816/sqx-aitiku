package com.sqx.app.base.search.constants;

/**
 * 搜题记录业务常量（与 uniapp SEARCH_RECORD_SOURCES 一致）
 */
public final class SearchRecordConstant {

    private SearchRecordConstant() {
    }

    /** 本地/云端条数上限 */
    public static final int MAX_RECORDS = 20;

    public static final String SOURCE_PHOTO = "拍照搜题";
    public static final String SOURCE_MANUAL = "手动搜题";
    public static final String SOURCE_AI = "AI答疑";
}
