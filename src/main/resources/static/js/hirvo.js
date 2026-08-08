(() => {
    'use strict';

    const modules = window.HirvoModules;
    const initialize = () => {
        modules.setupPublicNavigation();
        modules.setupSidebar();
        modules.setupFilterDrawer();
        modules.setupPasswordToggles();
        modules.setupSaveStates();
        modules.setupConfirmations();
        modules.setupAlerts();
        modules.setupCharacterCounters();
        modules.setupProfileCompleteness();
    };

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initialize, { once: true });
    } else {
        initialize();
    }
})();
