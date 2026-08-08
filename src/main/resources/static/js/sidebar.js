(() => {
    'use strict';

    const { qs, qsa, body, focusableSelector, setDrawerState } = window.HirvoUtils;

    const setupPublicNavigation = () => {
        const toggle = qs('[data-nav-toggle]');
        const nav = qs('[data-mobile-nav]');
        if (!toggle || !nav) return;

        const close = () => {
            nav.classList.remove('is-open');
            toggle.setAttribute('aria-expanded', 'false');
        };
        toggle.addEventListener('click', () => {
            const open = nav.classList.toggle('is-open');
            toggle.setAttribute('aria-expanded', String(open));
        });
        qsa('a', nav).forEach((link) => link.addEventListener('click', close));
        document.addEventListener('keydown', (event) => {
            if (event.key === 'Escape') close();
        });
    };

    const setupSidebar = () => {
        const sidebar = qs('[data-sidebar]');
        if (!sidebar) return;
        const overlay = qs('[data-sidebar-overlay]');
        const openButton = qs('[data-sidebar-open]');
        const closeButton = qs('[data-sidebar-close]');
        let restoreFocus = null;

        const open = () => {
            restoreFocus = document.activeElement;
            setDrawerState(sidebar, overlay, true);
            qs(focusableSelector, sidebar)?.focus();
        };
        const close = () => {
            setDrawerState(sidebar, overlay, false);
            if (restoreFocus && typeof restoreFocus.focus === 'function') restoreFocus.focus();
        };
        const trapFocus = (event) => {
            if (!sidebar.classList.contains('is-open') || event.key !== 'Tab') return;
            const items = qsa(focusableSelector, sidebar);
            if (!items.length) return;
            const first = items[0];
            const last = items[items.length - 1];
            if (event.shiftKey && document.activeElement === first) {
                event.preventDefault();
                last.focus();
            } else if (!event.shiftKey && document.activeElement === last) {
                event.preventDefault();
                first.focus();
            }
        };

        openButton?.addEventListener('click', open);
        closeButton?.addEventListener('click', close);
        overlay?.addEventListener('click', close);
        qsa('a', sidebar).forEach((link) => link.addEventListener('click', () => {
            if (window.matchMedia('(max-width: 820px)').matches) close();
        }));
        document.addEventListener('keydown', (event) => {
            if (event.key === 'Escape' && sidebar.classList.contains('is-open')) close();
            trapFocus(event);
        });
    };

    const setupFilterDrawer = () => {
        const panel = qs('[data-filter-panel]');
        const toggle = qs('[data-filter-toggle]');
        const closeButton = qs('[data-filter-close]');
        if (!panel || !toggle) return;
        const close = () => {
            panel.classList.remove('is-open');
            toggle.setAttribute('aria-expanded', 'false');
            body.classList.remove('drawer-open');
        };
        toggle.addEventListener('click', () => {
            const open = panel.classList.toggle('is-open');
            toggle.setAttribute('aria-expanded', String(open));
            body.classList.toggle('drawer-open', open);
            if (open) qs(focusableSelector, panel)?.focus();
        });
        closeButton?.addEventListener('click', close);
        document.addEventListener('keydown', (event) => {
            if (event.key === 'Escape' && panel.classList.contains('is-open')) close();
        });
    };

    window.HirvoModules = {
        ...(window.HirvoModules || {}),
        setupPublicNavigation,
        setupSidebar,
        setupFilterDrawer
    };
})();
