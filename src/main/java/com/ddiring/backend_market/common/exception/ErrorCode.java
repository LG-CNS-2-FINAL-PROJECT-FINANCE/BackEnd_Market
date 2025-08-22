package com.ddiring.backend_market.common.exception;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    //예시 오류입니다.
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다.", "500"),
    USER_DUPLICATED_ID(CONFLICT, "이미 가입된 아이디입니다.", "004"),
    NEWS_PARSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "뉴스 정보를 불러올 수 없습니다.", "1004"),
    UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED,"인증되지 않은 사용자입니다.","401"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED,"토큰이 만료되었습니다.","401"),

    USER_NOT_FOUND(NOT_FOUND, "사용자를 찾을 수 없습니다.", "40401"),
    FAVORITE_NOT_FOUND(NOT_FOUND, "좋아요를 찾을 수 없습니다.", "40402"),

    DUPLICATE_INTEREST(CONFLICT, "이미 존재하는 관심사입니다.", "409"),

    NO_INTEREST_SELECTED(BAD_REQUEST,"관심사를 하나도 선택하지 않았습니다.","40001"),
    INTEREST_NOT_FOUND(NOT_FOUND,"관심사를 찾을 수 없습니다.","40403"),
    INTEREST_NOT_FOUND_FOR_USER(NOT_FOUND,"사용자에게 등록된 관심사가 없습니다.","40404"),
    INVALID_PASSWORD(BAD_REQUEST,"올바르지 못한 비밀번호입니다." , "400")
    ;


    private final HttpStatus status;
    private final String message;
    private final String errorCode;


    public static CustomException userDuplicatedId() {
        return new CustomException(USER_DUPLICATED_ID);
    }

    public static CustomException internalServerError() {
        return new CustomException(INTERNAL_SERVER_ERROR);
    }

    public static CustomException newsParsingError() {
        return new CustomException(NEWS_PARSING_ERROR);
    }

    public static CustomException userNotFound() {
        return new CustomException(USER_NOT_FOUND);
    }
    public static CustomException favoriteNotFound() {
        return new CustomException(FAVORITE_NOT_FOUND);
    }
    public static CustomException duplicateInterest() {
        return new CustomException(DUPLICATE_INTEREST);
    }
    public static CustomException noInterestSelected(){
        return new CustomException(NO_INTEREST_SELECTED);
    }
    public static CustomException interestNotFound(){
        return new CustomException(INTEREST_NOT_FOUND);
    }
    public static CustomException interestNotFoundForUser(){
        return new CustomException(INTEREST_NOT_FOUND_FOR_USER);
    }

}