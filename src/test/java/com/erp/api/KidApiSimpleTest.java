package com.erp.api;

import com.erp.common.BaseIntegrationTest;
import com.erp.domain.classroom.entity.Classroom;
import com.erp.domain.kindergarten.entity.Kindergarten;
import com.erp.domain.kid.entity.Gender;
import com.erp.domain.kid.entity.Kid;
import com.erp.domain.kid.repository.KidRepository;
import com.erp.domain.kindergarten.repository.KindergartenRepository;
import com.erp.domain.classroom.repository.ClassroomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("원생 API 간단 테스트")
class KidApiSimpleTest extends BaseIntegrationTest {

    @Autowired
    private KidRepository kidRepository;

    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private KindergartenRepository kindergartenRepository;

    @BeforeEach
    void setUp() {
        testData.cleanup();
        testData.createPrincipalMember();
        testData.createTeacherMember();
        testData.createParentMember();

        Kindergarten kg = testData.createKindergarten();
        Classroom cr = testData.createClassroom(kg);

        Kid kid = Kid.create(cr, "테스트 원생", java.time.LocalDate.of(2020, 1, 1),
                Gender.MALE, java.time.LocalDate.now());
        kidRepository.save(kid);
    }

    @Test
    @WithMockUser(username = "parent@test.com", roles = {"PARENT"})
    @DisplayName("원생 단건 조회 - 성공")
    void getKid_Success() throws Exception {
        mockMvc.perform(get("/api/v1/kids/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
