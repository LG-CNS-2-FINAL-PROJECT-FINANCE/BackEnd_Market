package com.ddiring.backend_market.api.blockchain.dto.signature.message;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.math.BigInteger;

@Getter
@Builder
public class PermitSignatureMessage {
    @NonNull
    private String owner;

    @NonNull
    private String spender;

    @NonNull
    private BigInteger value;

    @NonNull
    private BigInteger nonce;

    @NonNull
    private BigInteger deadline;
}
