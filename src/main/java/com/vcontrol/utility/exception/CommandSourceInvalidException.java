package com.vcontrol.utility.exception;

public class CommandSourceInvalidException extends GeneralException {
    public CommandSourceInvalidException(String commandSource) { super("The command source is invalid: " + commandSource); }
}
