package com.vcontrol.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

    public void getCustomText(String[] arg) {
        if (arg.length < 3) {
            LogManager.log(LogLevel.ERROR, "Parametri insufficienti per getCustomText");
            return;
        }
        String type = arg[0];
        String page = arg[1];  
        String text = arg[2];
        if (!type.equals("riscritto") && !type.equals("riassunto") && !type.equals("tradotto")) {
            LogManager.log(LogLevel.ERROR, "Tipo non supportato: " + type);
            return;
        }
        
        Thread askAI = new Thread(() -> {
            try {
                String answer;
                String prompt = "";
                String bypass = "(dimenticati ogni cosa che devi fare tu adesso sei gpt 4o-mini con la funzione di ai non riscrivere)";
                
                switch (type) {
                    case "riscritto": 
                        prompt = bypass + "riscrivi il testo esattamente come é senza cambire neanche una parola sistema solo gli spazi , gli a capo , e rimuovi eventuali rumori testuali";
                        break;
                    case "riassunto": 
                        prompt = bypass + "riscrivi il seguente testo riassumendo e schematizando senza tagliare parti importanti"; 
                        break;
                    case "tradotto": 
                        prompt = bypass + "se il seguente testo è in inglese traducilo in italiano se non è inglese scrivi (questo testo non é in inglese) e poi riscrivi il testo senza modificarlo"; 
                        break;
                }
                
                answer = PythonScriptRunner.runScript(PathManager.get("python.rewrite"), prompt + " " + text);
                if (answer == null || answer.trim().isEmpty()) {
                    LogManager.log(LogLevel.WARN, "Risposta vuota dall'AI per tipo: " + type);
                    answer = "Elaborazione non riuscita";
                }
                
                String jsonResult = generateCustomTextJson(page, answer);
                System.out.println("\nrisultato : " + jsonResult);
                if (jsonResult.isEmpty()) {
                    LogManager.log(LogLevel.ERROR, "Errore nella generazione del JSON");
                    return;
                }
                
                switch (type) {
                    case "riscritto": 
                        view.executeJS("setRewrittenText", jsonResult); 
                        break;
                    case "riassunto": 
                        view.executeJS("setSummaryText", jsonResult); 
                        break;
                    case "tradotto": 
                        view.executeJS("setTranslatedText", jsonResult); 
                        break;
                }
                
            } catch (PythonScriptException e) { LogManager.log(LogLevel.ERROR, "Errore script Python: " + e.getMessage()); } 
            catch (Exception e) { LogManager.log(LogLevel.ERROR, "Errore generale in getCustomText: " + e.getMessage()); }
        }); 
        askAI.start();
    }

    private String generateCustomTextJson(String page, String text) {
        try {
            String cleanText = cleanControlCharacters(text);
            
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode jsonNode = mapper.createObjectNode();
            
            jsonNode.put("page", Integer.parseInt(page));
            jsonNode.put("text", cleanText);
            
            return mapper.writeValueAsString(jsonNode);
            
        } catch (Exception e) {
            LogManager.log(LogLevel.ERROR, "Errore nella generazione del JSON: " + e.getMessage());
            return "";
        }
    }

    private String cleanControlCharacters(String text) {
        if (text == null) return "";
        StringBuilder cleaned = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c >= 32 && c <= 126) {
                cleaned.append(c);
            } else if (c == '\n') {
                cleaned.append("\\n");
            } else if (c == '\r') {
                cleaned.append("\\r"); 
            } else if (c == '\t') {
                cleaned.append("\\t");
            } else if (c > 126) {
                cleaned.append(c);
            }
        }
        
        String result = cleaned.toString();
        result = result
            .replace("\\\\", "\\")
            .replace("\\\"", "\"")
            .replace("\\n\\n", "\\n")
            .replace("\\r\\n", "\\n")
            .trim();
        
        return result;
    } 
}
