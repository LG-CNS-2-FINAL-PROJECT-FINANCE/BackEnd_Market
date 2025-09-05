package com.ddiring.backend_market.api.blockchain.dto.signature.type;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignatureType {
    private String name;
    private String type;
}
