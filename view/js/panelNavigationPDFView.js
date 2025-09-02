const navMap = {
    'index': 'pdf-mode',
    'body': 'pdf-mode',
};

function showPanel(panelId) {
    document.querySelectorAll('#main-panel .content-mode').forEach(panel => {
        panel.style.display = 'none';
    });

    const panel = document.getElementById(panelId);
    if (panel) {
        panel.style.display = 'flex';
    }
}

document.addEventListener('DOMContentLoaded', () => {
    const buttons = document.querySelectorAll('#side-panel .nav-button');

    buttons.forEach(button => {
        if (button.querySelector('i').classList.contains('fa-house')) return;

        button.addEventListener('click', () => {
            buttons.forEach(b => b.classList.remove('active'));
            button.classList.add('active');

            let panelId = '';
            if (button.dataset.view === "index") panelId = navMap.index;
            else if (button.dataset.view === "body") panelId = navMap.body;

            if (panelId) showPanel(panelId);
        });
    });

    showPanel('pdf-mode');
    const firstPdfBtn = document.querySelector('#side-panel .nav-button[data-view="body"]');
    if(firstPdfBtn) firstPdfBtn.classList.add('active');
});
