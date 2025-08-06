package com.ddiring.backend_market.investment;

import com.ddiring.backend_market.investment.dto.request.BuyInvestmentRequest;
import com.ddiring.backend_market.investment.dto.request.CancelInvestmentRequest;
import com.ddiring.backend_market.investment.entity.Investment;
import com.ddiring.backend_market.investment.repository.InvestmentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("투자 기능 통합 테스트")
class IntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private InvestmentRepository investmentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        investmentRepository.deleteAll();
    }

    @Test
    @DisplayName("투자 상품 전체 조회 통합 테스트")
    void getAllProducts_Integration() throws Exception {
        // given
        Investment investment = createTestInvestment(1, 1, Investment.InvestmentStatus.COMPLETED);
        investmentRepository.save(investment);

        // when & then
        mockMvc.perform(get("/market/invest/list"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("개인 투자 내역 조회 통합 테스트")
    void getUserInvestments_Integration() throws Exception {
        // given
        Investment investment1 = createTestInvestment(1, 1, Investment.InvestmentStatus.COMPLETED);
        Investment investment2 = createTestInvestment(1, 2, Investment.InvestmentStatus.PENDING);
        investmentRepository.save(investment1);
        investmentRepository.save(investment2);

        // when & then
        mockMvc.perform(get("/market/invest/1/list"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].userSeq").value(1))
                .andExpect(jsonPath("$[1].userSeq").value(1));
    }

    @Test
    @DisplayName("상품별 투자자 조회 통합 테스트")
    void getProductInvestor_Integration() throws Exception {
        // given
        Investment investment1 = createTestInvestment(1, 1, Investment.InvestmentStatus.COMPLETED);
        Investment investment2 = createTestInvestment(2, 1, Investment.InvestmentStatus.COMPLETED);
        investmentRepository.save(investment1);
        investmentRepository.save(investment2);

        // when & then
        mockMvc.perform(get("/market/invest/1/userlist"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].projectId").value(1))
                .andExpect(jsonPath("$[1].projectId").value(1));
    }

    @Test
    @DisplayName("투자 주문 통합 테스트")
    void buyInvestment_Integration() throws Exception {
        // given
        BuyInvestmentRequest request = BuyInvestmentRequest.builder()
                .userSeq(1)
                .productId(1)
                .tokenQuantity(50)
                .build();

        // when & then
        mockMvc.perform(post("/market/invest/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tokenQuantity").value(50));

        // 데이터베이스에 저장되었는지 확인
        assertThat(investmentRepository.findByUserSeq(1)).hasSize(1);
    }

    @Test
    @DisplayName("투자 취소 통합 테스트")
    void cancelInvestment_Integration() throws Exception {
        // given
        Investment investment = createTestInvestment(1, 1, Investment.InvestmentStatus.PENDING);
        Investment savedInvestment = investmentRepository.save(investment);

        CancelInvestmentRequest request = CancelInvestmentRequest.builder()
                .userSeq(1)
                .productId(1)
                .investmentSeq(savedInvestment.getInvestmentSeq())
                .build();

        // when & then
        mockMvc.perform(post("/market/invest/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tokenQuantity").value(100));

        // 상태가 CANCELLED로 변경되었는지 확인
        Investment cancelledInvestment = investmentRepository.findById(savedInvestment.getInvestmentSeq()).orElse(null);
        assertThat(cancelledInvestment).isNotNull();
        assertThat(cancelledInvestment.getInvStatus()).isEqualTo(Investment.InvestmentStatus.CANCELLED);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 투자 내역 조회 테스트")
    void getUserInvestments_UserNotFound() throws Exception {
        // when & then
        mockMvc.perform(get("/market/invest/999/list"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("존재하지 않는 프로젝트 투자자 조회 테스트")
    void getProductInvestor_ProjectNotFound() throws Exception {
        // when & then
        mockMvc.perform(get("/market/invest/999/userlist"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("잘못된 투자 주문 요청 테스트")
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
    @DisplayName("잘못된 투자 취소 요청 테스트")
    void cancelInvestment_InvalidRequest() throws Exception {
        // given
        CancelInvestmentRequest invalidRequest = CancelInvestmentRequest.builder()
                .userSeq(1)
                .productId(1)
                .investmentSeq(999) // 존재하지 않는 투자
                .build();

        // when & then
        mockMvc.perform(post("/market/invest/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    private Investment createTestInvestment(Integer userSeq, Integer projectId, Investment.InvestmentStatus status) {
        return Investment.builder()
                .userSeq(userSeq)
                .projectId(projectId)
                .investedPrice(1000000)
                .tokenQuantity(100)
                .investedAt(LocalDate.now())
                .invStatus(status)
                .currentAmount(50000000)
                .totalInvestment(100000000)
                .totalInvestor(50)
                .achievementRate(75)
                .createdId(userSeq)
                .createdAt(LocalDate.now())
                .updatedId(userSeq)
                .updatedAt(LocalDate.now())
                .build();
    }
} 