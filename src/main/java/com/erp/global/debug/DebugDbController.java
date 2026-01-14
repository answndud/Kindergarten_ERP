package com.erp.global.debug;

import com.erp.global.common.ApiResponse;
import com.erp.global.security.user.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.Map;

@Profile("local")
@RestController
@RequiredArgsConstructor
public class DebugDbController {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/debug/db")
    @PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER', 'PARENT')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> db(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Map<String, Object> info = new LinkedHashMap<>();

        info.put("memberId", userDetails != null ? userDetails.getMemberId() : null);
        info.put("role", userDetails != null ? userDetails.getRole().name() : null);

        try (Connection connection = dataSource.getConnection()) {
            info.put("jdbcUrl", connection.getMetaData().getURL());
            info.put("dbUser", connection.getMetaData().getUserName());
            info.put("dbProduct", connection.getMetaData().getDatabaseProductName());
            info.put("dbVersion", connection.getMetaData().getDatabaseProductVersion());
        } catch (Exception e) {
            info.put("jdbcUrl", "<failed>");
            info.put("dbUser", "<failed>");
            info.put("dbProduct", "<failed>");
            info.put("dbVersion", "<failed>");
            info.put("error", e.getMessage());
        }

        info.put("counts", Map.of(
                "member", jdbcTemplate.queryForObject("select count(*) from member", Long.class),
                "kindergarten", jdbcTemplate.queryForObject("select count(*) from kindergarten", Long.class),
                "classroom", jdbcTemplate.queryForObject("select count(*) from classroom", Long.class),
                "kid", jdbcTemplate.queryForObject("select count(*) from kid", Long.class)
        ));

        if (userDetails != null) {
            Map<String, Object> me = jdbcTemplate.queryForMap(
                    "select id, email, role, status, kindergarten_id from member where id = ?",
                    userDetails.getMemberId()
            );
            info.put("me", me);

            Object kindergartenId = me.get("kindergarten_id");
            if (kindergartenId != null) {
                Long kgId = ((Number) kindergartenId).longValue();
                info.put("myKindergarten", jdbcTemplate.queryForMap(
                        "select id, name from kindergarten where id = ?",
                        kgId
                ));
                info.put("myKindergartenCounts", Map.of(
                        "classroom", jdbcTemplate.queryForObject("select count(*) from classroom where kindergarten_id = ?", Long.class, kgId),
                        "kid", jdbcTemplate.queryForObject("select count(*) from kid k join classroom c on c.id = k.classroom_id where c.kindergarten_id = ?", Long.class, kgId)
                ));
            }
        }

        return ResponseEntity.ok(ApiResponse.success(info));
    }
}
