package com.vcontrol.controller.interpreter.command.symbol;

public interface Symbolic {
    public String getSymbol();

    public static <T extends Enum<T> & Symbolic> boolean symbolIsValid(String symbol, Class<T> enumClass) {
        if (symbol == null) return false;
        for (T constant : enumClass.getEnumConstants()) {
            if (constant.getSymbol().equals(symbol)) return true;
        }
        return false;
    }

}