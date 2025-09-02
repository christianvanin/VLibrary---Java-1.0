const navMap = {
    'home': 'home-panel',
    'catalog': 'catalog-panel',
    'settings': 'settings-panel',
    'terminal': 'terminal-panel'
};

function showPanel(panelId) {
    document.querySelectorAll('#main-panel .panel').forEach(panel => {
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
        button.addEventListener('click', () => {
            buttons.forEach(b => b.classList.remove('active'));
            button.classList.add('active');

            const icon = button.querySelector('i');
            let panelId = '';

            if (icon.classList.contains('fa-house')) panelId = navMap.home;
            else if (icon.classList.contains('fa-book')) panelId = navMap.catalog;
            else if (icon.classList.contains('fa-gear')) panelId = navMap.settings;
            else if (icon.classList.contains('fa-terminal')) panelId = navMap.terminal;

            if (panelId) showPanel(panelId);
        });
    });

    showPanel('home-panel');

    const homeButton = document.querySelector('#side-panel .nav-button i.fa-house').parentElement;
    if(homeButton) homeButton.classList.add('active');
});

