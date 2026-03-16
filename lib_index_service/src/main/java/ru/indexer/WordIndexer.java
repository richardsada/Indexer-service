package ru.indexer;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class WordIndexer implements Indexer {


    @Override
    public Set<String> getIndexes(String text) {
        if (text == null || text.isEmpty()) {
            return Set.of();
        }

        text = text.toLowerCase();

        String[] words = text.split("[^a-zA-Zа-яА-Я0-9]+");
        return Arrays.stream(words)
                .filter(word -> !word.isEmpty())
                .collect(Collectors.toSet());
    }
}
