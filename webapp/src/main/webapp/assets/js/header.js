(function () {
  var form = document.getElementById("unified-search-form");
  var searchInput = document.getElementById("search-input");
  var modeToggle = document.getElementById("search-mode-toggle");
  var modeLabel = document.getElementById("search-mode-label");
  var modeMenu = document.getElementById("search-mode-menu");

  if (!form || !searchInput || !modeToggle || !modeMenu) {
    return;
  }

  var options = modeMenu.querySelectorAll("[data-mode]");
  var anchorByMode = {
    vinyls: "#vinyls-section",
    users: "#community-section",
  };

  // Determine current mode from the selected option
  function getCurrentMode() {
    var selected = modeMenu.querySelector(".is-selected");
    return selected ? selected.getAttribute("data-mode") : "vinyls";
  }

  // Open / close the dropdown
  function openMenu() {
    modeMenu.classList.add("is-open");
    modeMenu.setAttribute("aria-hidden", "false");
    modeToggle.setAttribute("aria-expanded", "true");
  }

  function closeMenu() {
    modeMenu.classList.remove("is-open");
    modeMenu.setAttribute("aria-hidden", "true");
    modeToggle.setAttribute("aria-expanded", "false");
  }

  modeToggle.addEventListener("click", function (e) {
    e.stopPropagation();
    if (modeMenu.classList.contains("is-open")) {
      closeMenu();
    } else {
      openMenu();
    }
  });

  modeMenu.addEventListener("click", function (e) {
    e.stopPropagation();
  });

  document.addEventListener("click", function () {
    closeMenu();
  });

  // Apply mode changes to form
  function applyMode(newMode) {
    // Update form action
    var actionAttr = newMode === "users" ? "data-users-action" : "data-vinyls-action";
    form.action = form.getAttribute(actionAttr);

    // Update input name
    var paramAttr = newMode === "users" ? "data-users-param" : "data-vinyls-param";
    searchInput.name = form.getAttribute(paramAttr);

    // Update placeholder
    var placeholderAttr = newMode === "users" ? "data-placeholder-users" : "data-placeholder-vinyls";
    searchInput.placeholder = searchInput.getAttribute(placeholderAttr);

    // Update label text
    var selectedOption = modeMenu.querySelector('[data-mode="' + newMode + '"]');
    if (selectedOption && modeLabel) {
      // Get text without the icon
      var textContent = selectedOption.textContent.trim();
      modeLabel.textContent = textContent;
    }

    // Mark selected option
    options.forEach(function (opt) {
      opt.classList.remove("is-selected");
    });
    if (selectedOption) {
      selectedOption.classList.add("is-selected");
    }
  }

  function buildEmptySearchUrl(mode) {
    var actionAttr = mode === "users" ? "data-users-action" : "data-vinyls-action";
    var baseUrl = form.getAttribute(actionAttr) || form.action || "/";
    var anchor = anchorByMode[mode] || "";
    return baseUrl + anchor;
  }

  // Handle option selection
  options.forEach(function (opt) {
    opt.addEventListener("click", function () {
      var newMode = opt.getAttribute("data-mode");
      var currentMode = getCurrentMode();

      if (newMode === currentMode) {
        closeMenu();
        return;
      }

      applyMode(newMode);
      closeMenu();

      var query = searchInput.value.trim();
      if (query) {
        // Has text → submit immediately to the new target
        form.submit();
      } else {
        // No text → navigate to the target page
        window.location.href = buildEmptySearchUrl(newMode);
      }
    });
  });

  // Prevent empty submit
  form.addEventListener("submit", function (e) {
    var query = searchInput.value.trim();
    if (!query) {
      e.preventDefault();
      window.location.href = buildEmptySearchUrl(getCurrentMode());
    }
  });

  // Restore search text from URL if needed
  var urlParams = new URLSearchParams(window.location.search);
  var currentSearch = urlParams.get("search-text") || urlParams.get("q");
  if (currentSearch && !searchInput.value) {
    searchInput.value = currentSearch;
  }
})();

(function () {
  const toggle = document.getElementById("notificationsToggle");
  const panel = document.getElementById("notificationsPanel");

  if (!toggle || !panel) {
    return;
  }

  const openPanel = function () {
    panel.classList.add("is-open");
    panel.setAttribute("aria-hidden", "false");
    toggle.classList.add("is-active");
  };

  const closePanel = function () {
    panel.classList.remove("is-open");
    panel.setAttribute("aria-hidden", "true");
    toggle.classList.remove("is-active");
  };

  toggle.addEventListener("click", function (e) {
    e.stopPropagation();
    if (panel.classList.contains("is-open")) {
      closePanel();
    } else {
      openPanel();
    }
  });

  panel.addEventListener("click", function (e) {
    e.stopPropagation();
  });

  document.addEventListener("click", function () {
    closePanel();
  });

  const urlParams = new URLSearchParams(window.location.search);
  if (urlParams.get("notifOpen") === "1") {
    openPanel();
    // Esto elimina el parametro de la URL para evitar que el panel se abra cuando recargas
    urlParams.delete("notifOpen");
    const queryString = urlParams.toString();
    const nextUrl = queryString ? `?${queryString}` : window.location.pathname;
    window.history.replaceState(null, "", nextUrl);
  }

  const filterButtons = document.querySelectorAll("[data-notif-filter]");
  filterButtons.forEach(function (btn) {
    btn.addEventListener("click", function () {
      const filter = btn.getAttribute("data-notif-filter");
      const params = new URLSearchParams(window.location.search);
      params.set("notifFilter", filter);
      params.set("notifPage", "1");
      params.set("notifOpen", "1");
      window.location.search = params.toString();
    });
  });

  const pageButtons = document.querySelectorAll("[data-notif-page]");
  pageButtons.forEach(function (btn) {
    btn.addEventListener("click", function () {
      const page = btn.getAttribute("data-notif-page");
      const params = new URLSearchParams(window.location.search);
      params.set("notifPage", page);
      params.set("notifOpen", "1");
      window.location.search = params.toString();
    });
  });

  // Localize and format notification timestamps nicely
  document.querySelectorAll(".notification-time").forEach(function (el) {
    const rawDate = el.textContent.trim();
    if (rawDate) {
      try {
        const date = new Date(rawDate);
        if (!isNaN(date.getTime())) {
          el.textContent = date.toLocaleDateString(undefined, {
            day: "2-digit",
            month: "2-digit",
            year: "numeric",
            hour: "2-digit",
            minute: "2-digit",
          });
        }
      } catch (e) {
        // Fallback to original text if parsing fails
      }
    }
  });
})();
