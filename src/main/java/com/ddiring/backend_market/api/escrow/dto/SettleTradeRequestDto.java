package com.ddiring.backend_market.api.escrow.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SettleTradeRequestDto {
    private String tradeId;
    private String buyerUserSeq;
    private String sellerUserSeq;
    private int amount;
    private int quantity;
}