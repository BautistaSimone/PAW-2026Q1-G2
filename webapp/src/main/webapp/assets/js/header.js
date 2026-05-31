(function () {
  const form = document.querySelector("form.search-form");
  const searchInput = document.getElementById("search-input");
  if (!searchInput) {
    return;
  }

  const urlParams = new URLSearchParams(window.location.search);
  const currentSearch = urlParams.get("search-text");
  if (currentSearch && !searchInput.value) {
    searchInput.value = currentSearch;
  }

  if (!form) {
    return;
  }

  form.addEventListener("submit", function (e) {
    const query = searchInput.value.trim();
    if (!query) {
      e.preventDefault();
    }
  });
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
