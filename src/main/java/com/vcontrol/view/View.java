package com.vcontrol.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.browser.CefMessageRouter;
import org.cef.callback.CefCommandLine;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefAppHandlerAdapter;
import org.cef.handler.CefMessageRouterHandlerAdapter;
import org.json.JSONException;
import org.json.JSONObject;

import com.vcontrol.controller.Controller;
import com.vcontrol.utility.JavaScriptChunkedSender;
import com.vcontrol.utility.config.LogManager;
import com.vcontrol.utility.config.PathManager;
import com.vcontrol.utility.config.LogManager.LogLevel;

public class View extends JFrame {
    private Controller controller;
    private CefBrowser browser;
    private CefClient client;
    private JavaScriptChunkedSender chunkSender;
    private double currentZoom = 0.0; // 0.0 = 100%

    public View(Controller controller, String[] args) {
        this.controller = controller;
        CefSettings settings = new CefSettings();
        settings.windowless_rendering_enabled = false;

        String projectDir = System.getProperty("user.dir");
        File logFile = new File(projectDir, PathManager.get("config.cefLog"));
        logFile.getParentFile().mkdirs();
        settings.log_file = logFile.getAbsolutePath();

        settings.log_severity = CefSettings.LogSeverity.LOGSEVERITY_ERROR;
        String userDataDir = System.getProperty("user.home") + "/.miaAppCEFCache";
        settings.root_cache_path = userDataDir;
        
        CefApp.addAppHandler(new CefAppHandlerAdapter(args) {
            @Override
            public void onBeforeCommandLineProcessing(String processType, CefCommandLine commandLine) {
                commandLine.appendSwitch("allow-file-access-from-files");
                commandLine.appendSwitch("enable-pinch");
                commandLine.appendSwitch("enable-viewport");
                commandLine.appendSwitchWithValue("force-device-scale-factor", "1.3");
                //commandLine.appendSwitch("disable-web-security");
            }
        });
        
        CefApp cefApp = CefApp.getInstance(args, settings);
        client = cefApp.createClient();

        CefMessageRouter msgRouter = CefMessageRouter.create();
        msgRouter.addHandler(new CefMessageRouterHandlerAdapter() {
            @Override
            public boolean onQuery(CefBrowser browser,
                                CefFrame frame,
                                long queryId,
                                String request,
                                boolean persistent,
                                CefQueryCallback callback) {
                try {
                    JSONObject obj = new JSONObject(request);
                    String cmd = obj.getString("command");
                    
                    if (cmd.equals("zoomIn")) {
                        zoomIn();
                        callback.success("Zoom aumentato");
                        return true;
                    } else if (cmd.equals("zoomOut")) {
                        zoomOut();
                        callback.success("Zoom diminuito");
                        return true;
                    } else if (cmd.equals("zoomReset")) {
                        resetZoom();
                        callback.success("Zoom ripristinato");
                        return true;
                    }
                    
                    controller.interpretCommandFromJS(cmd);
                    callback.success("Comando ricevuto: " + cmd);
                } catch (JSONException e) {
                    LogManager.log(LogLevel.ERROR, "Errore parsing JSON: " + e.getMessage());
                    callback.failure(0, "Errore parsing JSON: " + e.getMessage());
                }

                return true;
            }
            
        }, true);

        client.addMessageRouter(msgRouter);
        File htmlFile = new File(PathManager.get("app.viewsPath"));
        browser = client.createBrowser(htmlFile.toURI().toString(), false, false);
        chunkSender = new JavaScriptChunkedSender(browser);
        Component browserUI = browser.getUIComponent();

        this.setTitle("LibraryV - 1.0.0");
        this.getContentPane().add(browserUI, BorderLayout.CENTER);
        this.setMinimumSize(new Dimension(1024, 768));
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        addKeyboardZoomSupport();
        
        this.setVisible(true);
        CefApp.startup(args);
    }

    private void addKeyboardZoomSupport() {
        this.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.isControlDown() || e.isMetaDown()) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_PLUS:
                        case KeyEvent.VK_EQUALS:
                            zoomIn();
                            break;
                        case KeyEvent.VK_MINUS:
                            zoomOut();
                            break;
                        case KeyEvent.VK_0:
                            resetZoom();
                            break;
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {}

            @Override
            public void keyTyped(KeyEvent e) {}
        });
        
        this.setFocusable(true);
        this.requestFocus();
    }

    public void zoomIn() {
        currentZoom += 0.5;
        if (currentZoom > 5.0) currentZoom = 5.0;
        browser.setZoomLevel(currentZoom);
        LogManager.log(LogLevel.INFO, "Zoom impostato a: " + getZoomPercentage() + "%");
    }

    public void zoomOut() {
        currentZoom -= 0.5;
        if (currentZoom < -5.0) currentZoom = -5.0;
        browser.setZoomLevel(currentZoom);
        LogManager.log(LogLevel.INFO, "Zoom impostato a: " + getZoomPercentage() + "%");
    }

    public void resetZoom() {
        currentZoom = 0.0;
        browser.setZoomLevel(currentZoom);
        LogManager.log(LogLevel.INFO, "Zoom ripristinato a: 100%");
    }

    public void setZoomLevel(double level) {
        currentZoom = level;
        browser.setZoomLevel(currentZoom);
    }

    public double getCurrentZoom() {
        return currentZoom;
    }

    public int getZoomPercentage() {
        return (int) Math.round(Math.pow(1.2, currentZoom) * 100);
    }

    public void executeJS(String command, String payload) {
        String json = String.format(
            "JavaBridge._onMessage(JSON.stringify({command:'%s', payload:%s}));", 
            command,
            escapeForJS(payload)
        );
        sendCommandToJS(json);
    }

    private String escapeForJS(String text) {
        return "\"" + text.replace("\"", "\\\"") + "\"";
    }
    
    private void sendCommandToJS(String command) {
        browser.executeJavaScript(command, browser.getURL(), 0);
    }

    public void openNewView(String path) {
        try {
            File pdfViewHtml = new File(path);
            String url = pdfViewHtml.toURI().toString();
            browser.loadURL(url);
        } catch (Exception e) { 
            LogManager.log(LogLevel.ERROR, "Failed to open: " + e.getMessage()); 
        }
    }

    public JavaScriptChunkedSender getChunkSender() { return chunkSender; }
    public void setChunkSender(JavaScriptChunkedSender chunkSender) { this.chunkSender = chunkSender; }
}