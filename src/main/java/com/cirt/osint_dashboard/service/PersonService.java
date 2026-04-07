package com.cirt.osint_dashboard.service;

import com.cirt.osint_dashboard.model.PersonData;
import com.cirt.osint_dashboard.repository.PersonRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service OSINT optimisé pour le CIRT - ANTIC.
 * Gère la logique métier et la mise en cache Redis.
 */
@Service
public class PersonService {

    private final PersonRepository repository;

    public PersonService(PersonRepository repository) {
        this.repository = repository;
    }
    

    /* ============================================================
       RECHERCHE GLOBALE (MULTI-CHAMP)
       ============================================================ */

    /* ============================================================
       RECHERCHE GLOBALE (MULTI-CHAMP) - CORRECTION CARACTÈRES SPÉCIAUX
       ============================================================ */
    @Cacheable(value = "globalSearches", key = "#query", unless = "#result == null || #result.isEmpty()")
    public Page<PersonData> searchGlobal(String query, int page, int size) {
    // Échappement des caractères spéciaux (+, *, etc.)
    String safeQuery = query.replaceAll("([\\\\+\\\\*\\\\?\\\\^\\\\$\\\\(\\\\)\\\\[\\\\]\\\\{\\}\\\\.\\\\|])", "\\\\$1");
    
    // Création de la requête de page
    Pageable pageable = PageRequest.of(page, size);
    
    return repository.globalSearch(safeQuery, pageable);
}
    /* ============================================================
       RECHERCHES SPÉCIFIQUES & FILTRES
       ============================================================ */
    
    @Cacheable(value = "nameSearches", key = "#name", unless = "#result == null || #result.isEmpty()")
    public List<PersonData> searchByName(String name) {
        return repository.searchByNameText(name);
    }

    @Cacheable(value = "phoneSearches", key = "#phone", unless = "#result == null || #result.isEmpty()")
    public List<PersonData> searchByPhone(String phone) {
        return repository.findByPhonenumber(phone);
    }

    @Cacheable(value = "emailSearches", key = "#email", unless = "#result == null || #result.isEmpty()")
    public List<PersonData> searchByEmail(String email) {
        return repository.findByEmail(email);
    }

    public List<PersonData> searchByAddress(String address, int limit) {
        return repository.findByAddress1ContainingIgnoreCase(address).stream().limit(limit).toList();
    }

    public List<PersonData> searchByCountry(String country) {
        return repository.findByCountryIgnoreCase(country);
    }

    public List<PersonData> searchBySex(String sex) {
        return repository.findBySexIgnoreCase(sex);
    }

    public List<PersonData> getAllLimited(int limit) {
        return repository.findLimited(limit);
    }

    public long countAll() {
        return repository.count();
    }

    /* ============================================================
       GESTION DU CACHE (Requis par CacheController)
       ============================================================ */

    @CacheEvict(value = {"globalSearches", "nameSearches", "phoneSearches", "emailSearches", "addressSearches"}, allEntries = true)
    public void clearAllCaches() {
        System.out.println("🗑️ Vidage complet du cache OSINT.");
    }

    // Ces méthodes sont CRUCIALES pour corriger tes erreurs de compilation
    @CacheEvict(value = "nameSearches", allEntries = true)
    public void clearNameCache() {
        System.out.println("🗑️ Cache des noms vidé.");
    }

    @CacheEvict(value = "phoneSearches", allEntries = true)
    public void clearPhoneCache() {
        System.out.println("🗑️ Cache des téléphones vidé.");
    }

    @CacheEvict(value = "emailSearches", allEntries = true)
    public void clearEmailCache() {
        System.out.println("🗑️ Cache des emails vidé.");
    }

    @CacheEvict(value = "addressSearches", allEntries = true)
    public void clearAddressCache() {
        System.out.println("🗑️ Cache des adresses vidé.");
    }

    @Cacheable(value = "countrySearches", key = "#country")
    public List<PersonData> filterByCountry(String country) {
        return repository.findByCountryIgnoreCase(country);
    }

    @Cacheable(value = "sexSearches", key = "#sex")
    public List<PersonData> filterBySex(String sex) {
        return repository.findBySexIgnoreCase(sex);
    }
}