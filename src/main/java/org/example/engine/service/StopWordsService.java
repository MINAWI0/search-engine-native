package org.example.engine.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

@Service
public class StopWordsService {
    private Set<String> stopWords;

    public StopWordsService() {
        stopWords = loadStopWords();
    }

    private Set<String> loadStopWords() {
        Set<String> words = new HashSet<>();
        try {
            Resource resource = new ClassPathResource("stopwords.txt");
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream())
            );
            String line;
            while ((line = reader.readLine()) != null) {
                words.add(line.trim().toLowerCase());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return words;
    }
    public boolean isStopWord(String word) {
        return stopWords.contains(word.toLowerCase());
    }
}