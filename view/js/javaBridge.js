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

    function fire(commandStr) { 
        send(commandStr); 
    }
    
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

    const chunkedOperations = {};
    
    function _initChunkedOperation(operationId, command, totalChunks) {
        console.log(`🔄 Inizializzazione operazione chunked: ${operationId} (${totalChunks} chunks)`);
        
        chunkedOperations[operationId] = {
            command: command,
            totalChunks: totalChunks,
            receivedChunks: 0,
            chunks: new Array(totalChunks),
            startTime: Date.now()
        };
        
        if (totalChunks > 5) {
            _showChunkingProgress(operationId, 0, totalChunks);
        }
    }

    function _addChunk(operationId, chunkIndex, chunkData) {
        const operation = chunkedOperations[operationId];
        if (!operation) {
            console.error("❌ Operazione chunked non trovata:", operationId);
            return;
        }
        
        operation.chunks[chunkIndex] = chunkData;
        operation.receivedChunks++;

        if (operation.totalChunks > 5) {
            _updateChunkingProgress(operationId, operation.receivedChunks, operation.totalChunks);
        }

        if (operation.totalChunks > 20 && operation.receivedChunks % 10 === 0) {
            const progress = Math.round((operation.receivedChunks / operation.totalChunks) * 100);
            console.log(`📊 Progresso chunked ${operationId}: ${progress}% (${operation.receivedChunks}/${operation.totalChunks})`);
        }
    }

    function _finalizeChunkedOperation(operationId) {
        const operation = chunkedOperations[operationId];
        if (!operation) {
            console.error("❌ Operazione chunked non trovata per finalizzazione:", operationId);
            return;
        }
        
        const endTime = Date.now();
        const duration = endTime - operation.startTime;
        
        console.log(`✅ Finalizzazione chunked ${operationId}: ${operation.receivedChunks}/${operation.totalChunks} chunks in ${duration}ms`);

        const verification = _verifyChunks(operation);
        if (!verification.success) {
            console.error(`❌ Verifica chunks fallita per ${operationId}:`, verification.errors);
            _hideChunkingProgress(operationId);
            delete chunkedOperations[operationId];
            return;
        }
        let fullPayload;
        try {
            const fullText = operation.chunks.join('');
            fullPayload = JSON.parse(fullText);
        } catch (e) {
            console.error("❌ Errore nel parsing del payload ricomposto:", e);
            _hideChunkingProgress(operationId);
            delete chunkedOperations[operationId];
            return;
        }
        try {
            const handler = commandMap[operation.command];
            if (handler && typeof handler === 'function') {
                handler(fullPayload);
            } else {
                console.warn("⚠️ Nessun handler registrato per comando chunked:", operation.command);
            }
        } catch (e) {
            console.error("❌ Errore nell'esecuzione del handler chunked:", e);
        }

        _hideChunkingProgress(operationId);
        delete chunkedOperations[operationId];
        
        console.log(`🎉 Operazione chunked ${operationId} completata con successo`);
    }
    
    function _verifyChunks(operation) {
        const errors = [];
        const missingChunks = [];
        
        for (let i = 0; i < operation.totalChunks; i++) {
            if (operation.chunks[i] === undefined || operation.chunks[i] === null) {
                missingChunks.push(i);
            }
        }
        
        if (missingChunks.length > 0) {
            errors.push(`Chunks mancanti: ${missingChunks.join(', ')}`);
        }
        
        if (operation.receivedChunks !== operation.totalChunks) {
            errors.push(`Conteggio errato: ricevuti ${operation.receivedChunks}, attesi ${operation.totalChunks}`);
        }
        
        return {
            success: errors.length === 0,
            errors: errors
        };
    }
    const progressIndicators = {};
    
    function _showChunkingProgress(operationId, current, total) {
        const progressId = `chunking-progress-${operationId}`;
        _hideChunkingProgress(operationId);
        const indicator = document.createElement('div');
        indicator.id = progressId;
        indicator.className = 'chunking-progress-indicator';
        indicator.innerHTML = `
            <div style="
                position: fixed;
                top: 10px;
                right: 10px;
                background: rgba(0, 123, 255, 0.9);
                color: white;
                padding: 12px 16px;
                border-radius: 6px;
                font-size: 14px;
                font-weight: 500;
                box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
                z-index: 10000;
                display: flex;
                align-items: center;
                gap: 10px;
                min-width: 200px;
            ">
                <i class="fa-solid fa-download fa-spin"></i>
                <div>
                    <div>Caricamento dati...</div>
                    <div style="font-size: 12px; opacity: 0.8;" id="${progressId}-text">
                        0% (0/${total} chunks)
                    </div>
                    <div style="
                        width: 160px;
                        height: 4px;
                        background: rgba(255, 255, 255, 0.3);
                        border-radius: 2px;
                        margin-top: 4px;
                        overflow: hidden;
                    ">
                        <div id="${progressId}-bar" style="
                            width: 0%;
                            height: 100%;
                            background: white;
                            border-radius: 2px;
                            transition: width 0.3s ease;
                        "></div>
                    </div>
                </div>
            </div>
        `;
        
        document.body.appendChild(indicator);
        progressIndicators[operationId] = progressId;
    }
    
    function _updateChunkingProgress(operationId, current, total) {
        const progressId = progressIndicators[operationId];
        if (!progressId) return;
        
        const textEl = document.getElementById(progressId + '-text');
        const barEl = document.getElementById(progressId + '-bar');
        
        if (textEl && barEl) {
            const percentage = Math.round((current / total) * 100);
            textEl.textContent = `${percentage}% (${current}/${total} chunks)`;
            barEl.style.width = percentage + '%';
        }
    }
    
    function _hideChunkingProgress(operationId) {
        const progressId = progressIndicators[operationId];
        if (!progressId) return;
        
        const indicator = document.getElementById(progressId);
        if (indicator) {
            indicator.remove();
        }
        
        delete progressIndicators[operationId];
    }
    function _cleanupStaleOperations() {
        const now = Date.now();
        const maxAge = 30000;
        let cleaned = 0;
        
        for (const operationId in chunkedOperations) {
            const operation = chunkedOperations[operationId];
            if (now - operation.startTime > maxAge) {
                console.warn("🧹 Pulizia operazione chunked stale:", operationId);
                _hideChunkingProgress(operationId);
                delete chunkedOperations[operationId];
                cleaned++;
            }
        }
        
        if (cleaned > 0) {
            console.log(`🧹 Pulite ${cleaned} operazioni chunked obsolete`);
        }
    }
    const sendLargeText = (function() {
        const CHUNK_SIZE = 6000;
        
        function sendTextInChunks(command, instance, pageNumber, text) {
            if (!text || text.length <= CHUNK_SIZE) {
                send(`${command} ${instance} ${pageNumber} @${text}@`);
                return;
            }
            const operationId = Date.now() + '_' + Math.random().toString(36).substr(2, 9);
            const totalChunks = Math.ceil(text.length / CHUNK_SIZE);
            
            console.log(`📤 Invio testo lungo in ${totalChunks} chunk(s) - ID: ${operationId}`);

            send(`start_chunked_operation ${operationId} ${command} ${instance} ${pageNumber} ${totalChunks}`);

            for (let i = 0; i < totalChunks; i++) {
                const start = i * CHUNK_SIZE;
                const end = Math.min(start + CHUNK_SIZE, text.length);
                const chunk = text.substring(start, end);
                
                setTimeout(() => {
                    send(`chunk_data ${operationId} ${i} @${chunk}@`);

                    if (i === totalChunks - 1) {
                        setTimeout(() => {
                            send(`end_chunked_operation ${operationId}`);
                        }, 10);
                    }
                }, i * 10);
            }
        }
        
        return {
            sendTextInChunks: sendTextInChunks
        };
    })();
    function _getChunkingStats() {
        const operations = chunkedOperations;
        const stats = {
            activeOperations: Object.keys(operations).length,
            totalPendingChunks: 0,
            totalCompletedChunks: 0,
            oldestOperation: null,
            operations: []
        };
        
        let oldestTime = Date.now();
        for (const opId in operations) {
            const op = operations[opId];
            const pendingChunks = op.totalChunks - op.receivedChunks;
            stats.totalPendingChunks += pendingChunks;
            stats.totalCompletedChunks += op.receivedChunks;
            
            const opInfo = {
                id: opId,
                command: op.command,
                progress: `${op.receivedChunks}/${op.totalChunks}`,
                percentage: Math.round((op.receivedChunks / op.totalChunks) * 100),
                age: Date.now() - op.startTime,
                pending: pendingChunks
            };
            
            stats.operations.push(opInfo);
            
            if (op.startTime < oldestTime) {
                oldestTime = op.startTime;
                stats.oldestOperation = opInfo;
            }
        }
        
        return stats;
    }
    
    function _debugChunking() {
        const stats = _getChunkingStats();
        console.group("🔍 JavaBridge Chunking Debug");
        console.log("Operazioni attive:", stats.activeOperations);
        console.log("Chunks in attesa:", stats.totalPendingChunks);
        console.log("Chunks completati:", stats.totalCompletedChunks);
        
        if (stats.oldestOperation) {
            console.log("Operazione più vecchia:", stats.oldestOperation);
        }
        
        if (stats.operations.length > 0) {
            console.table(stats.operations);
        }
        
        console.groupEnd();
        
        return stats;
    }
    setInterval(_cleanupStaleOperations, 60000);
    window.debugChunking = _debugChunking; 
    return {
        send: send,
        fire: fire,
        register: register,
        _onMessage: _onMessage,
        commandMap: commandMap,
        _initChunkedOperation: _initChunkedOperation,
        _addChunk: _addChunk,
        _finalizeChunkedOperation: _finalizeChunkedOperation,
        sendLargeText: sendLargeText,
        _getChunkingStats: _getChunkingStats,
        _cleanupStaleOperations: _cleanupStaleOperations,
        _debugChunking: _debugChunking
    };
})();

(function injectChunkingCSS() {
    const style = document.createElement('style');
    style.textContent = `
        .chunking-progress-indicator {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
        }
        
        .chunking-progress-indicator .fa-spin {
            animation: fa-spin 2s infinite linear;
        }
        
        @keyframes fa-spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(359deg); }
        }
        
        /* Stili per il processing indicator */
        .processing-indicator {
            animation: pulse 1.5s infinite;
        }
        
        @keyframes pulse {
            0% { opacity: 0.6; }
            50% { opacity: 1; }
            100% { opacity: 0.6; }
        }
    `;
    document.head.appendChild(style);
})();