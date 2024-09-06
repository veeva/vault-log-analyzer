package com.veeva.vault.extension;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class TestRunHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestRunHelper.class);

    public static String[] getTestArgs(String filePath) throws IOException {
        File testFile = new File(filePath);
        String absolutePath = testFile.getAbsolutePath();

        List<String> lines = Files.readAllLines(Paths.get(absolutePath));
        return lines.toArray(new String[0]);
    }
}
