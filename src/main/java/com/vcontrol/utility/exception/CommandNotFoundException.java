package com.vcontrol.utility.exception;

public class CommandNotFoundException extends GeneralException {
    public CommandNotFoundException(String command) { super("Command not found: " + command); }
}
