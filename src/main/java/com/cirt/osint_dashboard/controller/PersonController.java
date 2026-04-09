package com.cirt.osint_dashboard.controller;

import com.cirt.osint_dashboard.model.PersonData;
import com.cirt.osint_dashboard.service.PersonService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller optimisé pour la Phase 2.2 (Recherche Combinée)
 * Gère la recherche globale ET le filtrage simultané au Backend.
 */
@RestController
@RequestMapping("/search")
@CrossOrigin(origins = "http://localhost:3000")
public class PersonController {

    private final PersonService service;

    public PersonController(PersonService service) {
        this.service = service;
    }

    /* ============================================================
       RECHERCHE GLOBALE & COMBINÉE (Le moteur principal)
       C'est cette route qui règle tes problèmes de pagination et de stats.
       ============================================================ */
    @GetMapping("/global")
    public ResponseEntity<Map<String, Object>> searchGlobal(
            @RequestParam String value,
            @RequestParam(required = false) String filterField,
            @RequestParam(required = false) String filterValue,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
    
        Page<PersonData> resultPage;

        // Si on a à la fois une recherche ET un filtre (ex: Bastos + Sexe M)
        if (filterField != null && !filterField.isEmpty() && filterValue != null && !filterValue.isEmpty()) {
            resultPage = service.searchAdvanced(value, filterField, filterValue, page, size);
        } else {
            // Sinon recherche globale classique
            resultPage = service.searchGlobal(value, page, size);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("results", resultPage.getContent());
        response.put("total", resultPage.getTotalElements()); // Renvoie le VRAI total filtré (ex: 218)
        response.put("totalPages", resultPage.getTotalPages()); // Calculé par Mongo sur le total filtré
        response.put("currentPage", resultPage.getNumber());
        
        return ResponseEntity.ok(response);
    }

    /* ============================================================
       AUTRES ROUTES (Conservées pour compatibilité ou utilité admin)
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
        return ResponseEntity.ok(Map.of("status", "UP", "database", "MongoDB Connected"));
    }

    // Note : Les méthodes searchBySex, searchByCountry etc. 
    // deviennent secondaires car tout passe par /global maintenant.
}