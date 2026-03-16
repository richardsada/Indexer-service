package ru.observer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.indexService.IndexServiceImpl;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;

import static org.junit.Assert.*;

public class ObserverTest {

    private IndexServiceImpl indexService;
    private Observer observer;
    private Path tempDir;

    @Before
    public void setUp() throws IOException {
        indexService = new IndexServiceImpl();
        observer = new Observer(indexService);
        tempDir = Files.createTempDirectory("observer-test");

        observer.registerAll(tempDir);
        observer.start();
    }

    @After
    public void tearDown() throws IOException {

        observer.stop();

        if (Files.exists(tempDir)) {
            Files.walk(tempDir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
    }

    @Test
    public void createFile_should_beIndexed() throws Exception {

        Path file = tempDir.resolve("file.txt");

        Files.writeString(file, "hello observer");

        Thread.sleep(500);

        assertTrue(indexService.search("hello").contains(file));
        assertTrue(indexService.search("observer").contains(file));
    }

    @Test
    public void modifyFile_should_reindex() throws Exception {

        Path file = tempDir.resolve("file.txt");

        Files.writeString(file, "java");
        indexService.addFile(file);

        Files.writeString(file, "python");

        Thread.sleep(500);

        assertFalse(indexService.search("java").contains(file));
        assertTrue(indexService.search("python").contains(file));
    }

    @Test
    public void deleteFile_should_removeFromIndex() throws Exception {

        Path file = tempDir.resolve("file.txt");

        Files.writeString(file, "delete me");
        indexService.addFile(file);

        Files.delete(file);

        Thread.sleep(500);

        assertFalse(indexService.search("delete").contains(file));
    }

    @Test
    public void createSubdirectory_should_beWatched() throws Exception {

        Path subDir = tempDir.resolve("sub");
        Files.createDirectory(subDir);

        Path file = subDir.resolve("nested.txt");
        Files.writeString(file, "nested file");

        Thread.sleep(500);

        assertTrue(indexService.search("nested").contains(file));
    }
}