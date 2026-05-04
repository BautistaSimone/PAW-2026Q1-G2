(function () {
    const form = document.querySelector('form.search-form');
    const searchInput = document.getElementById('search-input');
    if (!searchInput) {
        return;
    }

    const urlParams = new URLSearchParams(window.location.search);
    const currentSearch = urlParams.get('search-text');
    if (currentSearch && !searchInput.value) {
        searchInput.value = currentSearch;
    }

    if (!form) {
        return;
    }

    form.addEventListener('submit', function (e) {
        const query = searchInput.value.trim();
        if (!query) {
            e.preventDefault();
        }
    });
})();
