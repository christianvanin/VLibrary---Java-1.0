package com.vcontrol.controller.interpreter.command;

import java.util.function.Consumer;

import com.vcontrol.controller.interpreter.command.symbol.PrivilegeLevel;
import com.vcontrol.utility.exception.InsufficientPrivilegeException;

public class Command {
    private CommandType type;
    private String body;
    private Consumer<String[]> function;
    private PrivilegeLevel privilegeLevel;
    private String description;

    public Command(CommandType type, String body, PrivilegeLevel privilegeLevel, String description, Consumer<String[]> function) {
        this.type = type;
        this.body = body;
        this.function = function;
        this.privilegeLevel = privilegeLevel;
        this.description = description;
    }

    public void execute(PrivilegeLevel privilegeLevel, String[] args) throws InsufficientPrivilegeException {
        if(privilegeLevel.getLevel() >= this.privilegeLevel.getLevel()) function.accept(args);
        else throw new InsufficientPrivilegeException(privilegeLevel, this.privilegeLevel);
    }

    public CommandType getType() { return type; }
    public void setType(CommandType type) { this.type = type; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public Consumer<String[]> getFunction() { return function; }
    public void setFunction(Consumer<String[]> function) { this.function = function; }
    public PrivilegeLevel getPrivilegeLevel() { return privilegeLevel; }
    public void setPrivilegeLevel(PrivilegeLevel privilegeLevel) { this.privilegeLevel = privilegeLevel; } 
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}