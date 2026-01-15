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

// SweetAlert2 기반 공통 UI
window.UI = window.UI || {
    hasSwal() {
        return typeof window.Swal !== 'undefined';
    },

    async alert({ title = '알림', text = '', icon = 'info' } = {}) {
        if (this.hasSwal()) {
            await window.Swal.fire({
                title,
                text,
                icon,
                confirmButtonText: '확인',
                customClass: {
                    popup: 'rounded-2xl',
                    confirmButton: 'px-4 py-2 rounded-lg bg-primary-600 text-white font-medium',
                },
                buttonsStyling: false,
            });
            return;
        }

        window.alert(text ? `${title}\n\n${text}` : title);
    },

    async success(message, title = '완료') {
        return this.alert({ title, text: message, icon: 'success' });
    },

    async error(message, title = '오류') {
        return this.alert({ title, text: message, icon: 'error' });
    },

    async confirm(options) {
        if (this.hasSwal()) {
            const result = await window.Swal.fire(options);

            console.log('[DEBUG] SweetAlert version:', window.Swal.version);
            console.log('[DEBUG] Confirm result:', result);

            // SweetAlert2 v10 이하에서 result.isConfirmed 사용
            // SweetAlert2 v11 이상에서 result.value.isConfirmed 사용
            const isConfirmed = window.Swal.version && window.Swal.version.startsWith('11')
                ? result.value?.isConfirmed !== false
                : result.isConfirmed !== false;

            console.log('[DEBUG] Confirm isConfirmed:', isConfirmed);

            return isConfirmed;
        }

        return window.confirm(options.text || options);
    }

    async promptTextarea({
        title,
        label,
        placeholder = '',
        confirmText = '확인',
        cancelText = '취소',
        required = false,
    } = {}) {
        if (this.hasSwal()) {
            const result = await window.Swal.fire({
                title,
                input: 'textarea',
                inputLabel: label,
                inputPlaceholder: placeholder,
                inputAttributes: {
                    autocapitalize: 'off'
                },
                showCancelButton: true,
                confirmButtonText: confirmText,
                cancelButtonText: cancelText,
                inputValidator: (value) => {
                    if (required && (!value || value.trim() === '')) {
                        return '내용을 입력해 주세요.';
                    }
                    return undefined;
                },
                customClass: {
                    popup: 'rounded-2xl',
                    confirmButton: 'px-4 py-2 rounded-lg bg-primary-600 text-white font-medium',
                    cancelButton: 'px-4 py-2 rounded-lg bg-gray-100 text-gray-700 font-medium',
                },
                buttonsStyling: false,
            });

            return result;
        }

        const value = window.prompt(title, '');
        return { isConfirmed: value !== null, value };
    },

    async promptSelect({
        title,
        label,
        options,
        placeholder = '선택해 주세요',
        confirmText = '확인',
        cancelText = '취소',
        required = true,
    } = {}) {
        if (this.hasSwal()) {
            const result = await window.Swal.fire({
                title,
                input: 'select',
                inputLabel: label,
                inputOptions: options,
                inputPlaceholder: placeholder,
                showCancelButton: true,
                confirmButtonText: confirmText,
                cancelButtonText: cancelText,
                inputValidator: (value) => {
                    if (required && (!value || value === '')) {
                        return '항목을 선택해 주세요.';
                    }
                    return undefined;
                },
                customClass: {
                    popup: 'rounded-2xl',
                    confirmButton: 'px-4 py-2 rounded-lg bg-primary-600 text-white font-medium',
                    cancelButton: 'px-4 py-2 rounded-lg bg-gray-100 text-gray-700 font-medium',
                },
                buttonsStyling: false,
            });

            return result;
        }

        return { isConfirmed: false, value: null };
    }
};

// 알림 (공통)
window.Notifications = window.Notifications || {
    refresh() {
        if (window.htmx) {
            htmx.trigger(document.body, 'notifications-changed');
        }
    },

    async requestJson(url, method, body) {
        const response = await fetch(url, {
            method,
            credentials: 'same-origin',
            headers: {
                'Content-Type': 'application/json'
            },
            body: body ? JSON.stringify(body) : undefined
        });

        const payload = await response.json().catch(() => ({}));
        if (!response.ok) {
            throw new Error(payload.message || '요청이 실패했습니다');
        }
        return payload;
    },

    async open(notificationId, linkUrl) {
        if (notificationId) {
            await this.requestJson(`/api/v1/notifications/${notificationId}/read`, 'PUT');
            this.refresh();
        }

        if (linkUrl && linkUrl !== 'null' && linkUrl !== 'undefined' && linkUrl.trim() !== '') {
            window.location.href = linkUrl;
        }
    },

    async markAllRead() {
        await this.requestJson('/api/v1/notifications/read-all', 'PUT');
        this.refresh();
    }
};

// 페이지 로드 시 실행
document.addEventListener('DOMContentLoaded', function () {
    console.log('유치원 ERP initialized');
});

document.addEventListener('submit', async (e) => {
    const form = e.target;
    if (!(form instanceof HTMLFormElement)) return;

    const confirmMessage = form.dataset.uiConfirm;
    if (!confirmMessage) return;

    if (form.dataset.uiConfirmed === 'true') return;

    e.preventDefault();

    const ok = await window.UI.confirm({
        title: '확인',
        text: confirmMessage,
        confirmText: '계속',
        cancelText: '취소',
        icon: 'warning'
    });

    if (!ok) return;

    form.dataset.uiConfirmed = 'true';
    form.submit();
});
