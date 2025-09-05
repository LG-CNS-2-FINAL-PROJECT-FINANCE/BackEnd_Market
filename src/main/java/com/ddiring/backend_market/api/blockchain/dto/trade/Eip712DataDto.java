package com.ddiring.backend_market.api.blockchain.dto.trade;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class Eip712DataDto {
    private Map<String, Object> domain;
    private Map<String, Object> message;
    private Map<String, Object> types;
}