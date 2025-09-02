package com.vcontrol.utility;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import com.vcontrol.utility.config.LogManager;

public final class ResourceUtils {

    private ResourceUtils() {}

    public static InputStream getResourceAsStream(String resourceName) throws IOException {
        InputStream input = ResourceUtils.class.getClassLoader().getResourceAsStream(resourceName);
        if (input == null) throw new IOException("Resource not found: " + resourceName);
        return input;
    }

    public static List<String> getSubfolderNames(String folderPath) {
        List<String> folderNames = new ArrayList<>();
        File folder = new File(folderPath);
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) folderNames.add(f.getName());
                }
            }
        } else LogManager.log(LogManager.LogLevel.ERROR, "Directory not found or is not a directory: " + folderPath);
        return folderNames;
    }
}

