/**
 * 유치원 ERP - 공통 JavaScript
 * HTMX 및 Alpine.js 전역 설정
 */

const CSRF_COOKIE_NAME = 'XSRF-TOKEN';
const CSRF_HEADER_NAME = 'X-XSRF-TOKEN';
const CSRF_UNSAFE_METHODS = new Set(['POST', 'PUT', 'PATCH', 'DELETE']);

function getCookieValue(name) {
    const cookiePrefix = `${name}=`;
    const cookies = document.cookie ? document.cookie.split(';') : [];

    for (const rawCookie of cookies) {
        const cookie = rawCookie.trim();
        if (cookie.startsWith(cookiePrefix)) {
            return decodeURIComponent(cookie.substring(cookiePrefix.length));
        }
    }

    return null;
}

function isSameOrigin(url) {
    try {
        const target = new URL(url, window.location.origin);
        return target.origin === window.location.origin;
    } catch (e) {
        return false;
    }
}

function shouldAttachCsrfToken(method, url) {
    return CSRF_UNSAFE_METHODS.has(method) && isSameOrigin(url);
}

// HTMX 설정
document.addEventListener('htmx:configRequest', function (evt) {
    const method = (evt.detail.verb || 'GET').toUpperCase();
    const url = evt.detail.path || window.location.href;

    if (!shouldAttachCsrfToken(method, url)) {
        return;
    }

    const csrfToken = getCookieValue(CSRF_COOKIE_NAME);
    if (csrfToken) {
        evt.detail.headers[CSRF_HEADER_NAME] = csrfToken;
    }
});

const originalFetch = window.fetch.bind(window);
window.fetch = function (input, init = {}) {
    const method = (init.method || (input instanceof Request ? input.method : 'GET')).toUpperCase();
    const url = input instanceof Request ? input.url : String(input);

    if (!shouldAttachCsrfToken(method, url)) {
        return originalFetch(input, init);
    }

    const csrfToken = getCookieValue(CSRF_COOKIE_NAME);
    if (!csrfToken) {
        return originalFetch(input, init);
    }

    if (input instanceof Request) {
        const headers = new Headers(input.headers);
        if (!headers.has(CSRF_HEADER_NAME)) {
            headers.set(CSRF_HEADER_NAME, csrfToken);
        }
        const requestWithCsrf = new Request(input, {
            ...init,
            headers
        });
        return originalFetch(requestWithCsrf);
    }

    const headers = new Headers(init.headers || {});
    if (!headers.has(CSRF_HEADER_NAME)) {
        headers.set(CSRF_HEADER_NAME, csrfToken);
    }

    return originalFetch(input, {
        ...init,
        headers
    });
};

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
    },

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

        return { isConfirmed: false, value: null };
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
    _pollTimerId: null,

    refresh() {
        if (window.htmx) {
            htmx.trigger(document.body, 'notifications-changed');
        }
    },

    startAutoRefresh(intervalMs = 30000) {
        if (this._pollTimerId) {
            return;
        }

        this._pollTimerId = window.setInterval(() => {
            if (document.visibilityState === 'visible') {
                this.refresh();
            }
        }, intervalMs);

        document.addEventListener('visibilitychange', () => {
            if (document.visibilityState === 'visible') {
                this.refresh();
            }
        });

        window.addEventListener('focus', () => this.refresh());
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

    async markRead(notificationId) {
        if (!notificationId) return;
        await this.requestJson(`/api/v1/notifications/${notificationId}/read`, 'PUT');
        this.refresh();
    },

    async markAllRead() {
        const ok = await window.UI.confirm({
            title: '전체 읽음 처리',
            text: '모든 알림을 읽음 처리할까요?',
            confirmText: '처리',
            cancelText: '취소',
            icon: 'warning'
        });
        if (!ok) return;

        await this.requestJson('/api/v1/notifications/read-all', 'PUT');
        this.refresh();
    },

    async remove(notificationId) {
        if (!notificationId) return;

        const ok = await window.UI.confirm({
            title: '알림 삭제',
            text: '알림을 삭제할까요?',
            confirmText: '삭제',
            cancelText: '취소',
            icon: 'warning'
        });
        if (!ok) return;

        await this.requestJson(`/api/v1/notifications/${notificationId}`, 'DELETE');
        this.refresh();
    }
};

// 페이지 로드 시 실행
document.addEventListener('DOMContentLoaded', function () {
    console.log('유치원 ERP initialized');
    window.Notifications.startAutoRefresh();
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
