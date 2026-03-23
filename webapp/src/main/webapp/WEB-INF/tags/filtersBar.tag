<%@ tag language="java" pageEncoding="UTF-8" %>

<div class="filters-bar">
    <div class="filters-header">
        <h3 class="filters-title">Filtros</h3>
        <button class="clear-filters-btn" type="button">Limpiar todo</button>
    </div>

    <div class="filter-section">
        <button class="filter-section-header" type="button">
            <div class="filter-section-title">
                <i class="bi bi-currency-dollar" aria-hidden="true"></i>
                <span>Precio</span>
            </div>
            <i class="bi bi-chevron-up" aria-hidden="true"></i>
        </button>

        <div class="filter-options price-inputs">
            <div class="price-input-group">
                <label for="minPrice" class="price-label">Precio minimo</label>
                <input id="minPrice" type="number" class="price-input" placeholder="Min" />
            </div>
            <div class="price-input-group">
                <label for="maxPrice" class="price-label">Precio maximo</label>
                <input id="maxPrice" type="number" class="price-input" placeholder="Max" />
            </div>
        </div>
    </div>

    <div class="filter-section">
        <button class="filter-section-header" type="button">
            <div class="filter-section-title">
                <i class="bi bi-music-note" aria-hidden="true"></i>
                <span>Genero</span>
            </div>
            <i class="bi bi-chevron-up" aria-hidden="true"></i>
        </button>

        <div class="filter-options">
            <label class="filter-option">
                <input type="checkbox" />
                <span>Bandas Internacionales</span>
            </label>
            <label class="filter-option">
                <input type="checkbox" />
                <span>Solistas Masculinos</span>
            </label>
            <label class="filter-option">
                <input type="checkbox" />
                <span>Solistas Femeninas</span>
            </label>
            <label class="filter-option">
                <input type="checkbox" />
                <span>Jazz</span>
            </label>
            <label class="filter-option">
                <input type="checkbox" />
                <span>Musica Nacional</span>
            </label>
            <label class="filter-option">
                <input type="checkbox" />
                <span>Musica Brasilera</span>
            </label>
            <label class="filter-option">
                <input type="checkbox" />
                <span>Otros</span>
            </label>
        </div>
    </div>
</div>
