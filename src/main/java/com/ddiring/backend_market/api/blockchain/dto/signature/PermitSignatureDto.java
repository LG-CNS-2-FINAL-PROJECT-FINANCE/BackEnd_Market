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

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private String projectId;
        private String userAddress;
        private Long tokenAmount;
    }

    @Getter
    @NoArgsConstructor
    public static class Response {
        private PermitSignatureDomain domain;
        private PermitSignatureMessage message;
        private Map<String, List<SignatureType>> types;
    }
}