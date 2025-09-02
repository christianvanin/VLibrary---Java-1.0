package com.vcontrol.model.book;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import com.vcontrol.utility.config.LogManager;
import com.vcontrol.utility.config.PathManager;
import com.vcontrol.utility.exception.BookLoadPropertiesError;

public class Book {
    private String title;
    private String author;
    private String code;
    private String subject;
    private int bodyPages;
    private int indexPages;

    public Book() {
        this.title = "";
        this.author = "";
        this.code = "";
        this.subject = "";
        this.bodyPages = 0;
        this.indexPages = 0;
    }

    public Book(String title, String author, String code, String subject, int bodyPages, int indexPages) {
        this.title = title;
        this.author = author;
        this.code = code;
        this.subject = subject;
        this.bodyPages = bodyPages;
        this.indexPages = indexPages;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public int getBodyPages() { return bodyPages; }
    public void setBodyPages(int bodyPages) { this.bodyPages = bodyPages; }
    public int getIndexPages() { return indexPages; }
    public void setIndexPages(int indexPages) { this.indexPages = indexPages; }

    public String toJSON() {
        return "{"
                + "\"title\":\"" + escapeJSON(getTitle()) + "\","
                + "\"author\":\"" + escapeJSON(getAuthor()) + "\","
                + "\"code\":\"" + escapeJSON(getCode()) + "\","
                + "\"subject\":\"" + escapeJSON(getSubject()) + "\","
                + "\"bodyPages\":" + getBodyPages() + ","
                + "\"indexPages\":" + getIndexPages() + ","
                + "\"cover\":\"" + escapeJSON(getCoverPath()) + "\","
                + "\"bookPath\":\"" + null + "\""
                + "}";
    }

    public String pathToJSON() {
        return "{" 
                + "\"bodyPages\":" + getBodyPages() + ","
                + "\"indexPages\":" + getIndexPages() + ","
                + "\"bodyPagesPath\":\"" + escapeJSON(getBodyPagesPath()) + "\","
                + "\"indexPagesPath\":\"" + escapeJSON(getIndexPagesPath()) + "\""
                + "}";
    }

    public void loadFromProperties(String propertiesPath) throws BookLoadPropertiesError {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(propertiesPath)) {
            props.load(fis);

            String title = props.getProperty("nome");
            String author = props.getProperty("autore");
            String code = props.getProperty("code");
            String subject = props.getProperty("materia");
            String bodyPagesStr = props.getProperty("pagine");
            String indexPagesStr = props.getProperty("indice");


            if (title != null && author != null && code != null && subject != null) {
                this.title = title;
                this.author = author;
                this.code = code;
                this.subject = subject;
                
                try {
                    this.bodyPages = (bodyPagesStr != null) ? Integer.parseInt(bodyPagesStr) : 0;
                    this.indexPages = (indexPagesStr != null) ? Integer.parseInt(indexPagesStr) : 0;
                } catch (NumberFormatException e) {
                    LogManager.log(LogManager.LogLevel.ERROR, "Invalid page numbers in properties file: " + propertiesPath);
                    this.bodyPages = 0;
                    this.indexPages = 0;
                }
                
                LogManager.log(LogManager.LogLevel.INFO, "Book loaded -> " + code);
            } else {
                LogManager.log(LogManager.LogLevel.ERROR, "Missing required book properties in: " + propertiesPath);
            }
        } catch (IOException e) {
            throw new BookLoadPropertiesError("Error loading book properties from: " + propertiesPath);
        }
    }

    private String escapeJSON(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }

    public String getBookPath() {
        String projectPath = new java.io.File("").getAbsolutePath().replace("\\", "/");
        return "file:///" + projectPath + "/data/books/" + code + "/" + PathManager.get("book.path");
    }

    public String getCoverPath() {
        String projectPath = new java.io.File("").getAbsolutePath().replace("\\", "/");
        return "file:///" + projectPath + "/data/books/" + code + "/" + PathManager.get("book.cover");
    }

    public String getBodyPagesPath() {
        String projectPath = new java.io.File("").getAbsolutePath().replace("\\", "/");
        return "file:///" + projectPath + "/data/books/" + code + "/" + PathManager.get("book.pages.body");
    }

    public String getIndexPagesPath() {
        String projectPath = new java.io.File("").getAbsolutePath().replace("\\", "/");
        return "file:///" + projectPath + "/data/books/" + code + "/" + PathManager.get("book.pages.index");
    }
}