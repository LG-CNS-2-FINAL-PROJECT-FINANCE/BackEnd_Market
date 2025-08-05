package com.ddiring.backend_market.investment;

import com.ddiring.backend_market.api.client.AssetClient;
import com.ddiring.backend_market.api.client.ProductClient;
import com.ddiring.backend_market.api.dto.AssetDTO;
import com.ddiring.backend_market.api.dto.ProductDTO;
import com.ddiring.backend_market.common.exception.BadParameter;
import com.ddiring.backend_market.investment.dto.request.BuyInvestmentRequest;
import com.ddiring.backend_market.investment.dto.request.CancelInvestmentRequest;
import com.ddiring.backend_market.investment.dto.response.*;
import com.ddiring.backend_market.investment.entity.Investment;
import com.ddiring.backend_market.investment.repository.InvestmentRepository;
import com.ddiring.backend_market.investment.service.InvestmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Investment Service 테스트")
class InvestmentServiceTest {

    @Mock
    private InvestmentRepository investmentRepository;

    @Mock
    private ProductClient productClient;

    @Mock
    private AssetClient assetClient;

    @InjectMocks
    private InvestmentService investmentService;

    private Investment testInvestment1;
    private Investment testInvestment2;
    private ProductDTO testProduct;
    private BuyInvestmentRequest buyRequest;
    private CancelInvestmentRequest cancelRequest;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 생성
        testInvestment1 = Investment.builder()
                .investmentSeq(1)
                .userSeq(1)
                .projectId(1)
                .investedPrice(1000000)
                .tokenQuantity(100)
                .investedAt(LocalDate.now())
                .invStatus(Investment.InvestmentStatus.COMPLETED)
                .currentAmount(50000000)
                .totalInvestment(100000000)
                .totalInvestor(50)
                .achievementRate(75)
                .createdId(1)
                .createdAt(LocalDate.now())
                .updatedId(1)
                .updatedAt(LocalDate.now())
                .build();

        testInvestment2 = Investment.builder()
                .investmentSeq(2)
                .userSeq(2)
                .projectId(1)
                .investedPrice(2000000)
                .tokenQuantity(200)
                .investedAt(LocalDate.now())
                .invStatus(Investment.InvestmentStatus.PENDING)
                .currentAmount(50000000)
                .totalInvestment(100000000)
                .totalInvestor(50)
                .achievementRate(75)
                .createdId(2)
                .createdAt(LocalDate.now())
                .updatedId(2)
                .updatedAt(LocalDate.now())
                .build();

        testProduct = ProductDTO.builder()
                .title("테스트 프로젝트")
                .goalAmount(100000000)
                .startDate(LocalDate.now().minusDays(30))
                .endDate(LocalDate.now().plusDays(30))
                .status("승인 완료")
                .minInvestment(10000)
                .build();

        buyRequest = BuyInvestmentRequest.builder()
                .investmentSeq(1) // 실제 서비스에서 사용하는 필드
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
    @DisplayName("투자 상품 전체 조회 테스트")
    void getAllProducts() {
        // given
        when(investmentRepository.findAll()).thenReturn(Arrays.asList(testInvestment1, testInvestment2));
        when(productClient.getProduct(anyInt())).thenReturn(testProduct);

        // when
        List<AllProductListResponse> result = investmentService.getAllProducts();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getProjectId()).isEqualTo(1);
        assertThat(result.get(0).getTitle()).isEqualTo("테스트 프로젝트");
        verify(productClient, times(2)).getProduct(anyInt());
    }

    @Test
    @DisplayName("개인 투자 내역 조회 테스트")
    void getUserInvestments() {
        // given
        when(investmentRepository.findByUserSeq(1)).thenReturn(Arrays.asList(testInvestment1));
        when(productClient.getProduct(1)).thenReturn(testProduct);

        // when
        List<UserInvestmentListResponse> result = investmentService.getUserInvestments(1);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserSeq()).isEqualTo(1);
        assertThat(result.get(0).getProjectId()).isEqualTo(1);
        assertThat(result.get(0).getTitle()).isEqualTo("테스트 프로젝트");
        verify(productClient, times(1)).getProduct(1);
    }

    @Test
    @DisplayName("상품별 투자자 조회 테스트")
    void getProductInvestor() {
        // given
        when(investmentRepository.findByProjectId(1)).thenReturn(Arrays.asList(testInvestment1, testInvestment2));
        when(productClient.getProduct(1)).thenReturn(testProduct);

        // when
        List<ProductInvestorListResponse> result = investmentService.getProductInvestor(1);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getProjectId()).isEqualTo(1);
        assertThat(result.get(0).getTotalInvestment()).isEqualTo(100000000);
        verify(productClient, times(2)).getProduct(1);
    }

    @Test
    @DisplayName("투자 주문 성공 테스트")
    void buyInvestment_Success() {
        // given
        when(investmentRepository.findById(1)).thenReturn(Optional.of(testInvestment1));
        when(productClient.getProduct(1)).thenReturn(testProduct);
        when(investmentRepository.save(any(Investment.class))).thenReturn(testInvestment1);

        // when
        investmentService.buyInvestment(buyRequest, testProduct);

        // then
        verify(investmentRepository, times(1)).save(any(Investment.class));
        verify(assetClient, times(1)).updateAsset(any(AssetDTO.class));
    }

    @Test
    @DisplayName("투자 주문 실패 - 존재하지 않는 주문")
    void buyInvestment_OrderNotFound() {
        // given
        when(investmentRepository.findById(999)).thenReturn(Optional.empty());

        BuyInvestmentRequest invalidRequest = BuyInvestmentRequest.builder()
                .investmentSeq(999)
                .userSeq(1)
                .productId(1)
                .tokenQuantity(50)
                .build();

        // when & then
        assertThatThrownBy(() -> investmentService.buyInvestment(invalidRequest, testProduct))
                .isInstanceOf(BadParameter.class)
                .hasMessage("존재하지 않는 주문입니다.");
    }

    @Test
    @DisplayName("투자 주문 실패 - 유효하지 않은 사용자")
    void buyInvestment_InvalidUser() {
        // given
        when(investmentRepository.findById(1)).thenReturn(Optional.of(testInvestment1));

        BuyInvestmentRequest invalidRequest = BuyInvestmentRequest.builder()
                .investmentSeq(1)
                .userSeq(null)
                .productId(1)
                .tokenQuantity(50)
                .build();

        // when & then
        assertThatThrownBy(() -> investmentService.buyInvestment(invalidRequest, testProduct))
                .isInstanceOf(BadParameter.class)
                .hasMessage("유효하지 않은 사용자입니다.");
    }

    @Test
    @DisplayName("투자 주문 실패 - 유효하지 않은 상품")
    void buyInvestment_InvalidProduct() {
        // given
        when(investmentRepository.findById(1)).thenReturn(Optional.of(testInvestment1));

        BuyInvestmentRequest invalidRequest = BuyInvestmentRequest.builder()
                .investmentSeq(1)
                .userSeq(1)
                .productId(null)
                .tokenQuantity(50)
                .build();

        // when & then
        assertThatThrownBy(() -> investmentService.buyInvestment(invalidRequest, testProduct))
                .isInstanceOf(BadParameter.class)
                .hasMessage("유효하지 않은 상품입니다.");
    }

    @Test
    @DisplayName("투자 주문 실패 - 유효하지 않은 토큰 수량")
    void buyInvestment_InvalidTokenQuantity() {
        // given
        when(investmentRepository.findById(1)).thenReturn(Optional.of(testInvestment1));

        BuyInvestmentRequest invalidRequest = BuyInvestmentRequest.builder()
                .investmentSeq(1)
                .userSeq(1)
                .productId(1)
                .tokenQuantity(0)
                .build();

        // when & then
        assertThatThrownBy(() -> investmentService.buyInvestment(invalidRequest, testProduct))
                .isInstanceOf(BadParameter.class)
                .hasMessage("토큰 수량은 1개 이상이어야 합니다.");
    }

    @Test
    @DisplayName("투자 주문 실패 - 상품 상태가 승인되지 않음")
    void buyInvestment_ProductNotApproved() {
        // given
        when(investmentRepository.findById(1)).thenReturn(Optional.of(testInvestment1));

        ProductDTO notApprovedProduct = ProductDTO.builder()
                .title("미승인 프로젝트")
                .goalAmount(100000000)
                .startDate(LocalDate.now().minusDays(30))
                .endDate(LocalDate.now().plusDays(30))
                .status("검토 중")
                .minInvestment(10000)
                .build();

        when(productClient.getProduct(1)).thenReturn(notApprovedProduct);

        // when & then
        assertThatThrownBy(() -> investmentService.buyInvestment(buyRequest, notApprovedProduct))
                .isInstanceOf(BadParameter.class)
                .hasMessage("해당 상품은 현재 모집 중이 아닙니다.");
    }

    @Test
    @DisplayName("투자 취소 성공 테스트")
    void cancelInvestment_Success() {
        // given
        when(investmentRepository.findById(1)).thenReturn(Optional.of(testInvestment1));
        when(investmentRepository.save(any(Investment.class))).thenReturn(testInvestment1);

        // when
        InvestmentResponse result = investmentService.cancelInvestment(cancelRequest);

        // then
        assertThat(result.getTokenQuantity()).isEqualTo(100);
        verify(investmentRepository, times(1)).save(any(Investment.class));
    }

    @Test
    @DisplayName("투자 취소 실패 - 존재하지 않는 주문")
    void cancelInvestment_OrderNotFound() {
        // given
        when(investmentRepository.findById(999)).thenReturn(Optional.empty());

        CancelInvestmentRequest invalidRequest = CancelInvestmentRequest.builder()
                .userSeq(1)
                .productId(1)
                .investmentSeq(999)
                .build();

        // when & then
        assertThatThrownBy(() -> investmentService.cancelInvestment(invalidRequest))
                .isInstanceOf(BadParameter.class)
                .hasMessage("존재하지 않는 주문입니다.");
    }

    @Test
    @DisplayName("투자 취소 실패 - 주문 정보 불일치")
    void cancelInvestment_OrderMismatch() {
        // given
        when(investmentRepository.findById(1)).thenReturn(Optional.of(testInvestment1));

        CancelInvestmentRequest invalidRequest = CancelInvestmentRequest.builder()
                .userSeq(999) // 다른 사용자
                .productId(1)
                .investmentSeq(1)
                .build();

        // when & then
        assertThatThrownBy(() -> investmentService.cancelInvestment(invalidRequest))
                .isInstanceOf(BadParameter.class)
                .hasMessage("주문 정보가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("투자 취소 실패 - 이미 취소된 주문")
    void cancelInvestment_AlreadyCancelled() {
        // given
        Investment cancelledInvestment = Investment.builder()
                .investmentSeq(1)
                .userSeq(1)
                .projectId(1)
                .invStatus(Investment.InvestmentStatus.CANCELLED)
                .build();

        when(investmentRepository.findById(1)).thenReturn(Optional.of(cancelledInvestment));

        // when & then
        assertThatThrownBy(() -> investmentService.cancelInvestment(cancelRequest))
                .isInstanceOf(BadParameter.class)
                .hasMessage("이미 취소된 주문입니다.");
    }
} 