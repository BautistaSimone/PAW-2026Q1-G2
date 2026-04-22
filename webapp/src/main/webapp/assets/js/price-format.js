(function (window) {
    'use strict';

    function onlyDigits(value) {
        return (value || '').replace(/\D/g, '');
    }

    function formatInteger(value) {
        return onlyDigits(value).replace(/\B(?=(\d{3})+(?!\d))/g, '.');
    }

    function splitDecimal(value) {
        var text = (value || '').toString().trim();
        var commaIndex = text.indexOf(',');
        if (commaIndex >= 0) {
            return {
                integer: text.slice(0, commaIndex),
                decimal: text.slice(commaIndex + 1),
                hasDecimalSeparator: true
            };
        }

        var dotDecimal = text.match(/^([\d.]+)\.(\d{1,2})$/);
        if (dotDecimal) {
            return {
                integer: dotDecimal[1],
                decimal: dotDecimal[2],
                hasDecimalSeparator: true
            };
        }

        return {
            integer: text,
            decimal: '',
            hasDecimalSeparator: false
        };
    }

    function normalizePrice(value) {
        var text = (value || '').toString();
        var parts = splitDecimal(text);
        var integerDigits = onlyDigits(parts.integer);
        var decimalDigits = onlyDigits(parts.decimal).slice(0, 2);
        var formatted = formatInteger(integerDigits);
        var raw = integerDigits;

        if (parts.hasDecimalSeparator && (decimalDigits.length > 0 || text.charAt(text.length - 1) === ',')) {
            formatted += ',' + decimalDigits;
        }

        if (decimalDigits.length > 0) {
            raw += '.' + decimalDigits;
        }

        return {
            formatted: formatted,
            raw: raw
        };
    }

    function attachFormattedPriceInput(displayInput, rawInput) {
        if (!displayInput) {
            return;
        }

        function syncFromDisplay() {
            var normalized = normalizePrice(displayInput.value);
            displayInput.value = normalized.formatted;
            if (rawInput) {
                rawInput.value = normalized.raw;
            }
        }

        function syncFromRaw() {
            if (!rawInput || rawInput.value === '') {
                syncFromDisplay();
                return;
            }
            displayInput.value = normalizePrice(rawInput.value).formatted;
        }

        displayInput.addEventListener('input', syncFromDisplay);
        syncFromRaw();

        return syncFromDisplay;
    }

    window.VinylandPriceFormat = {
        normalizePrice: normalizePrice,
        attachFormattedPriceInput: attachFormattedPriceInput
    };
})(window);
