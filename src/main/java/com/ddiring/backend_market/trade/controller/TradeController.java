package com.ddiring.backend_market.trade.controller;

import com.ddiring.backend_market.api.asset.dto.request.AssetEscrowRequest;
import com.ddiring.backend_market.common.dto.ApiResponseDto;
import com.ddiring.backend_market.common.util.GatewayRequestHeaderUtils;
import com.ddiring.backend_market.trade.dto.*;
import com.ddiring.backend_market.trade.entity.Orders;
import com.ddiring.backend_market.trade.service.TradeService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(value = "/api/market/trade", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class TradeController {
    private final TradeService tradeService;

    @PostMapping("/sell")
    public ApiResponseDto<Long> sellOrder(@RequestBody OrdersRequestDto ordersRequestDto) {
//        String userSeq = GatewayRequestHeaderUtils.getUserSeq();
//        String role = GatewayRequestHeaderUtils.getRole();
        Long orderId = tradeService.sellReception("userSeq", "USER", ordersRequestDto);
        return ApiResponseDto.createOk(orderId);
    }

    @PostMapping("/purchase")
    public ApiResponseDto<String> purchaseOrder(@RequestBody OrdersRequestDto ordersRequestDto) {
//        String userSeq = GatewayRequestHeaderUtils.getUserSeq();
//        String role = GatewayRequestHeaderUtils.getRole();
        tradeService.buyReception("userSeq", "USER", ordersRequestDto);
        return ApiResponseDto.defaultOk();
    }

    @GetMapping("/{projectId}/history")
    public List<OrderHistoryResponseDto> tradeHistory(@PathVariable String projectId) {
        List<OrderHistoryResponseDto> tradeHistory = tradeService.getTradeHistory(projectId);
        return tradeHistory;
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

    @PostMapping("/order/delete")
    public ApiResponseDto<String> editPurchase(@RequestBody OrderDeleteDto orderDeleteDto) {
//        String userSeq = GatewayRequestHeaderUtils.getUserSeq();
//        String role = GatewayRequestHeaderUtils.getRole();
        tradeService.deleteOrder("userSeq", "USER", orderDeleteDto);
        return ApiResponseDto.createOk("삭제되었습니다.");
    }

    @GetMapping("/{projectId}/user/list")
    public ApiResponseDto<List<OrderUserHistory>> tradeSearch(@PathVariable String projectId) {
//        String userSeq = GatewayRequestHeaderUtils.getUserSeq();
        List<OrderUserHistory> orderUserHistory = tradeService.getUserOrder("userSeq", projectId);
        return ApiResponseDto.createOk(orderUserHistory);
    }

    @GetMapping("/{projectId}/user/history")
    public ApiResponseDto<List<TradeHistoryResponseDto>> tradeHistroy(@PathVariable String projectId) {
        String userSeq = GatewayRequestHeaderUtils.getUserSeq();
        List<TradeHistoryResponseDto> tradeHistroy = tradeService.getTradeHistory(userSeq, projectId);
        return ApiResponseDto.createOk(tradeHistroy);
    }

    @GetMapping("/history")
    public ApiResponseDto<List<TradeHistoryResponseDto>> tradeAllHistroy() {
        String userSeq = GatewayRequestHeaderUtils.getUserSeq();
        List<TradeHistoryResponseDto> tradeAllHistroy = tradeService.getTradeAllHistory(userSeq);
        return ApiResponseDto.createOk(tradeAllHistroy);
    }

    @GetMapping("/admin/history")
    public ApiResponseDto<List<TradeHistoryResponseDto>> tradeAdminHistroy() {
        List<TradeHistoryResponseDto> tradeAdminHistroy = tradeService.getAdminHistory();
        return ApiResponseDto.createOk(tradeAdminHistroy);
    }
    @PostMapping("/verify")
    public ApiResponseDto<?> checkTradeChainlink(@RequestBody VerifyTradeDto.Request requestDto) {
        VerifyTradeDto.Response response = tradeService.verifyTrade(requestDto);

        return ApiResponseDto.createOk(response);
    }
  
    @GetMapping("/{tradeId}")
    public ApiResponseDto<TradeInfoResponseDto> getTradeInfo(@PathVariable Long tradeId) {
        TradeInfoResponseDto tradeInfo = tradeService.getTradeInfoById(tradeId);
        return ApiResponseDto.createOk(tradeInfo);
    }
}