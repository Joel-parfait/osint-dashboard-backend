package com.cirt.osint_dashboard.controller;

import com.cirt.osint_dashboard.model.PersonData;
import com.cirt.osint_dashboard.service.PersonService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/search")
@CrossOrigin(origins = "https://osint-dashboard-frontend.vercel.app") // Autorise React vers domaine (ici c'est vercel)
//@CrossOrigin(origins = "http://localhost:3000") // Autorise React en local

public class PersonController {

    private final PersonService service;

    public PersonController(PersonService service) {
        this.service = service;
    }

    @GetMapping("/suggest")
    @CrossOrigin(origins = "http://localhost:3000") // Double sécurité CORS
    public ResponseEntity<List<String>> suggest(@RequestParam String value) {
        System.out.println("🔍 [CIRT-API] Demande de suggestion pour: " + value);
        List<String> suggestions = service.getSuggestions(value);
        System.out.println("✅ [CIRT-API] Suggestions trouvées: " + suggestions.size());
        return ResponseEntity.ok(suggestions);
    }

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

    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAll(@RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(Map.of(
            "results", service.getAllLimited(size), 
            "total", service.countAll()
        ));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of("status", "UP", "database", "OSINT Hybrid Engine Active"));
    }

    @GetMapping("/sync")
    public ResponseEntity<Map<String, String>> syncData() {
        try {
            System.out.println("🔄 [CIRT-SYNC] Début de la synchronisation manuelle...");
            service.syncAllToElasticsearch(); // Assure-toi que cette méthode existe dans ton PersonService
            return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Synchronisation terminée avec succès"
            ));
        } catch (Exception e) {
            System.err.println("❌ [CIRT-SYNC] Erreur : " + e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "ERROR",
                "message", e.getMessage()
            ));
        }
    }
}