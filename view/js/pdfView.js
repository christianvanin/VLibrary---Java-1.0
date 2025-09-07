let bodyPagesPath = '';
let indexPagesPath = '';
let pathPages = '';
let currentPage = 1;
let totalBodyPages = 0;
let totalIndexPages = 0;
let currentZoom = 100;
let currentPdfDocs = { left: null, right: null };
let extractedTexts = { 
    left: {
        normale: '',
        riscritto: '',
        riassunto: '',
        tradotto: '',
        esercizi: '',
        riassuntotradotto: ''

    }, 
    right: {
        normale: '',
        riscritto: '',
        riassunto: '',
        tradotto: '',
        esercizi: '',
        riassuntotradotto: ''
    }
};
let currentTextInstances = { left: 'normale', right: 'normale' };

pdfjsLib.GlobalWorkerOptions.workerSrc = 'https://cdnjs.cloudflare.com/ajax/libs/pdf.js/3.11.174/pdf.worker.min.js';

JavaBridge.register('setRewrittenText', function(jsonData) {
    if (typeof jsonData === "string") {
        try { jsonData = JSON.parse(jsonData); 
        } catch(e) { 
            window.JavaBridge.send("print logjs JSON parsing error in setRewrittenText: " + e.message);
            return;
        }
    }
    
    if(jsonData && jsonData.text && jsonData.page) {
        const side = (jsonData.page === currentPage) ? 'right' : 'left';
        
        if (jsonData.page === currentPage || jsonData.page === currentPage + 1) {
            extractedTexts[side].riscritto = jsonData.text;
            if (currentTextInstances[side] === 'riscritto') {
                updateTextPanel(side);
            }
        }
    }
});

JavaBridge.register('setWorkText', function(jsonData) {
    if (typeof jsonData === "string") {
        try { jsonData = JSON.parse(jsonData); 
        } catch(e) { 
            window.JavaBridge.send("print logjs JSON parsing error in setWorkText: " + e.message);
            return;
        }
    }
    
    if(jsonData && jsonData.text && jsonData.page) {
        const side = (jsonData.page === currentPage) ? 'right' : 'left';
        
        if (jsonData.page === currentPage || jsonData.page === currentPage + 1) {
            extractedTexts[side].esercizi = jsonData.text;
            if (currentTextInstances[side] === 'esercizi') {
                updateTextPanel(side);
            }
        }
    }
});

JavaBridge.register('setTranslatedSummaryText', function(jsonData) {
    if (typeof jsonData === "string") {
        try { jsonData = JSON.parse(jsonData); 
        } catch(e) { 
            window.JavaBridge.send("print logjs JSON parsing error in setTranslatedSummaryText: " + e.message);
            return;
        }
    }
    
    if(jsonData && jsonData.text && jsonData.page) {
        const side = (jsonData.page === currentPage) ? 'right' : 'left';
        
        if (jsonData.page === currentPage || jsonData.page === currentPage + 1) {
            extractedTexts[side].riassuntotradotto = jsonData.text;
            if (currentTextInstances[side] === 'riassuntotradotto') {
                updateTextPanel(side);
            }
        }
    }
});

JavaBridge.register('setSummaryText', function(jsonData) {
    if (typeof jsonData === "string") {
        try { jsonData = JSON.parse(jsonData); 
        } catch(e) { 
            window.JavaBridge.send("print logjs JSON parsing error in setSummaryText: " + e.message);
            return;
        }
    }
    
    if(jsonData && jsonData.text && jsonData.page) {
        const side = (jsonData.page === currentPage) ? 'right' : 'left';
        
        if (jsonData.page === currentPage || jsonData.page === currentPage + 1) {
            extractedTexts[side].riassunto = jsonData.text;
            if (currentTextInstances[side] === 'riassunto') {
                updateTextPanel(side);
            }
        }
    }
});

JavaBridge.register('setTranslatedText', function(jsonData) {
    if (typeof jsonData === "string") {
        try { jsonData = JSON.parse(jsonData); 
        } catch(e) { 
            window.JavaBridge.send("print logjs JSON parsing error in setTranslatedText: " + e.message);
            return;
        }
    }
    
    if(jsonData && jsonData.text && jsonData.page) {
        const side = (jsonData.page === currentPage) ? 'right' : 'left';
        
        if (jsonData.page === currentPage || jsonData.page === currentPage + 1) {
            extractedTexts[side].tradotto = jsonData.text;
            if (currentTextInstances[side] === 'tradotto') {
                updateTextPanel(side);
            }
        }
    }
});

function selectTextInstance(side, instance) {
    currentTextInstances[side] = instance;
    
    const buttons = document.querySelectorAll(`.instance-btn[data-side="${side}"]`);
    buttons.forEach(btn => {
        if (btn.dataset.instance === instance) {
            btn.classList.add('active');
        } else {
            btn.classList.remove('active');
        }
    });
    
    const pageNumber = side === 'left' ? currentPage + 1 : currentPage;
    
    const textToSend = extractedTexts[side][instance] || extractedTexts[side].normale || '';
    if (textToSend && textToSend !== 'Nessun testo disponibile per questa pagina' && 
        textToSend !== 'Pagina non caricata' && textToSend !== 'Errore nell\'estrazione del testo' &&
        textToSend !== 'Fine documento') {
        JavaBridge.send(`get text ${instance} ${pageNumber} @${textToSend}@`);
    }
    
    updateTextPanel(side);
}

JavaBridge.register('setPdfPaths', function(jsonData) {
    if (typeof jsonData === "string") {
        try { jsonData = JSON.parse(jsonData); 
        } catch(e) { 
            window.JavaBridge.send("print logjs JSON parsing error in setPdfPaths: " + e.message);
            return;
        }
    }
    
    if(jsonData && jsonData.bodyPagesPath && jsonData.indexPagesPath) {
        bodyPagesPath = jsonData.bodyPagesPath;
        indexPagesPath = jsonData.indexPagesPath;
        totalBodyPages = jsonData.bodyPages;
        totalIndexPages = jsonData.indexPages;
        if (!bodyPagesPath.endsWith('/')) bodyPagesPath += '/';
        if (!indexPagesPath.endsWith('/')) indexPagesPath += '/';
        pathPages = bodyPagesPath;
        loadCurrentPages();
        updateTotalPagesDisplay();
    } else window.JavaBridge.send("print logjs Invalid PDF paths data received");
});

function getCurrentMaxPages() {
    if (pathPages === bodyPagesPath) {
        return totalBodyPages;
    } else if (pathPages === indexPagesPath) {
        return totalIndexPages;
    }
    return 0;
}

function changePagesPathInIndex() {
    if (indexPagesPath) {
        currentPage = 1;
        pathPages = indexPagesPath;
        extractedTexts = { 
            left: { normale: '', riscritto: '', riassunto: '', tradotto: '' ,esercizi: '' , riassuntotradotto: ''}, 
            right: { normale: '', riscritto: '', riassunto: '', tradotto: '' ,esercizi: '' , riassuntotradotto: ''}
        };
        currentTextInstances = { left: 'normale', right: 'normale' };
        resetInstanceButtons();
        loadCurrentPages();
        updatePageDisplay();
        updateTotalPagesDisplay();
    } else window.JavaBridge.send("print logjs Index pages path not available");
}

function changePagesPathInBody() {
    if (bodyPagesPath) {
        currentPage = 1;
        pathPages = bodyPagesPath;
        extractedTexts = { 
            left: { normale: '', riscritto: '', riassunto: '', tradotto: '' ,esercizi: '' , riassuntotradotto: ''}, 
            right: { normale: '', riscritto: '', riassunto: '', tradotto: '' ,esercizi: '' , riassuntotradotto: ''}
        };
        currentTextInstances = { left: 'normale', right: 'normale' };
        resetInstanceButtons();
        loadCurrentPages();
        updatePageDisplay();
        updateTotalPagesDisplay();
    } else window.JavaBridge.send("print logjs Body pages path not available");
}

function resetInstanceButtons() {
    const buttons = document.querySelectorAll('.instance-btn');
    buttons.forEach(btn => {
        if (btn.dataset.instance === 'normale') {
            btn.classList.add('active');
        } else {
            btn.classList.remove('active');
        }
    });
}

function updateTotalPagesDisplay() {
    const totalPagesElement = document.getElementById('totalPages');
    if (totalPagesElement) {
        if (pathPages === bodyPagesPath) {
            totalPagesElement.textContent = totalBodyPages;
        } else if (pathPages === indexPagesPath) {
            totalPagesElement.textContent = totalIndexPages;
        }
    }
}

async function loadPDF(path) {
    try {
        const loadingTask = pdfjsLib.getDocument({
            url: path,
            cMapUrl: 'https://cdnjs.cloudflare.com/ajax/libs/pdf.js/3.11.174/cmaps/',
            cMapPacked: true
        });
        
        const pdf = await loadingTask.promise;
        return pdf;
        
    } catch (e) {
        window.JavaBridge.send("print logjs Error loading PDF " + path + ": " + e.message);
        return null;
    }
}

async function extractTextFromPage(pdfDoc, pageNum = 1) {
    try {
        if (!pdfDoc) return '';
        
        const page = await pdfDoc.getPage(pageNum);
        const textContent = await page.getTextContent();
        
        let text = '';
        let lastY = null;
        
        textContent.items.forEach(item => {
            if (item.str && item.str.trim()) {
                if (lastY !== null && Math.abs(lastY - item.transform[5]) > 5) text += '\n';
                text += item.str + ' ';
                lastY = item.transform[5];
            }
        });
        
        return text.trim();
    } catch (e) { 
        window.JavaBridge.send("print logjs Error extracting text: " + e.message);
        return ''; 
    }
}

async function renderPage(pdfDoc, canvas, scale = 1.0) {
    try {
        if (!pdfDoc || !canvas) return;
        
        const page = await pdfDoc.getPage(1);
        const viewport = page.getViewport({ scale: scale });

        canvas.width = viewport.width;
        canvas.height = viewport.height;

        const context = canvas.getContext('2d');
        context.clearRect(0, 0, canvas.width, canvas.height);
        
        const renderContext = {
            canvasContext: context,
            viewport: viewport
        };

        await page.render(renderContext).promise;
        
    } catch (e) { }
}

async function loadCurrentPages() {
    if (!pathPages) {
        window.JavaBridge.send("print logjs No path pages set");
        return;
    }
    
    const leftCanvas = document.getElementById('leftPage');
    const rightCanvas = document.getElementById('rightPage');
    const loadingElement = document.getElementById('loading');
    const pagesContainer = document.getElementById('pagesContainer');
    const maxPages = getCurrentMaxPages();

    try {
        if (loadingElement) loadingElement.style.display = 'block';
        if (pagesContainer) pagesContainer.style.display = 'none';
        
        extractedTexts.left.riscritto = '';
        extractedTexts.left.riassunto = '';
        extractedTexts.left.esercizi = '';
        extractedTexts.left.tradotto = '';
        extractedTexts.left.riassuntotradotto = '';
        extractedTexts.right.riscritto = '';
        extractedTexts.right.riassunto = '';
        extractedTexts.right.esercizi = '';
        extractedTexts.right.tradotto = '';
        extractedTexts.right.riassuntotradotto = '';
        
        currentTextInstances = { left: 'normale', right: 'normale' };
        resetInstanceButtons();
        
        const evenPagePath = pathPages + "Extract%5B" + currentPage + "%5D.pdf";
        const oddPagePath = pathPages + "Extract%5B" + (currentPage + 1) + "%5D.pdf";
        const scale = currentZoom / 50;
        
        try {
            const leftPdf = await loadPDF(evenPagePath);
            currentPdfDocs.left = leftPdf;
            
            if (leftPdf && leftCanvas) {
                await renderPage(leftPdf, leftCanvas, scale);
            }
        } catch (e) {
            window.JavaBridge.send("print logjs Failed to load/render left page: " + e.message);
            currentPdfDocs.left = null;
            if (leftCanvas) {
                const ctx = leftCanvas.getContext('2d');
                ctx.clearRect(0, 0, leftCanvas.width, leftCanvas.height);
            }
        }
        
        if (currentPage + 1 <= maxPages) {
            try {
                const rightPdf = await loadPDF(oddPagePath);
                currentPdfDocs.right = rightPdf;
                
                if (rightPdf && rightCanvas) {
                    await renderPage(rightPdf, rightCanvas, scale);
                }
            } catch (e) {
                window.JavaBridge.send("print logjs Failed to load/render right page: " + e.message);
                currentPdfDocs.right = null;
                if (rightCanvas) {
                    const ctx = rightCanvas.getContext('2d');
                    ctx.clearRect(0, 0, rightCanvas.width, rightCanvas.height);
                }
            }
        } else {
            currentPdfDocs.right = null;
            if (rightCanvas) {
                const ctx = rightCanvas.getContext('2d');
                ctx.clearRect(0, 0, rightCanvas.width, rightCanvas.height);
            }
        }
        
        await extractTextFromCurrentPages();
        if (loadingElement) loadingElement.style.display = 'none';
        if (pagesContainer) pagesContainer.style.display = 'flex';
    } catch (e) {
        console.error('Error loading pages:', e);
        window.JavaBridge.send("print logjs Error in loadCurrentPages: " + e.message);
        if (loadingElement) {
            loadingElement.innerHTML = '<i class="fa-solid fa-exclamation-triangle"></i> Errore nel caricamento: ' + e.message;
        }
    }
}

async function extractTextFromCurrentPages() {
    try {
        if (currentPdfDocs.left) {
            const leftText = await extractTextFromPage(currentPdfDocs.left, 1);
            extractedTexts.right.normale = leftText || 'Nessun testo disponibile per questa pagina';
        } else extractedTexts.right.normale = 'Pagina non caricata';
        
        if (currentPdfDocs.right) {
            const rightText = await extractTextFromPage(currentPdfDocs.right, 1);
            extractedTexts.left.normale = rightText || 'Nessun testo disponibile per questa pagina';
        } else {
            const maxPages = getCurrentMaxPages();
            if (currentPage + 1 > maxPages) {
                extractedTexts.left.normale = 'Fine documento';
            } else {
                extractedTexts.left.normale = 'Pagina non caricata';
            }
        }

        updateTextPanel('left');
        updateTextPanel('right');
        
    } catch (e) {
        console.error('Errore nell\'estrazione del testo:', e);
        extractedTexts.left.normale = 'Errore nell\'estrazione del testo';
        extractedTexts.right.normale = 'Errore nell\'estrazione del testo';
        updateTextPanel('left');
        updateTextPanel('right');
    }
}

function updateTextPanel(side) {
    const textContent = document.getElementById(side + 'TextContent');
    if (textContent) {
        const currentInstance = currentTextInstances[side];
        const text = extractedTexts[side][currentInstance];
        
        if (text && text !== 'Nessun testo disponibile per questa pagina' && 
            text !== 'Pagina non caricata' && text !== 'Errore nell\'estrazione del testo' &&
            text !== 'Fine documento') {
            
            const formattedText = text
                .replace(/\\n/g, '\n')
                .replace(/\n/g, '<br>')
                .replace(/\\r/g, '')
                .replace(/\r/g, '')
                .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
            
            textContent.innerHTML = '<div>' + formattedText + '</div>';
        } else if (currentInstance !== 'normale' && !text) {
            textContent.innerHTML = '<div class="no-text-message">Elaborazione in corso...</div>';
        } else {
            textContent.innerHTML = `<div class="no-text-message">${text}</div>`;
        }
    }
}

function toggleTextPanel(side) {
    const content = document.getElementById(side + 'TextPanel');
    const arrow = document.getElementById(side + 'Arrow');
    const oppositeSide = side === 'left' ? 'right' : 'left';
    const oppositeContent = document.getElementById(oppositeSide + 'TextPanel');
    const oppositeArrow = document.getElementById(oppositeSide + 'Arrow');
    
    if (content && arrow) {
        if (content.classList.contains('expanded')) {
            content.classList.remove('expanded');
            arrow.classList.remove('expanded');
        } else {
            if (oppositeContent && oppositeContent.classList.contains('expanded')) {
                oppositeContent.classList.remove('expanded');
                if (oppositeArrow) oppositeArrow.classList.remove('expanded');
            }

            content.classList.add('expanded');
            arrow.classList.add('expanded');
        }
    }
}

async function copyTextContent(event, side) {
    event.stopPropagation();
    
    try {
        const currentInstance = currentTextInstances[side];
        const textToCopy = extractedTexts[side][currentInstance] || '';
        await navigator.clipboard.writeText(textToCopy);

        const button = event.currentTarget;
        const originalIcon = button.innerHTML;
        button.innerHTML = '<i class="fa-solid fa-check"></i>';
        
        setTimeout(() => {
            button.innerHTML = originalIcon;
        }, 2000);
        
    } catch (e) {
        console.error('Errore nella copia:', e);
        const button = event.currentTarget;
        const originalIcon = button.innerHTML;
        button.innerHTML = '<i class="fa-solid fa-times"></i>';
        
        setTimeout(() => {
            button.innerHTML = originalIcon;
        }, 2000);
    }
}

function updatePageDisplay() {
    const pageInput = document.getElementById('currentPageInput');
    if (pageInput) {
        pageInput.value = currentPage;
    }
}

document.addEventListener('DOMContentLoaded', function() {
    document.addEventListener('visibilitychange', function() {
        if (!document.hidden) {
            if (document.activeElement) {
                document.activeElement.blur();
            }
        }
    });

    const prevBtn = document.getElementById('prevPageBtn');
    const nextBtn = document.getElementById('nextPageBtn');
    const pageInput = document.getElementById('currentPageInput');
    const refreshBtn = document.getElementById('refreshPagesBtn');

    if (prevBtn) {
        prevBtn.addEventListener('click', () => {
            if (currentPage > 1) {
                currentPage -= 2;
                if (currentPage < 1) currentPage = 1;
                loadCurrentPages();
                updatePageDisplay();
            }
        });
    }

    if (nextBtn) {
        nextBtn.addEventListener('click', () => {
            const maxPages = getCurrentMaxPages();
            if (currentPage + 2 <= maxPages) {
                currentPage += 2;
                loadCurrentPages();
                updatePageDisplay();
            } else {
                const lastValidPage = maxPages % 2 === 0 ? maxPages - 1 : maxPages - (maxPages % 2 === 1 ? 0 : 1);
                if (currentPage < lastValidPage) {
                    currentPage = Math.max(1, lastValidPage);
                    loadCurrentPages();
                    updatePageDisplay();
                }
                window.JavaBridge.send("print logjs Reached maximum pages: " + maxPages);
            }
        });
    }

    if (refreshBtn) {
        refreshBtn.addEventListener('click', refreshPages);
    }

    if (pageInput) {
        pageInput.addEventListener('change', (e) => {
            const newPage = parseInt(e.target.value);
            const maxPages = getCurrentMaxPages();
            
            if (newPage > 0 && newPage <= maxPages) {
                currentPage = newPage % 2 === 0 ? newPage : newPage - 1;
                if (currentPage < 1) currentPage = 1;
                
                loadCurrentPages();
                updatePageDisplay();
            } else {
                if (newPage > maxPages) {
                    const lastValidPage = maxPages % 2 === 0 ? maxPages - 1 : maxPages - (maxPages % 2 === 1 ? 0 : 1);
                    currentPage = Math.max(1, lastValidPage);
                    loadCurrentPages();
                    updatePageDisplay();
                    window.JavaBridge.send("print logjs Page number exceeds maximum. Set to last valid page: " + currentPage);
                } else {
                    updatePageDisplay();
                }
            }
        });
    }

    const zoomSlider = document.getElementById('zoomSlider');
    const zoomPercent = document.getElementById('zoomPercent');
    const zoomInBtn = document.getElementById('zoomInBtn');
    const zoomOutBtn = document.getElementById('zoomOutBtn');

    if (zoomSlider) {
        zoomSlider.addEventListener('input', (e) => {
            currentZoom = parseInt(e.target.value);
            if (zoomPercent) zoomPercent.textContent = `${currentZoom}%`;
            loadCurrentPages();
        });
    }

    if (zoomInBtn) {
        zoomInBtn.addEventListener('click', () => {
            if (currentZoom < 200) {
                currentZoom += 10;
                if (zoomSlider) zoomSlider.value = currentZoom;
                if (zoomPercent) zoomPercent.textContent = `${currentZoom}%`;
                loadCurrentPages();
            }
        });
    }

    if (zoomOutBtn) {
        zoomOutBtn.addEventListener('click', () => {
            if (currentZoom > 50) {
                currentZoom -= 10;
                if (zoomSlider) zoomSlider.value = currentZoom;
                if (zoomPercent) zoomPercent.textContent = `${currentZoom}%`;
                loadCurrentPages();
            }
        });
    }

    updatePageDisplay();
});

function refreshPages() {
    loadCurrentPages();
}

window.addEventListener('resize', () => {
    clearTimeout(window.resizeTimeout);
    window.resizeTimeout = setTimeout(() => {
        if (currentView !== 'chat') {
            loadCurrentPages();
        }
    }, 250);
});