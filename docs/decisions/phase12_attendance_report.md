# Phase 12: 출석 통계/리포트 (Attendance Statistics/Report)

## 개요
- 단계: 출석 통계 및 리포트 시스템 구현
- 목표: 월간/학기별 출석 통계, 개인 출석 리포트, 결석 현황 분석
- 작업: AttendanceStatistics, AttendanceReport 도메인

---

## 1. 통계 개념 설계

### 결정
실시간 통계 조회 (별도 엔티티 없이 쿼리로 계산)

### 이유
1. **데이터 정확성**: 원본 출석 데이터 기반 실시간 계산
2. **저장 공간 절약**: 통계 데이터 별도 저장 불필요
3. **유연성**: 다양한 조건의 통계 조회 가능

### 성능 고려
- 인덱스 활용: (kid_id, date, status) 복합 인덱스
- 캐싱: 월간 통산은 Redis에 1일 캐싱
- 비동기: 대용량 리포트는 비동기 생성

### 변경 이력
- 2026-02-13: 실시간 쿼리 기반 통계 결정

---

## 2. 통계 유형 설계

### 결정
3가지 레벨의 통계 제공

### 통계 레벨
```
1. 유치원 전체 통계 (원장용)
2. 반별 통계 (교사용)
3. 개인별 통계 (학부모용)
```

### 통계 항목
- **출석**: 출석일수, 출석률
- **결석**: 결석일수, 결석률, 결석 사유별 통계
- **지각**: 지각일수, 지각률
- **조퇴**: 조퇴일수
- **미정**: 미체크일수

### 변경 이력
- 2026-02-13: 3레벨 통계 구조 결정

---

## 3. API 설계

### 결정
RESTful API 설계 + 다운로드 API

### 통계 API
| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| GET | /api/v1/attendance/statistics/monthly | 월간 통계 조회 |
| GET | /api/v1/attendance/statistics/semester | 학기별 통계 조회 |
| GET | /api/v1/attendance/statistics/classroom/{id} | 반별 통계 조회 |
| GET | /api/v1/attendance/statistics/kid/{id} | 원생별 통계 조회 |
| GET | /api/v1/attendance/statistics/absence-reason | 결석 사유별 통계 |

### 리포트 다운로드 API
| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| GET | /api/v1/attendance/reports/monthly/download | 월간 리포트 다운로드 (Excel/PDF) |
| GET | /api/v1/attendance/reports/kid/{id}/download | 개인 리포트 다운로드 |
| GET | /api/v1/attendance/reports/classroom/{id}/download | 반별 리포트 다운로드 |

### 조회 파라미터
```
GET /api/v1/attendance/statistics/monthly?year=2026&month=2&classroomId=1
GET /api/v1/attendance/statistics/semester?year=2026&semester=1
```

### 변경 이력
- 2026-02-13: RESTful API + 다운로드 API 설계

---

## 4. 통계 응답 구조

### 결정
계층적 통계 응답 구조

### 응답 예시
```json
{
  "period": {
    "year": 2026,
    "month": 2,
    "startDate": "2026-02-01",
    "endDate": "2026-02-28",
    "totalDays": 20,
    "workingDays": 18
  },
  "summary": {
    "totalKids": 50,
    "averageAttendanceRate": 92.5,
    "totalAbsences": 45,
    "totalLates": 12
  },
  "dailyTrend": [
    {
      "date": "2026-02-01",
      "attendanceCount": 48,
      "absenceCount": 2,
      "lateCount": 1
    }
  ],
  "byClassroom": [
    {
      "classroomId": 1,
      "classroomName": "꽃사랑반",
      "attendanceRate": 94.2,
      "absenceCount": 8
    }
  ],
  "absenceReasonStats": [
    {
      "reason": "ILLNESS",
      "count": 25,
      "percentage": 55.6
    },
    {
      "reason": "FAMILY_EVENT",
      "count": 12,
      "percentage": 26.7
    }
  ]
}
```

### 변경 이력
- 2026-02-13: 계층적 응답 구조 결정

---

## 5. 리포트 생성 설계

### 결정
Apache POI (Excel) + OpenPDF (PDF) 활용

### 리포트 유형
1. **월간 출석부**: 전체 원생 출석 현황 (엑셀)
2. **개인 출석 리포트**: 특정 원생의 월간/학기별 출석 (PDF)
3. **결석 현황 보고서**: 결석자 목록 및 사유 (엑셀)

### Excel 리포트
- 월간 출석부: 날짜별 출석 현황 (O, X, △)
- 색상 코딩: 결석(빨강), 지각(노랑), 출석(초록)
- 필터링: 반별, 기간별

### PDF 리포트
- 개인별 출석 증명서 형태
- 차트 포함: 출석률 파이차트, 월별 추세 선차트
- 서명란 포함

### 변경 이력
- 2026-02-13: Apache POI + OpenPDF 결정

---

## 6. 통계 UI 설계

### 결정
차트 중심의 대시보드 형태

### 화면 구성
1. **요약 카드**: 평균 출석률, 총 결석일, 지각자 수 등
2. **일별 추세 차트**: 라인 차트로 출석 추세
3. **반별 비교 차트**: 바 차트로 반별 출석률 비교
4. **결석 사유 파이차트**: 결석 사유별 비율
5. **상세 테이블**: 원생별 상세 출석 현황

### 필터
- 기간 선택 (월/학기)
- 반 선택
- 원생 검색

### 변경 이력
- 2026-02-13: 차트 중심 대시보드 결정

---

## 7. 학부모용 출석 현황

### 결정
학부모는 자녀의 출석 현황만 조회 가능

### 제공 정보
- **이번 달 출석률**: 퍼센트로 표시
- **출석 달력**: 달력 형태로 출석/결석 표시
- **누적 통계**: 전월 대비 출석률 변화
- **결석 내역**: 결석일 및 사유 목록

### 알림 연동
- 결석일 누적 3일 이상: 학부모에게 알림
- 출석률 80% 미만: 관심 필요 알림

### 변경 이력
- 2026-02-13: 학부모 뷰 분리 결정

---

## 8. 권한 제어

### 결정
역할별 통계 접근 범위 제한

### 권한 규칙
| 통계 | PRINCIPAL | TEACHER | PARENT |
|------|-----------|---------|--------|
| 유치원 전체 | O | X | X |
| 반별 통계 | O | O (담당 반) | X |
| 개인별 통계 | O | O (반 원생) | O (자녀) |
| 리포트 다운로드 | O | O (담당 반) | O (자녀) |

### 변경 이력
- 2026-02-13: 권한 규칙 결정

---

## 면접 예상 질문 답변

### Q: 통계를 어떻게 계산하나요?
> A: 출석 데이터를 실시간으로 쿼리해서 계산합니다. 별도 통계 테이블을 두지 않고 원본 데이터 기반으로 계산해서 데이터 정확성을 보장하며, 인덱스와 캐싱으로 성능을 확보했습니다.

### Q: 대용량 리포트는 어떻게 생성하나요?
> A: 작은 리포트는 동기로 생성하지만, 반 전체 월간 출석부 같은 대용량 리포트는 비동기로 생성하고 완료 후 알림을 발송합니다. PDF/Excel 생성은 Apache POI와 OpenPDF를 사용합니다.

### Q: 학부모는 어떤 정보를 볼 수 있나요?
> A: 학부모는 자녀의 출석 현황만 조회할 수 있습니다. 이번 달 출석률, 출석 달력, 결석 내역 등을 제공하며, 결석이 3일 이상 누적되면 자동으로 알림을 발송합니다.

---

## 구현 체크리스트

### 백엔드
- [ ] 통계 조회 쿼리 구현 (QueryDSL)
- [ ] AttendanceStatisticsService 구현
- [ ] AttendanceStatisticsController 구현
- [ ] Excel 리포트 생성 서비스 구현
- [ ] PDF 리포트 생성 서비스 구현
- [ ] 인덱스 추가: (kid_id, date, status)

### 프론트엔드
- [ ] 통계 대시보드 페이지 (/attendance/statistics) 생성
- [ ] 차트 라이브러리 연동 (Chart.js)
- [ ] 요약 카드 UI 구현
- [ ] 일별 추세 차트 구현
- [ ] 반별 비교 차트 구현
- [ ] 결석 사유 차트 구현
- [ ] 리포트 다운로드 버튼 구현
- [ ] 학부모용 출석 현황 페이지 구현

### DB 마이그레이션
- [ ] attendance 테이블 인덱스 추가

### 라이브러리
- [ ] Apache POI 의존성 추가 (Excel)
- [ ] OpenPDF 의존성 추가 (PDF)

---

## 다음 단계
테스트 및 안정화, 기술적 개선

---

**Phase 12 설계일: 2026-02-13**
