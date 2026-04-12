package com.cirt.osint_dashboard.controller;

import com.cirt.osint_dashboard.model.PersonData;
import com.cirt.osint_dashboard.service.PersonService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller OSINT pour le CIRT - ANTIC.
 * Gère la recherche globale, les filtres et l'autocomplétion temps réel.
 */
@RestController
@RequestMapping("/search") // L'URL de base commence par /search
@CrossOrigin(origins = "http://localhost:3000")
public class PersonController {

    private final PersonService service;

    public PersonController(PersonService service) {
        this.service = service;
    }

    /* ============================================================
       1. AUTO-COMPLÉTION (Nouveau : Indispensable pour ton test)
       Accessible via : GET http://localhost:8080/search/suggest?value=ffr
       ============================================================ */
    @GetMapping("/suggest")
    public ResponseEntity<List<String>> suggest(@RequestParam String value) {
        // Appelle la méthode getSuggestions que nous avons codée dans PersonService
        List<String> suggestions = service.getSuggestions(value);
        return ResponseEntity.ok(suggestions);
    }

    /* ============================================================
       2. RECHERCHE GLOBALE & COMBINÉE
       ============================================================ */
    @GetMapping("/global")
    public ResponseEntity<Map<String, Object>> searchGlobal(
            @RequestParam String value,
            @RequestParam(required = false) String filterField,
            @RequestParam(required = false) String filterValue,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
    
        Page<PersonData> resultPage;

        if (filterField != null && !filterField.isEmpty() && filterValue != null && !filterValue.isEmpty()) {
            resultPage = service.searchAdvanced(value, filterField, filterValue, page, size);
        } else {
            resultPage = service.searchGlobal(value, page, size);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("results", resultPage.getContent());
        response.put("total", resultPage.getTotalElements());
        response.put("totalPages", resultPage.getTotalPages());
        response.put("currentPage", resultPage.getNumber());
        
        return ResponseEntity.ok(response);
    }

    /* ============================================================
       3. ROUTES ADMIN ET SANTÉ
       ============================================================ */

    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAll(@RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(Map.of(
            "results", service.getAllLimited(size), 
            "total", service.countAll()
        ));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of("status", "UP", "database", "MongoDB + Elasticsearch Connected"));
    }
}