(() => {
    'use strict';

    const { qs, qsa } = window.HirvoUtils;

    const setupPasswordToggles = () => {
        qsa('[data-password-toggle]').forEach((button) => {
            button.addEventListener('click', () => {
                const input = document.getElementById(button.dataset.passwordToggle);
                if (!input) return;
                const visible = input.type === 'text';
                input.type = visible ? 'password' : 'text';
                button.textContent = visible ? 'Show' : 'Hide';
                button.setAttribute('aria-pressed', String(!visible));
                input.focus();
            });
        });
    };

    const setupCharacterCounters = () => {
        qsa('[data-character-count]').forEach((field) => {
            const wrapper = field.closest('.field');
            const counter = qs('[data-character-counter]', wrapper || document);
            if (!counter) return;
            const limit = Number(field.maxLength) > 0 ? field.maxLength : Number(field.dataset.characterLimit || 0);
            const update = () => {
                const length = field.value.length;
                counter.textContent = limit ? `${length}/${limit}` : `${length} characters`;
                counter.classList.toggle('counter--warning', limit > 0 && length >= limit * .85);
                counter.classList.toggle('counter--limit', limit > 0 && length >= limit);
            };
            field.addEventListener('input', update);
            update();
        });
    };

    window.HirvoModules = { ...(window.HirvoModules || {}), setupPasswordToggles, setupCharacterCounters };
})();
