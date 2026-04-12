package com.cirt.osint_dashboard.service;

import com.cirt.osint_dashboard.model.PersonData;
import com.cirt.osint_dashboard.model.PersonDocument;
import com.cirt.osint_dashboard.repository.PersonRepository;
import com.cirt.osint_dashboard.repository.PersonElasticRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.suggest.Completion;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service de synchronisation massive MongoDB -> Elasticsearch.
 * Version optimisée avec Auto-complétion multi-champs pour le CIRT - ANTIC.
 */
@Service
public class SyncService {

    private final PersonRepository mongoRepository;
    private final PersonElasticRepository elasticRepository;

    public SyncService(PersonRepository mongoRepository, PersonElasticRepository elasticRepository) {
        this.mongoRepository = mongoRepository;
        this.elasticRepository = elasticRepository;
    }

    public void fullReindex() {
        try {
            long totalRecords = mongoRepository.count();
            int pageSize = 1000; 
            int totalPages = (int) Math.ceil((double) totalRecords / pageSize);

            System.out.println("🚀 [CIRT-SYNC] Début de l'indexation massive (" + totalRecords + " documents)...");

            for (int i = 0; i < totalPages; i++) {
                try {
                    Page<PersonData> page = mongoRepository.findAll(PageRequest.of(i, pageSize));
                    
                    List<PersonDocument> docs = page.getContent().stream().map(p -> {
                        PersonDocument d = new PersonDocument();
                        
                        // 1. Mapping des champs standards
                        d.setId(p.getId());
                        d.setName(p.getName());
                        d.setEmail(p.getEmail());
                        d.setPhonenumber(p.getPhonenumber());
                        d.setAddress1(p.getAddress1());
                        d.setOccupation(p.getOccupation());
                        d.setCountry(p.getCountry());

                        // 2. Préparation des entrées pour l'autocomplétion (Multi-champs)
                        List<String> inputs = new ArrayList<>();
                        if (p.getName() != null && !p.getName().isEmpty()) inputs.add(p.getName());
                        if (p.getEmail() != null && !p.getEmail().isEmpty()) inputs.add(p.getEmail());
                        if (p.getPhonenumber() != null && !p.getPhonenumber().isEmpty()) inputs.add(p.getPhonenumber());
                        if (p.getOccupation() != null && !p.getOccupation().isEmpty()) inputs.add(p.getOccupation());

                        // 3. Injection dans le champ suggest
                        if (!inputs.isEmpty()) {
                            // On transforme la liste en tableau pour l'objet Completion
                            d.setSuggest(new Completion(inputs.toArray(new String[0])));
                        }

                        return d;
                    }).collect(Collectors.toList());

                    // Sauvegarde dans Elasticsearch
                    elasticRepository.saveAll(docs);

                    if (i % 10 == 0) {
                        double percent = ((double) (i + 1) / totalPages) * 100;
                        System.out.printf("⏳ Progression : %.2f%% (%d / %d documents)%n", 
                                          percent, (i * pageSize), totalRecords);
                    }
                } catch (Exception e) {
                    System.err.println("⚠️ Erreur lors du lot " + i + " : " + e.getMessage());
                }
            }
            System.out.println("✅ [CIRT-SYNC] Indexation terminée ! Le moteur de suggestion est prêt.");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur critique : " + e.getMessage());
        }
    }
}