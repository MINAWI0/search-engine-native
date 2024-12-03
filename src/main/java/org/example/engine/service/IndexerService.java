package org.example.engine.service;

import org.example.engine.Repo.DocumentTermRepository;
import org.example.engine.models.DocumentTerm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class IndexerService {

    public Map<String, Double> getInverseDocFrequencies() {
        return inverseDocFrequencies;
    }

    private Map<String, Map<String, Double>> termFrequencies = new HashMap<>();
    private Map<String, Double> inverseDocFrequencies = new HashMap<>();
    private Map<String, Map<String, Double>> tfIdfScores = new HashMap<>();

    @Autowired
    private DocumentTermRepository documentTermRepository;

    @Autowired
    private PDFService pdfService;
    @Autowired
    private ShortTermService shortTermService; // Inject ShortTermService
    @Autowired
    private StopWordsService stopWordsService; // Inject StopWordsService

    public void processDirectory(String directoryPath) throws IOException {
        documentTermRepository.deleteAll();
        File directory = new File(directoryPath);
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Provided path is not a directory");
        }
        File[] pdfFiles = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));
        if (pdfFiles == null || pdfFiles.length == 0) {
            throw new IllegalArgumentException("No PDF files found in the directory");
        }
        System.out.println("Found " + pdfFiles.length + " PDF files.");
        // Process each PDF file
        for (File pdfFile : pdfFiles) {
            System.out.println("Processing file: " + pdfFile.getName());
            String content = pdfService.extractText(pdfFile);
            if (content == null || content.isEmpty()) {
                System.out.println("No content extracted from " + pdfFile.getName());
                continue; // Skip this document if no content is extracted
            }
            System.out.println("Extracted content length: " + content.length());
            processDocument(pdfFile.getName(), content);
        }
        // Calculate IDF and TF-IDF scores
        calculateInverseDocFrequencies(pdfFiles.length);
        calculateTfIdfScores();
        saveToDatabase();
    }

    private void processDocument(String docId, String content) {
        System.out.println("Starting to process document: " + docId);
        Map<String, Integer> wordCounts = new HashMap<>();
        int totalWords = 0;

        // Tokenize and clean the text
        String[] words = content.toLowerCase()
                .replaceAll("[^a-zA-Z0-9\\s-]", " ") // Keep hyphens and valid characters
                .split("\\s+");

        for (String word : words) {
            if (!word.trim().isEmpty() && (shortTermService.isImportantShortTerm(word) || (word.length() > 3 && !stopWordsService.isStopWord(word)))) {
                wordCounts.merge(word, 1, Integer::sum);
                totalWords++;
            }
        }
        System.out.println("Total words counted for " + docId + ": " + totalWords);
        System.out.println("Word counts for " + docId + ": " + wordCounts);

        // Extract numbers with units (e.g., 24GB, 450W)
        Pattern pattern = Pattern.compile("(\\d+\\s?[A-Za-z]+)"); // Find numbers with units
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            String term = matcher.group(1).toLowerCase().replaceAll("\\s", "");
            if (shortTermService.isImportantShortTerm(term) || (!stopWordsService.isStopWord(term) && term.length() > 2)) {
                wordCounts.merge(term, 1, Integer::sum);
                totalWords++;
            }
        }

        // Calculate term frequencies for this document
        for (Map.Entry<String, Integer> entry : wordCounts.entrySet()) {
            String word = entry.getKey();
            double tf = (double) entry.getValue() / totalWords;

            termFrequencies.computeIfAbsent(word, k -> new HashMap<>())
                    .put(docId, tf);
        }
        System.out.println("Term Frequencies for " + docId + ": " + termFrequencies.get(docId));

    }

    private void calculateInverseDocFrequencies(int totalDocuments) {
        System.out.println("Calculating IDF for " + totalDocuments + " documents.");
        for (Map.Entry<String, Map<String, Double>> entry : termFrequencies.entrySet()) {
            String word = entry.getKey();
            int documentFrequency = entry.getValue().size();
            double idf = Math.log((double) totalDocuments / documentFrequency);
            inverseDocFrequencies.put(word, idf);
        }
    }

    private void calculateTfIdfScores() {
        System.out.println("Calculating TF-IDF Scores.");
        for (Map.Entry<String, Map<String, Double>> termFreq : termFrequencies.entrySet()) {
            String word = termFreq.getKey();
            double idf = inverseDocFrequencies.get(word);

            for (Map.Entry<String, Double> docTf : termFreq.getValue().entrySet()) {
                String docId = docTf.getKey();
                double tf = docTf.getValue();
                double tfIdf = tf * idf;

                tfIdfScores.computeIfAbsent(docId, k -> new HashMap<>())
                        .put(word, tfIdf);
            }
        }
    }

    private void saveToDatabase() {
        List<DocumentTerm> documentTerms = new ArrayList<>();

        for (Map.Entry<String, Map<String, Double>> docScores : tfIdfScores.entrySet()) {
            String docId = docScores.getKey();
            for (Map.Entry<String, Double> wordScore : docScores.getValue().entrySet()) {
                String word = wordScore.getKey();
                double tfIdf = wordScore.getValue();
                DocumentTerm documentTerm = new DocumentTerm();
                documentTerm.setDocId(docId);
                documentTerm.setTerm(word);
                documentTerm.setTfIdf(tfIdf);

                documentTerms.add(documentTerm);
            }
        }

        documentTermRepository.saveAll(documentTerms);
    }
}
