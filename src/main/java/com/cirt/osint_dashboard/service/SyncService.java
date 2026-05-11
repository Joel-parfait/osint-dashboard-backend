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
 * Version stabilisée pour Render (Batching & Memory Management).
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
            // Taille de lot réduite à 500 pour stabiliser la RAM sur Render
            int pageSize = 500; 
            int totalPages = (int) Math.ceil((double) totalRecords / pageSize);

            System.out.println("🚀 [CIRT-SYNC] Début de l'indexation massive (" + totalRecords + " documents)...");

            for (int i = 0; i < totalPages; i++) {
                try {
                    Page<PersonData> page = mongoRepository.findAll(PageRequest.of(i, pageSize));
                    List<PersonData> content = page.getContent();

                    if (content.isEmpty()) continue;

                    List<PersonDocument> docs = content.stream().map(p -> {
                        PersonDocument d = new PersonDocument();
                        d.setId(p.getId());
                        d.setName(p.getName());
                        d.setEmail(p.getEmail());
                        d.setPhonenumber(p.getPhonenumber());
                        d.setAddress1(p.getAddress1());
                        d.setOccupation(p.getOccupation());
                        d.setCountry(p.getCountry());

                        List<String> inputs = new ArrayList<>();
                        if (p.getName() != null && !p.getName().isEmpty()) inputs.add(p.getName());
                        if (p.getEmail() != null && !p.getEmail().isEmpty()) inputs.add(p.getEmail());
                        if (p.getPhonenumber() != null && !p.getPhonenumber().isEmpty()) inputs.add(p.getPhonenumber());

                        if (!inputs.isEmpty()) {
                            d.setSuggest(new Completion(inputs.toArray(new String[0])));
                        }
                        return d;
                    }).collect(Collectors.toList());

                    // Sauvegarde par lot
                    elasticRepository.saveAll(docs);

                    // LOGS & PAUSE : Laisse respirer le CPU et le pool MongoDB
                    if (i % 5 == 0) {
                        double percent = ((double) (i + 1) / totalPages) * 100;
                        System.out.printf("⏳ Progression : %.2f%% (%d / %d documents)%n", 
                                          percent, (i * pageSize), totalRecords);
                        
                        // Pause de 200ms pour éviter le "state should be: open"
                        Thread.sleep(200); 
                    }

                    // Aide le Garbage Collector en vidant les listes lourdes
                    docs.clear();

                } catch (Exception e) {
                    System.err.println("⚠️ Erreur lors du lot " + i + " : " + e.getMessage());
                    // En cas d'erreur réseau, on attend 1 seconde avant de continuer
                    Thread.sleep(1000);
                }
            }
            System.out.println("✅ [CIRT-SYNC] Indexation terminée avec succès !");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur critique lors de la synchro : " + e.getMessage());
        }
    }
}