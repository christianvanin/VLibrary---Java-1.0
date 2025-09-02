package com.vcontrol.model;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.vcontrol.controller.interpreter.command.symbol.PrivilegeLevel;

public class Terminal extends Thread {
    private PrivilegeLevel privilegeLevel;
    private volatile String pathFileSource;
    private volatile boolean running = true;

    public interface LineProcessor {
        void processLine(String line);
    }

    private LineProcessor processor;
    private volatile boolean fileChanged = false;
    private volatile String newFilePath;

    public Terminal(PrivilegeLevel privilegeLevel, String pathFileSource, LineProcessor processor) {
        this.privilegeLevel = privilegeLevel;
        this.pathFileSource = pathFileSource;
        this.processor = processor;
        setDaemon(true);
    }

    @Override
    public void run() {
        RandomAccessFile raf = null;
        File file = new File(pathFileSource);

        try {
            raf = new RandomAccessFile(file, "r");
            long filePointer = 0;
            raf.seek(filePointer);

            while (running) {
                if (fileChanged) {
                    if (raf != null) raf.close();
                    file = new File(newFilePath);
                    pathFileSource = newFilePath;
                    raf = new RandomAccessFile(file, "r");
                    filePointer = raf.length();
                    fileChanged = false;
                }

                long fileLength = file.length();
                if (fileLength < filePointer) {
                    raf.seek(0);
                    filePointer = 0;
                }

                if (fileLength > filePointer) {
                    raf.seek(filePointer);
                    String line;
                    while ((line = raf.readLine()) != null) {
                        String decoded = new String(line.getBytes("ISO-8859-1"), "UTF-8");
                        if (processor != null) processor.processLine(decoded);
                    }
                    filePointer = raf.getFilePointer();
                }
                Thread.sleep(100);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (raf != null) {
                try { raf.close(); } 
                catch (IOException e) { e.printStackTrace(); }
            }
        }
    }

    public void stopTerminal() {
        running = false;
        interrupt();
    }

    public void changeFile(String newPath) {
        this.newFilePath = newPath;
        this.fileChanged = true;
    }

    public PrivilegeLevel getPrivilegeLevel() { return privilegeLevel; }
    public void setPrivilegeLevel(PrivilegeLevel privilegeLevel) { this.privilegeLevel = privilegeLevel; }
    public String getPathFileSource() { return pathFileSource; }
    public void setPathFileSource(String pathFileSource) { this.pathFileSource = pathFileSource; }
    public boolean isRunning() { return running; }
    public void setRunning(boolean running) { this.running = running; }
    public LineProcessor getProcessor() { return processor; }
    public void setProcessor(LineProcessor processor) { this.processor = processor; }
    public boolean isFileChanged() { return fileChanged; }
    public void setFileChanged(boolean fileChanged) { this.fileChanged = fileChanged; }
    public String getNewFilePath() { return newFilePath; }
    public void setNewFilePath(String newFilePath) { this.newFilePath = newFilePath; } 
}