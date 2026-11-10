package com.sqx.common.web.exception;

import com.sqx.common.core.domain.Result;
import com.sqx.common.core.enums.LoginClientErrorCodes;
import com.sqx.common.core.enums.SystemErrorCode;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.core.exception.PayException;
import com.sqx.common.core.exception.SqxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.yaml.snakeyaml.constructor.DuplicateKeyException;

/**
 * @author chengming
 * @since 2024/7/11
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(value = BizException.class)
    public ResponseEntity<Result<String>> handleBizException(BizException e) {
        if (e.getErrorCode() != null) {
            Result<String> result = Result.failed(e.getErrorCode());
            if (LoginClientErrorCodes.isBadRequest(e.getErrorCode().getCode())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }
            return ResponseEntity.ok(result);
        }

        return ResponseEntity.ok(Result.failed(e.getMessage()));
    }

    @ExceptionHandler(value = PayException.class)
    public Result<String> handlePayException(PayException e) {
        if (e.getErrorCode() != null) {
            return Result.failed(e.getErrorCode());
        }

        return Result.failed(e.getMessage());
    }

    @ExceptionHandler(value = SqxException.class)
    public ResponseEntity<Result<String>> handleSqxException(SqxException e) {
        Result<String> result = Result.failed(e.getCode(), e.getMessage());
        if (e.getCode() == HttpStatus.UNAUTHORIZED.value()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }
        return ResponseEntity.ok(result);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public Result<String> handlerNoFoundException(Exception e) {
        logger.error(e.getMessage(), e);
        return Result.failed(SystemErrorCode.NO_HANDLER_FOUND);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public Result<String> handleDuplicateKeyException(DuplicateKeyException e){
        logger.error(e.getMessage(), e);
        return Result.failed("数据库中已存在该记录");
    }

    /**
     * 参数校验异常处理
     * @param e
     * @return
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<Result<String>> handleValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        String message = SystemErrorCode.VALIDATE_FAILED.getMessage();
        if (bindingResult.hasErrors()) {
            FieldError fieldError = bindingResult.getFieldError();
            if (fieldError != null && fieldError.getDefaultMessage() != null) {
                message = fieldError.getDefaultMessage();
            }
        }

        Result<String> result = Result.validateFailed(message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e){
        logger.error(e.getMessage(), e);
        return Result.failed();
    }
}
