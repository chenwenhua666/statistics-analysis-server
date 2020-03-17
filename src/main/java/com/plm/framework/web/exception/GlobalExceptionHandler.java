package com.plm.framework.web.exception;

import com.plm.framework.web.domain.ResultEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import com.plm.common.constant.HttpStatus;
import com.plm.common.exception.BaseException;
import com.plm.common.exception.CustomException;
import com.plm.common.utils.StringUtils;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import java.util.Set;

/**
 * 全局异常处理器
 *
 * @author cwh
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 基础异常
     */
    @ExceptionHandler(BaseException.class)
    public ResultEntity baseException(BaseException e) {
        return ResultEntity.error(e.getMessage());
    }

    /**
     * 业务异常
     */
    @ExceptionHandler(CustomException.class)
    public ResultEntity businessException(CustomException e) {
        if (StringUtils.isNull(e.getCode())) {
            return ResultEntity.error(e.getMessage());
        }
        return ResultEntity.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResultEntity handlerNoFoundException(Exception e) {
        log.error(e.getMessage(), e);
        return ResultEntity.error(HttpStatus.NOT_FOUND, "路径不存在，请检查路径是否正确");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResultEntity handleAuthorizationException(AccessDeniedException e) {
        log.error(e.getMessage());
        return ResultEntity.error(HttpStatus.FORBIDDEN, "没有权限，请联系管理员授权");
    }

    @ExceptionHandler(AccountExpiredException.class)
    public ResultEntity handleAccountExpiredException(AccountExpiredException e) {
        log.error(e.getMessage(), e);
        return ResultEntity.error(e.getMessage());
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResultEntity handleUsernameNotFoundException(UsernameNotFoundException e) {
        log.error(e.getMessage(), e);
        return ResultEntity.error(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResultEntity handleException(Exception e) {
        log.error(e.getMessage(), e);
        return ResultEntity.error(e.getMessage());
    }

    /**
     * 自定义验证异常
     */
    @ExceptionHandler(BindException.class)
    public ResultEntity validatedBindException(BindException e) {
        log.error(e.getMessage(), e);
        String message = e.getAllErrors().get(0).getDefaultMessage();
        return ResultEntity.error(message);
    }

    /**
     * 自定义验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object validExceptionHandler(MethodArgumentNotValidException e) {
        log.error(e.getMessage(), e);
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        return ResultEntity.error(message);
    }

    /**
     * 统一处理请求参数校验(普通传参)
     *
     * @param e ConstraintViolationException
     * @return
     */
    @ExceptionHandler(value = ConstraintViolationException.class)
    public ResultEntity handleConstraintViolationException(ConstraintViolationException e) {
        StringBuilder message = new StringBuilder();
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        for (ConstraintViolation<?> violation : violations) {
            Path path = violation.getPropertyPath();
            String[] pathArr = StringUtils.splitByWholeSeparatorPreserveAllTokens(path.toString(), ".");
            message.append(pathArr[1]).append(violation.getMessage()).append(",");
        }
        message = new StringBuilder(message.substring(0, message.length() - 1));
        return ResultEntity.error(HttpStatus.BAD_REQUEST, message.toString());
    }
}
