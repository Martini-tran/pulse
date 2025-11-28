package com.tran.pulse.web.exception;

import com.tran.pulse.common.constants.PulseHttpStatus;
import com.tran.pulse.common.domain.model.PulseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一处理应用中的各种异常，返回标准化的错误响应。
 * 提供详细的日志记录和用户友好的错误消息。
 * 
 *
 * @author tran
 * @date 2025/7/02
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理参数验证异常 (javax.validation.ValidationException)
     *
     * @param e       验证异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<PulseResult> handleValidationException(ValidationException e, HttpServletRequest request) {
        String requestInfo = getRequestInfo(request);
        log.warn("参数验证异常 - {} - 异常：{}", requestInfo, e.getMessage());

        PulseResult result = PulseResult.paramError(e.getMessage());
        return ResponseEntity.status(PulseHttpStatus.BAD_REQUEST).body(result);
    }

    /**
     * 处理约束验证异常 (javax.validation.ConstraintViolationException)
     *
     * @param e       约束验证异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<PulseResult> handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        String requestInfo = getRequestInfo(request);

        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        List<String> errors = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());

        log.warn("约束验证异常 - {} - 验证错误：{}", requestInfo, errors);

        String errorMessage = errors.isEmpty() ? "参数验证失败" : String.join("; ", errors);
        PulseResult result = PulseResult.paramError(errorMessage);

        return ResponseEntity.status(PulseHttpStatus.BAD_REQUEST).body(result);
    }

    /**
     * 处理方法参数验证异常 (@Valid 注解触发)
     *
     * @param e       方法参数验证异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<PulseResult> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String requestInfo = getRequestInfo(request);

        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        List<String> errors = fieldErrors.stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());

        log.warn("方法参数验证异常 - {} - 字段错误：{}", requestInfo, errors);

        String errorMessage = errors.isEmpty() ? "参数验证失败" : errors.get(0);
        PulseResult result = PulseResult.paramError(errorMessage, errors);

        return ResponseEntity.status(PulseHttpStatus.BAD_REQUEST).body(result);
    }

    /**
     * 处理绑定异常 (表单数据绑定错误)
     *
     * @param e       绑定异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<PulseResult> handleBindException(BindException e, HttpServletRequest request) {
        String requestInfo = getRequestInfo(request);

        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        List<String> errors = fieldErrors.stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        log.warn("数据绑定异常 - {} - 字段错误：{}", requestInfo, errors);

        String errorMessage = errors.isEmpty() ? "数据绑定失败" : errors.get(0);
        PulseResult result = PulseResult.paramError("数据绑定失败：" + errorMessage, errors);

        return ResponseEntity.status(PulseHttpStatus.BAD_REQUEST).body(result);
    }

    /**
     * 处理缺少请求参数异常
     *
     * @param e       缺少请求参数异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<PulseResult> handleMissingServletRequestParameterException(MissingServletRequestParameterException e, HttpServletRequest request) {
        String requestInfo = getRequestInfo(request);
        log.warn("缺少请求参数异常 - {} - 缺少参数：{}", requestInfo, e.getParameterName());

        PulseResult result = PulseResult.paramError("缺少必需参数：" + e.getParameterName());
        return ResponseEntity.status(PulseHttpStatus.BAD_REQUEST).body(result);
    }

    /**
     * 处理方法参数类型不匹配异常
     *
     * @param e       参数类型不匹配异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<PulseResult> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        String requestInfo = getRequestInfo(request);
        log.warn("方法参数类型不匹配异常 - {} - 参数：{}，期望类型：{}",
                requestInfo, e.getName(), e.getRequiredType().getSimpleName());

        String message = String.format("参数 %s 类型错误，期望类型：%s", e.getName(), e.getRequiredType().getSimpleName());
        PulseResult result = PulseResult.paramError(message);

        return ResponseEntity.status(PulseHttpStatus.BAD_REQUEST).body(result);
    }

    /**
     * 处理Spring Security认证异常
     *
     * @param e       认证异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<PulseResult> handleAuthenticationException(AuthenticationException e, HttpServletRequest request) {
        String requestInfo = getRequestInfo(request);
        log.warn("认证异常 - {} - 异常：{}", requestInfo, e.getMessage());

        String message;
        if (e instanceof BadCredentialsException) {
            message = "用户名或密码错误";
        } else {
            message = "认证失败：" + e.getMessage();
        }

        PulseResult result = PulseResult.authFail(message);
        return ResponseEntity.status(PulseHttpStatus.UNAUTHORIZED).body(result);
    }

    /**
     * 处理Spring Security访问拒绝异常
     *
     * @param e       访问拒绝异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<PulseResult> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        String requestInfo = getRequestInfo(request);
        log.warn("访问拒绝异常 - {} - 异常：{}", requestInfo, e.getMessage());

        PulseResult result = PulseResult.accessDenied("权限不足，无法访问该资源");
        return ResponseEntity.status(PulseHttpStatus.FORBIDDEN).body(result);
    }



    /**
     * 处理请求方法不支持异常
     *
     * @param e       请求方法不支持异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<PulseResult> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        String requestInfo = getRequestInfo(request);
        log.warn("请求方法不支持异常 - {} - 当前方法：{}，支持的方法：{}",
                requestInfo, e.getMethod(), String.join(", ", e.getSupportedMethods()));

        String message = String.format("请求方法 %s 不支持，支持的方法：%s",
                e.getMethod(), String.join(", ", e.getSupportedMethods()));
        PulseResult result = PulseResult.fail(PulseHttpStatus.METHOD_NOT_ALLOWED, message);

        return ResponseEntity.status(PulseHttpStatus.METHOD_NOT_ALLOWED).body(result);
    }

    /**
     * 处理404异常
     *
     * @param e       404异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<PulseResult> handleNoHandlerFoundException(NoHandlerFoundException e, HttpServletRequest request) {
        String requestInfo = getRequestInfo(request);
        log.warn("404异常 - {} - 请求路径：{}", requestInfo, e.getRequestURL());

        PulseResult result = PulseResult.notFound("请求的资源不存在：" + e.getRequestURL());
        return ResponseEntity.status(PulseHttpStatus.NOT_FOUND).body(result);
    }

    /**
     * 处理文件上传大小超限异常
     *
     * @param e       文件上传大小超限异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<PulseResult> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e, HttpServletRequest request) {
        String requestInfo = getRequestInfo(request);
        log.warn("文件上传大小超限异常 - {} - 最大允许大小：{}", requestInfo, e.getMaxUploadSize());

        String message = "上传文件过大，最大允许大小：" + formatFileSize(e.getMaxUploadSize());
        PulseResult result = PulseResult.paramError(message);

        return ResponseEntity.status(PulseHttpStatus.PAYLOAD_TOO_LARGE).body(result);
    }

    /**
     * 处理非法参数异常
     *
     * @param e       非法参数异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<PulseResult> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        String requestInfo = getRequestInfo(request);
        log.warn("非法参数异常 - {} - 异常：{}", requestInfo, e.getMessage());

        PulseResult result = PulseResult.paramError("参数错误：" + e.getMessage());
        return ResponseEntity.status(PulseHttpStatus.BAD_REQUEST).body(result);
    }

    /**
     * 处理非法状态异常
     *
     * @param e       非法状态异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<PulseResult> handleIllegalStateException(IllegalStateException e, HttpServletRequest request) {
        String requestInfo = getRequestInfo(request);
        log.warn("非法状态异常 - {} - 异常：{}", requestInfo, e.getMessage());

        PulseResult result = PulseResult.fail("操作失败：" + e.getMessage());
        return ResponseEntity.status(PulseHttpStatus.CONFLICT).body(result);
    }

    /**
     * 处理运行时异常
     *
     * @param e       运行时异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<PulseResult> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        String requestInfo = getRequestInfo(request);
        log.error("运行时异常 - {}", requestInfo, e);

        PulseResult result = PulseResult.fail("系统异常，请稍后重试");
        return ResponseEntity.status(PulseHttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }

    /**
     * 处理所有其他异常
     *
     * @param e       异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<PulseResult> handleException(Exception e, HttpServletRequest request) {
        String requestInfo = getRequestInfo(request);
        log.error("未处理异常 - {}", requestInfo, e);

        PulseResult result = PulseResult.fail("系统内部错误，请联系管理员");
        return ResponseEntity.status(PulseHttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }

    /**
     * 获取请求信息字符串
     *
     * @param request HTTP请求
     * @return 请求信息字符串
     */
    private String getRequestInfo(HttpServletRequest request) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String clientIp = getClientIpAddress(request);

        StringBuilder info = new StringBuilder();
        info.append(method).append(" ").append(uri);
        if (queryString != null) {
            info.append("?").append(queryString);
        }
        info.append(" (客户端: ").append(clientIp).append(")");

        return info.toString();
    }

    /**
     * 获取客户端真实IP地址
     *
     * @param request HTTP请求
     * @return 客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
                "X-Forwarded-For",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR"
        };

        for (String headerName : headerNames) {
            String ip = request.getHeader(headerName);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                int index = ip.indexOf(',');
                if (index != -1) {
                    return ip.substring(0, index).trim();
                } else {
                    return ip.trim();
                }
            }
        }

        return request.getRemoteAddr();
    }

    /**
     * 格式化文件大小
     *
     * @param size 文件大小（字节）
     * @return 格式化后的文件大小字符串
     */
    private String formatFileSize(long size) {
        if (size <= 0) return "0B";

        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));

        return String.format("%.1f %s",
                size / Math.pow(1024, digitGroups),
                units[digitGroups]);
    }
}