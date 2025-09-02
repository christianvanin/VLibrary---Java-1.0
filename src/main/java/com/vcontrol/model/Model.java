package com.vcontrol.model;

import com.vcontrol.controller.interpreter.command.symbol.PrivilegeLevel;
import com.vcontrol.model.Terminal.LineProcessor;
import com.vcontrol.model.book.BooksManager;
import com.vcontrol.utility.config.PathManager;

public class Model {
    private Terminal terminal;
    private BooksManager booksManager;

    public Model() {
        terminal = new Terminal(PrivilegeLevel.LOW, PathManager.get("config.log.session"), null);
        booksManager = new BooksManager(PathManager.get("data.books.folder"));
    }

    public void startTerminal(LineProcessor processor) { 
        terminal.setProcessor(processor);
        terminal.start(); 
    }

    public Terminal getTerminal() { return terminal; }
    public void setTerminal(Terminal terminal) { this.terminal = terminal; }
    public BooksManager getBooksManager() { return booksManager; }
    public void setBooksManager(BooksManager booksManager) { this.booksManager = booksManager; }
}
