package com.vcontrol.controller.interpreter;

import java.util.HashMap;
import java.util.Map;

import com.vcontrol.controller.Controller;
import com.vcontrol.controller.interpreter.command.Command;
import com.vcontrol.controller.interpreter.command.CommandLauncher;
import com.vcontrol.controller.interpreter.command.CommandType;
import com.vcontrol.controller.interpreter.command.symbol.PrivilegeLevel;
import com.vcontrol.utility.config.LogManager;
import com.vcontrol.utility.config.LogManager.LogLevel;
import com.vcontrol.utility.exception.GeneralException;

public class Interpreter {
    private Controller controller;
    private Map<String, Command> commands;

    public Interpreter(Controller controller) {
        this.controller = controller;
        initializeCommands();
        registerCommands();
    }
    
    private void initializeCommands() { this.commands = new HashMap<>(); }

    private void registerCommands() {
        registerCommand(new Command(CommandType.AI, "ASK", PrivilegeLevel.LOW, null, (arg) -> { controller.rewriteAI(arg); }));
        registerCommand(new Command(CommandType.TERMINAL, "CLEAR", PrivilegeLevel.LOW, null, (arg) -> { controller.clearTerminal(); }));
        registerCommand(new Command(CommandType.TERMINAL, "RUN", PrivilegeLevel.LOW, null, (arg) -> { controller.startTerminal(); }));
        registerCommand(new Command(CommandType.PRINT, "LOGJS", PrivilegeLevel.LOW, null, (arg) -> { controller.stampJSLog(arg); }));
        registerCommand(new Command(CommandType.BOOK, "RELOAD", PrivilegeLevel.LOW, null, (arg) -> { controller.inizializeResources(); }));
        registerCommand(new Command(CommandType.BOOK, "GET", PrivilegeLevel.LOW, null, (arg) -> { controller.getBookActive(); }));
        registerCommand(new Command(CommandType.BOOK, "SET", PrivilegeLevel.LOW, null, (arg) -> { controller.setBookActiveCode(arg); }));
        registerCommand(new Command(CommandType.SETTING, "PDFVIEW", PrivilegeLevel.LOW, null, (arg) -> { controller.openPDFView(); }));
        registerCommand(new Command(CommandType.SETTING, "HOMEVIEW", PrivilegeLevel.LOW, null, (arg) -> { controller.openHomeView(); }));
        registerCommand(new Command(CommandType.GET, "TEXT", PrivilegeLevel.LOW, null, (arg) -> { controller.getCustomText(arg); }));
    }

    private void registerCommand(Command command) { commands.put(command.getType() + " " +command.getBody(), command); }

    public void interpretCommand(String command) {
        try { 
            CommandLauncher launcher = new CommandLauncher(command); 
            try { launcher.launchCommand(commands); 
            LogManager.log(LogLevel.COMMAND, command); }
            catch (GeneralException e) { LogManager.log(LogLevel.ERROR, e.getMessage()); }
        }
        catch (IllegalArgumentException e) {
            LogManager.log(LogLevel.INFO, command);
            return;
        }
    }
}