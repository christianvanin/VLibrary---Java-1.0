package com.vcontrol.utility.exception;

import com.vcontrol.controller.interpreter.command.symbol.PrivilegeLevel;

public class InsufficientPrivilegeException extends GeneralException {
    public InsufficientPrivilegeException(PrivilegeLevel commandPrivilege, PrivilegeLevel requiredPrivilege) {
        super("Required privilege: " + requiredPrivilege + ", but command has privilege: " + commandPrivilege);
    }
}

