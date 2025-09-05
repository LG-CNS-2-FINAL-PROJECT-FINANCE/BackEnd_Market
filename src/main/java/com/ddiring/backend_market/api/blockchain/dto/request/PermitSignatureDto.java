package com.ddiring.backend_market.api.blockchain.dto.request;

import com.ddiring.backend_market.api.blockchain.dto.signature.domain.PermitSignatureDomain;
import com.ddiring.backend_market.api.blockchain.dto.signature.message.PermitSignatureMessage;
import com.ddiring.backend_market.api.blockchain.dto.signature.type.SignatureType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

// 참고: Request DTO는 market 서비스 내부에서만 사용하므로 이 파일에 둘 필요는 없지만,
//       통일성을 위해 함께 관리하는 것도 좋은 방법입니다.
public class PermitSignatureDto {

    @Getter
    @NoArgsConstructor
    public static class Response {
        private PermitSignatureDomain domain;
        private PermitSignatureMessage message;
        private Map<String, List<SignatureType>> types;
    }
}