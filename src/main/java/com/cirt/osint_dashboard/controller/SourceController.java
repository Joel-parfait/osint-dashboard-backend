package com.cirt.osint_dashboard.controller;

import com.cirt.osint_dashboard.model.Source;
import com.cirt.osint_dashboard.repository.SourceRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sources")
//@CrossOrigin(origins = "http://localhost:3000")
public class SourceController {

    private final SourceRepository repository;

    public SourceController(SourceRepository repository) { this.repository = repository; }

    @GetMapping
    public List<Source> getAll() { return repository.findAll(); }

    @PostMapping
    public Source addSource(@RequestBody Source source) { return repository.save(source); }

    @PutMapping("/{id}")
    public Source updateSource(@PathVariable String id, @RequestBody Source source) {
        source.setId(id);
        return repository.save(source);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSource(@PathVariable String id) {
        repository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
