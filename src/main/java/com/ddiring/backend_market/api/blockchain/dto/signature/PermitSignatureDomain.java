package com.ddiring.backend_market.api.blockchain.dto.signature;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigInteger;

@Getter
@NoArgsConstructor
public class PermitSignatureDomain {
    private String name;
    private String version;
    private BigInteger chainId;
    private String verifyingContract;
}