package com.cirt.osint_dashboard.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

/**
 * PersonData Model - MongoDB & Redis Compatible
 *
 * Implements Serializable for Redis caching
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Document(collection = "leakeddata")
public class PersonData implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @Field("phonenumber")
    private String phonenumber;

    @Field("facebook_id")
    private String facebookId;

    @Field("name")
    private String name;

    @Field("sex")
    private String sex;

    @Field("address1")
    private String address1;

    @Field("address2")
    private String address2;

    @Field("maritalstatus")
    private String maritalstatus;

    @Field("placeofwork")
    private String placeofwork;

    @Field("creationdatetime")
    private String creationdatetime;

    @Field("email")
    private String email;

    @Field("date_of_birth")
    private String dateOfBirth;

    @Field("placeofbirth")
    private String placeofbirth;

    @Field("nui")
    private String nui;

    @Field("occupation")
    private String occupation;

    @Field("country")
    private String country;

    @Field("raw")
    private String raw;

    public PersonData() {}

    // ✅ Getters for all fields
    public String getId() { return id; }
    public String getPhonenumber() { return phonenumber; }
    public String getFacebookId() { return facebookId; }
    public String getName() { return name; }
    public String getSex() { return sex; }
    public String getAddress1() { return address1; }
    public String getAddress2() { return address2; }
    public String getMaritalstatus() { return maritalstatus; }
    public String getPlaceofwork() { return placeofwork; }
    public String getCreationdatetime() { return creationdatetime; }
    public String getEmail() { return email; }
    public String getDateOfBirth() { return dateOfBirth; }
    public String getPlaceofbirth() { return placeofbirth; }
    public String getNui() { return nui; }
    public String getOccupation() { return occupation; }
    public String getCountry() { return country; }
    public String getRaw() { return raw; }
}