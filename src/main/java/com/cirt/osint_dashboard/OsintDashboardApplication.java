package com.cirt.osint_dashboard;

import com.cirt.osint_dashboard.repository.PersonElasticRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories(
    basePackages = "com.cirt.osint_dashboard.repository",
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PersonElasticRepository.class)
)
@EnableElasticsearchRepositories(
    basePackages = "com.cirt.osint_dashboard.repository",
    includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PersonElasticRepository.class)
)
public class OsintDashboardApplication {
    public static void main(String[] args) {
        SpringApplication.run(OsintDashboardApplication.class, args);
    }
}