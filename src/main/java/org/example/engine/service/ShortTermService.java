package org.example.engine.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

@Service
public class ShortTermService {
    private Set<String> importantShortTerms = new HashSet<>();

    @PostConstruct
    private void init() {
        importantShortTerms = loadImportantShortTerms();
    }

    private Set<String> loadImportantShortTerms() {
        Set<String> terms = new HashSet<>();
        // Use ClassPathResource to read from resources folder
        String filename = "important_short_terms.txt";
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new ClassPathResource(filename).getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Convert to lowercase right when adding to the set
                terms.add(line.trim().toLowerCase());
            }
        } catch (IOException e) {
            // Log the error or handle it as you see appropriate
            System.out.println("Failed to load important short terms from file: {}");
        }
        return terms;
    }

    // Method to check if a word is an important short term
    public boolean isImportantShortTerm(String word) {
        // Ensure the input word is also in lowercase to match how terms are stored
        return importantShortTerms.contains(word.toLowerCase());
    }
}