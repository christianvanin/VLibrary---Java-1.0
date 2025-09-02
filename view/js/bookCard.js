let allBooks = [];

function addBookFromJava(book) {
    allBooks.push(book);
    renderBooks(allBooks);
}

JavaBridge.register("addBook", function(payload) {
    if (typeof payload === "string") {
        try { payload = JSON.parse(payload); }
        catch(e) { 
            window.JavaBridge.send("print logjs JSON parsing error in addBook: " + e.message);
            return;
        }
    }
    if (payload && payload.title && payload.subject && payload.cover && payload.code) {
        addBookFromJava(payload);
    }
});

JavaBridge.register("addBooks", function(payload) {
    try {
        if (typeof payload === "string") payload = JSON.parse(payload);
        if (Array.isArray(payload)) {
            allBooks.push(...payload);
            renderBooks(allBooks);
        }
    } catch(e) {
        window.JavaBridge.send("print logjs parsing error in addBooks: " + e.message);
    }
});

JavaBridge.register("clearBooks", function() {
    allBooks = [];
    renderBooks(allBooks);
});

function renderBooks(list) {
    const grid = document.querySelector("#content-area .books-grid");
    if (!grid) return;
    grid.innerHTML = "";
    list.forEach(book => {
        const card = createBookCard(book);
        if (card) grid.appendChild(card);
    });
    updateBooksCount(list.length);
    updateDropdowns(allBooks);
}

function createBookCard(book) {
    const card = document.createElement("div");
    card.className = "book-card";

    const img = document.createElement("img");
    img.src = book.cover;
    img.alt = book.title;

    const header = document.createElement("div");
    header.className = "book-header";
    const title = document.createElement("div");
    title.className = "book-title";
    title.textContent = book.title;
    header.appendChild(title);

    const footer = document.createElement("div");
    footer.className = "book-footer";
    const subject = document.createElement("div");
    subject.className = "book-subject";
    subject.textContent = book.subject;
    const author = document.createElement("div");
    author.className = "book-author";
    author.textContent = book.author;
    footer.appendChild(subject);
    footer.appendChild(author);

    card.appendChild(img);
    card.appendChild(header);
    card.appendChild(footer);

    card.addEventListener("click", () => {
        if (book.code) {
            window.JavaBridge.send("book set " + book.code);
            window.JavaBridge.send("setting pdfview");
        }
        
    });

    return card;
}

function updateBooksCount(count) {
    const countElem = document.getElementById("books-count");
    if (countElem) countElem.textContent = count;
}

function populateDropdown(dropdownId, options) {
    const container = document.getElementById(dropdownId);
    container.innerHTML = '';

    const allLink = document.createElement('a');
    allLink.href = "#";
    allLink.textContent = "Tutti";
    allLink.dataset.value = "Tutti";
    allLink.classList.add("selected");
    allLink.onclick = e => e.preventDefault();
    container.appendChild(allLink);

    options.items.forEach(item => {
        const link = document.createElement('a');
        link.href = "#";
        link.textContent = item.label;
        link.dataset.value = item.value;
        link.addEventListener('click', (e) => {
            e.preventDefault();
            container.querySelectorAll('a').forEach(a => a.classList.remove('selected'));
            link.classList.add('selected');
            const button = container.parentElement.querySelector('.dropbtn');
            button.textContent = item.label + " ▼";
            applyFilters();
        });
        container.appendChild(link);
    });
}

function addOptionToDropdown(dropdownId, value, label) {
    const container = document.getElementById(dropdownId);
    if (!container) return;

    const link = document.createElement('a');
    link.href = "#";
    link.textContent = label;
    link.dataset.value = value;
    link.addEventListener('click', (e) => {
        e.preventDefault();
        container.querySelectorAll('a').forEach(a => a.classList.remove('selected'));
        link.classList.add('selected');
        const button = container.parentElement.querySelector('.dropbtn');
        button.textContent = label + " ▼";
        applyFilters();
    });

    container.appendChild(link);
}

function updateDropdowns(books) {
    populateDropdown("subject-dropdown", {
        items: [...new Set(books.map(b => b.subject))].map(v => ({label: v, value: v}))
    });
    populateDropdown("author-dropdown", {
        items: [...new Set(books.map(b => b.author))].map(v => ({label: v, value: v}))
    });
}

function applyFilters() {
    const subject = document.querySelector("#subject-dropdown a.selected")?.dataset.value || "Tutti";
    const author = document.querySelector("#author-dropdown a.selected")?.dataset.value || "Tutti";
    const searchQuery = document.getElementById("search-bar")?.value.toLowerCase() || "";

    const filtered = allBooks.filter(b => {
        // Controllo filtro materia
        const subjectMatch = (subject === "Tutti" || b.subject === subject);
        
        // Controllo filtro autore
        const authorMatch = (author === "Tutti" || b.author === author);
        
        // Controllo ricerca testuale (solo se c'è una query di ricerca)
        const searchMatch = !searchQuery || 
            b.title.toLowerCase().includes(searchQuery) ||
            b.author.toLowerCase().includes(searchQuery) ||
            b.subject.toLowerCase().includes(searchQuery);

        return subjectMatch && authorMatch && searchMatch;
    });

    renderBooks(filtered);
}

// Funzione per resettare tutti i filtri
function resetAllFilters() {
    // Reset dei dropdown
    document.querySelectorAll('#subject-dropdown a, #author-dropdown a').forEach(link => {
        link.classList.remove('selected');
    });
    
    // Seleziona "Tutti" in entrambi i dropdown
    document.querySelector('#subject-dropdown a[data-value="Tutti"]')?.classList.add('selected');
    document.querySelector('#author-dropdown a[data-value="Tutti"]')?.classList.add('selected');
    
    // Reset dei testi dei bottoni
    const subjectBtn = document.querySelector('#subject-dropdown').parentElement.querySelector('.dropbtn');
    const authorBtn = document.querySelector('#author-dropdown').parentElement.querySelector('.dropbtn');
    if (subjectBtn) subjectBtn.textContent = "Tutti ▼";
    if (authorBtn) authorBtn.textContent = "Tutti ▼";
    
    // Reset della barra di ricerca
    const searchBar = document.getElementById("search-bar");
    if (searchBar) searchBar.value = "";
    
    // Mostra tutti i libri
    renderBooks(allBooks);
}

document.addEventListener("DOMContentLoaded", () => {
    const searchBar = document.getElementById("search-bar");
    const searchBtn = document.getElementById("search-btn");

    if (searchBar) searchBar.addEventListener("input", applyFilters);
    if (searchBtn) searchBtn.addEventListener("click", applyFilters);
    
    // Aggiungi evento click per "Tutti" nei dropdown
    document.addEventListener('click', (e) => {
        if (e.target.matches('#subject-dropdown a[data-value="Tutti"], #author-dropdown a[data-value="Tutti"]')) {
            e.preventDefault();
            const container = e.target.parentElement;
            container.querySelectorAll('a').forEach(a => a.classList.remove('selected'));
            e.target.classList.add('selected');
            const button = container.parentElement.querySelector('.dropbtn');
            button.textContent = "Tutti ▼";
            applyFilters();
        }
    });
});