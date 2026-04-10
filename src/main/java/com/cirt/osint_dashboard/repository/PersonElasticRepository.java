package com.cirt.osint_dashboard.repository;

import com.cirt.osint_dashboard.model.PersonDocument;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import java.util.List;

public interface PersonElasticRepository extends ElasticsearchRepository<PersonDocument, String> {
    
    /**
     * Recherche Floue Multichamp (OSINT Global Search)
     * Cherche dans le nom, l'adresse, l'email, le téléphone et la profession.
     */
    @Query("{" +
           "  \"multi_match\": {" +
           "    \"query\": \"?0\"," +
           "    \"fields\": [\"name\", \"address1\", \"email\", \"phonenumber\", \"occupation\"]," +
           "    \"fuzziness\": \"AUTO\"" +
           "  }" +
           "}")
    List<PersonDocument> findByAnyFieldCustom(String query);
}