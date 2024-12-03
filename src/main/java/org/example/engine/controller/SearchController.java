package org.example.engine.controller;

import org.example.engine.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @GetMapping("/query")
    public ResponseEntity<?> search(@RequestParam String query) {
        try {
            List<String> result = searchService.search(query);
            return ResponseEntity.ok(result);  // Return the ranked document IDs as a response
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error during search: " + e.getMessage());
        }
    }
}
