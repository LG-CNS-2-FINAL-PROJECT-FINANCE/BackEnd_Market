package com.ddiring.backend_market.advice;

import com.ddiring.backend_market.common.dto.ApiResponseDto;
import com.ddiring.backend_market.common.exception.BadParameter;
import com.ddiring.backend_market.common.exception.ClientError;
import com.ddiring.backend_market.common.exception.NotFound;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@Order(value = 1)
@RestControllerAdvice
public class ApiCommonAdvice {

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({Exception.class})
    public ApiResponseDto<String> handleException(Exception e) {
        log.warn(e.getMessage());

        return ApiResponseDto.createError("ServerError", "서버 에러입니다.");
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({NoResourceFoundException.class})
    public ApiResponseDto<String> handleResourceFoundException(NoResourceFoundException e) {
        log.warn(e.getMessage());
        return ApiResponseDto.createError("NoResource", "리소스를 찾을 수 없습니다.");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({ClientError.class})
    public ApiResponseDto<String> handleClientError(ClientError e) {
        log.warn(e.getMessage());
        return ApiResponseDto.createError(e.getErrorCode(), e.getErrorMessage());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({NotFound.class})
    public ApiResponseDto<String> handleNotFound(NotFound e) {
        log.warn(e.getMessage());
        return ApiResponseDto.createError(e.getErrorCode(), e.getErrorMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({BadParameter.class})
    public ApiResponseDto<String> handleBadParameter(BadParameter e) {
        return ApiResponseDto.createError(e.getErrorCode(), e.getErrorMessage());
    }

}
