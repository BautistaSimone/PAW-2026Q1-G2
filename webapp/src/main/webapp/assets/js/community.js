(function () {
    'use strict';

    var FOLLOW_SCROLL_KEY = 'vinyland.community.followScrollY';

    function parseNonNegativeInt(value) {
        var parsed = parseInt(value, 10);
        return Number.isFinite(parsed) && parsed >= 0 ? parsed : 0;
    }

    function restoreFollowScrollPosition() {
        var storedValue = null;
        try {
            storedValue = window.sessionStorage.getItem(FOLLOW_SCROLL_KEY);
            window.sessionStorage.removeItem(FOLLOW_SCROLL_KEY);
        } catch (error) {
            return;
        }

        var scrollY = parseNonNegativeInt(storedValue);
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

    function getCarouselScrollAmount(track) {
        var tile = track.querySelector('.community-product-tile, .community-carousel-more-tile');
        if (!tile) {
            return Math.max(Math.round(track.clientWidth * 0.85), 160);
        }

        var gap = 0;
        var styles = window.getComputedStyle(track);
        if (styles) {
            gap = parseFloat(styles.columnGap || styles.gap || '0') || 0;
        }
        var tileStep = tile.getBoundingClientRect().width + gap;
        return Math.max(Math.round(track.clientWidth * 0.85), Math.round(tileStep * 3));
    }

    function updateCarouselControls(track, prev, next) {
        var maxScrollLeft = Math.max(track.scrollWidth - track.clientWidth, 0);
        var atStart = track.scrollLeft <= 1;
        var atEnd = track.scrollLeft >= maxScrollLeft - 1;

        prev.disabled = atStart;
        next.disabled = atEnd;
    }

    function scrollCarousel(track, delta) {
        var maxScrollLeft = Math.max(track.scrollWidth - track.clientWidth, 0);
        var nextScrollLeft = Math.max(0, Math.min(track.scrollLeft + delta, maxScrollLeft));
        track.scrollLeft = nextScrollLeft;
    }

    function attachCarouselControls() {
        document.querySelectorAll('.community-carousel').forEach(function (carousel) {
            var track = carousel.querySelector('[data-carousel-scroll-track]');
            var prev = carousel.querySelector('[data-carousel-scroll-prev]');
            var next = carousel.querySelector('[data-carousel-scroll-next]');

            if (!track || !prev || !next) {
                return;
            }

            function update() {
                updateCarouselControls(track, prev, next);
            }

            prev.addEventListener('click', function () {
                scrollCarousel(track, -getCarouselScrollAmount(track));
            });

            next.addEventListener('click', function () {
                scrollCarousel(track, getCarouselScrollAmount(track));
            });

            track.addEventListener('scroll', update, { passive: true });
            window.addEventListener('resize', update);
            update();
        });
    }

    function attachCommunityBehaviors() {
        restoreFollowScrollPosition();
        attachFollowScrollMemory();
        attachCarouselControls();
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', attachCommunityBehaviors);
    } else {
        attachCommunityBehaviors();
    }
})();
