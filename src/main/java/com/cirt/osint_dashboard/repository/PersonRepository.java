package com.cirt.osint_dashboard.repository;

import com.cirt.osint_dashboard.model.PersonData;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Repository pour l'accès aux données de fuites (leaks).
 * Optimisé par Tchuente Kenmegne Joel Parfait pour le CIRT.
 */
@Repository
public interface PersonRepository extends MongoRepository<PersonData, String> {

    /* ============================================================
       1. RECHERCHE COMBINÉE (GLOBALE + FILTRE) - SOLUTION FINALE PHASE 2
       Cette requête permet de chercher un mot-clé ET d'appliquer un filtre
       dynamique (ex: sexe ou pays) tout en gardant une pagination réelle.
       ============================================================ */
    @Query("{ '$and': [ " +
           "  { '$or': [ " +
           "    { 'name': { '$regex': ?0, '$options': 'i' } }, " +
           "    { 'email': { '$regex': ?0, '$options': 'i' } }, " +
           "    { 'phonenumber': { '$regex': ?0, '$options': 'i' } }, " +
           "    { 'facebook_id': { '$regex': ?0, '$options': 'i' } }, " +
           "    { 'nui': { '$regex': ?0, '$options': 'i' } }, " +
           "    { 'address1': { '$regex': ?0, '$options': 'i' } }, " +
           "    { 'country': { '$regex': ?0, '$options': 'i' } } " +
           "  ]}, " +
           "  { ?1 : { '$regex': ?2, '$options': 'i' } } " +
           "]}")
    Page<PersonData> advancedSearch(String query, String filterField, String filterValue, Pageable pageable);

    /* ============================================================
       2. RECHERCHE GLOBALE SIMPLE
       ============================================================ */
    @Query("{ '$or': [ " +
           "{ 'name': { '$regex': ?0, '$options': 'i' } }, " +
           "{ 'email': { '$regex': ?0, '$options': 'i' } }, " +
           "{ 'phonenumber': { '$regex': ?0, '$options': 'i' } }, " +
           "{ 'facebook_id': { '$regex': ?0, '$options': 'i' } }, " +
           "{ 'nui': { '$regex': ?0, '$options': 'i' } }, " +
           "{ 'occupation': { '$regex': ?0, '$options': 'i' } }, " +
           "{ 'placeofwork': { '$regex': ?0, '$options': 'i' } }, " +
           "{ 'address1': { '$regex': ?0, '$options': 'i' } }, " +
           "{ 'country': { '$regex': ?0, '$options': 'i' } }" +
           "] }")
    Page<PersonData> globalSearch(String query, Pageable pageable);

    /* ============================================================
       3. RECHERCHES INDEXÉES ET FILTRES (STABILISATION)
       ============================================================ */
    List<PersonData> findByPhonenumber(String phonenumber);
    List<PersonData> findByEmail(String email);
    List<PersonData> findByAddress1ContainingIgnoreCase(String address);
    List<PersonData> findByCountryIgnoreCase(String country);
    List<PersonData> findBySexIgnoreCase(String sex);
    List<PersonData> findByOccupationContainingIgnoreCase(String occupation);

    /* ============================================================
       4. FULL-TEXT SEARCH
       ============================================================ */
    @Query("{ $text: { $search: ?0 } }")
    List<PersonData> searchByNameText(String value);

    /* ============================================================
       5. UTILITAIRES
       ============================================================ */
    default List<PersonData> findLimited(int limit) {
        return findAll(PageRequest.of(0, limit)).getContent();
    }
}