package com.ddiring.backend_market.api.blockchain.dto.signature;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigInteger;

@Getter
@NoArgsConstructor
public class PermitSignatureMessage {
    private String owner;
    private String spender;
    private BigInteger value;
    private BigInteger nonce;
    private BigInteger deadline;
}