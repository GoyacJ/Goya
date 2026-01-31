package com.ysmjjsy.goya.component.mybatisplus.exception;

import com.ysmjjsy.goya.component.framework.servlet.web.GlobalExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Strings;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * <p>Mybatis异常处理器</p>
 *
 * @author goya
 * @since 2026/2/1 00:27
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class MybatisExceptionHandler {

    private final GlobalExceptionHandler globalExceptionHandler;

    /**
     * 主键或UNIQUE索引，数据重复异常
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<?> handleDuplicateKeyException(DuplicateKeyException e, HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        log.error("请求地址'{}',数据库中已存在记录'{}'", requestUri, e.getMessage());
        return globalExceptionHandler.handleThrowable(e, request);
    }

    /**
     * Mybatis系统异常 通用处理
     */
    @ExceptionHandler(MyBatisSystemException.class)
    public ResponseEntity<?> handleCannotFindDataSourceException(MyBatisSystemException e, HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String message = e.getMessage();
        if (Strings.CS.contains(message, "CannotFindDataSourceException")) {
            log.error("请求地址'{}', 未找到数据源", requestUri);
            return globalExceptionHandler.handleThrowable(e, request);
        }
        log.error("请求地址'{}', Mybatis系统异常", requestUri, e);
        return globalExceptionHandler.handleThrowable(e, request);
    }
}
