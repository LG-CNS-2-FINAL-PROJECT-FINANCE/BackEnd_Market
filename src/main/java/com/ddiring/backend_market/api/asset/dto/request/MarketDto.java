package com.ddiring.backend_market.api.asset.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketDto {

    private Integer investmentSeq;
    private String projectId;
    private String userSeq;
    private Integer price;
}
