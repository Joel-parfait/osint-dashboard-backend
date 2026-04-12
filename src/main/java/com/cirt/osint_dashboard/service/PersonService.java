package com.cirt.osint_dashboard.service;

import com.cirt.osint_dashboard.model.PersonData;
import com.cirt.osint_dashboard.model.PersonDocument;
import com.cirt.osint_dashboard.repository.PersonRepository;
import org.bson.types.ObjectId;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service OSINT Hybride (MongoDB + Elasticsearch) - Version Haute Performance.
 * Corrigé pour supprimer la limitation arbitraire des 100 résultats.
 */
@Service
public class PersonService {

    private final PersonRepository mongoRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    public PersonService(PersonRepository mongoRepository, ElasticsearchOperations elasticsearchOperations) {
        this.mongoRepository = mongoRepository;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    /* ============================================================
       1. RECHERCHE GLOBALE HYBRIDE (PUISSANCE MAXIMALE)
       ============================================================ */
    @Cacheable(value = "globalSearches", key = "{#query, #page, #size}", unless = "#result == null")
    public Page<PersonData> searchGlobal(String query, int page, int size) {
        System.out.println("\n--- 🕵️ ANALYSE MASSIVE CIRT ---");
        System.out.println("🔎 Requête : [" + query + "]");
        
        try {
            // Utilisation d'une limite étendue pour ne pas brider le moteur
            // On demande 10 000 IDs pour permettre une pagination fluide sur MongoDB
            NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(q -> q
                    .multiMatch(m -> m
                        .fields("name", "address1", "email", "phonenumber", "occupation", "country")
                        .query(query)
                        .fuzziness("AUTO")
                    )
                )
                .withPageable(PageRequest.of(0, 10000)) 
                .build();

            SearchHits<PersonDocument> hits = elasticsearchOperations.search(nativeQuery, PersonDocument.class);

            if (hits.hasSearchHits()) {
                System.out.println("✅ Elastic a trouvé : " + hits.getTotalHits() + " hits.");
                
                List<ObjectId> objectIds = hits.getSearchHits().stream()
                    .map(hit -> new ObjectId(hit.getContent().getId()))
                    .collect(Collectors.toList());
                
                System.out.println("🎯 Volume d'IDs traités : " + objectIds.size());

                // MongoDB s'occupe de la pagination (page/size) sur ces IDs
                return mongoRepository.findAllByIdIn(objectIds, PageRequest.of(page, size));
            } else {
                System.out.println("ℹ️ Elastic : Aucun résultat pour cette recherche.");
            }
        } catch (Exception e) {
            System.err.println("❌ ÉCHEC MOTEUR ELASTIC : " + e.getMessage());
        }

        // Fallback vers MongoDB Regex (Sécurité)
        System.out.println("⚠️ Utilisation du Fallback Regex MongoDB...");
        String safeQuery = query.replaceAll("([\\\\+\\\\*\\\\?\\\\^\\\\$\\\\(\\\\)\\\\[\\\\]\\\\{\\}\\\\.\\\\|])", "\\\\$1");
        return mongoRepository.globalSearch(safeQuery, PageRequest.of(page, size));
    }

    /* ============================================================
       2. RECHERCHE AVANCÉE & FILTRAGE
       ============================================================ */
    @Cacheable(value = "advancedSearches", key = "{#query, #filterField, #filterValue, #page, #size}", unless = "#result == null")
    public Page<PersonData> searchAdvanced(String query, String filterField, String filterValue, int page, int size) {
        String safeQuery = query.replaceAll("([\\\\+\\\\*\\\\?\\\\^\\\\$\\\\(\\\\)\\\\[\\\\]\\\\{\\}\\\\.\\\\|])", "\\\\$1");
        String safeFilterValue = (filterValue != null) ? filterValue.replaceAll("([\\\\+\\\\*\\\\?\\\\^\\\\$\\\\(\\\\)\\\\[\\\\]\\\\{\\}\\\\.\\\\|])", "\\\\$1") : "";
        return mongoRepository.advancedSearch(safeQuery, filterField, safeFilterValue, PageRequest.of(page, size));
    }

    /* ============================================================
       3. RECHERCHES UNITAIRES ET FILTRES
       ============================================================ */
    public List<PersonData> searchByName(String name) { return mongoRepository.searchByNameText(name); }
    public List<PersonData> searchByPhone(String phone) { return mongoRepository.findByPhonenumber(phone); }
    public List<PersonData> searchByAddress(String address, int limit) {
        return mongoRepository.findByAddress1ContainingIgnoreCase(address).stream().limit(limit).toList();
    }
    public List<PersonData> filterBySex(String sex) {
        String formattedSex = (sex != null) ? sex.trim().toUpperCase() : "";
        if (formattedSex.startsWith("M")) formattedSex = "M";
        if (formattedSex.startsWith("F")) formattedSex = "F";
        return mongoRepository.findBySexIgnoreCase(formattedSex);
    }
    public List<PersonData> getAllLimited(int limit) { return mongoRepository.findLimited(limit); }
    public long countAll() { return mongoRepository.count(); }

    /* ============================================================
       4. GESTION DU CACHE
       ============================================================ */
    @CacheEvict(value = {"globalSearches", "advancedSearches", "nameSearches", "phoneSearches", "emailSearches", "addressSearches", "sexSearches"}, allEntries = true)
    public void clearAllCaches() { System.out.println("🗑️ Tous les caches OSINT ont été vidés."); }

    @CacheEvict(value = "nameSearches", allEntries = true) public void clearNameCache() { }
    @CacheEvict(value = "phoneSearches", allEntries = true) public void clearPhoneCache() { }
    @CacheEvict(value = "addressSearches", allEntries = true) public void clearAddressCache() { }
    @CacheEvict(value = "emailSearches", allEntries = true) public void clearEmailCache() { }
}