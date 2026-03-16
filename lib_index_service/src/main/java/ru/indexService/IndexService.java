package ru.indexService;

import ru.indexer.Indexer;

import java.nio.file.Path;
import java.util.Set;

public interface IndexService {

    void addFile(Path path);

    void addDir(Path DirPath);

    Set<Path> search(String word);

    void removeFile(Path path);

    void removeDir(Path DirPath);



}
