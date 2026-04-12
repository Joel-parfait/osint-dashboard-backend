package com.cirt.osint_dashboard.repository;

import com.cirt.osint_dashboard.model.PersonDocument;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import java.util.List;

public interface PersonElasticRepository extends ElasticsearchRepository<PersonDocument, String> {
    
    // Utilisation de multi_match avec fuzziness AUTO pour gérer frence -> France
    @Query("{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"name\", \"address1\", \"email\", \"phonenumber\", \"occupation\", \"country\"], \"fuzziness\": \"AUTO\"}}")
    List<PersonDocument> findByAnyFieldFuzzy(String query);
}