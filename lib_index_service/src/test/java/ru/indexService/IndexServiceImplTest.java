package ru.indexService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;
// Если мы захотим протестировать без конкретного индексера,то можем использовать моки
// но по заданию указано такие моменты самому решать, ну я и решил делать с конкретным
public class IndexServiceImplTest {

    private IndexServiceImpl indexService;
    private Path tempDir;

    @Before
    public void setUp() throws IOException {
        indexService = new IndexServiceImpl();
        tempDir = Files.createTempDirectory("index-test");
    }

    @After
    public void tearDown() throws IOException {
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
    public void addFile_singleFile_should_search_correctly() throws IOException {
        Path file = tempDir.resolve("test.txt");
        Files.writeString(file, "hello world hello");


        indexService.addFile(file);


        Set<Path> result = indexService.search("hello");
        assertEquals(1, result.size());
        assertTrue(result.contains(file));

        result = indexService.search("world");
        assertEquals(1, result.size());
        assertTrue(result.contains(file));

        result = indexService.search("noExist");
        assertTrue(result.isEmpty());
    }

    @Test
    public void addFile_multipleFiles_should_search_correctly() throws IOException {
        Path file1 = tempDir.resolve("file1.txt");
        Path file2 = tempDir.resolve("file2.txt");
        Path file3 = tempDir.resolve("file3.txt");

        Files.writeString(file1, "hello 1");
        Files.writeString(file2, "hello 2");
        Files.writeString(file3, "java 3");

        indexService.addFile(file1);
        indexService.addFile(file2);
        indexService.addFile(file3);


        assertEquals(2, indexService.search("hello").size());
        assertTrue(indexService.search("hello").contains(file1));
        assertTrue(indexService.search("hello").contains(file2));

        assertEquals(1, indexService.search("java").size());
        assertTrue(indexService.search("java").contains(file3));


    }

    @Test
    public void addFile_withRussianText_should_search_correctly() throws IOException {
        Path file = tempDir.resolve("ru.txt");
        Files.writeString(file, "Привет мир!");


        indexService.addFile(file);


        assertTrue(indexService.search("привет").contains(file));
        assertTrue(indexService.search("мир").contains(file));
    }

    @Test
    public void addFile_emptyFile_should_not_added() throws IOException {
        Path file = tempDir.resolve("empty.txt");
        Files.createFile(file);

        indexService.addFile(file);

        assertTrue(indexService.storageWord.isEmpty());
        assertTrue(indexService.storageFiles.containsKey(file));
        assertTrue(indexService.storageFiles.get(file).isEmpty());
    }

    @Test(expected = RuntimeException.class)
    public void addFile_nonExist_should_throw_exception() {
        Path path = Paths.get("/nonexistent/path/path.txt");

        indexService.addFile(path);
    }


    @Test
    public void addDir_withFiles_should_search_correctly() throws IOException {
        Path file1 = tempDir.resolve("file1.txt");
        Path file2 = tempDir.resolve("file2.txt");
        Path subDir = Files.createDirectory(tempDir.resolve("subdir"));
        Path file3 = subDir.resolve("file3.txt");

        Files.writeString(file1, "hello");
        Files.writeString(file2, "hello");
        Files.writeString(file3, "java");


        indexService.addDir(tempDir);


        assertEquals(2, indexService.search("hello").size());
        assertTrue(indexService.search("hello").contains(file1));
        assertTrue(indexService.search("hello").contains(file2));

        assertEquals(1, indexService.search("java").size());
        assertTrue(indexService.search("java").contains(file3));
    }




    @Test
    public void search_existingWord_should_search_correctly() throws IOException {
        Path file1 = tempDir.resolve("file1.txt");
        Path file2 = tempDir.resolve("file2.txt");

        Files.writeString(file1, "hello world");
        Files.writeString(file2, "hello java");

        indexService.addFile(file1);
        indexService.addFile(file2);


        Set<Path> result = indexService.search("hello");

        assertEquals(2, result.size());
        assertTrue(result.contains(file1));
        assertTrue(result.contains(file2));
    }

    @Test
    public void search_nonexistentWord_should_return_emptySet() throws IOException {
        Path file = tempDir.resolve("test.txt");
        Files.writeString(file, "hello world");

        indexService.addFile(file);


        Set<Path> result = indexService.search("nonexistent");


        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void search_caseMixed_should_search_correctly() throws IOException {
        Path file = tempDir.resolve("test.txt");
        Files.writeString(file, "Hello World");

        indexService.addFile(file);

        assertTrue(indexService.search("hello").contains(file));
        assertTrue(indexService.search("HELLO").contains(file));
    }


    @Test
    public void removeFile_existingFile_should_search_correctly() throws IOException {
        Path file1 = tempDir.resolve("file1.txt");
        Path file2 = tempDir.resolve("file2.txt");

        Files.writeString(file1, "hello world");
        Files.writeString(file2, "hello java");

        indexService.addFile(file1);
        indexService.addFile(file2);
        indexService.removeFile(file1);


        assertEquals(1, indexService.search("hello").size());
        assertTrue(indexService.search("hello").contains(file2));
        assertFalse(indexService.search("hello").contains(file1));

        assertTrue(indexService.search("world").isEmpty());
        assertEquals(1, indexService.search("java").size());
    }

    @Test
    public void removeFile_lastFileWithWord_should_search_correctly() throws IOException {
        Path file = tempDir.resolve("test.txt");
        Files.writeString(file, "hello");

        indexService.addFile(file);
        indexService.removeFile(file);

        assertTrue(indexService.search("hello").isEmpty());

    }

    @Test
    public void removeFile_nonexistentFile_should_do_nothing() {
        Path file = Paths.get("/nonexistent/file.txt");

        indexService.removeFile(file);

        assertTrue(indexService.storageWord.isEmpty());
        assertTrue(indexService.storageFiles.isEmpty());
    }


    @Test
    public void removeDir_withFiles_should_remove_all_file_in_directory() throws IOException {
        Path file1 = tempDir.resolve("file1.txt");
        Path file2 = tempDir.resolve("file2.txt");
        Path subDir = Files.createDirectory(tempDir.resolve("subdir"));
        Path file3 = subDir.resolve("file3.txt");

        Files.writeString(file1, "hello");
        Files.writeString(file2, "hello");
        Files.writeString(file3, "java");


        indexService.addDir(tempDir);
        indexService.removeDir(tempDir);


        assertTrue(indexService.search("hello").isEmpty());
        assertTrue(indexService.search("java").isEmpty());
        assertTrue(indexService.storageWord.isEmpty());
        assertTrue(indexService.storageFiles.isEmpty());
    }


    @Test
    public void concurrentAdd_should_work_correctly() throws IOException, InterruptedException {
        int threadCount = 12;
        int filesPerThread = 20;
        try (ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {

            for (int i = 0; i < threadCount; i++) {
                final int threadNum = i;
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < filesPerThread; j++) {
                            Path file = tempDir.resolve("thread" + threadNum + "_" + j + ".txt");
                            Files.writeString(file, "word" + threadNum + " common");
                            indexService.addFile(file);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.SECONDS);
        }

        assertEquals(threadCount * filesPerThread, indexService.storageFiles.size());


        Set<Path> commonResults = indexService.search("common");
        assertEquals(threadCount * filesPerThread, commonResults.size());


        for (int i = 0; i < threadCount; i++) {
            Set<Path> results = indexService.search("word" + i);
            assertEquals(filesPerThread, results.size());
        }
    }

    @Test
    public void concurrentAddAndRemove_should_work_correctly() throws IOException, InterruptedException {
        int threadCount = 12;
        int fileCount = 3000;
        Path[] files = new Path[fileCount];

        for (int i = 0; i < fileCount; i++) {
            files[i] = tempDir.resolve("OldFile" + i + ".txt");
            Files.writeString(files[i], "word" + i + " common");
            indexService.addFile(files[i]);
        }
        try (ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {

            for (int i = 0; i < fileCount / 2; i++) {
                final int index = i;
                executor.submit(() -> {
                    indexService.removeFile(files[index]);
                });
                executor.submit(() -> {
                    try {
                        Path newFile = tempDir.resolve("newFile" + index + ".txt");
                        Files.writeString(newFile, "word" + index + " common");
                        indexService.addFile(newFile);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.SECONDS);
        }

        assertEquals(fileCount, indexService.search("common").size());
    }
}
