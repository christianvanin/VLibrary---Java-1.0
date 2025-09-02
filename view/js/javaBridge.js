window.JavaBridge = (function () {
    function send(commandStr, onSuccess, onFailure) {
        if (typeof cefQuery !== "function") {
            console.error("cefQuery non disponibile! Funziona solo in JCEF.");
            return;
        }

        const requestObj = { command: commandStr };
        const requestStr = JSON.stringify(requestObj);

        cefQuery({
            request: requestStr,
            onSuccess: function (response) {
                if (typeof onSuccess === "function") {
                    onSuccess(response);
                }
            },
            onFailure: function (error_code, error_message) {
                if (typeof onFailure === "function") {
                    onFailure(error_code, error_message);
                } else {
                    console.error("Errore JavaBridge:", error_code, error_message);
                }
            }
        });
    }

    function fire(commandStr) { send(commandStr); }
    const commandMap = {};

    function register(command, handler) {
        if (typeof handler === "function") {
            commandMap[command] = handler;
        } else {
            console.error("Handler per comando '" + command + "' non è una funzione!");
        }
    }

    function _onMessage(message) {
        try {
            let cmd, payload;
            try {
                const obj = JSON.parse(message);
                cmd = obj.command;
                payload = obj.payload;
            } catch (e) {
                cmd = message;
                payload = null;
            }

            const handler = commandMap[cmd];
            if (handler) {
                handler(payload);
            } else {
                console.warn("Nessun handler registrato per comando:", cmd);
            }
        } catch (e) {
            console.error("Errore in _onMessage:", e);
        }
    }

    return {
        send: send,
        fire: fire,
        register: register,
        _onMessage: _onMessage
    };
})();
