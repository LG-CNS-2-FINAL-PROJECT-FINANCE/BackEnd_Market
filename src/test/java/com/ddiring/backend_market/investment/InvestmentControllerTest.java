package com.ddiring.backend_market.investment;

import com.ddiring.backend_market.api.dto.ProductDTO;
import com.ddiring.backend_market.investment.controller.InvestmentController;
import com.ddiring.backend_market.investment.dto.request.BuyInvestmentRequest;
import com.ddiring.backend_market.investment.dto.request.CancelInvestmentRequest;
import com.ddiring.backend_market.investment.dto.response.*;
import com.ddiring.backend_market.investment.service.InvestmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InvestmentController.class)
@DisplayName("Investment Controller 테스트")
class InvestmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InvestmentService investmentService;

    @Autowired
    private ObjectMapper objectMapper;

    private AllProductListResponse testProductResponse;
    private UserInvestmentListResponse testUserInvestmentResponse;
    private ProductInvestorListResponse testProductInvestorResponse;
    private InvestmentResponse testInvestmentResponse;
    private BuyInvestmentRequest buyRequest;
    private CancelInvestmentRequest cancelRequest;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 생성
        testProductResponse = AllProductListResponse.builder()
                .projectId(1)
                .title("테스트 프로젝트")
                .currentAmount(50000000)
                .achievementRate(75)
                .startDate(LocalDate.now().minusDays(30))
                .endDate(LocalDate.now().plusDays(30))
                .build();

        testUserInvestmentResponse = UserInvestmentListResponse.builder()
                .userSeq(1)
                .projectId(1)
                .title("테스트 프로젝트")
                .investedPrice(1000000)
                .tokenQuantity(100)
                .build();

        testProductInvestorResponse = ProductInvestorListResponse.builder()
                .projectId(1)
                .totalInvestment(100000000)
                .totalInvestors(50)
                .build();

        testInvestmentResponse = InvestmentResponse.builder()
                .tokenQuantity(100)
                .build();

        buyRequest = BuyInvestmentRequest.builder()
                .userSeq(1)
                .productId(1)
                .tokenQuantity(50)
                .build();

        cancelRequest = CancelInvestmentRequest.builder()
                .userSeq(1)
                .productId(1)
                .investmentSeq(1)
                .build();
    }

    @Test
    @DisplayName("투자 상품 전체 조회 API 테스트")
    void getListInvestment() throws Exception {
        // given
        List<AllProductListResponse> productList = Arrays.asList(testProductResponse);
        when(investmentService.getAllProducts()).thenReturn(productList);

        // when & then
        mockMvc.perform(get("/market/invest/list"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].projectId").value(1))
                .andExpect(jsonPath("$[0].title").value("테스트 프로젝트"))
                .andExpect(jsonPath("$[0].currentAmount").value(50000000))
                .andExpect(jsonPath("$[0].achievementRate").value(75));
    }

    @Test
    @DisplayName("개인 투자 내역 조회 API 테스트")
    void getListByUserSeq() throws Exception {
        // given
        List<UserInvestmentListResponse> userInvestments = Arrays.asList(testUserInvestmentResponse);
        when(investmentService.getUserInvestments(1)).thenReturn(userInvestments);

        // when & then
        mockMvc.perform(get("/market/invest/1/list"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].userSeq").value(1))
                .andExpect(jsonPath("$[0].projectId").value(1))
                .andExpect(jsonPath("$[0].title").value("테스트 프로젝트"))
                .andExpect(jsonPath("$[0].investedPrice").value(1000000))
                .andExpect(jsonPath("$[0].tokenQuantity").value(100));
    }

    @Test
    @DisplayName("상품별 투자자 조회 API 테스트")
    void getListInvestorByProductId() throws Exception {
        // given
        List<ProductInvestorListResponse> productInvestors = Arrays.asList(testProductInvestorResponse);
        when(investmentService.getProductInvestor(1)).thenReturn(productInvestors);

        // when & then
        mockMvc.perform(get("/market/invest/1/userlist"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].projectId").value(1))
                .andExpect(jsonPath("$[0].totalInvestment").value(100000000))
                .andExpect(jsonPath("$[0].totalInvestors").value(50));
    }

    @Test
    @DisplayName("투자 주문 API 테스트")
    void buyInvestment() throws Exception {
        // given
        ProductDTO productDTO = ProductDTO.builder()
                .title("테스트 프로젝트")
                .goalAmount(100000000)
                .startDate(LocalDate.now().minusDays(30))
                .endDate(LocalDate.now().plusDays(30))
                .status("승인 완료")
                .minInvestment(10000)
                .build();

        // when & then
        mockMvc.perform(post("/market/invest/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buyRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tokenQuantity").value(50));
    }

    @Test
    @DisplayName("투자 취소 API 테스트")
    void cancelInvestment() throws Exception {
        // given
        when(investmentService.cancelInvestment(any(CancelInvestmentRequest.class)))
                .thenReturn(testInvestmentResponse);

        // when & then
        mockMvc.perform(post("/market/invest/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tokenQuantity").value(100));
    }

    @Test
    @DisplayName("투자 주문 API - 잘못된 요청 데이터 테스트")
    void buyInvestment_InvalidRequest() throws Exception {
        // given
        BuyInvestmentRequest invalidRequest = BuyInvestmentRequest.builder()
                .userSeq(null)
                .productId(1)
                .tokenQuantity(50)
                .build();

        // when & then
        mockMvc.perform(post("/market/invest/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("투자 취소 API - 잘못된 요청 데이터 테스트")
    void cancelInvestment_InvalidRequest() throws Exception {
        // given
        CancelInvestmentRequest invalidRequest = CancelInvestmentRequest.builder()
                .userSeq(null)
                .productId(1)
                .investmentSeq(1)
                .build();

        // when & then
        mockMvc.perform(post("/market/invest/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("존재하지 않는 엔드포인트 테스트")
    void notFoundEndpoint() throws Exception {
        // when & then
        mockMvc.perform(get("/market/invest/nonexistent"))
                .andExpect(status().isNotFound());
    }
} 