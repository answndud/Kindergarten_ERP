/**
 * 유치원 ERP - 공통 JavaScript
 * HTMX 및 Alpine.js 전역 설정
 */

// HTMX 설정
document.addEventListener('htmx:configRequest', function (evt) {
    // 모든 요청에 CSRF 토큰 추가 (추후 Spring Security 적용 시)
    // evt.detail.headers['X-CSRF-TOKEN'] = csrfToken;
});

// HTMX 이벤트 로깅 (개발용)
document.addEventListener('htmx:beforeRequest', function (evt) {
    console.log('HTMX Request:', evt.detail.xhr);
});

document.addEventListener('htmx:afterRequest', function (evt) {
    console.log('HTMX Response:', evt.detail.xhr);
});

document.addEventListener('htmx:responseError', function (evt) {
    console.error('HTMX Error:', evt.detail.xhr);
    // 에러 처리 (예: 토스트 메시지 표시)
});

// Alpine.js 전역 데이터
document.addEventListener('alpine:init', () => {
    // 전역 유틸리티 함수
    Alpine.store('utils', {
        formatDate(date) {
            if (!date) return '';
            return new Date(date).toLocaleDateString('ko-KR');
        },
        formatDateTime(date) {
            if (!date) return '';
            return new Date(date).toLocaleString('ko-KR');
        }
    });
});

// 공통 함수
const API = {
    // 에러 메시지 표시 (HTMX + Alpine)
    showError(message) {
        Alpine.store('toast').show(message, 'error');
    },

    // 성공 메시지 표시
    showSuccess(message) {
        Alpine.store('toast').show(message, 'success');
    }
};

// 페이지 로드 시 실행
document.addEventListener('DOMContentLoaded', function () {
    console.log('유치원 ERP initialized');
});
