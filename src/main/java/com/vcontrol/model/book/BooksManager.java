package com.vcontrol.model.book;

import java.util.HashMap;
import java.util.List;

import com.vcontrol.utility.ResourceUtils;
import com.vcontrol.utility.config.LogManager;
import com.vcontrol.utility.config.PathManager;

public class BooksManager {
    private HashMap<String, Book> books;
    private String bookCode;

    public BooksManager(String dataPath) {
        books = new HashMap<>();
        bookCode = "";
        loadBooks(dataPath);
    }

    private void loadBooks(String dataPath) {
        List<String> bookFolders = ResourceUtils.getSubfolderNames(dataPath);
        for (String folder : bookFolders) {
            Book book = new Book();
                String propertiesPath = dataPath + "/" + folder + "/" + PathManager.get("data.books.metadata");
            try {
                book.loadFromProperties(propertiesPath);
                books.put(book.getCode(), book);
            } catch (Exception e) {
                LogManager.log(LogManager.LogLevel.ERROR, "Failed to load book from: " + propertiesPath);
            }
        }
    }

    public Book getBookByCode(String code) { return books.get(code); }
    public HashMap<String, Book> getAllBooks() { return books; }

    public String getBookCode() { return bookCode; }
    public void setBookCode(String bookCode) { this.bookCode = bookCode; }
}