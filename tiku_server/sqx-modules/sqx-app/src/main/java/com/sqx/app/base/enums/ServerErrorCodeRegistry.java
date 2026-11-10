package com.sqx.app.base.enums;

import com.sqx.app.base.ai.enums.AiError;
import com.sqx.app.base.ocr.enums.OcrError;
import com.sqx.app.vip.pay.enums.PayError;
import com.sqx.common.core.enums.IErrorCode;
import com.sqx.common.core.enums.LoginClientErrorCodes;
import com.sqx.common.core.enums.SystemErrorCode;
import com.sqx.common.file.enums.FileError;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 服务端已注册业务错误码表（供文档核对、测试与前端对接查询）
 */
public final class ServerErrorCodeRegistry {

    public enum ToastType {
        TOAST,
        INLINE_CODE,
        ACCOUNT_BANNED,
        REDIRECT_LOGIN,
        DIALOG_VIP,
        SILENT
    }

    public static final class Entry {
        private final int code;
        private final String message;
        private final String enumName;
        private final String module;
        private final ToastType toastType;
        private final boolean clientFacing;
        private final boolean httpBadRequest;

        Entry(int code, String message, String enumName, String module,
              ToastType toastType, boolean clientFacing) {
            this.code = code;
            this.message = message;
            this.enumName = enumName;
            this.module = module;
            this.toastType = toastType;
            this.clientFacing = clientFacing;
            this.httpBadRequest = LoginClientErrorCodes.isBadRequest(code);
        }

        public int getCode() { return code; }
        public String getMessage() { return message; }
        public String getEnumName() { return enumName; }
        public String getModule() { return module; }
        public ToastType getToastType() { return toastType; }
        public boolean isClientFacing() { return clientFacing; }
        public boolean isHttpBadRequest() { return httpBadRequest; }
    }

    private static final List<Entry> ALL_ENTRIES;
    private static final Map<Integer, Entry> BY_CODE;

    static {
        List<Entry> entries = new ArrayList<>();
        registerSystem(entries);
        registerEnum(entries, AppError.class, "app", ToastType.TOAST, true, specialAppToast());
        registerEnum(entries, FileError.class, "file", ToastType.TOAST, true, Collections.<Integer, ToastType>emptyMap());
        registerEnum(entries, OcrError.class, "ocr", ToastType.TOAST, true, specialOcrToast());
        registerEnum(entries, AiError.class, "ai", ToastType.TOAST, true, specialAiToast());
        registerEnum(entries, PayError.class, "pay", ToastType.TOAST, true, Collections.<Integer, ToastType>emptyMap());
        ALL_ENTRIES = Collections.unmodifiableList(entries);
        Map<Integer, Entry> byCode = new HashMap<>();
        for (Entry entry : entries) { byCode.put(entry.getCode(), entry); }
        BY_CODE = Collections.unmodifiableMap(byCode);
    }

    private ServerErrorCodeRegistry() {}

    public static Entry find(Integer code) {
        if (code == null) { return null; }
        return BY_CODE.get(code);
    }

    public static Collection<Entry> allEntries() { return ALL_ENTRIES; }

    public static Collection<Entry> allClientFacing() {
        List<Entry> result = new ArrayList<>();
        for (Entry entry : ALL_ENTRIES) {
            if (entry.isClientFacing()) { result.add(entry); }
        }
        return Collections.unmodifiableList(result);
    }

    public static Set<Integer> clientFacingCodes() {
        Set<Integer> codes = new HashSet<>();
        for (Entry entry : ALL_ENTRIES) {
            if (entry.isClientFacing()) { codes.add(entry.getCode()); }
        }
        return Collections.unmodifiableSet(codes);
    }

    private static void registerSystem(List<Entry> entries) {
        Map<Integer, ToastType> toastOverrides = new HashMap<>();
        toastOverrides.put(SystemErrorCode.VALIDATE_FAILED.getCode(), ToastType.TOAST);
        toastOverrides.put(SystemErrorCode.UNAUTHORIZED.getCode(), ToastType.REDIRECT_LOGIN);
        toastOverrides.put(401, ToastType.REDIRECT_LOGIN);
        for (SystemErrorCode errorCode : SystemErrorCode.values()) {
            ToastType toastType = toastOverrides.getOrDefault(errorCode.getCode(), ToastType.TOAST);
            boolean clientFacing = errorCode.getCode() != SystemErrorCode.SYSTEM_ERROR.getCode();
            entries.add(new Entry(errorCode.getCode(), errorCode.getMessage(), errorCode.name(), "system", toastType, clientFacing));
        }
        entries.add(new Entry(401, "暂未登录或token已经过期", "APP_UNAUTHORIZED", "app-auth", ToastType.REDIRECT_LOGIN, true));
    }

    private static Map<Integer, ToastType> specialAppToast() {
        Map<Integer, ToastType> map = new HashMap<>();
        map.put(AppError.USER_BANNED.getCode(), ToastType.ACCOUNT_BANNED);
        map.put(AppError.VERIFY_CODE_ERROR.getCode(), ToastType.INLINE_CODE);
        map.put(AppError.VERIFY_CODE_EXPIRED.getCode(), ToastType.INLINE_CODE);
        return map;
    }

    private static Map<Integer, ToastType> specialOcrToast() {
        Map<Integer, ToastType> map = new HashMap<>();
        map.put(OcrError.QUOTA_EXCEEDED.getCode(), ToastType.DIALOG_VIP);
        return map;
    }

    private static Map<Integer, ToastType> specialAiToast() {
        Map<Integer, ToastType> map = new HashMap<>();
        map.put(AiError.QUOTA_EXCEEDED.getCode(), ToastType.DIALOG_VIP);
        map.put(AiError.SENSITIVE_INPUT.getCode(), ToastType.SILENT);
        map.put(AiError.SENSITIVE_REPLY.getCode(), ToastType.SILENT);
        return map;
    }

    private static void registerEnum(List<Entry> entries, Class<? extends Enum<?>> enumClass,
                                     String module, ToastType defaultToast,
                                     boolean clientFacing, Map<Integer, ToastType> toastOverrides) {
        for (Enum<?> constant : enumClass.getEnumConstants()) {
            if (!(constant instanceof IErrorCode)) { continue; }
            IErrorCode errorCode = (IErrorCode) constant;
            ToastType toastType = toastOverrides.getOrDefault(errorCode.getCode(), defaultToast);
            entries.add(new Entry(errorCode.getCode(), errorCode.getMessage(), constant.name(), module, toastType, clientFacing));
        }
    }
}
