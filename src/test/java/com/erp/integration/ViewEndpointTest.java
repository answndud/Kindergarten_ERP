package com.erp.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 뷰 엔드포인트 통합 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ViewEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testHomePage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    void testLoginPage() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    @Test
    void testSignupPage() throws Exception {
        mockMvc.perform(get("/signup"))
                .andExpect(status().isOk());
    }

    // 인증이 필요한 페이지들 (로그인 없으면 리다이렉트되어야 함)
    @Test
    void testNotepadPageWithoutAuth() throws Exception {
        mockMvc.perform(get("/notepad"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void testAttendancePageWithoutAuth() throws Exception {
        mockMvc.perform(get("/attendance"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void testAnnouncementsPageWithoutAuth() throws Exception {
        mockMvc.perform(get("/announcements"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void testProfilePageWithoutAuth() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void testSettingsPageWithoutAuth() throws Exception {
        mockMvc.perform(get("/settings"))
                .andExpect(status().is3xxRedirection());
    }
}
