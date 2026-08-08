(() => {
    'use strict';

    const qs = (selector, root = document) => root.querySelector(selector);
    const qsa = (selector, root = document) => [...root.querySelectorAll(selector)];
    const body = document.body;
    const focusableSelector = 'a[href], button:not([disabled]), input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])';

    const setDrawerState = (drawer, overlay, open) => {
        if (!drawer) return;
        drawer.classList.toggle('is-open', open);
        drawer.setAttribute('aria-hidden', String(!open));
        overlay?.classList.toggle('is-visible', open);
        body.classList.toggle('drawer-open', open);
    };

    window.HirvoUtils = { qs, qsa, body, focusableSelector, setDrawerState };
})();
