package com.vcontrol.utility;

import java.util.Timer;
import java.util.TimerTask;

import org.cef.browser.CefBrowser;

public class JavaScriptChunkedSender {
    private static final int CHUNK_SIZE = 5000;
    private CefBrowser browser;

    public JavaScriptChunkedSender(CefBrowser browser) {
        this.browser = browser;
    }
    
    public void executeJS(String command, String payload) {
        if (payload == null || payload.length() <= CHUNK_SIZE) {
            sendNormalJS(command, payload);
            return;
        }
        
        sendChunkedJS(command, payload);
    }
    
    private void sendNormalJS(String command, String payload) {
        String json = String.format(
            "JavaBridge._onMessage(JSON.stringify({command:'%s', payload:%s}));", 
            command,
            escapeForJS(payload)
        );
        sendCommandToJS(json);
    }
    
    private void sendChunkedJS(String command, String payload) {
        String operationId = System.currentTimeMillis() + "_" + 
                           Integer.toString(payload.hashCode(), 36);
        
        int totalChunks = (int) Math.ceil((double) payload.length() / CHUNK_SIZE);
        
        System.out.println("Invio payload chunked: " + payload.length() + 
                          " caratteri in " + totalChunks + " chunks");
        
        String initCommand = String.format(
            "JavaBridge._initChunkedOperation('%s', '%s', %d);",
            operationId, command, totalChunks
        );
        sendCommandToJS(initCommand);
        
        for (int i = 0; i < totalChunks; i++) {
            final int chunkIndex = i;
            final int start = i * CHUNK_SIZE;
            final int end = Math.min(start + CHUNK_SIZE, payload.length());
            final String chunk = payload.substring(start, end);
            
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    String chunkCommand = String.format(
                        "JavaBridge._addChunk('%s', %d, %s);",
                        operationId, chunkIndex, escapeForJS(chunk)
                    );
                    sendCommandToJS(chunkCommand);
                    
                    if (chunkIndex == totalChunks - 1) {
                        Timer finalTimer = new Timer();
                        finalTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                String finalCommand = String.format(
                                    "JavaBridge._finalizeChunkedOperation('%s');",
                                    operationId
                                );
                                sendCommandToJS(finalCommand);
                            }
                        }, 10);
                    }
                }
            }, i * 5);
        }
    }
    
    private String escapeForJS(String text) {
        if (text == null) return "null";
        
        StringBuilder sb = new StringBuilder(text.length() + 100);
        sb.append('"');
        
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
        
        sb.append('"');
        return sb.toString();
    }
    
    private void sendCommandToJS(String command) {
        try {
            browser.executeJavaScript(command, browser.getURL(), 0);
        } catch (Exception e) {
            System.err.println("Errore nell'esecuzione JavaScript: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void setRewrittenText(String jsonPayload) {
        executeJS("setRewrittenText", jsonPayload);
    }
    
    public void setSummaryText(String jsonPayload) {
        executeJS("setSummaryText", jsonPayload);
    }
    
    public void setTranslatedText(String jsonPayload) {
        executeJS("setTranslatedText", jsonPayload);
    }
    
    public void setWorkText(String jsonPayload) {
        executeJS("setWorkText", jsonPayload);
    }
    
    public void setTranslatedSummaryText(String jsonPayload) {
        executeJS("setTranslatedSummaryText", jsonPayload);
    }
}