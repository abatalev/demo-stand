package com.batal.actions.model;

import com.batal.actions.model.fetchers.FileFetcher;
import com.batal.actions.model.fixers.FileFixer;
import com.batal.actions.model.messages.Message;
import com.batal.actions.model.savers.FileSaver;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class FileTest {

    @Test
    void checkFiles() throws IOException {

        FileUtils.copyDirectory(new File("src/test/resources/files"), new File("target/files/in"));

        FileFetcher fetcher = new FileFetcher("target/files/in", "target/files/in/proc");
        FileSaver saver = new FileSaver("target/files/out");
        FileFixer fixer = new FileFixer("target/files/in/good", "target/files/in/bad");

        Message message = fetcher.get(null);
        if (message != null) {
            saver.put(null, message);
            fixer.fix(null, message, 2, "ok");
        }

        assertFalse(new File("target/files/in", "1.txt").exists());
        assertFalse(new File("target/files/in/proc", "1.txt").exists());
        assertTrue(new File("target/files/out", "1.txt").exists());
        assertTrue(new File("target/files/in/good", "1.txt").exists());
    }
}
