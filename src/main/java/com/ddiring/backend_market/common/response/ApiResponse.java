package com.ddiring.backend_market.common.response;

public record ApiResponse(ApiStatus status,
                          String message,
                          String errorCode,
                          Object data       )
{
    public static ApiResponse error(String message, String errorCode){
        return new ApiResponse(ApiStatus.ERROR,message,errorCode,null);
    }
    public static ApiResponse success(Object data) {
        return new ApiResponse(ApiStatus.SUCCESS, "요청에 성공했습니다.", null, data);
    }
    public static ApiResponse success(String message, Object data) {
        return new ApiResponse(ApiStatus.SUCCESS, message, null, data);
    }
}
