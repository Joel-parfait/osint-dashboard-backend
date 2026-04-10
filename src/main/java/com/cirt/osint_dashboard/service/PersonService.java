package com.cirt.osint_dashboard.service;

import com.cirt.osint_dashboard.model.PersonData;
import com.cirt.osint_dashboard.model.PersonDocument;
import com.cirt.osint_dashboard.repository.PersonRepository;
import com.cirt.osint_dashboard.repository.PersonElasticRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service OSINT Hybride (MongoDB + Elasticsearch) optimisé pour le CIRT - ANTIC.
 * Gère l'intelligence de recherche floue multichamp et la mise en cache Redis.
 */
@Service
public class PersonService {

    private final PersonRepository mongoRepository;
    private final PersonElasticRepository elasticRepository;

    public PersonService(PersonRepository mongoRepository, PersonElasticRepository elasticRepository) {
        this.mongoRepository = mongoRepository;
        this.elasticRepository = elasticRepository;
    }

    /* ============================================================
       1. RECHERCHE GLOBALE HYBRIDE MULTICHAMP (Elasticsearch + MongoDB)
       ============================================================ */
    @Cacheable(value = "globalSearches", key = "{#query, #page, #size}", unless = "#result == null")
    public Page<PersonData> searchGlobal(String query, int page, int size) {
        System.out.println("🕵️ Analyse Hybride CIRT : Recherche Multichamp Floue sur : " + query);
        
        try {
            // APPEL CORRIGÉ : On utilise la méthode multichamp définie dans le Repository
            List<PersonDocument> elasticHits = elasticRepository.findByAnyFieldCustom(query);

            if (elasticHits != null && !elasticHits.isEmpty()) {
                System.out.println("🚀 Elastic a trouvé " + elasticHits.size() + " correspondances (Nom/Adresse/Email/etc.)");
                
                List<String> ids = elasticHits.stream()
                                             .map(PersonDocument::getId)
                                             .collect(Collectors.toList());

                return mongoRepository.findByIdIn(ids, PageRequest.of(page, size));
            }
        } catch (Exception e) {
            System.err.println("⚠️ Alerte Elastic : " + e.getMessage() + ". Repli sur Regex MongoDB.");
        }

        // Fallback Regex si Elastic est vide ou hors-ligne
        String safeQuery = query.replaceAll("([\\\\+\\\\*\\\\?\\\\^\\\\$\\\\(\\\\)\\\\[\\\\]\\\\{\\}\\\\.\\\\|])", "\\\\$1");
        return mongoRepository.globalSearch(safeQuery, PageRequest.of(page, size));
    }

    /* ============================================================
       2. RECHERCHE AVANCÉE (MULTICHAMP MONGODB)
       ============================================================ */
    @Cacheable(value = "advancedSearches", key = "{#query, #filterField, #filterValue, #page, #size}", 
               unless = "#result == null")
    public Page<PersonData> searchAdvanced(String query, String filterField, String filterValue, int page, int size) {
        String safeQuery = query.replaceAll("([\\\\+\\\\*\\\\?\\\\^\\\\$\\\\(\\\\)\\\\[\\\\]\\\\{\\}\\\\.\\\\|])", "\\\\$1");
        String safeFilterValue = filterValue.replaceAll("([\\\\+\\\\*\\\\?\\\\^\\\\$\\\\(\\\\)\\\\[\\\\]\\\\{\\}\\\\.\\\\|])", "\\\\$1");

        Pageable pageable = PageRequest.of(page, size);
        return mongoRepository.advancedSearch(safeQuery, filterField, safeFilterValue, pageable);
    }

    /* ============================================================
       3. RECHERCHES SPÉCIFIQUES & FILTRES
       ============================================================ */
    
    public List<PersonData> searchByName(String name) {
        return mongoRepository.searchByNameText(name);
    }

    public List<PersonData> searchByPhone(String phone) {
        return mongoRepository.findByPhonenumber(phone);
    }

    public List<PersonData> searchByAddress(String address, int limit) {
        return mongoRepository.findByAddress1ContainingIgnoreCase(address).stream().limit(limit).toList();
    }

    public List<PersonData> filterBySex(String sex) {
        String formattedSex = sex.trim().toUpperCase();
        if (formattedSex.startsWith("M")) formattedSex = "M";
        if (formattedSex.startsWith("F")) formattedSex = "F";
        return mongoRepository.findBySexIgnoreCase(formattedSex);
    }

    public List<PersonData> getAllLimited(int limit) {
        return mongoRepository.findLimited(limit);
    }

    public long countAll() {
        return mongoRepository.count();
    }

    /* ============================================================
       4. GESTION DU CACHE REDIS
       ============================================================ */

    @CacheEvict(value = {"globalSearches", "advancedSearches", "nameSearches", "phoneSearches", "emailSearches", "addressSearches", "sexSearches"}, allEntries = true)
    public void clearAllCaches() {
        System.out.println("🗑️ Vidage complet du cache OSINT.");
    }

    @CacheEvict(value = "nameSearches", allEntries = true)
    public void clearNameCache() { System.out.println("🗑️ Cache des noms vidé."); }

    @CacheEvict(value = "phoneSearches", allEntries = true)
    public void clearPhoneCache() { System.out.println("🗑️ Cache des téléphones vidé."); }

    @CacheEvict(value = "addressSearches", allEntries = true)
    public void clearAddressCache() { System.out.println("🗑️ Cache des adresses vidé."); }

    @CacheEvict(value = "emailSearches", allEntries = true)
    public void clearEmailCache() {
        System.out.println("🗑️ Cache des emails vidé.");
    }
}