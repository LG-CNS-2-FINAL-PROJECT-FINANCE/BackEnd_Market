package com.ddiring.backend_market.market.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProfitRequest {

    private String projectId;
    private String userSeq;
    private Integer transSeq;
    private Integer transType;
    private Integer amount;
}
