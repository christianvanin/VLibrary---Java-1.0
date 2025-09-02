package com.vcontrol;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;
import com.vcontrol.utility.exception.PythonScriptException;

public class PythonScriptRunner {

    private static String getPythonCommand() {
        String custom = System.getenv("PYTHON_CMD");
        if (custom != null && !custom.isBlank()) {
            System.out.println("[DEBUG] Usando PYTHON_CMD da environment: " + custom);
            return custom;
        }

        String os = System.getProperty("os.name").toLowerCase();
        String pythonCmd = os.contains("win") ? "python" : "python3";
        System.out.println("[DEBUG] OS rilevato: " + os + ", comando Python scelto: " + pythonCmd);
        return pythonCmd;
    }

    public static String runScript(String scriptPath, String inputText, int timeoutSeconds, boolean sanitizeInput) throws PythonScriptException {
        StringBuilder output = new StringBuilder();
        StringBuilder errorOutput = new StringBuilder();
        File tempFile = null;

        try {
            String processedText = sanitizeInput ? 
                inputText.replaceAll("[^\\p{L}\\p{N}\\p{P}\\p{Z}]", "") : 
                inputText;

            tempFile = Files.createTempFile("python_input_", ".txt").toFile();
            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(tempFile), StandardCharsets.UTF_8))) {
                writer.write(processedText);
            }
            String pythonCmd = getPythonCommand();
            String[] cmd = new String[]{pythonCmd, scriptPath, tempFile.getAbsolutePath()};

            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.environment().put("PYTHONIOENCODING", "utf-8");
            Process p = pb.start();

            ExecutorService executor = Executors.newFixedThreadPool(2);

            Future<?> stdoutTask = executor.submit(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        synchronized(output) { output.append(line).append("\n"); }
                    }
                } catch (IOException e) { }
            });

            Future<?> stderrTask = executor.submit(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(p.getErrorStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        synchronized(errorOutput) { errorOutput.append(line).append("\n"); }
                    }
                } catch (IOException e) { }
            });

            stdoutTask.get(timeoutSeconds, TimeUnit.SECONDS);
            stderrTask.get(timeoutSeconds, TimeUnit.SECONDS);

            executor.shutdown();

            int exitCode = p.waitFor();
            if (exitCode != 0) {
                throw new PythonScriptException("[PythonScriptRunner ERROR] Script '" + scriptPath + 
                        "' failed (exit code " + exitCode + "):\n" + errorOutput.toString());
            }

        } catch (PythonScriptException e) {
            throw e;
        } catch (Exception e) {
            throw new PythonScriptException("[PythonScriptRunner ERROR] Exception while running script '" + 
                    scriptPath + "': " + e.getMessage());
        } finally {
            if (tempFile != null && tempFile.exists()) tempFile.delete();
        }
        return output.toString().trim();
    }

    public static String runScript(String scriptPath, String inputText) throws PythonScriptException {
        return runScript(scriptPath, inputText, 60, true);
    }

    public static String runScript(String scriptPath, String inputText, int timeoutSeconds) throws PythonScriptException {
        return runScript(scriptPath, inputText, timeoutSeconds, true);
    }
    public static String runScriptWithArgs(String scriptPath, List<String> args, int timeoutSeconds) throws PythonScriptException {
        StringBuilder output = new StringBuilder();
        StringBuilder errorOutput = new StringBuilder();

        try {
            List<String> cmd = new ArrayList<>();
            cmd.add(getPythonCommand());
            cmd.add(scriptPath);
            cmd.addAll(args);
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.environment().put("PYTHONIOENCODING", "utf-8");
            Process p = pb.start();

            ExecutorService executor = Executors.newFixedThreadPool(2);
            Future<?> stdoutTask = executor.submit(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        synchronized(output) { output.append(line).append("\n"); }
                    }
                } catch (IOException e) { }
            });

            Future<?> stderrTask = executor.submit(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(p.getErrorStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        synchronized(errorOutput) { errorOutput.append(line).append("\n"); }
                    }
                } catch (IOException e) { }
            });

            stdoutTask.get(timeoutSeconds, TimeUnit.SECONDS);
            stderrTask.get(timeoutSeconds, TimeUnit.SECONDS);

            executor.shutdown();

            int exitCode = p.waitFor();
            if (exitCode != 0) {
                throw new PythonScriptException("[PythonScriptRunner ERROR] Script '" + scriptPath + 
                        "' failed (exit code " + exitCode + "):\n" + errorOutput.toString());
            }

        } catch (PythonScriptException e) {
            throw e;
        } catch (Exception e) {
            throw new PythonScriptException("[PythonScriptRunner ERROR] Exception while running script '" + 
                    scriptPath + "': " + e.getMessage());
        }
        return output.toString().trim();
    }

    public static String runScriptWithArgs(String scriptPath, List<String> args) throws PythonScriptException {
        return runScriptWithArgs(scriptPath, args, 60);
    }
}