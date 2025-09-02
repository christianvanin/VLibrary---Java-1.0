package com.vcontrol.utility.config;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.vcontrol.utility.ResourceUtils;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

public abstract class LogManager {

    public enum LogLevel {
        INFO,
        ERROR,
        DEBUG,
        WARN,
        TRACE,
        CEF,
        COMMAND,
        AI;
    }

    public static void loadConfig(String configFilePath) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            context.reset();
            try (InputStream configStream = ResourceUtils.getResourceAsStream(configFilePath)) { configurator.doConfigure(configStream); }
            log(LogLevel.INFO, "Logback configuration loaded from: " + configFilePath);
        } catch (JoranException | IOException e) {
            LoggerFactory.getLogger(LogManager.class).error("Error loading logback config: {}", configFilePath, e);
        }
    }

    public static void log(Class<?> clazz, String logPath, LogLevel level, String message) {
        log(clazz, logPath, level, message, null);
    }

    public static void log(Class<?> clazz, LogLevel level, String message) {
        log(clazz, null, level, message, null);
    }

    public static void log(LogLevel level, String message) {
        log(getCallingClass(), null, level, message, null);
    }

    public static void log(String logPath, LogLevel level, String message) {
        log(getCallingClass(), logPath, level, message, null);
    }

    public static void log(Class<?> clazz, String logPath, LogLevel level, String message, Throwable cause) {
        try {
            checkValue(clazz, level, message);
            logWithContext(clazz, logPath, level, message, cause);
        } catch (IllegalArgumentException e) {
            logWithContext(getCallingClass(), null, LogLevel.ERROR, e.getMessage(), e);
        }
    }

    public static void log(Class<?> clazz, LogLevel level, String message, Throwable cause) {
        log(clazz, null, level, message, cause);
    }

    public static void log(LogLevel level, String message, Throwable cause) {
        log(getCallingClass(), null, level, message, cause);
    }

    public static void log(String logPath, LogLevel level, String message, Throwable cause) {
        log(getCallingClass(), logPath, level, message, cause);
    }

    private static void logWithContext(Class<?> clazz, String logPath, LogLevel level, String message, Throwable cause) {
        try {
            if (logPath != null) MDC.put("logPath", logPath);
            Logger logger = LoggerFactory.getLogger(clazz);
            logMessage(logger, level, message, cause);
        } finally {
            if (logPath != null) MDC.remove("logPath");
        }
    }

    private static Class<?> getCallingClass() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        if (stack.length >= 4) {
            String className = stack[3].getClassName();
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                return LogManager.class;
            }
        }
        return LogManager.class;
    }

    private static void checkValue(Class<?> clazz, LogLevel level, String message) throws IllegalArgumentException {
        if (clazz == null) throw new IllegalArgumentException("Parameter clazz cannot be null");
        if (level == null) throw new IllegalArgumentException("Parameter level cannot be null");
        if (message == null) throw new IllegalArgumentException("Parameter message cannot be null");
    }

    private static void logMessage(Logger logger, LogLevel level, String message, Throwable cause) {
        switch (level) {
            case INFO -> {
                if (cause == null) logger.info(message);
                else logger.info(message, cause);
            }
            case ERROR -> {
                if (cause == null) logger.error(message);
                else logger.error(message, cause);
            }
            case DEBUG -> {
                if (cause == null) logger.debug(message);
                else logger.debug(message, cause);
            }
            case WARN -> {
                if (cause == null) logger.warn(message);
                else logger.warn(message, cause);
            }
            case TRACE -> {
                if (cause == null) logger.trace(message);
                else logger.trace(message, cause);
            }
            case CEF -> {
                if (cause == null) logger.info("CEF: " + message);
                else logger.info("CEF: " + message, cause);
            }
            case COMMAND -> {
                if (cause == null) logger.info("COMMAND: " + message);
                else logger.info("COMMAND: " + message, cause);
            }
            case AI -> {
                if (cause == null) logger.info("AI: " + message);
                else logger.info("AI: " + message, cause);
            }
        }
    }
}
