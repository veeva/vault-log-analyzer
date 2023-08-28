package com.veeva.vault.extension;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class TestRunHelper {

    private static final Logger LOGGER = Logger.getLogger(TestRunHelper.class);

    public static String[] getTestArgs(String filePath) throws IOException {
        File testFile = new File(filePath);
        String absolutePath = testFile.getAbsolutePath();

        List<String> lines = Files.readAllLines(Paths.get(absolutePath));
        return lines.toArray(new String[0]);
    }
}
