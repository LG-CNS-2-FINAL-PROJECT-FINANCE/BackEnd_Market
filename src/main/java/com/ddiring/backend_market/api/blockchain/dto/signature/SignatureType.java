package com.ddiring.backend_market.api.blockchain.dto.signature;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor // JSON 역직렬화를 위해 기본 생성자 필요
public class SignatureType {
    private String name;
    private String type;
}