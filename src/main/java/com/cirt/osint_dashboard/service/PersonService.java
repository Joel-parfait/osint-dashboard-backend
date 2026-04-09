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
 * Gère la logique métier avancée et la mise en cache Redis.
 */
@Service
public class PersonService {

    private final PersonRepository repository;

    public PersonService(PersonRepository repository) {
        this.repository = repository;
    }

    /* ============================================================
       1. RECHERCHE COMBINÉE (Moteur Principal)
       Fixe les problèmes de statistiques et de pagination en filtrant 
       directement au niveau de la base de données.
       ============================================================ */
    @Cacheable(value = "advancedSearches", key = "{#query, #filterField, #filterValue, #page, #size}", 
               unless = "#result == null")
    public Page<PersonData> searchAdvanced(String query, String filterField, String filterValue, int page, int size) {
        // Sécurité : Échappement des caractères spéciaux
        String safeQuery = query.replaceAll("([\\\\+\\\\*\\\\?\\\\^\\\\$\\\\(\\\\)\\\\[\\\\]\\\\{\\}\\\\.\\\\|])", "\\\\$1");
        String safeFilterValue = filterValue.replaceAll("([\\\\+\\\\*\\\\?\\\\^\\\\$\\\\(\\\\)\\\\[\\\\]\\\\{\\}\\\\.\\\\|])", "\\\\$1");

        Pageable pageable = PageRequest.of(page, size);
        return repository.advancedSearch(safeQuery, filterField, safeFilterValue, pageable);
    }

    /* ============================================================
       2. RECHERCHE GLOBALE SIMPLE
       ============================================================ */
    @Cacheable(value = "globalSearches", key = "{#query, #page, #size}", unless = "#result == null")
    public Page<PersonData> searchGlobal(String query, int page, int size) {
        String safeQuery = query.replaceAll("([\\\\+\\\\*\\\\?\\\\^\\\\$\\\\(\\\\)\\\\[\\\\]\\\\{\\}\\\\.\\\\|])", "\\\\$1");
        Pageable pageable = PageRequest.of(page, size);
        return repository.globalSearch(safeQuery, pageable);
    }

    /* ============================================================
       3. RECHERCHES SPÉCIFIQUES & FILTRES
       ============================================================ */
    
    public List<PersonData> searchByName(String name) {
        return repository.searchByNameText(name);
    }

    public List<PersonData> searchByPhone(String phone) {
        return repository.findByPhonenumber(phone);
    }

    public List<PersonData> searchByAddress(String address, int limit) {
        return repository.findByAddress1ContainingIgnoreCase(address).stream().limit(limit).toList();
    }

    public List<PersonData> filterBySex(String sex) {
        // Normalisation pour MongoDB (M ou F)
        String formattedSex = sex.trim().toUpperCase();
        if (formattedSex.startsWith("M")) formattedSex = "M";
        if (formattedSex.startsWith("F")) formattedSex = "F";
        return repository.findBySexIgnoreCase(formattedSex);
    }

    public List<PersonData> getAllLimited(int limit) {
        return repository.findLimited(limit);
    }

    public long countAll() {
        return repository.count();
    }

    /* ============================================================
       4. GESTION DU CACHE
       ============================================================ */

    @CacheEvict(value = {"globalSearches", "advancedSearches", "nameSearches", "phoneSearches", "emailSearches", "addressSearches", "sexSearches"}, allEntries = true)
    public void clearAllCaches() {
        System.out.println("🗑️ Vidage complet du cache OSINT (Recherche Globale + Avancée).");
    }

    @CacheEvict(value = "nameSearches", allEntries = true)
    public void clearNameCache() { System.out.println("🗑️ Cache des noms vidé."); }

    @CacheEvict(value = "phoneSearches", allEntries = true)
    public void clearPhoneCache() { System.out.println("🗑️ Cache des téléphones vidé."); }

    @CacheEvict(value = "addressSearches", allEntries = true)
    public void clearAddressCache() { System.out.println("🗑️ Cache des adresses vidé."); }

    // Ajoute cette méthode spécifique pour corriger l'erreur de compilation
    @CacheEvict(value = "emailSearches", allEntries = true)
    public void clearEmailCache() {
        System.out.println("🗑️ Cache des emails vidé individuellement.");
    }
}