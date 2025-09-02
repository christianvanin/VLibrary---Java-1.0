package com.vcontrol.controller.interpreter.command;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.vcontrol.controller.interpreter.command.symbol.CommandSource;
import com.vcontrol.controller.interpreter.command.symbol.PrivilegeLevel;
import com.vcontrol.controller.interpreter.command.symbol.Symbolic;
import com.vcontrol.utility.exception.CommandNotFoundException;
import com.vcontrol.utility.exception.CommandSourceInvalidException;
import com.vcontrol.utility.exception.GeneralException;
import com.vcontrol.utility.exception.PrivilegeLevelInvalidException;

public class CommandLauncher {
    private String commandSource;
    private String privilegeLevel;
    private String commandKey;
    private String[] commandArgs;

    public CommandLauncher(String command) throws IllegalArgumentException {
        if (command == null || command.isEmpty())
            throw new IllegalArgumentException("Command cannot be null or empty");

        List<String> parts = parseCommand(command);

        if (parts.size() < 3)
            throw new IllegalArgumentException("Command must have at least 3 parts: symbol, type, body");

        symbolManagement(parts.get(0));
        commandKeyManagement(parts.get(1), parts.get(2));
        commandArgsManagement(parts);
    }


    private List<String> parseCommand(String command) {
        List<String> tokens = new ArrayList<>();
        Matcher m = Pattern.compile("@([^@]*)@|(\\S+)").matcher(command);
        while (m.find()) {
            if (m.group(1) != null) tokens.add(m.group(1));
            else tokens.add(m.group(2));
        }
        return tokens;
    }

    private void symbolManagement(String symbolPart) {
        commandSource = symbolPart.substring(0, 1);
        privilegeLevel = symbolPart.substring(1, 2);
    }

    private void commandKeyManagement(String commandType, String commandBody) {
        commandKey = commandType.toUpperCase() + " " + commandBody.toUpperCase();
    }

    private void commandArgsManagement(List<String> commandParts) {
        if (commandParts.size() > 3)
            commandArgs = commandParts.subList(3, commandParts.size()).toArray(new String[0]);
        else
            commandArgs = new String[0];
    }

    private void symbolIsValid() throws GeneralException {
        if (!Symbolic.symbolIsValid(commandSource, CommandSource.class))
            throw new CommandSourceInvalidException(commandSource);
        if (!Symbolic.symbolIsValid(privilegeLevel, PrivilegeLevel.class))
            throw new PrivilegeLevelInvalidException(privilegeLevel);
    }

    public void launchCommand(Map<String, Command> commandList) throws GeneralException {
        symbolIsValid();
        if (!commandList.containsKey(commandKey))
            throw new CommandNotFoundException(commandKey);
        commandList.get(commandKey).execute(PrivilegeLevel.fromSymbol(privilegeLevel), commandArgs);
    }

    public String getCommandSource() { return commandSource; }
    public String getPrivilegeLevel() { return privilegeLevel; }
    public String getCommandKey() { return commandKey; }
    public String[] getCommandArgs() { return commandArgs; }
}