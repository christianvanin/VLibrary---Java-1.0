package com.vcontrol.controller.interpreter.command.symbol;

public enum PrivilegeLevel implements Symbolic {
    HIGH("#", 3),
    MEDIUM("^", 2),
    LOW("/", 1);

    private final String symbol;
    private final int level;

    PrivilegeLevel(String symbol, int level) {
        this.symbol = symbol;
        this.level = level;
    }

    public static PrivilegeLevel fromSymbol(String symbol) {
        for (PrivilegeLevel p : values()) if (p.getSymbol().equals(symbol)) return p;
        return null;
    }

    public String getSymbol() { return symbol; }
    public int getLevel() { return level; }
}

