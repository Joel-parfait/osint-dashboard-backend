# 🛡️ Manuel Technique Ultime — OSINT Dashboard Backend Engine (CIRT Edition)

**Organisation :** Centre de Réponse aux Incidents Informatiques (CIRT) — ANTIC
**Projet :** OSINT Intelligence Platform — Backend Engine
**Auteur :** Tchuente Kenmegne Joel Parfait
**Fonction :** Backend Developer | OSINT Analyst
**Version :** Production-Ready Documentation v1.0

---

# 1. Objectif du Document

Ce document constitue la documentation technique complète du moteur backend du **OSINT Dashboard**, incluant :

* Installation et configuration du système
* Architecture technique
* Déploiement sur un poste ou serveur
* Configuration de MongoDB, Redis et Elasticsearch
* Synchronisation des données
* Autocomplétion temps réel
* Sécurité et performance
* Procédures d'exploitation
* Troubleshooting
* Bonnes pratiques de production

Ce manuel est conçu pour permettre à **tout ingénieur système ou développeur** de déployer et maintenir la plateforme sans dépendance à l'auteur original.

---

# 2. Présentation du Système

Le **OSINT Backend Engine** est une API basée sur **Spring Boot** permettant :

* la recherche rapide dans des bases de données volumineuses
* l'analyse de données issues de fuites (leaks)
* la gestion de l'authentification
* la mise en cache des requêtes
* l'indexation haute performance
* l'autocomplétion en temps réel
* l'ingestion massive de données
* la stabilité opérationnelle du système

Cette plateforme est conçue pour un environnement opérationnel de type :

* CIRT
* SOC
* CERT
* Investigation numérique
* Analyse OSINT

---

# 3. Architecture du Système

## Diagramme d’Architecture Professionnel

```text
                        UTILISATEURS / ANALYSTES
                                  │
                                  │ HTTPS
                                  ▼
                          ┌───────────────────┐
                          │     Frontend      │
                          │      React        │
                          └───────────────────┘
                                  │
                                  │ REST API
                                  ▼
                      ┌─────────────────────────┐
                      │      Load Balancer      │
                      └─────────────────────────┘
                                  │
                                  ▼
                      ┌─────────────────────────┐
                      │      API Gateway        │
                      │  (Security / Routing)   │
                      └─────────────────────────┘
                                  │
                                  ▼
                      ┌─────────────────────────┐
                      │     Spring Boot API     │
                      │   Authentication Layer  │
                      └─────────────────────────┘
                         │           │
                         │           │
                         ▼           ▼
            ┌─────────────────┐   ┌─────────────────┐
            │   Redis Cache   │   │   Logging System │
            │  Session / Cache│   │  Audit / Errors  │
            └─────────────────┘   └─────────────────┘
                         │
                         ▼
                ┌─────────────────────┐
                │    Elasticsearch    │
                │   Search Engine     │
                └─────────────────────┘
                         │
                         ▼
                ┌─────────────────────┐
                │       MongoDB       │
                │   Source of Truth   │
                └─────────────────────┘
                         │
                         ▼
                ┌─────────────────────┐
                │    Backup Storage   │
                │   Snapshot System   │
                └─────────────────────┘

Monitoring Layer:

        Prometheus  ─────────► Metrics Collection
        Grafana     ─────────► Dashboards & Alerts
```

---

Architecture actuelle :

Frontend (React)
↓
Spring Boot API
↓
Redis Cache
↓
MongoDB Database

Architecture optimisée avec moteur de recherche :

Frontend
↓
Spring Boot API
↓
Redis Cache
↓
Elasticsearch
↓
MongoDB

Architecture cible (production avancée) :

Load Balancer
↓
API Gateway
↓
Spring Boot API
↓
Redis Cache
↓
Elasticsearch Cluster
↓
MongoDB Cluster
↓
Backup Storage
↓
Monitoring System

---

# 4. Technologies Utilisées

## Backend

* Java 17
* Spring Boot
* Spring Security
* REST API
* Maven

## Base de données

* MongoDB

## Moteur de recherche

* Elasticsearch

## Cache

* Redis

## Outils

* Docker
* Postman
* Git
* VS Code

---

# 5. Prérequis Système

## Java

java -version

Requis :

Java 17 ou supérieur

---

## Maven

mvn -version

Requis :

Maven 3.6 ou supérieur

---

## MongoDB

mongod --version

Requis :

MongoDB 5.0 ou supérieur

---

## Redis

redis-server --version

Recommandé :

Redis 6 ou supérieur

---

## Elasticsearch

curl [http://localhost:9200](http://localhost:9200)

Recommandé :

Elasticsearch 8.x

---

# 6. Installation des Services

## Démarrage MongoDB

sudo systemctl start mongodb
sudo systemctl enable mongodb

---

## Démarrage Redis

sudo systemctl start redis
sudo systemctl enable redis

---

# 7. Configuration Elasticsearch

Fichier :

/etc/elasticsearch/elasticsearch.yml

Configuration recommandée :

network.host: 0.0.0.0
http.port: 9200

discovery.type: single-node

---

## Allocation mémoire JVM

Fichier :

/etc/elasticsearch/jvm.options

Configuration recommandée :

-Xms4g
-Xmx4g

Cette configuration garantit :

* performance stable
* autocomplétion rapide
* indexation massive

---

## Vérification du cluster

curl -X GET "[http://localhost:9200/_cluster/health?pretty](http://localhost:9200/_cluster/health?pretty)"

Résultat attendu :

status: green ou yellow

---

# 8. Création de l'Index Elasticsearch

Commande :

curl -X PUT "[http://localhost:9200/person_index](http://localhost:9200/person_index)" 
-H "Content-Type: application/json" 
-d '
{
"mappings": {
"properties": {

```
  "name": {
    "type": "text"
  },

  "email": {
    "type": "text"
  },

  "phonenumber": {
    "type": "text"
  },

  "occupation": {
    "type": "text"
  },

  "country": {
    "type": "text"
  },

  "suggest": {
    "type": "completion"
  }

}
```

}
}
'

---

# 9. Fonctionnement de l'Autocomplétion

Le système utilise :

Completion Suggester

Cette technologie repose sur :

FST — Finite State Transducer

Avantages :

* réponse en quelques millisecondes
* faible consommation CPU
* performance stable

---

# 10. Pipeline d'Ingestion des Données

Étape 1 : Import dans MongoDB

mongoimport 
--db osint_db 
--collection persons 
--file base_externe.json 
--jsonArray

---

Étape 2 : Synchronisation vers Elasticsearch

Le service :

SyncService

assure la projection des données depuis MongoDB vers Elasticsearch.

---

# 11. Synchronisation Massive (Batch Processing)

Implémentation recommandée :

Page<PersonData> page;

int pageNumber = 0;

do {

```
page = repository.findAll(
    PageRequest.of(pageNumber, 1000)
);

page.forEach(syncService::syncExternalData);

pageNumber++;
```

} while (page.hasNext());

---

# 12. Configuration Backend

Fichier :

src/main/resources/application.properties

---

## MongoDB

spring.data.mongodb.uri=mongodb://127.0.0.1:27017/osint_db

---

## Elasticsearch

spring.elasticsearch.uris=[http://127.0.0.1:9200](http://127.0.0.1:9200)

---

## Redis

spring.data.redis.host=localhost
spring.data.redis.port=6379

---

## Port serveur

server.port=8080

---

# 13. Lancement du Backend

Compilation :

mvn clean install

---

Démarrage :

mvn spring-boot:run

---

URL API :

[http://localhost:8080](http://localhost:8080)

---

# 14. Endpoints API

## Authentication

POST /auth/login

POST /auth/logout

POST /auth/change-password

---

## Search

GET /search/name

GET /search/email

GET /search/phone

GET /search/address

---

## Autocomplete

GET /search/suggest?value=...

---

## Health Check

GET /search/health

---

## Cache

GET /api/cache/stats

POST /api/cache/clear

---

# 15. Sécurité

Bonnes pratiques obligatoires :

* authentification utilisateur
* validation des requêtes
* firewall actif
* accès réseau restreint
* journalisation des actions

---

## Ports à protéger

27017
9200
8080

---

## Règle Firewall recommandée

Autoriser uniquement :

192.168.0.0/16

---

# 16. Monitoring et Logs

Logs recommandés :

logs/

app.log

error.log

audit.log

---

Monitoring recommandé :

Prometheus

Grafana

Elastic Monitoring

---

# 17. Sauvegarde des Données

Méthode recommandée :

Elasticsearch Snapshot

Fréquence :

Daily Backup

---

Commande exemple :

PUT _snapshot/backup_repository/snapshot_01

---

# 18. Performance

Caractéristiques actuelles :

* recherche rapide
* autocomplétion temps réel
* cache Redis
* indexation Elasticsearch
* synchronisation automatique

---

Capacité :

Testé avec plus de 2 000 000 enregistrements

Architecture conçue pour plusieurs millions d'enregistrements

---

# 19. Troubleshooting

## Erreur 401

Vérifier :

SecurityConfig.java

---

## Erreur 500

Cause :

Index MongoDB manquant

Correction :

db.leakeddata.createIndex({ name: "text" })

---

## Suggestions vides

Vérifier :

Mapping Elasticsearch

---

## Port déjà utilisé

lsof -i :8080

kill -9 PID

---


# 21. Conclusion

Le moteur backend du OSINT Dashboard constitue une plateforme robuste conçue pour des environnements opérationnels critiques.

Il permet :

* une recherche rapide
* une ingestion massive de données
* une stabilité système
* une architecture scalable
* une exploitation sécurisée

Le système est prêt pour une utilisation en environnement CIRT / SOC.

---

# Maintenu par

Tchuente Kenmegne Joel Parfait

Backend Developer

OSINT Analyst

CIRT — ANTIC
