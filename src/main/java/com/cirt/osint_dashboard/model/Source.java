package com.cirt.osint_dashboard.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "sources")
public class Source {
    @Id
    private String id;
    private String name;
    private String description;
    private String url;

    public Source() {}
    public Source(String name, String description, String url) {
        this.name = name; this.description = description; this.url = url;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}
