package ru.indexService;

import ru.indexer.Indexer;
import ru.indexer.WordIndexer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class IndexServiceImpl implements IndexService {
    ConcurrentHashMap<String, Set<Path>> storageWord;
    ConcurrentHashMap<Path, Set<String>> storageFiles;
    Indexer indexer;

    public IndexServiceImpl(){
        storageWord = new ConcurrentHashMap<>();
        indexer = new WordIndexer();
        storageFiles = new ConcurrentHashMap<>();
    }

    public IndexServiceImpl(Indexer indexer){
        storageWord = new ConcurrentHashMap<>();
        this.indexer = indexer;
        storageFiles = new ConcurrentHashMap<>();
    }


    @Override
    public void addFile(Path path) {
        try (Stream<String> lines = Files.lines(path)) {
            Set<String> words = lines
                    .flatMap(line -> indexer.getIndexes(line).stream())
                    .collect(java.util.stream.Collectors.toSet());

            for (String word : words) {
                storageWord
                        .computeIfAbsent(word, k -> ConcurrentHashMap.newKeySet())
                        .add(path);
            }
            storageFiles.put(path, words);

        } catch (IOException e) {
            throw new RuntimeException("Не удалось проиндексировать файл " + path, e);
        }
    }


    @Override
    public void addDir(Path dirPath) {
        try (Stream<Path> stream = Files.walk(dirPath)) {
            stream.filter(Files::isRegularFile)
                    .forEach(this::addFile);

        } catch (IOException e) {
            throw new RuntimeException("Не удалось обработать папку " + dirPath, e);
        }
    }


    @Override
    public Set<Path> search(String word) {
        return storageWord.getOrDefault(word.toLowerCase(), Set.of());
    }

    @Override
    public void removeFile(Path path) {
        Set<String> words = storageFiles.remove(path);

        if (words == null) {
            return;
        }

        for (String word : words) {
            Set<Path> paths = storageWord.get(word);

            if (paths != null) {
                paths.remove(path);
                if (paths.isEmpty()) {
                    storageWord.remove(word);
                }
            }
        }
    }

    @Override
    public void removeDir(Path dirPath) {
        try (Stream<Path> stream = Files.walk(dirPath)) {

            stream.forEach(path -> {
                if (Files.isRegularFile(path)) {
                    removeFile(path);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("Не удалось удалить папку " + dirPath, e);
        }
    }
}
