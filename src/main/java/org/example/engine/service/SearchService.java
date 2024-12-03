package org.example.engine.service;

import org.example.engine.Repo.DocumentTermRepository;
import org.example.engine.models.DocumentTerm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchService {


    @Autowired
    private DocumentTermRepository documentTermRepository;

    @Autowired
    private IndexerService indexerService; // Inject IndexerService

    public List<String> search(String query) {
        // Tokenize and clean the query (similar to how it's done in the IndexerService)
        String[] queryTerms = query.toLowerCase().split("\\s+");
        Map<String, Double> queryVector = new HashMap<>();
        double queryMagnitude = 0.0;

        // Calculate the query vector and its magnitude
        for (String term : queryTerms) {
            if (!term.trim().isEmpty()) {
                double tfIdf = calculateQueryTfIdf(term); // Use the IDF from the IndexerService
                queryVector.put(term, tfIdf);
                queryMagnitude += tfIdf * tfIdf;
            }
        }
        queryMagnitude = Math.sqrt(queryMagnitude);

        // Get document vectors
        Map<String, Map<String, Double>> docVectors = getDocumentVectors();

        // Rank documents by cosine similarity
        List<Map.Entry<String, Double>> rankedDocs = rankDocuments(queryVector, queryMagnitude, docVectors);

        // Return the document IDs ranked by relevance
        return rankedDocs.stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private double calculateQueryTfIdf(String term) {
        // Get the IDF value from the IndexerService
        return indexerService.getInverseDocFrequencies().getOrDefault(term, 0.0); // Access the pre-calculated IDF
    }

    private Map<String, Map<String, Double>> getDocumentVectors() {
        // Retrieve all documents and their term frequencies from the database or index
        List<DocumentTerm> documentTerms = documentTermRepository.findAll();
        Map<String, Map<String, Double>> docVectors = new HashMap<>();

        for (DocumentTerm documentTerm : documentTerms) {
            docVectors
                    .computeIfAbsent(documentTerm.getDocId(), k -> new HashMap<>())
                    .put(documentTerm.getTerm(), documentTerm.getTfIdf());
        }
        return docVectors;
    }

    private List<Map.Entry<String, Double>> rankDocuments(Map<String, Double> queryVector, double queryMagnitude, Map<String, Map<String, Double>> docVectors) {
        List<Map.Entry<String, Double>> docRankings = new ArrayList<>();

        for (Map.Entry<String, Map<String, Double>> docEntry : docVectors.entrySet()) {
            String docId = docEntry.getKey();
            Map<String, Double> docVector = docEntry.getValue();

            double dotProduct = 0.0;
            double docMagnitude = 0.0;

            // Calculate dot product of query and document vectors
            for (Map.Entry<String, Double> queryTermEntry : queryVector.entrySet()) {
                String term = queryTermEntry.getKey();
                double queryTfIdf = queryTermEntry.getValue();
                double docTfIdf = docVector.getOrDefault(term, 0.0);

                dotProduct += queryTfIdf * docTfIdf;
                docMagnitude += docTfIdf * docTfIdf;
            }

            docMagnitude = Math.sqrt(docMagnitude);

            // Compute cosine similarity (cosine similarity = dotProduct / (magnitudeQuery * magnitudeDoc))
            if (docMagnitude > 0) {
                double similarity = dotProduct / (queryMagnitude * docMagnitude);
                docRankings.add(new AbstractMap.SimpleEntry<>(docId, similarity));
            }
        }

        // Sort documents by similarity in descending order
        docRankings.sort((entry1, entry2) -> Double.compare(entry2.getValue(), entry1.getValue()));

        return docRankings;
    }
}
