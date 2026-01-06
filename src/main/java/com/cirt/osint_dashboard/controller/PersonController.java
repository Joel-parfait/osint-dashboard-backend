package com.cirt.osint_dashboard.controller;

import com.cirt.osint_dashboard.model.PersonData;
import com.cirt.osint_dashboard.service.PersonService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/search")
@CrossOrigin(origins = "http://localhost:3000")
public class PersonController {

    private final PersonService service;

    public PersonController(PersonService service) {
        this.service = service;
    }

    // Health check
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "healthy");
    }

    // NAME SEARCH - UPDATED WITH SIZE PARAMETER
    @GetMapping("/name")
    public ResponseEntity<Map<String, Object>> searchByName(
            @RequestParam String value,
            @RequestParam(defaultValue = "5000") int size) {  // ADDED

        List<PersonData> allResults = service.searchByName(value);
        List<PersonData> limitedResults = allResults.stream()
                .limit(size)
                .collect(Collectors.toList());

        return buildWithTotal(limitedResults, allResults.size());
    }

    // PHONE SEARCH - UPDATED WITH SIZE PARAMETER
    @GetMapping("/phone")
    public ResponseEntity<Map<String, Object>> searchByPhone(
            @RequestParam String value,
            @RequestParam(defaultValue = "5000") int size) {  // ADDED

        List<PersonData> allResults = service.searchByPhone(value);
        List<PersonData> limitedResults = allResults.stream()
                .limit(size)
                .collect(Collectors.toList());

        return buildWithTotal(limitedResults, allResults.size());
    }

    // EMAIL SEARCH - UPDATED WITH SIZE PARAMETER
    @GetMapping("/email")
    public ResponseEntity<Map<String, Object>> searchByEmail(
            @RequestParam String value,
            @RequestParam(defaultValue = "5000") int size) {  // ADDED

        List<PersonData> allResults = service.searchByEmail(value);
        List<PersonData> limitedResults = allResults.stream()
                .limit(size)
                .collect(Collectors.toList());

        return buildWithTotal(limitedResults, allResults.size());
    }

    // ADDRESS SEARCH - ALREADY HAS SIZE
    @GetMapping("/address")
    public ResponseEntity<Map<String, Object>> searchByAddress(
            @RequestParam String value,
            @RequestParam(defaultValue = "5000") int size) {

        return build(service.searchByAddress(value, size));
    }

    // SHOW ALL (LIMITED)
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAll(
            @RequestParam(defaultValue = "5000") int size) {

        List<PersonData> results = service.getAllLimited(size);
        long total = service.countAll();

        Map<String, Object> response = new HashMap<>();
        response.put("results", results);
        response.put("total", total);
        return ResponseEntity.ok(response);
    }

    // OLD BUILD METHOD - for endpoints that already handle size
    private ResponseEntity<Map<String, Object>> build(List<PersonData> results) {
        return ResponseEntity.ok(
                Map.of(
                        "results", results,
                        "total", results.size()
                )
        );
    }

    // NEW BUILD METHOD - returns limited results but shows actual total
    private ResponseEntity<Map<String, Object>> buildWithTotal(
            List<PersonData> limitedResults,
            int actualTotal) {
        Map<String, Object> response = new HashMap<>();
        response.put("results", limitedResults);
        response.put("total", actualTotal);
        return ResponseEntity.ok(response);
    }
}