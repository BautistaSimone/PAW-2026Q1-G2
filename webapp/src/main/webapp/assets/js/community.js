(function () {
    'use strict';

    var FOLLOW_SCROLL_KEY = 'vinyland.community.followScrollY';

    function parsePositiveInt(value, fallback) {
        var parsed = parseInt(value, 10);
        return Number.isFinite(parsed) && parsed > 0 ? parsed : fallback;
    }

    function buildPageUrl(endpoint, page) {
        var separator = endpoint.indexOf('?') === -1 ? '?' : '&';
        return endpoint + separator + 'page=' + encodeURIComponent(page);
    }

    function createIcon(className) {
        var icon = document.createElement('i');
        icon.className = className;
        icon.setAttribute('aria-hidden', 'true');
        return icon;
    }

    function createProductTile(product) {
        var link = document.createElement('a');
        link.className = 'community-product-tile';
        link.href = product.href || '#';

        var cover = document.createElement('div');
        cover.className = 'community-product-cover';

        if (product.imageUrl) {
            var img = document.createElement('img');
            img.src = product.imageUrl;
            img.alt = [product.artist, product.title].filter(Boolean).join(' - ');
            cover.appendChild(img);
        } else {
            cover.appendChild(createIcon('bi bi-vinyl'));
        }

        var body = document.createElement('div');
        body.className = 'community-product-body';

        var title = document.createElement('h3');
        title.textContent = product.title || '';

        var artist = document.createElement('p');
        artist.textContent = product.artist || '';

        var price = document.createElement('span');
        price.textContent = product.priceLabel || '';

        body.appendChild(title);
        body.appendChild(artist);
        body.appendChild(price);
        link.appendChild(cover);
        link.appendChild(body);

        return link;
    }

    function initCarousel(root) {
        var endpoint = root.getAttribute('data-endpoint');
        var track = root.querySelector('[data-carousel-track]');
        var prev = root.querySelector('[data-carousel-prev]');
        var next = root.querySelector('[data-carousel-next]');
        var status = root.querySelector('[data-carousel-status]');
        var loading = root.querySelector('[data-carousel-loading]');

        if (!endpoint || !track || !prev || !next || !status || typeof window.fetch !== 'function') {
            return;
        }

        var currentPage = parsePositiveInt(root.getAttribute('data-current-page'), 1);
        var totalPages = parsePositiveInt(root.getAttribute('data-total-pages'), 1);
        var emptyMessage = root.getAttribute('data-empty-message') || '';
        var errorMessage = root.getAttribute('data-error-message') || '';
        var loadingMessage = root.getAttribute('data-loading-message') || '';
        var pageLabel = root.getAttribute('data-page-label') || '';
        var ofLabel = root.getAttribute('data-of-label') || '';
        var activeController = null;

        function updateStatus() {
            status.textContent = pageLabel + ' ' + currentPage + ' ' + ofLabel + ' ' + totalPages;
        }

        function setLoading(isLoading) {
            root.classList.toggle('community-carousel-is-loading', isLoading);
            if (loading) {
                loading.hidden = !isLoading;
                loading.querySelector('span:last-child').textContent = loadingMessage;
            }
            prev.disabled = isLoading || currentPage <= 1;
            next.disabled = isLoading || currentPage >= totalPages;
        }

        function renderMessage(message, modifier) {
            track.textContent = '';
            var box = document.createElement('div');
            box.className = 'community-carousel-message ' + modifier;
            box.textContent = message;
            track.appendChild(box);
        }

        function renderProducts(products) {
            track.textContent = '';
            if (!Array.isArray(products) || products.length === 0) {
                renderMessage(emptyMessage, 'community-carousel-message-empty');
                return;
            }

            products.forEach(function (product) {
                track.appendChild(createProductTile(product || {}));
            });
        }

        function requestPage(page) {
            if (page < 1 || page > totalPages) {
                return;
            }

            if (activeController) {
                activeController.abort();
                activeController = null;
            }

            var options = {
                headers: {
                    'Accept': 'application/json'
                },
                credentials: 'same-origin'
            };

            if (typeof window.AbortController === 'function') {
                activeController = new AbortController();
                options.signal = activeController.signal;
            }

            setLoading(true);

            window.fetch(buildPageUrl(endpoint, page), options)
                .then(function (response) {
                    var contentType = response.headers.get('Content-Type') || '';
                    if (!response.ok || contentType.indexOf('application/json') === -1) {
                        throw new Error('Invalid carousel response');
                    }
                    return response.json();
                })
                .then(function (payload) {
                    activeController = null;
                    currentPage = parsePositiveInt(payload.currentPage, page);
                    totalPages = parsePositiveInt(payload.totalPages, 1);
                    root.setAttribute('data-current-page', String(currentPage));
                    root.setAttribute('data-total-pages', String(totalPages));
                    renderProducts(payload.products);
                    updateStatus();
                    setLoading(false);
                })
                .catch(function (error) {
                    if (error && error.name === 'AbortError') {
                        return;
                    }
                    activeController = null;
                    renderMessage(errorMessage, 'community-carousel-message-error');
                    setLoading(false);
                });
        }

        prev.addEventListener('click', function () {
            requestPage(currentPage - 1);
        });

        next.addEventListener('click', function () {
            requestPage(currentPage + 1);
        });

        updateStatus();
        setLoading(false);
    }

    function attachCarousels() {
        document.querySelectorAll('[data-community-carousel]').forEach(initCarousel);
    }

    function restoreFollowScrollPosition() {
        var storedValue = null;
        try {
            storedValue = window.sessionStorage.getItem(FOLLOW_SCROLL_KEY);
            window.sessionStorage.removeItem(FOLLOW_SCROLL_KEY);
        } catch (error) {
            return;
        }

        var scrollY = parsePositiveInt(storedValue, 0);
        if (scrollY <= 0) {
            return;
        }

        window.requestAnimationFrame(function () {
            window.scrollTo({ top: scrollY, left: 0, behavior: 'auto' });
        });
    }

    function attachFollowScrollMemory() {
        document.querySelectorAll('.community-follow-form').forEach(function (form) {
            form.addEventListener('submit', function () {
                try {
                    window.sessionStorage.setItem(FOLLOW_SCROLL_KEY, String(window.scrollY || window.pageYOffset || 0));
                } catch (error) {
                    // Storage can be disabled; the follow action still works normally.
                }
            });
        });
    }

    function attachCommunityBehaviors() {
        restoreFollowScrollPosition();
        attachCarousels();
        attachFollowScrollMemory();
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', attachCommunityBehaviors);
    } else {
        attachCommunityBehaviors();
    }
})();
