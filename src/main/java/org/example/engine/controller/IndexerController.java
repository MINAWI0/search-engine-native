package org.example.engine.controller;

import org.example.engine.service.IndexerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/indexer")
public class IndexerController {

    @Autowired
    private IndexerService indexerService;

    @PostMapping("/process-directory")
    public ResponseEntity<?> processDirectory() {
        try {
            String directoryPath = "/home/minaoui/Documents/web";
            indexerService.processDirectory(directoryPath);
            return ResponseEntity.ok("PDF documents processed successfully");
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Error processing PDF files: " + e.getMessage());
        }
    }
}