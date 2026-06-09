(function () {
    'use strict';

    function initFollowModal(modalEl) {
        var searchUrl = modalEl.getAttribute('data-follow-search-url');
        var targetUserId = modalEl.getAttribute('data-follow-target-id');
        var searchInput = modalEl.querySelector('.follow-modal-search-input');
        var listContainer = modalEl.querySelector('.follow-modal-list');
        var paginationContainer = modalEl.querySelector('.follow-modal-pagination');
        var csrfToken = null;
        var csrfHeader = null;

        // Get CSRF info from a hidden input on the page
        var csrfMeta = document.querySelector('input[name="_csrf"]');
        if (csrfMeta) {
            csrfToken = csrfMeta.value;
            csrfHeader = csrfMeta.getAttribute('name') || 'X-CSRF-TOKEN';
        }

        function buildUserRow(user, isFollowing) {
            var isCurrentUser = function () { return false; };
            var currentUserIdEl = document.querySelector('meta[name="current-user-id"]');
            var currentUserId = currentUserIdEl ? parseInt(currentUserIdEl.getAttribute('content'), 10) : null;

            var row = document.createElement('div');
            row.className = 'user-card-row';
            row.setAttribute('data-user-id', user.id);

            var link = document.createElement('a');
            link.href = window.contextPath ? window.contextPath + '/profile?userId=' + user.id : '/profile?userId=' + user.id;
            link.className = 'user-card-link';

            var avatar = document.createElement('div');
            avatar.className = 'user-card-avatar';
            avatar.textContent = (user.username || '?').charAt(0).toUpperCase();

            var info = document.createElement('div');
            info.className = 'user-card-info';

            var usernameDiv = document.createElement('div');
            usernameDiv.className = 'user-card-username';
            usernameDiv.textContent = user.username || '';

            info.appendChild(usernameDiv);

            if (user.firstName || user.lastName) {
                var nameDiv = document.createElement('div');
                nameDiv.className = 'user-card-name';
                nameDiv.textContent = (user.firstName || '') + ' ' + (user.lastName || '');
                info.appendChild(nameDiv);
            }

            link.appendChild(avatar);
            link.appendChild(info);
            row.appendChild(link);

            // Follow/unfollow button (only if authenticated and not same user)
            var isAuthenticated = document.querySelector('meta[name="is-authenticated"]');
            if (isAuthenticated && isAuthenticated.getAttribute('content') === 'true' && currentUserId !== null && user.id !== currentUserId) {
                var form = document.createElement('form');
                form.action = (window.contextPath || '') + '/profile/follow';
                form.method = 'post';
                form.style.flex = 'none';

                var csrfInput = document.createElement('input');
                csrfInput.type = 'hidden';
                csrfInput.name = '_csrf';
                csrfInput.value = csrfToken || '';
                form.appendChild(csrfInput);

                var userIdInput = document.createElement('input');
                userIdInput.type = 'hidden';
                userIdInput.name = 'userId';
                userIdInput.value = user.id;
                form.appendChild(userIdInput);

                var btn = document.createElement('button');
                btn.type = 'submit';
                if (isFollowing) {
                    btn.className = 'btn btn-retro btn-retro-secondary btn-follow-sm';
                    btn.textContent = 'Unfollow'; // Will be replaced by modal refresh
                } else {
                    btn.className = 'btn btn-retro btn-retro-primary btn-follow-sm';
                    btn.textContent = 'Follow'; // Will be replaced by modal refresh
                }
                form.appendChild(btn);
                row.appendChild(form);
            }

            return row;
        }

        function showEmptyState(message, iconClass) {
            var emptyDiv = document.createElement('div');
            emptyDiv.className = 'empty-products-state follow-modal-empty';
            var icon = document.createElement('i');
            icon.className = iconClass || 'bi bi-people profile-i-4';
            emptyDiv.appendChild(icon);
            var p = document.createElement('p');
            p.className = 'profile-p-5';
            p.textContent = message || '';
            emptyDiv.appendChild(p);
            return emptyDiv;
        }

        function doSearch(query, page) {
            var params = new URLSearchParams();
            params.set('userId', targetUserId);
            params.set('q', query);
            params.set('page', page);

            var url = searchUrl + '?' + params.toString();

            fetch(url, {
                method: 'GET',
                headers: {
                    'Accept': 'application/json'
                }
            })
            .then(function (response) {
                if (!response.ok) {
                    throw new Error('Search failed');
                }
                return response.json();
            })
            .then(function (data) {
                // Clear list
                listContainer.innerHTML = '';

                if (!data.results || data.results.length === 0) {
                    var emptyMsg = query ?
                        'No users match your search' :
                        'No results';
                    var emptyIcon = modalEl.id === 'followersModal' ?
                        'bi bi-people profile-i-4' :
                        'bi bi-person-check profile-i-4';
                    listContainer.appendChild(showEmptyState(emptyMsg, emptyIcon));
                    paginationContainer.innerHTML = '';
                    return;
                }

                // Render each user
                data.results.forEach(function (user) {
                    var followStatus = data.followStatusMap && data.followStatusMap[user.id] ? true : false;
                    var row = buildUserRow(user, followStatus);
                    listContainer.appendChild(row);
                });

                // Render pagination
                if (data.totalPages > 1) {
                    renderPagination(data, query, paginationContainer);
                } else {
                    paginationContainer.innerHTML = '';
                }
            })
            .catch(function (err) {
                listContainer.innerHTML = '';
                paginationContainer.innerHTML = '';
                var errDiv = document.createElement('div');
                errDiv.className = 'empty-products-state';
                var errP = document.createElement('p');
                errP.className = 'profile-p-5';
                errP.textContent = 'An error occurred while searching.';
                errDiv.appendChild(errP);
                listContainer.appendChild(errDiv);
            });
        }

        function renderPagination(data, query, container) {
            container.innerHTML = '';

            if (data.totalPages <= 1) return;

            var nav = document.createElement('nav');
            nav.setAttribute('aria-label', 'Pagination');
            nav.className = 'mt-4 mb-2';

            var ul = document.createElement('ul');
            ul.className = 'pagination justify-content-center';

            // Helper to create a page link
            function addPageLink(label, page, disabled, isActive) {
                var li = document.createElement('li');
                li.className = 'page-item';
                if (disabled) {
                    li.classList.add('disabled');
                }
                if (isActive) {
                    li.classList.add('active');
                    li.setAttribute('aria-current', 'page');
                }

                if (disabled || isActive) {
                    var span = document.createElement('span');
                    span.className = 'page-link';
                    if (isActive) {
                        span.classList.add('pagination-span-1');
                    }
                    span.innerHTML = label;
                    li.appendChild(span);
                } else {
                    var a = document.createElement('a');
                    a.className = 'page-link';
                    a.href = '#';
                    a.innerHTML = label;
                    a.addEventListener('click', function (e) {
                        e.preventDefault();
                        doSearch(query, page);
                    });
                    li.appendChild(a);
                }
                ul.appendChild(li);
            }

            // First page
            addPageLink('&laquo;&laquo;', 1, data.page <= 1, false);

            // Previous page
            addPageLink('&laquo;', data.page - 1, data.page <= 1, false);

            // Page numbers
            for (var i = 1; i <= data.totalPages; i++) {
                addPageLink(i, i, false, i === data.page);
            }

            // Next page
            addPageLink('&raquo;', data.page + 1, data.page >= data.totalPages, false);

            // Last page
            addPageLink('&raquo;&raquo;', data.totalPages, data.page >= data.totalPages, false);

            nav.appendChild(ul);
            container.appendChild(nav);
        }

        // Search on input (debounced)
        var debounceTimer = null;
        searchInput.addEventListener('input', function () {
            clearTimeout(debounceTimer);
            var query = searchInput.value.trim();
            debounceTimer = setTimeout(function () {
                doSearch(query, 1);
            }, 300);
        });

        // Re-search when modal is shown (to reset state)
        modalEl.addEventListener('show.bs.modal', function () {
            searchInput.value = '';
            doSearch('', 1);
        });
    }

    function initFollowModals() {
        var modals = document.querySelectorAll('.profile-modal[data-follow-search-url]');
        modals.forEach(function (modalEl) {
            initFollowModal(modalEl);
        });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initFollowModals);
    } else {
        initFollowModals();
    }
})();