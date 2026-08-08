(() => {
    'use strict';

    const { qs, qsa, body } = window.HirvoUtils;

    const setupConfirmations = () => {
        const triggers = qsa('[data-confirm]');
        if (!triggers.length) return;
        const dialog = document.createElement('dialog');
        dialog.className = 'confirm-dialog';
        dialog.innerHTML = '<div class="confirm-dialog-card"><span class="confirm-dialog-icon">!</span><h2>Are you sure?</h2><p data-confirm-message></p><div class="confirm-dialog-actions"><button type="button" class="button button--secondary" data-confirm-cancel>Cancel</button><button type="button" class="button button--danger" data-confirm-continue>Continue</button></div></div>';
        document.body.append(dialog);
        let pending = null;

        const close = () => {
            if (typeof dialog.close === 'function' && dialog.open) dialog.close();
            dialog.classList.remove('is-open');
            body.classList.remove('modal-open');
            if (pending && typeof pending.focus === 'function') pending.focus();
            pending = null;
        };
        const open = (trigger) => {
            pending = trigger;
            qs('[data-confirm-message]', dialog).textContent = trigger.dataset.confirm;
            body.classList.add('modal-open');
            if (typeof dialog.showModal === 'function') dialog.showModal();
            dialog.classList.add('is-open');
            qs('[data-confirm-cancel]', dialog).focus();
        };

        document.addEventListener('click', (event) => {
            const trigger = event.target.closest('[data-confirm]');
            if (!trigger || trigger.dataset.confirmed === 'true') return;
            event.preventDefault();
            open(trigger);
        });
        qs('[data-confirm-cancel]', dialog).addEventListener('click', close);
        qs('[data-confirm-continue]', dialog).addEventListener('click', () => {
            if (!pending) return;
            const trigger = pending;
            close();
            trigger.dataset.confirmed = 'true';
            trigger.click();
            delete trigger.dataset.confirmed;
        });
        dialog.addEventListener('cancel', (event) => {
            event.preventDefault();
            close();
        });
        dialog.addEventListener('click', (event) => {
            if (event.target === dialog) close();
        });
    };

    window.HirvoModules = { ...(window.HirvoModules || {}), setupConfirmations };
})();
