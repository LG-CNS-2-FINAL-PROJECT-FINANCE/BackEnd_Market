package com.ddiring.backend_market.trade.controller;

import com.ddiring.backend_market.common.dto.ApiResponseDto;
import com.ddiring.backend_market.trade.dto.*;
import com.ddiring.backend_market.trade.entity.Orders;
import com.ddiring.backend_market.trade.service.TradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/api/market/trade", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class TradeController {
    private final TradeService tradeService;

    @PostMapping("/sell")
    public ApiResponseDto<String> sellOrder(@RequestHeader("userSeq") String userSeq, @RequestHeader("role") String role, @RequestBody OrdersRequestDto ordersRequestDto) {
        tradeService.OrderReception(userSeq, ordersRequestDto.getProjectId(), role, ordersRequestDto.getPurchasePrice(), ordersRequestDto.getTokenQuantity(), 0);
        return ApiResponseDto.defaultOk();
    }

    @PostMapping("/purchase")
    public ApiResponseDto<String> purchaseOrder(@RequestHeader("userSeq") String userSeq, @RequestHeader("role") String role, @RequestBody OrdersRequestDto ordersRequestDto) {
        tradeService.OrderReception(userSeq, ordersRequestDto.getProjectId(), role, ordersRequestDto.getPurchasePrice(), ordersRequestDto.getTokenQuantity(), 1);
        return ApiResponseDto.defaultOk();
    }

    @GetMapping("/{projectId}/history")
    public ApiResponseDto<List<OrderHistoryResponseDto>> tradeHistory(@PathVariable String projectId) {
        List<OrderHistoryResponseDto> tradeHistory = tradeService.getTradeHistory(projectId);
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
    public ApiResponseDto<Orders> editPurchase(@RequestHeader("userSeq") String userSeq, @RequestBody OrdersCorrectionRequestDto ordersCorrectionRequestDto) {
        Orders orders = tradeService.updateOrder(ordersCorrectionRequestDto.getOrdersId(), userSeq, ordersCorrectionRequestDto.getProjectId(), ordersCorrectionRequestDto.getPurchasePrice(), ordersCorrectionRequestDto.getTokenQuantity(), 1);
        return ApiResponseDto.createOk(orders);
    }

    @PostMapping("/edit/sell")
    public ApiResponseDto<Orders> editSell(@RequestHeader("userSeq") String userSeq, @RequestBody OrdersCorrectionRequestDto ordersCorrectionRequestDto) {
        Orders orders = tradeService.updateOrder(ordersCorrectionRequestDto.getOrdersId(), userSeq, ordersCorrectionRequestDto.getProjectId(), ordersCorrectionRequestDto.getPurchasePrice(), ordersCorrectionRequestDto.getTokenQuantity(), 0);
        return ApiResponseDto.createOk(orders);
    }

    @GetMapping("/{userSeq}/list")
    public ApiResponseDto<List<TradeSearchResponseDto>> tradeSearch(@RequestHeader("userSeq") String userSeq) {
        List<TradeSearchResponseDto> tradeSearchResponseDto = tradeService.getUserInfo(userSeq);
        return ApiResponseDto.createOk(tradeSearchResponseDto);
    }

    @GetMapping("/history/{tradeType}")
    public ApiResponseDto<List<TradeHistoryResponseDto>> tradeHistroy(@RequestHeader("userSeq") String userSeq, @PathVariable Integer tradeType) {
        List<TradeHistoryResponseDto> tradeHistroy = tradeService.getTradeHistory(userSeq, tradeType);
        return ApiResponseDto.createOk(tradeHistroy);
    }

    @GetMapping("/history")
    public ApiResponseDto<List<TradeHistoryResponseDto>> tradeAllHistroy(@RequestHeader("userSeq") String userSeq) {
        List<TradeHistoryResponseDto> tradeAllHistroy = tradeService.getTradeAllHistory(userSeq);
        return ApiResponseDto.createOk(tradeAllHistroy);
    }

    @GetMapping("/admin/history")
    public ApiResponseDto<List<TradeHistoryResponseDto>> tradeAdminHistroy() {
        List<TradeHistoryResponseDto> tradeAdminHistroy = tradeService.getAdminHistory();
        return ApiResponseDto.createOk(tradeAdminHistroy);
    }
}