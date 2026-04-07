package com.cirt.osint_dashboard.controller;

import com.cirt.osint_dashboard.model.PersonData;
import com.cirt.osint_dashboard.service.PersonService;

import org.springframework.data.domain.Page;
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

    /* ============================================================
       RECHERCHE GLOBALE (Celle que tu dois utiliser maintenant)
       ============================================================ */
       @GetMapping("/global")
    public ResponseEntity<Map<String, Object>> searchGlobal(
        @RequestParam String value,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size) {
    
    Page<PersonData> resultPage = service.searchGlobal(value, page, size);
    
    Map<String, Object> response = new HashMap<>();
    response.put("results", resultPage.getContent()); // Les données de la page actuelle
    response.put("total", resultPage.getTotalElements()); // Le nombre TOTAL dans la base
    response.put("totalPages", resultPage.getTotalPages());
    response.put("currentPage", resultPage.getNumber());
    
    return ResponseEntity.ok(response);
}

    @GetMapping("/name")
    public ResponseEntity<Map<String, Object>> searchByName(@RequestParam String value, @RequestParam(defaultValue = "5000") int size) {
        return buildResponse(service.searchByName(value), size);
    }

    @GetMapping("/phone")
    public ResponseEntity<Map<String, Object>> searchByPhone(@RequestParam String value, @RequestParam(defaultValue = "5000") int size) {
        return buildResponse(service.searchByPhone(value), size);
    }

    @GetMapping("/country")
    public ResponseEntity<Map<String, Object>> searchByCountry(@RequestParam String value, @RequestParam(defaultValue = "5000") int size) {
        return buildResponse(service.searchByCountry(value), size);
    }

    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAll(@RequestParam(defaultValue = "5000") int size) {
        return ResponseEntity.ok(Map.of("results", service.getAllLimited(size), "total", service.countAll()));
    }

    // Dans PersonController.java

    @GetMapping("/address1") // Assure-toi que c'est bien "address1" pour matcher le Frontend
    public ResponseEntity<Map<String, Object>> searchByAddress(@RequestParam String value) {
        List<PersonData> results = service.searchByAddress(value, 5000);
        Map<String, Object> response = new HashMap<>();
        response.put("results", results);
        response.put("total", results.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sex")
    public ResponseEntity<Map<String, Object>> searchBySex(@RequestParam String value) {
        // On force la majuscule si besoin (M ou F)
        List<PersonData> results = service.filterBySex(value.toUpperCase());
        Map<String, Object> response = new HashMap<>();
        response.put("results", results);
        response.put("total", results.size());
        return ResponseEntity.ok(response);
    }

    private ResponseEntity<Map<String, Object>> buildResponse(List<PersonData> allResults, int size) {
        List<PersonData> limited = allResults.stream().limit(size).collect(Collectors.toList());
        Map<String, Object> response = new HashMap<>();
        response.put("results", limited);
        response.put("total", allResults.size());
        return ResponseEntity.ok(response);
    }
}