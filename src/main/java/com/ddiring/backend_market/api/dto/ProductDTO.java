package com.ddiring.backend_market.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.kafka.common.protocol.types.Field;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {

    private String projectId;
    private String title;
    private Integer goalAmount;
    private Integer deadline;
    private Integer minInvestment;
    private String status;
}
