package com.cirt.osint_dashboard.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "person_index")
public class PersonDocument {
    @Id
    private String id;
    @Field(type = FieldType.Text)
    private String name;
    @Field(type = FieldType.Text)
    private String email;
    @Field(type = FieldType.Text)
    private String phonenumber;
    @Field(type = FieldType.Text)
    private String address1;
    @Field(type = FieldType.Text)
    private String occupation;
    @Field(type = FieldType.Text)
    private String country;

    // Getters et Setters pour TOUS les champs (Vérifie bien qu'ils y sont tous)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhonenumber() { return phonenumber; }
    public void setPhonenumber(String phonenumber) { this.phonenumber = phonenumber; }
    public String getAddress1() { return address1; }
    public void setAddress1(String address1) { this.address1 = address1; }
    public String getOccupation() { return occupation; }
    public void setOccupation(String occupation) { this.occupation = occupation; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
}