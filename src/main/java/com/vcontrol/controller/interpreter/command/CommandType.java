package com.vcontrol.controller.interpreter.command;

public enum CommandType {
    HELP("HELP"),
    GET("GET"),
    PRINT("PRINT"),
    DELETE("DELETE"),
    SETTING("SETTING"),
    TERMINAL("TERMINAL"),
    BOOK("BOOK"),
    AI("AI");

    private final String code;

    CommandType(String code) { this.code = code; }
    
    public String getCode() { return code; }
}
