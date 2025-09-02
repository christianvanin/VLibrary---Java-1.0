const terminalBody = document.querySelector('.terminal-body');
const terminalInput = document.querySelector('.terminal-input-bar input');

const colors = {
    'error': 'red',
    'info': 'blue',
    'cef': 'orange',
    'warn': 'yellow',
    'ai': 'green',
    'command': 'purple'
};

function addTextToTerminal(text) {
    if (!terminalBody) return;

    const pre = document.createElement('pre');
    pre.style.margin = 0;
    pre.style.fontFamily = "'Courier New', monospace";
    pre.style.fontSize = '14px';
    pre.style.fontWeight = 'normal';
    pre.style.whiteSpace = 'pre';

    pre.innerHTML = text.replace(/\b([a-zA-Zàèéìòù]+)\b/gi, (match) => {
        const lower = match.toLowerCase();
        if (colors[lower]) {
            return `<span style="color:${colors[lower]}; font-weight:bold;">${match}</span>`;
        }
        return match;
    });

    terminalBody.appendChild(pre);
    terminalBody.scrollTop = terminalBody.scrollHeight;
}

function clearTerminal() {
    if (!terminalBody) return;
    terminalBody.innerHTML = '';
}

function handleCommand(command) {
    if (window.JavaBridge && typeof window.JavaBridge.fire === 'function') {
        try {
            window.JavaBridge.fire(command);
        } catch (err) {
            console.error('Errore nell\'invocare JavaBridge.fire:', err);
        }
    } else {
        console.warn('JavaBridge.fire non è disponibile.');
    }
}

terminalInput.addEventListener('keydown', function(e) {
    if (e.key === 'Enter') {
        const commandText = terminalInput.value.trim();
        if (commandText !== '') {
            handleCommand(commandText);
            terminalInput.value = '';
        }
    }
});

if (window.JavaBridge && typeof window.JavaBridge.register === 'function') {
    JavaBridge.register('addTextToTerminal', addTextToTerminal);
    JavaBridge.register('clearTerminal', clearTerminal);
}

