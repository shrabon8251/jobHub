(() => {
    'use strict';

    const { qs, qsa } = window.HirvoUtils;

    const ensureToastRegion = () => {
        let region = qs('[data-toast-region]');
        if (!region) {
            region = document.createElement('div');
            region.className = 'toast-region';
            region.dataset.toastRegion = 'true';
            region.setAttribute('aria-live', 'polite');
            document.body.append(region);
        }
        return region;
    };

    const showToast = (message, type = 'info') => {
        const toast = document.createElement('div');
        toast.className = `toast toast--${type}`;
        toast.setAttribute('role', type === 'error' ? 'alert' : 'status');
        toast.innerHTML = `<span class="toast-dot" aria-hidden="true"></span><span>${message}</span><button type="button" aria-label="Dismiss notification">×</button>`;
        const region = ensureToastRegion();
        region.append(toast);
        const dismiss = () => toast.remove();
        qs('button', toast).addEventListener('click', dismiss);
        window.setTimeout(dismiss, 5200);
    };

    const setupAlerts = () => {
        qsa('.flash-stack .alert').forEach((alert) => {
            const timeout = alert.classList.contains('alert--error') ? 9000 : 6500;
            window.setTimeout(() => alert.remove(), timeout);
        });
    };

    window.HirvoModules = { ...(window.HirvoModules || {}), showToast, setupAlerts };
})();
