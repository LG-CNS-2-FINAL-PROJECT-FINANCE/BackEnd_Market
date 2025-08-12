package com.ddiring.backend_market.trade.controller;

import com.ddiring.backend_market.common.dto.ApiResponseDto;
import com.ddiring.backend_market.trade.dto.*;
import com.ddiring.backend_market.trade.service.TradeService;
import com.ddiring.backend_market.trade.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/market/trade", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class TradeController {
    private final TradeService tradeService;

    @PostMapping("/sell")
    public ApiResponseDto<String> sellOrder(@RequestBody OrdersRequestDto ordersRequestDto) {
        tradeService.OrderReception(ordersRequestDto.getUserSeq(), ordersRequestDto.getProjectId(), ordersRequestDto.getPurchasePrice(), ordersRequestDto.getTokenQuantity(), 0);
        return ApiResponseDto.defaultOk();
    }

    @PostMapping("/purchase")
    public ApiResponseDto<String> purchaseOrder(@RequestBody OrdersRequestDto ordersRequestDto) {
        tradeService.OrderReception(ordersRequestDto.getUserSeq(), ordersRequestDto.getProjectId(), ordersRequestDto.getPurchasePrice(), ordersRequestDto.getTokenQuantity(), 1);
        return ApiResponseDto.defaultOk();
    }
    @GetMapping("/{projectId}/history")
    public ApiResponseDto<List<TradeHistoryResponseDto>> tradeHistory(@PathVariable String projectId) {
        List<TradeHistoryResponseDto> tradeHistory = tradeService.getTradeHistory(projectId);
        return ApiResponseDto.createOk(tradeHistory);
    }
    @GetMapping("/{projectId}/purchase")
    public ApiResponseDto<List<OrdersResponseDto>>  purchaseCorrection(@PathVariable String projectId) {
        List<OrdersResponseDto> historyResponseDtos = tradeService.getPurchaseOrders(projectId);
        return ApiResponseDto.createOk(historyResponseDtos);
    }
    @GetMapping("/{projectId}/sell")
    public ApiResponseDto<List<OrdersResponseDto>>  sellCorrection(@PathVariable String projectId) {
        List<OrdersResponseDto> historyResponseDtos = tradeService.getSellOrders(projectId);
        return ApiResponseDto.createOk(historyResponseDtos);
    }
    @PostMapping("/edit/purchase")
    public ApiResponseDto<String> editPurchase(@RequestBody OrdersCorrectionRequestDto ordersCorrectionRequestDto) {
        tradeService.updateOrder(ordersCorrectionRequestDto.getOrdersId(), ordersCorrectionRequestDto.getUserSeq(), ordersCorrectionRequestDto.getProjectId(), ordersCorrectionRequestDto.getPurchasePrice(), ordersCorrectionRequestDto.getTokenQuantity(), 1);
        return ApiResponseDto.defaultOk();
    }
    @PostMapping("/edit/sell")
    public ApiResponseDto<String> editSell(@RequestBody OrdersCorrectionRequestDto ordersCorrectionRequestDto) {
        tradeService.updateOrder(ordersCorrectionRequestDto.getOrdersId(), ordersCorrectionRequestDto.getUserSeq(), ordersCorrectionRequestDto.getProjectId(), ordersCorrectionRequestDto.getPurchasePrice(), ordersCorrectionRequestDto.getTokenQuantity(), 0);
        return ApiResponseDto.defaultOk();
    }
    @GetMapping("/{userId}/list")
    public ApiResponseDto<List<TradeSearchResponseDto>> tradeSearch(@PathVariable Integer userId) {
        List<TradeSearchResponseDto> tradeSearchResponseDto = tradeService.getUserInfo(userId);
        return ApiResponseDto.createOk(tradeSearchResponseDto);
    }

}
