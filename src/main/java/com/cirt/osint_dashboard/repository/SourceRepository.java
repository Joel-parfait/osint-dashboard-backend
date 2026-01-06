package com.cirt.osint_dashboard.repository;

import com.cirt.osint_dashboard.model.Source;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SourceRepository extends MongoRepository<Source, String> {}
