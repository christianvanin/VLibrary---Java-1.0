package com.vcontrol.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.vcontrol.PythonScriptRunner;
import com.vcontrol.controller.interpreter.Interpreter;
import com.vcontrol.controller.interpreter.command.symbol.CommandSource;
import com.vcontrol.controller.interpreter.command.symbol.PrivilegeLevel;
import com.vcontrol.model.Model;
import com.vcontrol.model.book.Book;
import com.vcontrol.utility.config.LogManager;
import com.vcontrol.utility.config.LogManager.LogLevel;
import com.vcontrol.utility.config.PathManager;
import com.vcontrol.utility.exception.PythonScriptException;
import com.vcontrol.view.View;

public class Controller {
    private Model model;
    private View view;

    private Interpreter interpreter;

    public Controller(Model model, String[] args) {
        this.model = model;
        this.view = new View(this, args);
        this.interpreter = new Interpreter(this);
    }

    public void startTerminal() { 
        clearTerminal();
        readAsciiFile(PathManager.get("art.ascii.book")); 
        model.startTerminal(line -> view.executeJS("addTextToTerminal", line)); 
    }

    public void clearTerminal() { 
        view.executeJS("clearTerminal", "");
    }

    public void inizializeResources() {
        view.executeJS("clearBooks", "");
        showBookInList();
    }

    public void readAsciiFile(String resourcePath) {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(getClass().getClassLoader().getResourceAsStream(resourcePath)))) {
            String line;
            while ((line = br.readLine()) != null) view.executeJS("addTextToTerminal", line.replace(" ", "\u00A0"));
        } catch (IOException e) { LogManager.log(LogLevel.ERROR, e.getMessage()); }
    }

    public void openPDFView() {
        view.openNewView(PathManager.get("app.pdfviewsPath"));
    }

    public void openHomeView() {
        view.openNewView(PathManager.get("app.viewsPath"));
    }

    public void interpretCommandFromJava(String command) {
        interpreter.interpretCommand(prependSymbols(command, CommandSource.JAVA, PrivilegeLevel.HIGH));
    }

    public void interpretCommandFromJS(String command) {
        interpreter.interpretCommand(prependSymbols(command, CommandSource.JAVASCRIPT, PrivilegeLevel.MEDIUM));
    }

    public void interpretCommandFromTerminal(String command) {
        interpreter.interpretCommand(prependSymbols(command, CommandSource.TERMINAL, PrivilegeLevel.LOW));
    }

    private String prependSymbols(String command, CommandSource source, PrivilegeLevel privilegeLevel) {
        if (command == null) return null;
        return source.getSymbol() + privilegeLevel.getSymbol() + " " + command;
    }

    public void rewriteAI(String[] question) {
        String combined = String.join(" ", question);
        Thread askAI = new Thread(() -> {
            String answer;
            try {
                answer = PythonScriptRunner.runScript(PathManager.get("python.rewrite"), combined);
                LogManager.log(LogLevel.INFO,"AI :\n" + answer);
            } catch (PythonScriptException e) { LogManager.log(LogLevel.ERROR, e.getMessage()); return; }
        });
        askAI.start();
    }

    public void stampJSLog(String[] message) {
        String combined = String.join(" ", message);
        LogManager.log(LogLevel.DEBUG, combined);
    }

    public void showBookInList() {
        view.executeJS("setBooksCount", "" + model.getBooksManager().getAllBooks().size());
        for (Book book : model.getBooksManager().getAllBooks().values()) {
            view.executeJS("addBook", book.toJSON());
        }
    }

    public void setBookActiveCode(String[] code) {
        model.getBooksManager().setBookCode(String.join(" ", code));
    }

    public void getBookActive() {
        view.executeJS("setPdfPaths", model.getBooksManager().getBookByCode(model.getBooksManager().getBookCode()).pathToJSON());
    }
}
