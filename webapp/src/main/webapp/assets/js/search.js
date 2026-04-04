(function () {
    const form = document.querySelector('form.navbar-search');
    const searchInput = document.getElementById('search-input');

    if (!form || !searchInput) return;

    // Prevent empty search submissions
    form.addEventListener('submit', function (e) {
        const query = searchInput.value.trim();
        if (!query) {
            e.preventDefault();  // stop submission
            // TODO: Error handling
        }
    });

    const searchButton = document.getElementById('search-button');
    searchButton.addEventListener('click', function () {
        const query = searchInput.value.trim();
        if (!query) {
            // TODO: Error handling
            searchInput.focus();
        }
    });
})();