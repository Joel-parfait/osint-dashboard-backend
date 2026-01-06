package com.cirt.osint_dashboard.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import jakarta.annotation.PostConstruct;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class MongoConnectionCheck {

    private final MongoClient mongoClient;

    public MongoConnectionCheck(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    @PostConstruct
    public void printMongoInfo() {
        System.out.println("=== MONGO CONNECTION CHECK ===");

        MongoDatabase database = mongoClient.getDatabase("leaks_db");
        database.runCommand(new Document("ping", 1));

        System.out.println("MongoDB connection OK (database: leaks_db)");
    }
}
