package com.vcontrol.view;

import java.awt.BorderLayout;
import java.awt.Component;
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
import com.vcontrol.utility.config.LogManager;
import com.vcontrol.utility.config.PathManager;
import com.vcontrol.utility.config.LogManager.LogLevel;

public class View extends JFrame {
    private Controller controller;
    private CefBrowser browser;
    private CefClient client;

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
        Component browserUI = browser.getUIComponent();

        this.setTitle("LibraryV - 1.0.0");
        this.getContentPane().add(browserUI, BorderLayout.CENTER);
        this.setSize(1024, 768);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setVisible(true);

        CefApp.startup(args);
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
        } catch (Exception e) { LogManager.log(LogLevel.ERROR, "Failed to open"); }
    }

}
