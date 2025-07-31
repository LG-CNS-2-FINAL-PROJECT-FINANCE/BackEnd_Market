package com.ddiring.backend_market.investment.config;

import com.ddiring.backend_market.investment.entity.Product;
import com.ddiring.backend_market.investment.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) throws Exception {
        // 테스트 데이터가 없을 때만 생성
        if (productRepository.count() == 0) {
            log.info("테스트용 투자 상품 데이터를 생성합니다.");
            createTestProducts();
            log.info("테스트용 투자 상품 데이터 생성 완료.");
        } else {
            log.info("이미 테스트 데이터가 존재합니다. 건너뜁니다.");
        }
    }

    private void createTestProducts() {
        LocalDate now = LocalDate.now();
        
        Product product1 = Product.builder()
                .userSeq(1)
                .status(1) // 승인완료
                .startDate(now)
                .endDate(now.plusMonths(3))
                .title("친환경 태양광 발전소 프로젝트")
                .summary("지속 가능한 에너지 생산을 위한 태양광 발전소 건설 프로젝트입니다.")
                .content("이 프로젝트는 지역사회의 친환경 에너지 생산을 목표로 합니다. " +
                        "태양광 패널 설치, 인버터 시스템 구축, 전력 계통 연동 등이 포함됩니다.")
                .goalAmount(100000000) // 1억원
                .minInvestment(100000) // 10만원
                .account(1234567890)
                .document("[\"business_plan.pdf\", \"solar_panel_spec.jpg\", \"location_map.png\"]")
                .createdId(1)
                .createdAt(now)
                .updatedId(1)
                .updatedAt(now)
                .build();

        Product product2 = Product.builder()
                .userSeq(2)
                .status(1) // 승인완료
                .startDate(now.plusDays(7))
                .endDate(now.plusMonths(6))
                .title("스마트팜 IoT 기술 개발")
                .summary("농업 생산성 향상을 위한 IoT 기반 스마트팜 시스템 개발 프로젝트입니다.")
                .content("IoT 센서를 활용한 환경 모니터링, 자동 급수 시스템, " +
                        "작물 생장 데이터 분석 등이 포함된 스마트팜 솔루션을 개발합니다.")
                .goalAmount(50000000) // 5천만원
                .minInvestment(50000) // 5만원
                .account(987654321)
                .document("[\"tech_spec.pdf\", \"prototype_image.jpg\", \"market_analysis.pdf\"]")
                .createdId(2)
                .createdAt(now)
                .updatedId(2)
                .updatedAt(now)
                .build();

        Product product3 = Product.builder()
                .userSeq(3)
                .status(1) // 승인완료
                .startDate(now.plusDays(14))
                .endDate(now.plusMonths(4))
                .title("전기차 충전소 네트워크 구축")
                .summary("도시 전역에 전기차 충전소를 구축하여 친환경 교통 인프라를 확충합니다.")
                .content("주요 교통 요지에 고속 충전소를 설치하고, " +
                        "모바일 앱을 통한 충전소 위치 안내 및 결제 시스템을 구축합니다.")
                .goalAmount(200000000) // 2억원
                .minInvestment(200000) // 20만원
                .account(1122334455)
                .document("[\"infrastructure_plan.pdf\", \"charging_station_design.jpg\", \"financial_forecast.pdf\"]")
                .createdId(3)
                .createdAt(now)
                .updatedId(3)
                .updatedAt(now)
                .build();

        Product product4 = Product.builder()
                .userSeq(4)
                .status(0) // 등록대기
                .startDate(now.plusDays(30))
                .endDate(now.plusMonths(8))
                .title("AI 기반 의료 진단 시스템")
                .summary("인공지능을 활용한 의료 영상 진단 시스템 개발 프로젝트입니다.")
                .content("X-ray, CT, MRI 영상을 분석하여 질병을 조기 진단하는 " +
                        "AI 시스템을 개발하여 의료진의 진단 정확도를 향상시킵니다.")
                .goalAmount(150000000) // 1억 5천만원
                .minInvestment(150000) // 15만원
                .account(556677889)
                .document("[\"medical_ai_spec.pdf\", \"sample_images.zip\", \"regulatory_approval.pdf\"]")
                .createdId(4)
                .createdAt(now)
                .updatedId(4)
                .updatedAt(now)
                .build();

        Product product5 = Product.builder()
                .userSeq(5)
                .status(-1) // 숨김
                .startDate(now.minusDays(30))
                .endDate(now.plusMonths(2))
                .title("중단된 프로젝트")
                .summary("이 프로젝트는 중단되었습니다.")
                .content("프로젝트 중단 사유: 자금 부족")
                .goalAmount(30000000) // 3천만원
                .minInvestment(30000) // 3만원
                .account(998877665)
                .document("[\"suspension_reason.pdf\"]")
                .createdId(5)
                .createdAt(now.minusDays(30))
                .updatedId(5)
                .updatedAt(now.minusDays(30))
                .build();

        productRepository.saveAll(Arrays.asList(product1, product2, product3, product4, product5));
    }
} 