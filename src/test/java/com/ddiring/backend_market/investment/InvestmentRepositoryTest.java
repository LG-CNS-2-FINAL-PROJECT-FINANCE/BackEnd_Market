package com.ddiring.backend_market.investment;

import com.ddiring.backend_market.investment.entity.Investment;
import com.ddiring.backend_market.investment.repository.InvestmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Investment Repository 테스트")
class InvestmentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private InvestmentRepository investmentRepository;

    private Investment testInvestment1;
    private Investment testInvestment2;
    private Investment testInvestment3;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 생성
        testInvestment1 = Investment.builder()
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
                .userSeq(1)
                .projectId(2)
                .investedPrice(2000000)
                .tokenQuantity(200)
                .investedAt(LocalDate.now())
                .invStatus(Investment.InvestmentStatus.PENDING)
                .currentAmount(75000000)
                .totalInvestment(150000000)
                .totalInvestor(30)
                .achievementRate(85)
                .createdId(1)
                .createdAt(LocalDate.now())
                .updatedId(1)
                .updatedAt(LocalDate.now())
                .build();

        testInvestment3 = Investment.builder()
                .userSeq(2)
                .projectId(1)
                .investedPrice(1500000)
                .tokenQuantity(150)
                .investedAt(LocalDate.now())
                .invStatus(Investment.InvestmentStatus.COMPLETED)
                .currentAmount(50000000)
                .totalInvestment(100000000)
                .totalInvestor(50)
                .achievementRate(75)
                .createdId(2)
                .createdAt(LocalDate.now())
                .updatedId(2)
                .updatedAt(LocalDate.now())
                .build();
    }

    @Test
    @DisplayName("투자 정보 저장 테스트")
    void saveInvestment() {
        // when
        Investment savedInvestment = investmentRepository.save(testInvestment1);

        // then
        assertThat(savedInvestment.getInvestmentSeq()).isNotNull();
        assertThat(savedInvestment.getUserSeq()).isEqualTo(1);
        assertThat(savedInvestment.getProjectId()).isEqualTo(1);
        assertThat(savedInvestment.getInvStatus()).isEqualTo(Investment.InvestmentStatus.COMPLETED);
    }

    @Test
    @DisplayName("사용자별 투자 내역 조회 테스트")
    void findByUserSeq() {
        // given
        investmentRepository.save(testInvestment1);
        investmentRepository.save(testInvestment2);
        investmentRepository.save(testInvestment3);

        // when
        List<Investment> userInvestments = investmentRepository.findByUserSeq(1);

        // then
        assertThat(userInvestments).hasSize(2);
        assertThat(userInvestments).allMatch(investment -> investment.getUserSeq().equals(1));
    }

    @Test
    @DisplayName("프로젝트별 투자 내역 조회 테스트")
    void findByProjectId() {
        // given
        investmentRepository.save(testInvestment1);
        investmentRepository.save(testInvestment2);
        investmentRepository.save(testInvestment3);

        // when
        List<Investment> projectInvestments = investmentRepository.findByProjectId(1);

        // then
        assertThat(projectInvestments).hasSize(2);
        assertThat(projectInvestments).allMatch(investment -> investment.getProjectId().equals(1));
    }

    @Test
    @DisplayName("투자 상태별 조회 테스트")
    void findByInvStatus() {
        // given
        investmentRepository.save(testInvestment1);
        investmentRepository.save(testInvestment2);
        investmentRepository.save(testInvestment3);

        // when
        List<Investment> completedInvestments = investmentRepository.findByInvStatus(Investment.InvestmentStatus.COMPLETED);

        // then
        assertThat(completedInvestments).hasSize(2);
        assertThat(completedInvestments).allMatch(investment -> 
            investment.getInvStatus().equals(Investment.InvestmentStatus.COMPLETED));
    }

    @Test
    @DisplayName("전체 투자 내역 조회 테스트")
    void findAll() {
        // given
        investmentRepository.save(testInvestment1);
        investmentRepository.save(testInvestment2);
        investmentRepository.save(testInvestment3);

        // when
        List<Investment> allInvestments = investmentRepository.findAll();

        // then
        assertThat(allInvestments).hasSize(3);
    }

    @Test
    @DisplayName("투자 정보 수정 테스트")
    void updateInvestment() {
        // given
        Investment savedInvestment = investmentRepository.save(testInvestment1);
        savedInvestment = Investment.builder()
                .investmentSeq(savedInvestment.getInvestmentSeq())
                .userSeq(savedInvestment.getUserSeq())
                .projectId(savedInvestment.getProjectId())
                .investedPrice(savedInvestment.getInvestedPrice())
                .tokenQuantity(savedInvestment.getTokenQuantity())
                .investedAt(savedInvestment.getInvestedAt())
                .invStatus(Investment.InvestmentStatus.CANCELLED)
                .currentAmount(savedInvestment.getCurrentAmount())
                .totalInvestment(savedInvestment.getTotalInvestment())
                .totalInvestor(savedInvestment.getTotalInvestor())
                .achievementRate(savedInvestment.getAchievementRate())
                .createdId(savedInvestment.getCreatedId())
                .createdAt(savedInvestment.getCreatedAt())
                .updatedId(savedInvestment.getUpdatedId())
                .updatedAt(LocalDate.now())
                .build();

        // when
        Investment updatedInvestment = investmentRepository.save(savedInvestment);

        // then
        assertThat(updatedInvestment.getInvStatus()).isEqualTo(Investment.InvestmentStatus.CANCELLED);
    }
} 