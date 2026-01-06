package com.cirt.osint_dashboard.repository;

import com.cirt.osint_dashboard.model.PersonData;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface PersonRepository extends MongoRepository<PersonData, String> {

    /* =======================
       FAST INDEXED SEARCHES
       ======================= */

    // Uses idx_phonenumber
    List<PersonData> findByPhonenumber(String phonenumber);

    // Uses idx_email
    List<PersonData> findByEmail(String email);

    // Uses idx_address1
    List<PersonData> findByAddress1ContainingIgnoreCase(String address);

    /* =======================
       TEXT SEARCH (NAME)
       Uses idx_name_text
       ======================= */
    @Query("{ $text: { $search: ?0 } }")
    List<PersonData> searchByNameText(String value);

    /* =======================
       SAFE LIMITED FETCH
       ======================= */
    default List<PersonData> findLimited(int limit) {
        return findAll(PageRequest.of(0, limit)).getContent();
    }
}
