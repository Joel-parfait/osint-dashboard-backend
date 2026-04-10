package com.cirt.osint_dashboard.service;

import com.cirt.osint_dashboard.model.PersonData;
import com.cirt.osint_dashboard.model.PersonDocument;
import com.cirt.osint_dashboard.repository.PersonRepository;
import com.cirt.osint_dashboard.repository.PersonElasticRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service de synchronisation massive MongoDB -> Elasticsearch.
 * Optimisé pour le traitement par lots (Batch Processing) du CIRT.
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
            // Taille de lot de 1000 pour un bon équilibre RAM/Vitesse
            int pageSize = 1000; 
            int totalPages = (int) Math.ceil((double) totalRecords / pageSize);

            System.out.println("🚀 [CIRT-SYNC] Début de l'indexation de " + totalRecords + " documents...");

            for (int i = 0; i < totalPages; i++) {
                try {
                    Page<PersonData> page = mongoRepository.findAll(PageRequest.of(i, pageSize));
                    
                    List<PersonDocument> docs = page.getContent().stream().map(p -> {
                        PersonDocument d = new PersonDocument();
                        // Copie rigoureuse de tous les champs pour la recherche multichamp
                        d.setId(p.getId());
                        d.setName(p.getName());
                        d.setEmail(p.getEmail());
                        d.setPhonenumber(p.getPhonenumber());
                        d.setAddress1(p.getAddress1());     // Crucial pour "Bastos"
                        d.setOccupation(p.getOccupation()); // Optionnel mais utile
                        return d;
                    }).collect(Collectors.toList());

                    // Sauvegarde groupée dans Elasticsearch
                    elasticRepository.saveAll(docs);

                    if (i % 10 == 0) {
                        double percent = ((double) (i + 1) / totalPages) * 100;
                        System.out.printf("⏳ Progression : %.2f%% (%d / %d documents)%n", 
                                          percent, (i * pageSize), totalRecords);
                    }
                } catch (Exception e) {
                    System.err.println("⚠️ Erreur lors du lot " + i + " : " + e.getMessage());
                    // On continue le lot suivant malgré l'erreur
                }
            }
            System.out.println("✅ [CIRT-SYNC] Indexation terminée avec succès !");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur critique pendant la synchronisation : " + e.getMessage());
        }
    }
}