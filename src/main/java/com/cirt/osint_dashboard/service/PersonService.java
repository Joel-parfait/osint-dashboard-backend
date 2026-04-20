package com.cirt.osint_dashboard.service;

import com.cirt.osint_dashboard.model.PersonData;
import com.cirt.osint_dashboard.model.PersonDocument;
import com.cirt.osint_dashboard.repository.PersonRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.util.EntityUtils;
import org.bson.types.ObjectId;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service OSINT Hybride - Version Finale Certifiée (Bas Niveau).
 * Optimisé avec filtrage des doublons (.distinct).
 */
@Service
public class PersonService {

    private final PersonRepository mongoRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final RestClient restClient; 
    private final SyncService syncService; // Ajout pour la synchronisation
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PersonService(PersonRepository mongoRepository, 
                         ElasticsearchOperations elasticsearchOperations, 
                         RestClient restClient,
                         SyncService syncService) { // Injection du SyncService
        this.mongoRepository = mongoRepository;
        this.elasticsearchOperations = elasticsearchOperations;
        this.restClient = restClient;
        this.syncService = syncService;
    }

    /* ============================================================
       NOUVEAU : PONT DE SYNCHRONISATION (Appelé par le Controller)
       ============================================================ */
    public void syncAllToElasticsearch() {
        System.out.println("🔄 [CIRT-BRIDGE] Relais vers SyncService pour réindexation...");
        syncService.fullReindex();
    }

    /* ============================================================
       1. AUTO-COMPLÉTION (MÉTHODE BAS NIVEAU + DISTINCT)
       ============================================================ */
    public List<String> getSuggestions(String prefix) {
        List<String> rawSuggestions = new ArrayList<>();
        try {
            Request request = new Request("POST", "/person_index/_search");
            String body = "{\"suggest\":{\"osint-suggest\":{\"prefix\":\"" + prefix + "\",\"completion\":{\"field\":\"suggest\",\"size\":20}}}}";
            request.setJsonEntity(body);

            Response response = restClient.performRequest(request);
            String responseBody = EntityUtils.toString(response.getEntity());

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode suggestNode = root.path("suggest").path("osint-suggest");

            if (suggestNode.isArray() && suggestNode.size() > 0) {
                JsonNode options = suggestNode.get(0).path("options");
                if (options.isArray()) {
                    for (JsonNode option : options) {
                        rawSuggestions.add(option.path("text").asText());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur Suggestion Bas Niveau : " + e.getMessage());
        }

        return rawSuggestions.stream()
                .distinct()
                .limit(10)
                .collect(Collectors.toList());
    }

    /* ============================================================
       2. RECHERCHE GLOBALE HYBRIDE
       ============================================================ */
    @Cacheable(value = "globalSearches", key = "{#query, #page, #size}", unless = "#result == null")
    public Page<PersonData> searchGlobal(String query, int page, int size) {
        try {
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
                List<ObjectId> objectIds = hits.getSearchHits().stream()
                    .map(hit -> new ObjectId(hit.getContent().getId()))
                    .collect(Collectors.toList());
                return mongoRepository.findAllByIdIn(objectIds, PageRequest.of(page, size));
            }
        } catch (Exception e) {
            System.err.println("❌ ÉCHEC MOTEUR ELASTIC : " + e.getMessage());
        }

        String safeQuery = query.replaceAll("([\\\\+\\\\*\\\\?\\\\^\\\\$\\\\(\\\\)\\\\[\\\\]\\\\{\\}\\\\.\\\\|])", "\\\\$1");
        return mongoRepository.globalSearch(safeQuery, PageRequest.of(page, size));
    }

    /* ============================================================
       3. RECHERCHE AVANCÉE
       ============================================================ */
    @Cacheable(value = "advancedSearches", key = "{#query, #filterField, #filterValue, #page, #size}", unless = "#result == null")
    public Page<PersonData> searchAdvanced(String query, String filterField, String filterValue, int page, int size) {
        String safeQuery = (query != null) ? query.replaceAll("([\\\\+\\\\*\\\\?\\\\^\\\\$\\\\(\\\\)\\\\[\\\\]\\\\{\\}\\\\.\\\\|])", "\\\\$1") : "";
        String safeFilterValue = (filterValue != null) ? filterValue.replaceAll("([\\\\+\\\\*\\\\?\\\\^\\\\$\\\\(\\\\)\\\\[\\\\]\\\\{\\}\\\\.\\\\|])", "\\\\$1") : "";
        return mongoRepository.advancedSearch(safeQuery, filterField, safeFilterValue, PageRequest.of(page, size));
    }

    /* ============================================================
       4. UTILITAIRES
       ============================================================ */
    public List<PersonData> searchByName(String name) { return mongoRepository.searchByNameText(name); }
    public List<PersonData> searchByPhone(String phone) { return mongoRepository.findByPhonenumber(phone); }
    public List<PersonData> getAllLimited(int limit) { return mongoRepository.findLimited(limit); }
    public long countAll() { return mongoRepository.count(); }

    @CacheEvict(value = {"globalSearches", "advancedSearches", "nameSearches", "phoneSearches", "emailSearches", "addressSearches", "sexSearches"}, allEntries = true)
    public void clearAllCaches() { System.out.println("🗑️ Tous les caches OSINT ont été vidés."); }

    @CacheEvict(value = "nameSearches", allEntries = true) public void clearNameCache() { }
    @CacheEvict(value = "phoneSearches", allEntries = true) public void clearPhoneCache() { }
    @CacheEvict(value = "addressSearches", allEntries = true) public void clearAddressCache() { }
    @CacheEvict(value = "emailSearches", allEntries = true) public void clearEmailCache() { }
}