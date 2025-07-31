package com.ddiring.backend_market.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {

    private String title;
    private Integer goalAmount;
    private LocalDate endDate;
    private String status;
}
