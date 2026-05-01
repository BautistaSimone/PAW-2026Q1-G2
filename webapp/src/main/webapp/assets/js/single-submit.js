/**
 * single-submit.js
 * Prevents double-submission on critical action forms.
 *
 * Usage: add data-single-submit="true" to any <form>.
 * The submit button will be disabled and its text replaced with a spinner
 * the moment the user submits, preventing repeated clicks.
 */
(function () {
    'use strict';

    function initSingleSubmit(form) {
        var submitted = false;
        form.addEventListener('submit', function (ev) {
            if (submitted) {
                ev.preventDefault();
                return;
            }
            // Find the primary submit button inside this form
            var btn = form.querySelector('button[type="submit"]');
            if (btn) {
                submitted = true;
                btn.disabled = true;
                // Keep original width so layout doesn't jump
                btn.style.minWidth = btn.offsetWidth + 'px';
                btn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>';
            } else {
                submitted = true;
            }
        });
    }

    function attachAll() {
        document.querySelectorAll('form[data-single-submit]').forEach(function (form) {
            initSingleSubmit(form);
        });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', attachAll);
    } else {
        attachAll();
    }
})();
