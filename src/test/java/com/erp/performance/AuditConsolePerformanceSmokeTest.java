package com.erp.performance;

import com.erp.common.BaseIntegrationTest;
import com.erp.domain.authaudit.service.AuthAuditLogQueryService;
import com.erp.domain.authaudit.service.AuthAuditLogService;
import com.erp.domain.domainaudit.entity.DomainAuditAction;
import com.erp.domain.domainaudit.entity.DomainAuditTargetType;
import com.erp.domain.domainaudit.service.DomainAuditLogQueryService;
import com.erp.domain.domainaudit.service.DomainAuditLogService;
import com.erp.domain.member.entity.MemberAuthProvider;
import com.erp.domain.member.entity.Member;
import com.erp.domain.member.entity.MemberRole;
import com.erp.domain.kindergarten.entity.Kindergarten;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("성능 스모크 - 감사 로그 운영 콘솔")
@Tag("performance")
class AuditConsolePerformanceSmokeTest extends BaseIntegrationTest {

    private static final int BULK_LOG_COUNT = 80;

    @Autowired
    private AuthAuditLogService authAuditLogService;

    @Autowired
    private AuthAuditLogQueryService authAuditLogQueryService;

    @Autowired
    private DomainAuditLogService domainAuditLogService;

    @Autowired
    private DomainAuditLogQueryService domainAuditLogQueryService;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("인증 감사 로그 목록/CSV export 경로는 예상 쿼리 예산 안에 들어온다")
    void authAuditConsole_StaysWithinQueryBudget() {
        AuditConsoleFixture fixture = seedCommittedFixture();
        seedAuthAuditLogs(fixture);

        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.setStatisticsEnabled(true);

        Measurement list = readCommitted(() -> measure(statistics, () -> authAuditLogQueryService.getAuditLogsForPrincipal(
                fixture.principalId(),
                null,
                null,
                null,
                null,
                null,
                null,
                0,
                20
        )));
        Measurement export = readCommitted(() -> measure(statistics, () -> authAuditLogQueryService.exportAuditLogsCsvForPrincipal(
                fixture.principalId(),
                null,
                null,
                null,
                null,
                null,
                null
        )));

        System.out.printf("[PERF] auth-audit-list   - queries=%d, elapsedMs=%d%n", list.queryCount, list.elapsedMs);
        System.out.printf("[PERF] auth-audit-export - queries=%d, elapsedMs=%d%n", export.queryCount, export.elapsedMs);

        assertTrue(list.queryCount <= 3, "auth audit list should stay within member lookup + page + count budget");
        assertTrue(export.queryCount <= 2, "auth audit export should stay within member lookup + export query budget");
    }

    @Test
    @DisplayName("업무 감사 로그 목록/CSV export 경로는 예상 쿼리 예산 안에 들어온다")
    void domainAuditConsole_StaysWithinQueryBudget() {
        AuditConsoleFixture fixture = seedCommittedFixture();
        seedDomainAuditLogs(fixture);

        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.setStatisticsEnabled(true);

        Measurement list = readCommitted(() -> measure(statistics, () -> domainAuditLogQueryService.getAuditLogsForPrincipal(
                fixture.principalId(),
                null,
                null,
                null,
                null,
                null,
                0,
                20
        )));
        Measurement export = readCommitted(() -> measure(statistics, () -> domainAuditLogQueryService.exportAuditLogsCsvForPrincipal(
                fixture.principalId(),
                null,
                null,
                null,
                null,
                null
        )));

        System.out.printf("[PERF] domain-audit-list   - queries=%d, elapsedMs=%d%n", list.queryCount, list.elapsedMs);
        System.out.printf("[PERF] domain-audit-export - queries=%d, elapsedMs=%d%n", export.queryCount, export.elapsedMs);

        assertTrue(list.queryCount <= 3, "domain audit list should stay within lookup + page + count budget");
        assertTrue(export.queryCount <= 2, "domain audit export should stay within lookup + export query budget");
    }

    private AuditConsoleFixture seedCommittedFixture() {
        return writeCommitted(() -> {
            String token = Long.toString(System.nanoTime());
            Kindergarten committedKindergarten = kindergartenRepository.save(
                    Kindergarten.create(
                            "성능 유치원 " + token,
                            "서울시 어딘가",
                            "02-0000-0000",
                            java.time.LocalTime.of(9, 0),
                            java.time.LocalTime.of(18, 0)
                    )
            );

            Member committedPrincipal = testData.createTestMember(
                    "perf-principal-" + token + "@test.com",
                    "성능원장" + token,
                    MemberRole.PRINCIPAL,
                    "test1234"
            );
            committedPrincipal.assignKindergarten(committedKindergarten);
            memberRepository.save(committedPrincipal);

            return new AuditConsoleFixture(
                    committedPrincipal.getId(),
                    committedPrincipal.getEmail(),
                    committedKindergarten.getId()
            );
        });
    }

    private void seedAuthAuditLogs(AuditConsoleFixture fixture) {
        writeCommitted(() -> {
            for (int i = 0; i < BULK_LOG_COUNT; i++) {
                authAuditLogService.recordLoginSuccess(
                        fixture.principalId(),
                        fixture.principalEmail(),
                        MemberAuthProvider.LOCAL,
                        "198.51.100." + (10 + (i % 50))
                );
            }
            return null;
        });
    }

    private void seedDomainAuditLogs(AuditConsoleFixture fixture) {
        writeCommitted(() -> {
            var actor = memberRepository.findById(fixture.principalId()).orElseThrow();
            for (int i = 0; i < BULK_LOG_COUNT; i++) {
                domainAuditLogService.record(
                        actor,
                        fixture.kindergartenId(),
                        DomainAuditAction.ANNOUNCEMENT_UPDATED,
                        DomainAuditTargetType.ANNOUNCEMENT,
                        (long) (1000 + i),
                        "운영 성능 스모크 공지 변경 " + i,
                        Map.of("index", i)
                );
            }
            entityManager.flush();
            entityManager.clear();
            return null;
        });
    }

    private Measurement measure(Statistics statistics, Runnable action) {
        entityManager.clear();
        statistics.clear();
        long start = System.nanoTime();
        action.run();
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        long queryCount = statistics.getPrepareStatementCount();
        return new Measurement(queryCount, elapsedMs);
    }

    private record Measurement(long queryCount, long elapsedMs) {
    }

    private record AuditConsoleFixture(Long principalId, String principalEmail, Long kindergartenId) {
    }
}
