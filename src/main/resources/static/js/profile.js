(() => {
    'use strict';

    const { qs, qsa } = window.HirvoUtils;

    const setupProfileCompleteness = () => {
        const form = qs('[data-profile-completeness-form]');
        const value = qs('[data-completeness-value]');
        const bar = qs('[data-completeness-bar]');
        if (!form || !value || !bar) return;
        const fields = qsa('input:not([type="hidden"]), textarea, select', form);
        const update = () => {
            const complete = fields.filter((field) => field.value.trim().length > 0).length;
            const percent = fields.length ? Math.round((complete / fields.length) * 100) : 0;
            value.textContent = `${percent}% complete`;
            bar.style.width = `${percent}%`;
            bar.parentElement.setAttribute('aria-valuenow', String(percent));
        };
        fields.forEach((field) => field.addEventListener('input', update));
        fields.forEach((field) => field.addEventListener('change', update));
        update();
    };

    window.HirvoModules = { ...(window.HirvoModules || {}), setupProfileCompleteness };
})();
