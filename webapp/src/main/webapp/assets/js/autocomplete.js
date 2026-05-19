(function () {
    'use strict';

    var MIN_QUERY_LENGTH = 2;
    var MAX_RESULTS = 7;
    var DEBOUNCE_MS = 250;
    var ACTIVE_CLASS = 'vinyland-autocomplete-option-active';

    function normalize(value) {
        return (value || '').trim().toLowerCase();
    }

    function currentQuery(input) {
        return (input.value || '').trim();
    }

    function buildUrl(endpoint, query) {
        var separator = endpoint.indexOf('?') === -1 ? '?' : '&';
        return endpoint + separator + 'q=' + encodeURIComponent(query);
    }

    function sanitizeSuggestions(values) {
        var seen = Object.create(null);
        var result = [];

        if (!Array.isArray(values)) {
            return result;
        }

        values.some(function (value) {
            if (typeof value !== 'string' || value.trim().length === 0) {
                return false;
            }

            var key = normalize(value);
            if (Object.prototype.hasOwnProperty.call(seen, key)) {
                return false;
            }

            seen[key] = true;
            result.push(value);
            return result.length >= MAX_RESULTS;
        });

        return result;
    }

    function dispatchInputEvent(input) {
        var event;
        if (typeof Event === 'function') {
            event = new Event('input', { bubbles: true });
        } else {
            event = document.createEvent('Event');
            event.initEvent('input', true, false);
        }
        input.dispatchEvent(event);
    }

    function initAutocomplete(input) {
        var endpoint = input.getAttribute('data-autocomplete-url');
        var listId = input.getAttribute('data-autocomplete-list');
        var list = listId ? document.getElementById(listId) : null;

        if (!endpoint || !list || typeof window.fetch !== 'function') {
            return;
        }

        var matches = [];
        var activeIndex = -1;
        var debounceTimer = null;
        var requestCounter = 0;
        var activeController = null;
        var suppressNextInputFetch = false;

        function setExpanded(expanded) {
            input.setAttribute('aria-expanded', expanded ? 'true' : 'false');
        }

        function clearList() {
            list.textContent = '';
        }

        function clearPendingRequest() {
            if (debounceTimer) {
                window.clearTimeout(debounceTimer);
                debounceTimer = null;
            }
            if (activeController) {
                activeController.abort();
                activeController = null;
            }
        }

        function closeList() {
            clearList();
            list.hidden = true;
            matches = [];
            activeIndex = -1;
            setExpanded(false);
            input.removeAttribute('aria-activedescendant');
        }

        function cancelAndCloseList() {
            requestCounter += 1;
            clearPendingRequest();
            closeList();
        }

        function setActive(index) {
            var options = Array.prototype.slice.call(list.querySelectorAll('.vinyland-autocomplete-option'));
            if (!options.length) {
                activeIndex = -1;
                input.removeAttribute('aria-activedescendant');
                return;
            }

            activeIndex = (index + options.length) % options.length;
            options.forEach(function (option, optionIndex) {
                var active = optionIndex === activeIndex;
                option.classList.toggle(ACTIVE_CLASS, active);
                option.setAttribute('aria-selected', active ? 'true' : 'false');
                if (active) {
                    input.setAttribute('aria-activedescendant', option.id);
                }
            });
        }

        function selectSuggestion(value) {
            suppressNextInputFetch = true;
            input.value = value;
            cancelAndCloseList();
            dispatchInputEvent(input);
        }

        function focusNextField() {
            var form = input.form;
            if (!form) {
                return;
            }

            var controls = Array.prototype.slice.call(form.querySelectorAll('input, textarea, select, button'))
                .filter(function (control) {
                    var type = (control.getAttribute('type') || '').toLowerCase();
                    return !control.disabled &&
                        !control.hidden &&
                        control.tabIndex !== -1 &&
                        type !== 'hidden' &&
                        control.offsetParent !== null;
                });
            var currentIndex = controls.indexOf(input);
            if (currentIndex >= 0 && currentIndex + 1 < controls.length) {
                controls[currentIndex + 1].focus();
            }
        }

        function renderList() {
            clearList();
            activeIndex = -1;
            input.removeAttribute('aria-activedescendant');

            if (!matches.length) {
                closeList();
                return;
            }

            matches.forEach(function (suggestion, index) {
                var option = document.createElement('div');
                option.className = 'vinyland-autocomplete-option';
                option.id = list.id + '-option-' + index;
                option.setAttribute('role', 'option');
                option.setAttribute('aria-selected', 'false');
                option.tabIndex = -1;
                option.textContent = suggestion;

                option.addEventListener('mousedown', function (ev) {
                    ev.preventDefault();
                });
                option.addEventListener('click', function () {
                    selectSuggestion(suggestion);
                    input.focus();
                });

                list.appendChild(option);
            });

            list.hidden = false;
            setExpanded(true);
        }

        function requestMatches(delay) {
            var query = currentQuery(input);

            if (normalize(query).length < MIN_QUERY_LENGTH) {
                cancelAndCloseList();
                return;
            }

            clearPendingRequest();
            closeList();

            debounceTimer = window.setTimeout(function () {
                var requestId = ++requestCounter;
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

                window.fetch(buildUrl(endpoint, query), options)
                    .then(function (response) {
                        if (!response.ok) {
                            throw new Error('Autocomplete request failed');
                        }
                        return response.json();
                    })
                    .then(function (values) {
                        if (requestId !== requestCounter) {
                            return;
                        }
                        activeController = null;
                        matches = sanitizeSuggestions(values);
                        renderList();
                    })
                    .catch(function (error) {
                        if (error && error.name === 'AbortError') {
                            return;
                        }
                        if (requestId === requestCounter) {
                            activeController = null;
                            closeList();
                        }
                    });
            }, delay);
        }

        input.addEventListener('input', function () {
            if (suppressNextInputFetch) {
                suppressNextInputFetch = false;
                return;
            }
            requestMatches(DEBOUNCE_MS);
        });
        input.addEventListener('focus', function () {
            requestMatches(0);
        });

        input.addEventListener('keydown', function (ev) {
            var menuIsOpen = !list.hidden && matches.length > 0;

            if ((ev.key === 'ArrowDown' || ev.key === 'ArrowUp') && !menuIsOpen) {
                requestMatches(0);
                return;
            }

            if (!menuIsOpen) {
                return;
            }

            if (ev.key === 'ArrowDown') {
                ev.preventDefault();
                setActive(activeIndex < 0 ? 0 : activeIndex + 1);
            } else if (ev.key === 'ArrowUp') {
                ev.preventDefault();
                setActive(activeIndex < 0 ? matches.length - 1 : activeIndex - 1);
            } else if (ev.key === 'Enter') {
                if (activeIndex >= 0) {
                    ev.preventDefault();
                    selectSuggestion(matches[activeIndex]);
                    focusNextField();
                }
            } else if (ev.key === 'Tab') {
                ev.preventDefault();
                setActive(ev.shiftKey
                    ? (activeIndex < 0 ? matches.length - 1 : activeIndex - 1)
                    : (activeIndex < 0 ? 0 : activeIndex + 1)
                );
            } else if (ev.key === 'Escape') {
                ev.preventDefault();
                cancelAndCloseList();
            }
        });

        document.addEventListener('mousedown', function (ev) {
            if (!input.parentNode.contains(ev.target)) {
                cancelAndCloseList();
            }
        });

        input.addEventListener('blur', function () {
            window.setTimeout(function () {
                if (!input.parentNode.contains(document.activeElement)) {
                    cancelAndCloseList();
                }
            }, 0);
        });
    }

    function attachAll() {
        document.querySelectorAll('[data-autocomplete-url]').forEach(initAutocomplete);
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', attachAll);
    } else {
        attachAll();
    }
})();
