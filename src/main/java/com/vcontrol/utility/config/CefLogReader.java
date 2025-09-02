package com.vcontrol.utility.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class CefLogReader {

    private static volatile boolean running = true;

    public static void startMonitoring(String logFilePath) {
        File logFile = new File(logFilePath);
        if (!logFile.exists()) {
            LogManager.log(LogManager.LogLevel.ERROR, "CEF log file not found: " + logFilePath);
            return;
        }

        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
                String line;
                while (running) {
                    while ((line = reader.readLine()) != null) LogManager.log(LogManager.LogLevel.CEF, line);
                    try { Thread.sleep(200); } 
                    catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            } catch (IOException e) {
                LogManager.log(LogManager.LogLevel.ERROR, "Error reading CEF log file: " + logFilePath, e);
            }
        }, "CEF-Log-Monitor").start();
    }

    public static void stopMonitoring() { running = false; }
}