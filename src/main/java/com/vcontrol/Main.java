package com.vcontrol;

import com.vcontrol.controller.Controller;
import com.vcontrol.model.Model;
import com.vcontrol.utility.config.CefLogReader;
import com.vcontrol.utility.config.LogManager;
import com.vcontrol.utility.config.PathManager;

public class Main {
    public static void main(String[] args) {
        try { 
            PathManager.load("config/paths.properties");
            LogManager.loadConfig(PathManager.get("config.logback"));
            CefLogReader.startMonitoring(PathManager.get("config.cefLog"));
            Controller controller = new Controller(new Model(), args);
        } catch (Exception e) { }
    }
    
}
