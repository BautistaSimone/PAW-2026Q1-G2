(function () {
    'use strict';

    var MIN_QUERY_LENGTH = 2;
    var MAX_RESULTS = 5;
    var ACTIVE_CLASS = 'vinyland-autocomplete-option-active';

    function normalize(value) {
        return (value || '').trim().toLowerCase();
    }

    function readSuggestions(source) {
        return Array.prototype.slice.call(source.options || [])
            .map(function (option) {
                return option.value;
            })
            .filter(function (value) {
                return value && value.trim().length > 0;
            });
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
        var sourceId = input.getAttribute('data-autocomplete-source');
        var listId = input.getAttribute('data-autocomplete-list');
        var source = sourceId ? document.getElementById(sourceId) : null;
        var list = listId ? document.getElementById(listId) : null;

        if (!source || !list) {
            return;
        }

        var suggestions = readSuggestions(source);
        var matches = [];
        var activeIndex = -1;

        function setExpanded(expanded) {
            input.setAttribute('aria-expanded', expanded ? 'true' : 'false');
        }

        function clearList() {
            list.textContent = '';
        }

        function closeList() {
            clearList();
            list.hidden = true;
            matches = [];
            activeIndex = -1;
            setExpanded(false);
            input.removeAttribute('aria-activedescendant');
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

        function getMatches() {
            var query = normalize(input.value);
            var seen = Object.create(null);
            var result = [];

            if (query.length < MIN_QUERY_LENGTH) {
                return result;
            }

            suggestions.some(function (suggestion) {
                var normalizedSuggestion = normalize(suggestion);
                if (
                    Object.prototype.hasOwnProperty.call(seen, normalizedSuggestion) ||
                    normalizedSuggestion.indexOf(query) === -1
                ) {
                    return false;
                }

                seen[normalizedSuggestion] = true;
                result.push(suggestion);
                return result.length >= MAX_RESULTS;
            });

            return result;
        }

        function selectSuggestion(value) {
            input.value = value;
            closeList();
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
            matches = getMatches();
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

        input.addEventListener('input', renderList);
        input.addEventListener('focus', renderList);

        input.addEventListener('keydown', function (ev) {
            var menuIsOpen = !list.hidden && matches.length > 0;

            if ((ev.key === 'ArrowDown' || ev.key === 'ArrowUp') && !menuIsOpen) {
                renderList();
                menuIsOpen = !list.hidden && matches.length > 0;
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
                closeList();
            }
        });

        document.addEventListener('mousedown', function (ev) {
            if (!input.parentNode.contains(ev.target)) {
                closeList();
            }
        });

        input.addEventListener('blur', function () {
            window.setTimeout(function () {
                if (!input.parentNode.contains(document.activeElement)) {
                    closeList();
                }
            }, 0);
        });
    }

    function attachAll() {
        document.querySelectorAll('[data-autocomplete-source]').forEach(initAutocomplete);
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', attachAll);
    } else {
        attachAll();
    }
})();
