(() => {
    'use strict';

    const { qsa } = window.HirvoUtils;

    const setupSaveStates = () => {
        qsa('[data-save-button]').forEach((button) => {
            button.setAttribute('aria-pressed', 'false');
            button.addEventListener('click', () => {
                const saved = button.classList.toggle('is-saved');
                button.textContent = saved ? '♥' : '♡';
                button.setAttribute('aria-pressed', String(saved));
                button.setAttribute('aria-label', saved ? 'Remove saved job' : 'Save job');
                window.HirvoModules.showToast(saved ? 'Saved in this view.' : 'Removed from this view.', saved ? 'success' : 'info');
            });
        });
    };

    window.HirvoModules = { ...(window.HirvoModules || {}), setupSaveStates };
})();
