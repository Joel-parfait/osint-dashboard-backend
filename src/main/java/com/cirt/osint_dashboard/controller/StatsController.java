package com.cirt.osint_dashboard.controller;

import com.cirt.osint_dashboard.model.PersonData;
import com.cirt.osint_dashboard.repository.PersonRepository;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/stats")
@CrossOrigin(origins = "http://localhost:3000")
public class StatsController {

    private final PersonRepository repository;

    public StatsController(PersonRepository repository) {
        this.repository = repository;
    }

    // ✅ Database statistics endpoint
    @GetMapping
    public Map<String, Object> getStats() {
        List<PersonData> allRecords = repository.findAll();

        Map<String, Long> byCountry = allRecords.stream()
                .filter(p -> p.getCountry() != null)
                .collect(Collectors.groupingBy(PersonData::getCountry, Collectors.counting()));

        Map<String, Long> byAddress = allRecords.stream()
                .filter(p -> p.getAddress1() != null)
                .collect(Collectors.groupingBy(PersonData::getAddress1, Collectors.counting()));

        Map<String, Long> byGender = allRecords.stream()
                .filter(p -> p.getSex() != null)
                .collect(Collectors.groupingBy(PersonData::getSex, Collectors.counting()));

        Map<String, Long> byMaritalStatus = allRecords.stream()
                .filter(p -> p.getMaritalstatus() != null)
                .collect(Collectors.groupingBy(PersonData::getMaritalstatus, Collectors.counting()));

        Map<String, Object> response = new HashMap<>();
        response.put("total_records", allRecords.size());
        response.put("by_country", byCountry);
        response.put("by_address", byAddress);
        response.put("by_gender", byGender);
        response.put("by_maritalstatus", byMaritalStatus);

        return response;
    }
}
