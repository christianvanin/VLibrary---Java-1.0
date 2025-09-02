package com.vcontrol.utility.exception;

public class PrivilegeLevelInvalidException extends GeneralException {
    public PrivilegeLevelInvalidException(String privilegeLevel) { super("The privilege level is invalid: " + privilegeLevel); }

}
