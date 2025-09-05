package com.ddiring.backend_market.api.blockchain.dto.signature;

import com.ddiring.backend_market.api.blockchain.dto.signature.domain.PermitSignatureDomain;
import com.ddiring.backend_market.api.blockchain.dto.signature.message.PermitSignatureMessage;
import com.ddiring.backend_market.api.blockchain.dto.signature.type.SignatureType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

public class PermitSignatureDto {

    // 요청 시 사용할 DTO
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private String projectId;
        private String userAddress;
        private Long tokenAmount;
    }

    // 응답으로 받을 DTO
    @Getter
    @NoArgsConstructor
    public static class Response {
        private PermitSignatureDomain domain;
        private PermitSignatureMessage message;
        private Map<String, List<SignatureType>> types;
    }
}