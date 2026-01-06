package com.cirt.osint_dashboard.service;

import com.cirt.osint_dashboard.model.PersonData;
import com.cirt.osint_dashboard.repository.PersonRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Person Service with Redis Caching
 *
 * Cache Strategy:
 * - Each search type has its own cache zone
 * - Cache key = search value
 * - Different TTL for different search types
 * - Manual cache clearing available
 */
@Service
public class PersonService {

    private final PersonRepository repository;

    public PersonService(PersonRepository repository) {
        this.repository = repository;
    }

    /* =======================
       NAME SEARCH (CACHED 20 MIN)
       ======================= */
    @Cacheable(value = "nameSearches", key = "#name", unless = "#result == null || #result.isEmpty()")
    public List<PersonData> searchByName(String name) {
        System.out.println("🔍 [CACHE MISS] Querying MongoDB for name: " + name);
        return repository.searchByNameText(name);
    }

    /* =======================
       PHONE SEARCH (CACHED 30 MIN)
       ======================= */
    @Cacheable(value = "phoneSearches", key = "#phone", unless = "#result == null || #result.isEmpty()")
    public List<PersonData> searchByPhone(String phone) {
        System.out.println("🔍 [CACHE MISS] Querying MongoDB for phone: " + phone);
        return repository.findByPhonenumber(phone);
    }

    /* =======================
       EMAIL SEARCH (CACHED 30 MIN)
       ======================= */
    @Cacheable(value = "emailSearches", key = "#email", unless = "#result == null || #result.isEmpty()")
    public List<PersonData> searchByEmail(String email) {
        System.out.println("🔍 [CACHE MISS] Querying MongoDB for email: " + email);
        return repository.findByEmail(email);
    }

    /* =======================
       ADDRESS SEARCH (CACHED 10 MIN)
       ======================= */
    @Cacheable(value = "addressSearches", key = "#address + '-' + #limit", unless = "#result == null || #result.isEmpty()")
    public List<PersonData> searchByAddress(String address, int limit) {
        System.out.println("🔍 [CACHE MISS] Querying MongoDB for address: " + address);
        return repository.findByAddress1ContainingIgnoreCase(address)
                .stream()
                .limit(limit)
                .toList();
    }

    /* =======================
       SAFE FETCH ALL (NOT CACHED - TOO LARGE)
       ======================= */
    public List<PersonData> getAllLimited(int limit) {
        return repository.findLimited(limit);
    }

    public long countAll() {
        return repository.count();
    }

    /* =======================
       CACHE MANAGEMENT
       ======================= */

    /**
     * Clear all search caches
     */
    @CacheEvict(value = {"nameSearches", "phoneSearches", "emailSearches", "addressSearches"}, allEntries = true)
    public void clearAllCaches() {
        System.out.println("🗑️ All search caches cleared!");
    }

    /**
     * Clear specific cache by type
     */
    @CacheEvict(value = "nameSearches", allEntries = true)
    public void clearNameCache() {
        System.out.println("🗑️ Name search cache cleared!");
    }

    @CacheEvict(value = "phoneSearches", allEntries = true)
    public void clearPhoneCache() {
        System.out.println("🗑️ Phone search cache cleared!");
    }

    @CacheEvict(value = "emailSearches", allEntries = true)
    public void clearEmailCache() {
        System.out.println("🗑️ Email search cache cleared!");
    }

    @CacheEvict(value = "addressSearches", allEntries = true)
    public void clearAddressCache() {
        System.out.println("🗑️ Address search cache cleared!");
    }
}