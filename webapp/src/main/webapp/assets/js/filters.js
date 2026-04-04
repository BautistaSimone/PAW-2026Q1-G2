(function () {
    var form = document.querySelector('form.filters-bar');
    if (!form) {
        return;
    }

    function submitFilters() {
        if (typeof form.requestSubmit === 'function') {
            form.requestSubmit();
        } else {
            form.submit();
        }
    }

    form.addEventListener('change', function () {
        submitFilters();
    });

    document.querySelectorAll('.price-preset-btn').forEach(function (btn) {
        btn.addEventListener('click', function () {
            var minEl = document.getElementById('filterMinPrice');
            var maxEl = document.getElementById('filterMaxPrice');
            var dMin = btn.getAttribute('data-min');
            var dMax = btn.getAttribute('data-max');
            if (minEl) {
                minEl.value = dMin !== null && dMin !== '' ? dMin : '';
            }
            if (maxEl) {
                maxEl.value = dMax !== null && dMax !== '' ? dMax : '';
            }
            submitFilters();
        });
    });
})();
