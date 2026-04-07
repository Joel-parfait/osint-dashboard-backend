package com.cirt.osint_dashboard.repository;

import com.cirt.osint_dashboard.model.PersonData;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour l'accès aux données de fuites (leaks).
 * Optimisé par Tchuente Kenmegne Joel Parfait pour le CIRT.
 */
@Repository
public interface PersonRepository extends MongoRepository<PersonData, String> {

    /* ============================================================
       1. RECHERCHE GLOBALE (MULTI-CHAMP) - PHASE 2
       Cherche la valeur dans TOUS les champs principaux en même temps.
       L'option 'i' rend la recherche insensible à la casse.
       ============================================================ */

       @Query("{ '$or': [ " +
       "{ 'name': { '$regex': ?0, '$options': 'i' } }, " +
       "{ 'email': { '$regex': ?0, '$options': 'i' } }, " +
       "{ 'phonenumber': { '$regex': ?0, '$options': 'i' } }, " +
       "{ 'facebook_id': { '$regex': ?0, '$options': 'i' } }, " + // NOUVEAU
       "{ 'nui': { '$regex': ?0, '$options': 'i' } }, " +         // NOUVEAU
       "{ 'occupation': { '$regex': ?0, '$options': 'i' } }, " +   // NOUVEAU
       "{ 'placeofwork': { '$regex': ?0, '$options': 'i' } }, " + // NOUVEAU
       "{ 'address1': { '$regex': ?0, '$options': 'i' } }, " +
       "{ 'country': { '$regex': ?0, '$options': 'i' } }" +
       "] }")
      List<PersonData> globalSearch(String query);

    /* ============================================================
       2. RECHERCHES INDEXÉES (SPÉCIFIQUES)
       Utilisées pour les recherches directes et précises.
       ============================================================ */

    // Recherche exacte par Téléphone
    List<PersonData> findByPhonenumber(String phonenumber);

    // Recherche exacte par Email
    List<PersonData> findByEmail(String email);

    // Recherche partielle par Adresse
    List<PersonData> findByAddress1ContainingIgnoreCase(String address);

    /* ============================================================
       3. FILTRES OSINT (STABILISATION PHASE 1)
       Permet d'affiner les résultats sur le Dashboard.
       ============================================================ */
    
    // Filtre par Pays
    List<PersonData> findByCountryIgnoreCase(String country);

    // Filtre par Sexe (M/F)
    List<PersonData> findBySexIgnoreCase(String sex);

    /* ============================================================
       4. FULL-TEXT SEARCH (MONGODB NATIVE)
       Nécessite : db.leakeddata.createIndex({ "name": "text" })
       ============================================================ */
    @Query("{ $text: { $search: ?0 } }")
    List<PersonData> searchByNameText(String value);

    /* ============================================================
       5. UTILITAIRES DE NAVIGATION
       ============================================================ */
    
    // Récupération limitée pour éviter de saturer le Frontend
    default List<PersonData> findLimited(int limit) {
        return findAll(PageRequest.of(0, limit)).getContent();
    }
}