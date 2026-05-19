package com.erp.api;

import com.erp.common.BaseIntegrationTest;
import com.erp.domain.member.entity.Member;
import com.erp.domain.notification.entity.Notification;
import com.erp.domain.notification.entity.NotificationDeliveryStatus;
import com.erp.domain.notification.entity.NotificationOutbox;
import com.erp.domain.notification.entity.NotificationType;
import com.erp.domain.notification.repository.NotificationOutboxRepository;
import com.erp.domain.notification.repository.NotificationRepository;
import com.erp.domain.notification.service.channel.NotificationChannel;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("알림 outbox 운영 API 테스트")
@Tag("integration")
class NotificationOutboxOpsApiIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationOutboxRepository notificationOutboxRepository;

    @Test
    @DisplayName("원장은 dead-letter summary와 목록을 조회할 수 있다")
    void principalCanReadDeadLetterOps() throws Exception {
        Long outboxId = createDeadLetterOutbox();

        mockMvc.perform(get("/api/v1/notification-outbox/summary")
                        .with(authenticated(principalMember)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.statusCounts.DEAD_LETTER").value(1))
                .andExpect(jsonPath("$.data.deadLetterCountsByChannel.APP").value(1));

        mockMvc.perform(get("/api/v1/notification-outbox/dead-letters")
                        .with(authenticated(principalMember)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(outboxId))
                .andExpect(jsonPath("$.data.content[0].status").value("DEAD_LETTER"));
    }

    @Test
    @DisplayName("교사는 outbox 운영 API에 접근할 수 없다")
    void teacherCannotAccessOutboxOps() throws Exception {
        mockMvc.perform(get("/api/v1/notification-outbox/summary")
                        .with(authenticated(teacherMember)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("A004"));
    }

    @Test
    @DisplayName("원장은 dead-letter outbox를 즉시 재시도 대기 상태로 되돌릴 수 있다")
    void principalCanRetryDeadLetter() throws Exception {
        Long outboxId = createDeadLetterOutbox();

        mockMvc.perform(post("/api/v1/notification-outbox/{outboxId}/retry", outboxId)
                        .with(authenticated(principalMember))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.lastError").doesNotExist());

        NotificationOutbox outbox = notificationOutboxRepository.findById(outboxId).orElseThrow();
        assertThat(outbox.getStatus()).isEqualTo(NotificationDeliveryStatus.PENDING);
        assertThat(outbox.getDeadLetteredAt()).isNull();
        assertThat(outbox.canRetry()).isTrue();
    }

    private Long createDeadLetterOutbox() {
        Member receiver = memberRepository.findById(parentMember.getId()).orElseThrow();
        Notification notification = notificationRepository.save(Notification.createWithLink(
                receiver,
                NotificationType.SYSTEM,
                "전송 실패 알림",
                "outbox 운영 API 테스트",
                "/notifications"
        ));
        NotificationOutbox outbox = NotificationOutbox.create(notification, NotificationChannel.APP, 1);
        LocalDateTime now = LocalDateTime.now();
        outbox.markProcessing(now.minusMinutes(1));
        outbox.markDeadLetter(now, "webhook timeout");
        return notificationOutboxRepository.saveAndFlush(outbox).getId();
    }
}
