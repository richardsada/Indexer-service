package ru.indexer;

import java.util.Set;

public interface Indexer {

    Set<String> getIndexes(String text);
}
