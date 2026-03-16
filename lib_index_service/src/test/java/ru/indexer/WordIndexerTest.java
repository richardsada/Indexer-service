package ru.indexer;


import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WordIndexerTest {
    private Indexer indexer;

    @Before
    public void setUp() {
        indexer = new WordIndexer();
    }

    @Test
    public void getIndex_with_empty_text_should_return_empty_set() {
        Set<String> words = indexer.getIndexes("");
        assertTrue(words.isEmpty());
    }

    @Test
    public void getIndex_with_different_symbols_should_extract_words_and_numbers() {
        Set<String> words = indexer.getIndexes("1 123! & word1? word2--word3");
        assertEquals(Set.of("1", "123", "word1", "word2", "word3"), words);
    }

    @Test
    public void getIndex_with_ru_words_should_extract_ru_words() {
        Set<String> words = indexer.getIndexes("ру 2слово уникальное231312слово");
        assertEquals(Set.of("ру", "2слово", "уникальное231312слово"), words);
    }

    @Test
    public void getIndex_from_null_should_return_emptySet() {
        Set<String> words = indexer.getIndexes(null);
        assertTrue(words.isEmpty());
    }

    @Test
    public void getIndex_with_some_lines_should_extract_words_from_multiline_text() {
        Set<String> words = indexer.getIndexes("""
              Строка1
              Строка2
              Строка3
              """);

        assertEquals(Set.of("строка1", "строка2", "строка3"), words);
    }

    @Test
    public void getIndex_with_duplicate_words_should_return_unique_set() {
        Set<String> words = indexer.getIndexes("word word word test test");
        assertEquals(Set.of("word", "test"), words);
    }

    @Test
    public void getIndex_with_mixed_case_should_extract_one_word() {
        Set<String> words = indexer.getIndexes("Hello HELLO hello HeLlO");
        assertEquals(Set.of("hello"), words);
    }
}