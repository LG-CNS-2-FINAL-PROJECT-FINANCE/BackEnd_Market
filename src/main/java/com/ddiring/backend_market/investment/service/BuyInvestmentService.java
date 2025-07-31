package com.ddiring.backend_market.investment.service;

import com.ddiring.backend_market.investment.dto.request.BuyInvestmentRequest;
import com.ddiring.backend_market.investment.dto.request.CancleInvestmentRequest;
import com.ddiring.backend_market.investment.entity.Investment;
import com.ddiring.backend_market.investment.repository.BuyInvestmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class BuyInvestmentService {

    private final BuyInvestmentRepository buyInvestmentRepository;

    @Transactional
    public String buyInvestment(BuyInvestmentRequest request) {
        try {
            log.info("투자 토큰 구매: userSeq={}, productId={}, tokenQuantity={}",
                    request.getUserSeq(), request.getProductId(), request.getTokenQuantity());

            validateBuyRequest(request);

            Investment investment = createInvestment(request);

            buyInvestmentRepository.save(investment);

            String tokenResult = callExternalTokenService(request);

            log.info("투자 토큰 구매 완료: investmentSeq={}", investment.getInvestmentSeq());
            
            return "주문번호: " + investment.getInvestmentSeq() + ", 토큰 수령 완료: " + tokenResult;
            
        } catch (Exception e) {
            log.error("투자 토큰 구매 중 오류 발생", e);
            throw new RuntimeException("투자 토큰 구매에 실패했습니다.", e);
        }
    }

    /**
     * 구매 요청 검증
     */
    private void validateBuyRequest(BuyInvestmentRequest request) {
        if (request.getUserSeq() == null || request.getUserSeq() <= 0) {
            throw new IllegalArgumentException("유효하지 않은 사용자 ID입니다.");
        }
        if (request.getProductId() == null || request.getProductId() <= 0) {
            throw new IllegalArgumentException("유효하지 않은 상품 ID입니다.");
        }
        if (request.getTokenQuantity() == null || request.getTokenQuantity() <= 0) {
            throw new IllegalArgumentException("유효하지 않은 토큰 수량입니다.");
        }
    }

    /**
     * 투자 내역 엔티티 생성
     */
    private Investment createInvestment(BuyInvestmentRequest request) {
        LocalDate now = LocalDate.now();
        
        // TODO: 실제 구현에서는 상품 정보를 조회하여 투자 금액을 계산해야 함
        // 임시로 토큰 수량 * 1000원으로 계산
        Integer investedPrice = request.getTokenQuantity() * 1000;
        
        return Investment.builder()
                .userSeq(request.getUserSeq())
                .productId(request.getProductId())
                .investedPrice(investedPrice)
                .tokenQuantity(request.getTokenQuantity())
                .investedAt(now)
                .createdId(request.getUserSeq())
                .createdAt(now)
                .updatedId(request.getUserSeq())
                .updatedAt(now)
                .build();
    }

    /**
     * 외부 토큰 서비스 호출 (실제 구현에서는 외부 API 호출)
     */
    private String callExternalTokenService(BuyInvestmentRequest request) {
        // TODO: 실제 외부 토큰 서비스 API 호출 구현
        // 예시: RestTemplate 또는 WebClient를 사용하여 외부 서비스 호출
        log.info("외부 토큰 서비스 호출: userSeq={}, productId={}, tokenQuantity={}", 
                request.getUserSeq(), request.getProductId(), request.getTokenQuantity());
        
        // 임시 구현 - 실제로는 외부 API 호출 결과를 반환
        return "토큰 " + request.getTokenQuantity() + "개 수령 완료";
    }

    /**
     * 투자 토큰 구매 취소 (주문 취소)
     * @param request 취소 요청 정보
     * @return 취소 완료 메시지
     */
    @Transactional
    public String cancelInvestment(CancleInvestmentRequest request) {
        try {
            log.info("투자 토큰 구매 취소 시작: userSeq={}, projectId={}, investmentId={}", 
                    request.getUserSeq(), request.getProjectId(), request.getInvestmentId());

            // 1. 입력값 검증
            validateCancelRequest(request);

            // 2. 투자 내역 조회 및 검증
            Investment investment = findAndValidateInvestment(request);

            // 3. 취소 가능 여부 확인
            validateCancellationEligibility(investment);

            // 4. 외부 서비스에서 토큰 환불 처리
            String refundResult = callExternalRefundService(request, investment);

            // 5. 투자 내역 삭제 또는 상태 변경
            buyInvestmentRepository.delete(investment);

            log.info("투자 토큰 구매 취소 완료: investmentId={}", request.getInvestmentId());
            
            return "주문 취소 완료: " + refundResult;
            
        } catch (Exception e) {
            log.error("투자 토큰 구매 취소 중 오류 발생", e);
            throw new RuntimeException("투자 토큰 구매 취소에 실패했습니다.", e);
        }
    }

    /**
     * 취소 요청 검증
     */
    private void validateCancelRequest(CancleInvestmentRequest request) {
        if (request.getUserSeq() == null || request.getUserSeq() <= 0) {
            throw new IllegalArgumentException("유효하지 않은 사용자 ID입니다.");
        }
        if (request.getProjectId() == null || request.getProjectId() <= 0) {
            throw new IllegalArgumentException("유효하지 않은 프로젝트 ID입니다.");
        }
        if (request.getInvestmentId() == null || request.getInvestmentId() <= 0) {
            throw new IllegalArgumentException("유효하지 않은 투자 ID입니다.");
        }
    }

    /**
     * 투자 내역 조회 및 검증
     */
    private Investment findAndValidateInvestment(CancleInvestmentRequest request) {
        Investment investment = buyInvestmentRepository.findById(request.getInvestmentId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 투자 내역입니다."));
        
        // 투자자 본인 확인
        if (!investment.getUserSeq().equals(request.getUserSeq())) {
            throw new IllegalArgumentException("본인의 투자 내역만 취소할 수 있습니다.");
        }
        
        // 프로젝트 ID 확인
        if (!investment.getProductId().equals(request.getProjectId())) {
            throw new IllegalArgumentException("프로젝트 ID가 일치하지 않습니다.");
        }
        
        return investment;
    }

    /**
     * 취소 가능 여부 확인
     */
    private void validateCancellationEligibility(Investment investment) {
        // TODO: 실제 구현에서는 프로젝트 상태, 취소 기간 등을 확인해야 함
        // 예시: 투자 후 24시간 이내에만 취소 가능
        LocalDate investmentDate = investment.getInvestedAt();
        LocalDate now = LocalDate.now();
        
        if (investmentDate.plusDays(1).isBefore(now)) {
            throw new IllegalArgumentException("투자 후 24시간이 지나면 취소할 수 없습니다.");
        }
    }

    /**
     * 외부 환불 서비스 호출
     */
    private String callExternalRefundService(CancleInvestmentRequest request, Investment investment) {
        // TODO: 실제 외부 환불 서비스 API 호출 구현
        log.info("외부 환불 서비스 호출: userSeq={}, projectId={}, investmentId={}, tokenQuantity={}", 
                request.getUserSeq(), request.getProjectId(), request.getInvestmentId(), investment.getTokenQuantity());
        
        // 임시 구현 - 실제로는 외부 API 호출 결과를 반환
        return "토큰 " + investment.getTokenQuantity() + "개 환불 완료";
    }
}
