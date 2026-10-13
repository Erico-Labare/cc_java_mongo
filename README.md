# CC Java MongoDB — API REST de gestion médicale

## 1. Présentation du projet

**CC Java MongoDB** est une API REST développée avec Java et Spring Boot permettant de gérer les patients, les médecins, les médicaments, les consultations médicales et les prescriptions associées.

Le projet a pour objectif de mettre en œuvre une architecture en couches, une persistance documentaire avec MongoDB, une authentification sécurisée par JWT, la validation des données entrantes et des tests automatisés.

### Fonctionnalités principales

- Authentification des utilisateurs par identifiant et mot de passe, avec génération d'un jeton JWT.
- Gestion des patients : création, consultation, modification, suppression, pagination et recherche par nom.
- Gestion des médecins : création, consultation, modification, suppression, pagination et recherche par nom.
- Gestion des médicaments : création, consultation, modification et suppression.
- Gestion des consultations médicales : création, consultation, modification et suppression.
- Recherche des consultations d'un patient, avec pagination.
- Gestion des prescriptions associées à une consultation, avec référence au médicament et nombre de prises.
- Modification des prescriptions d'une consultation.
- Ajout et téléchargement d'un document associé à une consultation.
- Validation des données reçues par l'API.
- Gestion centralisée des exceptions.
- Documentation interactive de l'API avec Swagger UI.
- Tests automatisés des services métier et du contexte Spring.
- Analyse statique du code avec SonarQube.

## 2. Technologies utilisées

| Technologie | Utilisation |
|---|---|
| Java 21 | Langage et version cible du projet |
| Spring Boot 3.5.16 | Configuration et démarrage de l'application |
| Spring Web | Création des endpoints REST |
| Spring Data MongoDB | Persistance et accès aux données |
| MongoDB | Base de données documentaire |
| Spring Security | Authentification et protection des endpoints |
| JSON Web Token (JWT) | Authentification par jeton Bearer |
| BCrypt | Hachage des mots de passe |
| Jakarta Bean Validation | Validation des données entrantes |
| Springdoc OpenAPI / Swagger UI | Documentation interactive de l'API |
| GridFS | Stockage des documents dans MongoDB |
| JUnit 5 et Mockito | Tests automatisés |
| JaCoCo | Mesure de la couverture des tests |
| SonarQube | Analyse de la qualité du code |
| Maven | Gestion des dépendances et construction du projet |
| Docker et Docker Compose | Exécution facultative de SonarQube |

Le projet utilise Maven Wrapper (`mvnw`), ce qui permet d'exécuter Maven sans installer manuellement une version particulière de Maven.

## 3. Prérequis

Avant de démarrer le projet, les éléments suivants doivent être disponibles :

1. **Java Development Kit (JDK) 21**.
2. **MongoDB**, installé localement ou exécuté dans un conteneur.
3. **IntelliJ IDEA** ou un éditeur permettant d'exécuter une application Java.
4. **Git**, si le projet est récupéré depuis un dépôt.
5. Un client HTTP tel que Postman, Insomnia ou `curl` pour tester les endpoints.

Docker Desktop est facultatif pour l'application elle-même. Il est notamment utilisé pour exécuter SonarQube.

### Vérifier Java

Dans un terminal :

```powershell
java -version
```

Vérifier également que la version du JDK configurée dans IntelliJ correspond au JDK 21.

Le projet peut avoir été exécuté avec un JDK plus récent, mais Java 21 constitue la version cible à utiliser pour reproduire l'environnement du projet.

### Vérifier MongoDB

Si MongoDB est installé localement, vérifier que le serveur fonctionne et écoute sur le port `27017`.

L'URI locale utilisée par défaut est :

```text
mongodb://localhost:27017/medicaldb
```

MongoDB doit être démarré avant l'application.

## 4. Récupération du projet

Cloner le dépôt Git, si le projet est hébergé sur un dépôt :

```bash
git clone <URL_DU_DEPOT>
cd cc_java_mongo
```

Remplacer `<URL_DU_DEPOT>` par l'adresse réelle du dépôt.

Si le projet est fourni sous forme d'archive, extraire son contenu puis ouvrir le dossier contenant `pom.xml`.

La structure doit notamment contenir les éléments suivants :

```text
cc_java_mongo/
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   └── test/
├── .mvn/
├── mvnw
├── mvnw.cmd
├── pom.xml
├── compose-sonarqube.yml
└── README.md
```

Le fichier `compose-sonarqube.yml` est nécessaire uniquement si l'on souhaite reproduire la configuration SonarQube décrite plus loin.

## 5. Configuration de MongoDB

### 5.1. Configuration par défaut

La configuration de MongoDB se trouve dans `src/main/resources/application.yml`.

Les paramètres principaux sont :

```yaml
spring:
  application:
    name: cc_java_mongo
  data:
    mongodb:
      uri: ${MONGODB_URI:mongodb://localhost:27017/medicaldb}
      auto-index-creation: true
```

La variable `MONGODB_URI` permet de remplacer l'URI par défaut.

Lorsque cette variable n'est pas définie, l'application utilise :

```text
mongodb://localhost:27017/medicaldb
```

La base `medicaldb` et les collections sont créées par MongoDB au fur et à mesure des premières écritures, selon les opérations réalisées par l'application.

Les collections métier utilisées sont :

- `patients`
- `medecins`
- `medicaments`
- `consultations`
- `utilisateurs`

GridFS utilise également ses collections dédiées pour les fichiers stockés.

### 5.2. Utiliser une autre instance MongoDB

Pour utiliser une instance distante ou une autre base locale, définir la variable d'environnement `MONGODB_URI`.

Exemple :

```text
MONGODB_URI=mongodb://localhost:27017/medicaldb
```

Pour une instance protégée par authentification, utiliser une URI adaptée à la configuration du serveur. Les identifiants doivent rester secrets et ne pas être inscrits dans le dépôt Git.

### 5.3. Données initiales

Le projet ne doit pas être considéré comme livré avec un jeu de données métier prérempli.

Pour tester les fonctionnalités, il est possible de créer les patients, médecins et médicaments via les endpoints REST, puis de créer des consultations en référençant les identifiants existants.

## 6. Configuration des variables d'environnement

La configuration de l'application utilise des variables d'environnement afin de ne pas inscrire les secrets dans le code source.

Les variables importantes sont :

| Variable | Obligatoire pour le lancement local sécurisé ? | Description |
|---|---|---|
| `MONGODB_URI` | Non si MongoDB utilise l'URI par défaut | URI de connexion à MongoDB |
| `JWT_SECRET` | Oui | Clé secrète encodée en Base64 utilisée pour signer les JWT |
| `BOOTSTRAP_USER_ENABLED` | Pour créer automatiquement le compte initial | Active ou désactive la création du compte administrateur initial |
| `APP_ADMIN_USERNAME` | Si le bootstrap est activé | Identifiant du compte initial |
| `APP_ADMIN_PASSWORD` | Si le bootstrap est activé | Mot de passe initial, qui doit être haché avant stockage |

### 6.1. Générer une clé JWT

La clé doit être propre à l'environnement. Ne jamais réutiliser une clé de test ou publier une clé secrète dans le dépôt.

Sous Windows, ouvrir PowerShell et exécuter :

```powershell
$bytes = New-Object byte[] 32
[System.Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
[Convert]::ToBase64String($bytes)
```

Copier la valeur générée dans la variable `JWT_SECRET` de la configuration locale.

Cette commande produit 32 octets aléatoires, encodés en Base64.

### 6.2. Définir les variables dans IntelliJ

1. Ouvrir le projet dans IntelliJ IDEA.
2. Aller dans **Run → Edit Configurations**.
3. Sélectionner la configuration de l'application Spring Boot, ou en créer une.
4. Dans **Environment variables**, définir les variables suivantes :

```text
JWT_SECRET=<CLE_BASE64_GENEREE>
BOOTSTRAP_USER_ENABLED=true
APP_ADMIN_USERNAME=admin
APP_ADMIN_PASSWORD=<MOT_DE_PASSE_LOCAL_FORT>
```

Remplacer les valeurs entre chevrons par les valeurs locales choisies. Ne pas conserver littéralement ces marqueurs.

Le nom `admin` est un exemple d'identifiant local ; il peut être remplacé.

Le mot de passe doit être choisi par la personne qui installe le projet. Il ne doit jamais être partagé dans le dépôt Git ou dans la documentation publique.

**Important :** un fichier `.env.local` n'est pas automatiquement chargé par Spring Boot ou par IntelliJ. Il faut donc soit définir les variables dans la configuration d'exécution IntelliJ, soit les exporter dans le terminal avant de démarrer l'application.

### 6.3. Utiliser PowerShell pour charger un fichier `.env.local`

Il est possible de créer un fichier `.env.local` à la racine du projet contenant les variables locales :

```text
MONGODB_URI=mongodb://localhost:27017/medicaldb
JWT_SECRET=<CLE_BASE64_GENEREE>
BOOTSTRAP_USER_ENABLED=true
APP_ADMIN_USERNAME=admin
APP_ADMIN_PASSWORD=<MOT_DE_PASSE_LOCAL_FORT>
```

Ce fichier ne doit pas être commité. Le `.gitignore` du projet doit notamment contenir :

```gitignore
.env
.env.*
!.env.example
```

Dans le terminal PowerShell ouvert à la racine du projet, charger les variables :

```powershell
Get-Content .env.local | ForEach-Object {
    if ($_ -match '^([^#=]+)=(.*)$') {
        $name = $Matches[1].Trim()
        $value = $Matches[2].Trim().Trim('"').Trim("'")
        Set-Item -Path "Env:$name" -Value $value
    }
}
```

Ce script convient aux valeurs simples sans besoin d'interpréter des échappements complexes. Pour les mots de passe contenant des caractères particuliers, vérifier soigneusement le format du fichier et privilégier la configuration d'environnement d'IntelliJ.

Les variables chargées dans un terminal ne sont pas automatiquement transmises aux autres terminaux ou aux configurations d'exécution IntelliJ.

## 7. Lancer l'application

### 7.1. Depuis IntelliJ IDEA

1. Ouvrir le projet Maven en sélectionnant le dossier contenant `pom.xml`.
2. Attendre la synchronisation des dépendances Maven.
3. Vérifier que le JDK du projet est configuré sur Java 21.
4. Vérifier que MongoDB fonctionne.
5. Définir les variables d'environnement décrites précédemment.
6. Lancer la classe principale `CcJavaMongoApplication`.

L'application écoute sur le port `8081`.

Le port `8080` était déjà occupé dans l'environnement de développement ; c'est pourquoi le port `8081` est configuré.

L'URL de base de l'API est :

```text
http://localhost:8081
```

### 7.2. Depuis le terminal Windows

Depuis la racine du projet, charger les variables d'environnement dans PowerShell, puis exécuter :

```powershell
.\mvnw.cmd spring-boot:run
```

Cette commande compile et lance l'application.

### 7.3. Construire le JAR

Pour construire l'application et exécuter les tests :

```powershell
.\mvnw.cmd clean verify
```

Le JAR exécutable est généré dans le dossier `target/`.

Le lancement du JAR nécessite toujours une instance MongoDB disponible et les variables d'environnement nécessaires :

```powershell
java -jar target/cc_java_mongo-0.0.1-SNAPSHOT.jar
```

Sous Linux ou macOS, utiliser `./mvnw` à la place de `.\mvnw.cmd`.

## 8. Vérifier que l'application fonctionne

### 8.1. Contrôle de santé

Une fois l'application démarrée, ouvrir :

```text
http://localhost:8081/actuator/health
```

Une réponse de santé positive ressemble à :

```json
{
  "status": "UP"
}
```

Le détail exact de la réponse dépend de l'état des composants surveillés.

### 8.2. Swagger UI

La documentation interactive est accessible à l'adresse :

```text
http://localhost:8081/swagger-ui/index.html
```

Le document OpenAPI est accessible ici :

```text
http://localhost:8081/v3/api-docs
```

Swagger permet de consulter les endpoints, leurs paramètres et leurs modèles de données.

Les routes métier protégées nécessitent une authentification. Il faut donc obtenir un JWT puis le fournir dans l'en-tête `Authorization` pour les tester.

## 9. Authentification et sécurité

L'API utilise Spring Security et des jetons JWT transmis en tant que Bearer Token.

### 9.1. Connexion

L'endpoint de connexion est :

```http
POST /api/auth/login
Content-Type: application/json
```

Exemple de corps JSON :

```json
{
  "username": "admin",
  "password": "REMPLACER_PAR_LE_MOT_DE_PASSE_LOCAL"
}
```

Le mot de passe doit correspondre à celui configuré lors de la création du compte initial.

Une connexion réussie retourne un jeton JWT et ses informations associées, sous une forme similaire à :

```json
{
  "token": "<JWT>",
  "tokenType": "Bearer",
  "expiresIn": 3600000
}
```

La valeur de `expiresIn` est exprimée en millisecondes. La durée configurée par défaut est d'une heure.

### 9.2. Utiliser le JWT

Pour appeler un endpoint protégé, ajouter l'en-tête :

```http
Authorization: Bearer <JWT>
```

Exemple avec PowerShell :

```powershell
$token = "<JWT_RETOURNE_PAR_LA_CONNEXION>"

Invoke-RestMethod `
  -Uri "http://localhost:8081/api/patients" `
  -Headers @{ Authorization = "Bearer $token" }
```

Dans cet exemple, remplacer `<JWT_RETOURNE_PAR_LA_CONNEXION>` par le jeton obtenu lors de la connexion.

Un appel sans jeton valide à un endpoint protégé doit être refusé.

### 9.3. Compte initial

Le compte initial peut être créé au démarrage si :

```text
BOOTSTRAP_USER_ENABLED=true
```

L'identifiant et le mot de passe sont alors définis par :

- `APP_ADMIN_USERNAME`
- `APP_ADMIN_PASSWORD`

Le mot de passe doit être stocké sous forme hachée par le mécanisme BCrypt du projet, et non en clair dans la base de données.

Pour un environnement partagé ou de production, désactiver le bootstrap après avoir établi une procédure adaptée de gestion des utilisateurs et des secrets.

### 9.4. Principes de sécurité

- Les endpoints métier sont protégés par Spring Security.
- L'authentification est sans session serveur et utilise des JWT.
- Le filtre JWT lit le jeton dans l'en-tête `Authorization`.
- Les mots de passe sont traités par un encodeur BCrypt.
- Les entrées des endpoints sont validées.
- La configuration de développement utilise des variables d'environnement pour les secrets.
- CSRF est désactivé dans la configuration actuelle, car l'API est conçue pour une authentification stateless utilisant exclusivement des Bearer Tokens dans l'en-tête `Authorization`, sans authentification par cookie.

Cette dernière décision suppose que l'application ne soit pas modifiée pour utiliser une authentification basée sur des cookies automatiquement envoyés par le navigateur. Toute évolution de ce type nécessite de réévaluer la protection CSRF.

## 10. Modèle de données MongoDB

Le modèle métier comprend quatre entités principales et une entité utilisateur pour l'authentification.

### 10.1. Patient

Collection : `patients`

Principaux champs :

- `numeroSecuriteSociale` : identifiant métier utilisé comme identifiant MongoDB.
- `nom` : nom du patient.

Le numéro de sécurité sociale est représenté sous forme de chaîne de caractères afin de conserver son format.

### 10.2. Médecin

Collection : `medecins`

Principaux champs :

- `matricule` : identifiant métier utilisé comme identifiant MongoDB.
- `nom` : nom du médecin.

### 10.3. Médicament

Collection : `medicaments`

Principaux champs :

- `code` : identifiant métier utilisé comme identifiant MongoDB.
- `libelle` : libellé du médicament.

### 10.4. Consultation

Collection : `consultations`

Principaux champs :

- `numero` : identifiant de la consultation.
- `date` : date et heure de la consultation.
- `patientId` : référence vers le patient.
- `medecinId` : référence vers le médecin.
- `prescriptions` : liste de prescriptions intégrées au document.
- `documentId` : référence facultative vers un fichier stocké dans GridFS.

Chaque prescription comprend :

- `medicamentId` : référence vers un médicament.
- `nbPrises` : nombre de prises prescrit.

Les prescriptions sont intégrées au document consultation, tandis que les patients, médecins et médicaments sont stockés dans des collections distinctes.

### 10.5. Utilisateur

Collection : `utilisateurs`

Cette collection contient les informations nécessaires à l'authentification, notamment l'identifiant utilisateur, le nom d'utilisateur, le mot de passe haché et le rôle.

### 10.6. Relations métier

Le modèle respecte les règles métier suivantes :

- Une consultation est associée à un patient.
- Une consultation est associée à un médecin.
- Une consultation peut contenir plusieurs prescriptions.
- Chaque prescription référence un médicament.
- Une consultation peut être associée à un document.

Les références sont représentées par des identifiants métier plutôt que par des relations SQL. Les services vérifient l'existence des entités référencées et la validité des données avant les opérations concernées.

Une consultation programmée doit respecter la règle de date future appliquée par les validations et services du projet.

## 11. Endpoints REST

Tous les endpoints métier présentés ci-dessous nécessitent un JWT valide, sauf les routes explicitement autorisées publiquement dans la configuration de sécurité.

La pagination repose sur les paramètres Spring Data usuels `page`, `size` et, si nécessaire, `sort`.

### 11.1. Authentification

| Méthode | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/login` | Authentifier un utilisateur et obtenir un JWT |

### 11.2. Patients

| Méthode | Endpoint | Description |
|---|---|---|
| GET | `/api/patients` | Lister les patients avec pagination |
| GET | `/api/patients?nom=Martin` | Rechercher les patients par nom |
| GET | `/api/patients/{numeroSecuriteSociale}` | Consulter un patient |
| POST | `/api/patients` | Créer un patient |
| PUT | `/api/patients/{numeroSecuriteSociale}` | Modifier un patient |
| DELETE | `/api/patients/{numeroSecuriteSociale}` | Supprimer un patient |

Exemple de création :

```json
{
  "numeroSecuriteSociale": "123456789012345",
  "nom": "Martin"
}
```

Le numéro de sécurité sociale est validé au format de 15 chiffres.

### 11.3. Médecins

| Méthode | Endpoint | Description |
|---|---|---|
| GET | `/api/medecins` | Lister les médecins avec pagination |
| GET | `/api/medecins?nom=Durand` | Rechercher les médecins par nom |
| GET | `/api/medecins/{matricule}` | Consulter un médecin |
| POST | `/api/medecins` | Créer un médecin |
| PUT | `/api/medecins/{matricule}` | Modifier un médecin |
| DELETE | `/api/medecins/{matricule}` | Supprimer un médecin |

Exemple de création :

```json
{
  "matricule": "MED001",
  "nom": "Durand"
}
```

### 11.4. Médicaments

| Méthode | Endpoint | Description |
|---|---|---|
| GET | `/api/medicaments` | Lister les médicaments |
| GET | `/api/medicaments/{code}` | Consulter un médicament |
| POST | `/api/medicaments` | Créer un médicament |
| PUT | `/api/medicaments/{code}` | Modifier un médicament |
| DELETE | `/api/medicaments/{code}` | Supprimer un médicament |

Exemple de création :

```json
{
  "code": "MEDICAMENT001",
  "libelle": "Paracétamol"
}
```

Les exemples sont destinés aux tests locaux et ne constituent pas des données médicales réelles.

### 11.5. Consultations

| Méthode | Endpoint | Description |
|---|---|---|
| GET | `/api/consultations` | Lister les consultations avec pagination |
| GET | `/api/consultations/{numero}` | Consulter une consultation |
| GET | `/api/consultations/patient/{patientId}` | Lister les consultations d'un patient avec pagination |
| POST | `/api/consultations` | Créer une consultation |
| PUT | `/api/consultations/{numero}` | Modifier une consultation |
| DELETE | `/api/consultations/{numero}` | Supprimer une consultation |
| PUT | `/api/consultations/{numero}/prescriptions` | Modifier les prescriptions d'une consultation |

Exemple de création :

```json
{
  "numero": "CONS001",
  "date": "2030-05-20T14:30:00",
  "patientId": "123456789012345",
  "medecinId": "MED001",
  "prescriptions": [
    {
      "medicamentId": "MEDICAMENT001",
      "nbPrises": 3
    }
  ]
}
```

La date doit être future au moment de la validation. Cet exemple utilise un format ISO-8601 sans fuseau horaire, correspondant au type `LocalDateTime` du projet. Lors des tests, utiliser une date future par rapport à l'heure réelle d'exécution.

Les identifiants du patient, du médecin et du médicament doivent correspondre à des données existantes. Le nombre de prises doit être strictement positif.

### 11.6. Documents associés aux consultations

| Méthode | Endpoint | Description |
|---|---|---|
| POST | `/api/consultations/{numero}/document` | Joindre un fichier à une consultation |
| GET | `/api/consultations/{numero}/document` | Télécharger le fichier associé |

L'envoi d'un fichier utilise une requête `multipart/form-data`. Le champ de fichier attendu doit être vérifié dans la déclaration du contrôleur `DocumentController`.

Les fichiers sont stockés dans MongoDB avec GridFS. La consultation conserve une référence vers le document associé.

Le comportement de remplacement d'un fichier existant dépend de la logique implémentée dans `DocumentService`.

## 12. Validation et gestion des erreurs

Les DTO d'entrée utilisent Jakarta Bean Validation pour vérifier notamment :

- Les champs obligatoires.
- Les tailles maximales des chaînes.
- Le format du numéro de sécurité sociale.
- La validité de la date de consultation.
- La positivité du nombre de prises.
- La présence des références nécessaires.

Les règles métier sont également vérifiées dans les services, notamment l'existence des patients, médecins et médicaments référencés.

La gestion centralisée des exceptions permet de renvoyer des réponses d'erreur structurées et des codes HTTP adaptés au type d'erreur.

Exemples de situations à tester :

- Requête sans authentification sur un endpoint protégé.
- Identifiant métier inexistant.
- Données entrantes invalides.
- Référence à un patient ou un médicament inexistant.
- Date de consultation non conforme à la règle métier.
- Nombre de prises nul ou négatif.

La forme exacte des réponses d'erreur est définie par `GlobalExceptionHandler`.

## 13. Exécuter les tests automatisés

Les tests sont exécutés avec Maven :

```powershell
.\mvnw.cmd test
```

Pour exécuter la suite complète et vérifier également la construction du projet :

```powershell
.\mvnw.cmd clean verify
```

Les tests du projet comprennent notamment :

- `CcJavaMongoApplicationTests` : test du contexte Spring Boot.
- `PatientServiceTest` : tests du service patient.
- `MedecinServiceTest` : tests du service médecin.
- `MedicamentServiceTest` : tests du service médicament.
- `ConsultationServiceTest` : tests du service consultation.

La dernière exécution vérifiée pendant le développement a donné **24 tests exécutés, aucune erreur, aucun échec et aucun test ignoré**. Ce résultat correspond à l'état du projet à ce moment-là et doit être confirmé après toute modification.

Les tests unitaires des services utilisent Mockito pour isoler la logique métier de ses dépendances. Le test de contexte vérifie le démarrage de l'application Spring Boot.

## 14. Analyse de la couverture avec JaCoCo

JaCoCo est configuré dans le projet Maven pour produire un rapport de couverture.

Exécuter :

```powershell
.\mvnw.cmd clean verify
```

Le rapport HTML est généré dans :

```text
target/site/jacoco/index.html
```

Ouvrir ce fichier dans un navigateur pour examiner la couverture des classes, méthodes et lignes.

La couverture doit être interprétée avec les tests réellement exécutés : une couverture élevée ne garantit pas à elle seule que tous les scénarios métier sont correctement testés.

## 15. Analyse de qualité avec SonarQube (facultatif)

SonarQube est utilisé pour analyser les problèmes de sécurité, de fiabilité, de maintenabilité, de couverture et de duplication du code.

Cette étape n'est pas nécessaire pour lancer l'API.

### 15.1. Démarrer SonarQube avec Docker

Le projet dispose d'un fichier `compose-sonarqube.yml` utilisé pendant le développement.

Depuis la racine du projet :

```powershell
docker compose -f compose-sonarqube.yml up -d
```

SonarQube est alors accessible à l'adresse :

```text
http://localhost:9000
```

Le premier démarrage peut prendre plusieurs minutes. Il faut attendre que le service soit disponible avant de lancer une analyse.

Le fichier Compose utilise des volumes Docker pour conserver les données, extensions et journaux entre les redémarrages.

### 15.2. Configurer le projet dans SonarQube

1. Ouvrir l'interface SonarQube.
2. Créer un projet nommé `CC Java MongoDB`.
3. Utiliser la clé de projet `CC-Java-MongoDB`.
4. Générer un jeton d'analyse depuis le compte utilisateur.
5. Conserver ce jeton localement sans le publier.

### 15.3. Lancer une analyse

Le jeton doit être chargé dans la variable d'environnement `SONAR_TOKEN`.

Si un fichier `.env.local` contient cette variable, charger sa valeur dans PowerShell :

```powershell
Get-Content .env.local | ForEach-Object {
    if ($_ -match '^SONAR_TOKEN=(.*)$') {
        $env:SONAR_TOKEN = $Matches[1].Trim().Trim('"').Trim("'")
    }
}
```

Vérifier que la variable est chargée sans afficher sa valeur :

```powershell
if ([string]::IsNullOrWhiteSpace($env:SONAR_TOKEN)) {
    Write-Host "SONAR_TOKEN absent"
} else {
    Write-Host "SONAR_TOKEN chargé"
}
```

Lancer ensuite l'analyse :

```powershell
.\mvnw.cmd clean verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar `
  "-Dsonar.projectKey=CC-Java-MongoDB" `
  "-Dsonar.projectName=CC Java MongoDB" `
  "-Dsonar.host.url=http://localhost:9000" `
  "-Dsonar.token=$env:SONAR_TOKEN"
```

Une exécution réussie se termine par :

```text
BUILD SUCCESS
```

Si SonarQube renvoie `401 Unauthorized`, vérifier que le jeton est valide, qu'il n'a pas été révoqué et que la variable `SONAR_TOKEN` contient bien le jeton attendu.

Les résultats sont consultables dans l'interface SonarQube. Les indicateurs dépendent de l'état du code, des tests et de la configuration au moment de l'analyse.

## 16. Architecture du projet

Le code est organisé par responsabilités dans le package principal :

```text
fr.dawan.cc_java_mongo
├── config
├── controller
├── dto
├── exception
├── model
├── repository
├── security
└── service
```

### Rôle des packages

- `config` : configuration générale de Spring et de la sécurité.
- `controller` : exposition des endpoints REST et réception des requêtes HTTP.
- `dto` : objets de transfert, validation des données d'entrée et modèles de réponse.
- `exception` : exceptions métier et gestion centralisée des erreurs.
- `model` : documents persistés dans MongoDB et objets intégrés.
- `repository` : accès aux collections MongoDB via Spring Data.
- `security` : authentification, chargement des utilisateurs, génération et validation des JWT.
- `service` : logique métier et coordination des opérations de persistance.

Cette architecture sépare les responsabilités et facilite les tests, la maintenance et les évolutions du projet.

## 17. Configuration applicative

Les principaux paramètres se trouvent dans `src/main/resources/application.yml`.

| Paramètre | Valeur par défaut ou rôle |
|---|---|
| `spring.application.name` | `cc_java_mongo` |
| URI MongoDB | `mongodb://localhost:27017/medicaldb` |
| Port HTTP | `8081` |
| Niveau de journalisation racine | `INFO` |
| Niveau de journalisation du projet | `DEBUG` |
| Durée de validité du JWT | `3600000` millisecondes |
| Endpoints Actuator exposés | `health`, `info` |

La clé JWT et les paramètres du compte initial sont injectés par variables d'environnement. Il ne faut pas inscrire de secrets dans `application.yml`.

Les endpoints de documentation Swagger et le contrôle de santé sont autorisés par la configuration de sécurité actuelle. Les autres requêtes nécessitent une authentification valide.

## 18. Dépannage

### L'application ne démarre pas

- Vérifier la version du JDK.
- Vérifier que les dépendances Maven ont été téléchargées.
- Consulter les premières erreurs dans la console.
- Vérifier que les variables d'environnement nécessaires sont présentes.

### Impossible de se connecter à MongoDB

- Vérifier que MongoDB fonctionne.
- Vérifier le port `27017`.
- Vérifier `MONGODB_URI`.
- Vérifier les droits d'accès si la base utilise une authentification.

### Erreur de configuration JWT

- Vérifier que `JWT_SECRET` est défini.
- Vérifier que la valeur Base64 a été générée correctement.
- Ne pas utiliser une valeur vide ou un exemple de documentation comme clé de production.

### Erreur HTTP 401

- Vérifier que l'identifiant et le mot de passe sont corrects.
- Vérifier que la connexion retourne un JWT.
- Vérifier que les appels protégés incluent `Authorization: Bearer <JWT>`.
- Vérifier que le jeton n'a pas expiré.

### Erreur HTTP 400

- Vérifier les champs obligatoires.
- Vérifier le format des identifiants.
- Vérifier les contraintes de validation des DTO.
- Vérifier que la date de consultation est future et que le nombre de prises est positif.

### Erreur HTTP 404

- Vérifier que l'identifiant demandé existe.
- Vérifier le chemin de l'endpoint et la méthode HTTP utilisée.

### Le port 8081 est occupé

Modifier temporairement la propriété `server.port` dans la configuration ou utiliser un autre port libre, puis adapter les URL de test en conséquence.

### SonarQube ne démarre pas

- Vérifier que Docker Desktop fonctionne.
- Vérifier que le port `9000` n'est pas déjà occupé.
- Consulter les journaux du conteneur avec :

```powershell
docker logs cc-java-mongo-sonarqube
```

## 19. Bonnes pratiques et limites

- Ne jamais commiter de secrets, de jetons JWT ou de mots de passe.
- Ne jamais utiliser de véritables données médicales pour les tests locaux.
- Utiliser des DTO plutôt que d'exposer directement les documents MongoDB dans l'API.
- Maintenir la validation dans les DTO et les règles métier dans les services.
- Tester les modifications avec Maven avant de les intégrer.
- Réévaluer les règles de sécurité avant toute mise en production.
- Prévoir une stratégie adaptée de sauvegarde, de gestion des utilisateurs et de protection des données pour tout environnement réel.

Le projet constitue une base d'API REST pédagogique. Une mise en production nécessiterait notamment une revue de sécurité complète, une politique de gestion des secrets, une configuration de déploiement adaptée et des tests d'intégration plus étendus.

## 20. État du projet

À la dernière vérification effectuée pendant le développement :

- L'application Spring Boot démarre.
- MongoDB est utilisé comme base de données.
- La connexion JWT a été testée avec succès.
- Les endpoints protégés ont été testés avec un jeton valide.
- Swagger UI est accessible.
- Les tests automatisés passent : 24 tests, sans échec.
- L'analyse SonarQube se termine par `BUILD SUCCESS`.
- La fiabilité SonarQube est notée A.
- La maintenabilité SonarQube est notée A.
- Des améliorations restent possibles sur la couverture des tests et sur certains problèmes de sécurité ou de maintenabilité.

Ces résultats décrivent l'état constaté pendant le développement et devront être actualisés après les prochaines évolutions.

## 21. Licence et contribution

Aucune licence spécifique n'est définie dans cette documentation. Ajouter un fichier de licence si le projet doit être distribué selon des conditions particulières.

Toute contribution devrait conserver la séparation des couches, les validations métier et les tests automatisés. Avant de soumettre une modification, exécuter au minimum :

```powershell
.\mvnw.cmd clean verify
```
