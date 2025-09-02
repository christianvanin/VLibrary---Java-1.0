package com.vcontrol.controller.interpreter.command.symbol;

public enum CommandSource implements Symbolic {
    JAVA("@"),
    JAVASCRIPT("&"),
    TERMINAL("$");

    private final String symbol;

    CommandSource(String symbol) { this.symbol = symbol; }

    public String getSymbol() { return symbol; }
}

