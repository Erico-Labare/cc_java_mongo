# CC Java MongoDB

API REST de gestion de patients, médecins, médicaments, consultations et documents médicaux, construite avec Java et Spring Boot. Les données métier sont stockées dans MongoDB ; les documents joints sont stockés avec GridFS.

## Sommaire

- [Technologies et prérequis](#technologies-et-prérequis)
- [Récupérer le projet](#récupérer-le-projet)
- [Démarrer MongoDB](#démarrer-mongodb)
- [Configuration](#configuration)
- [Lancer l’application](#lancer-lapplication)
- [Authentification](#authentification)
- [API REST](#api-rest)
- [Données de démonstration](#données-de-démonstration)
- [Tests et documentation](#tests-et-documentation)
- [Architecture](#architecture)

## Technologies et prérequis

Le projet utilise les versions et dépendances déclarées dans [`pom.xml`](pom.xml) : Java 21, Spring Boot 3.5.16, Spring Web, Spring Data MongoDB, Spring Security, Jakarta Validation, Springdoc OpenAPI, JJWT et JaCoCo. Maven Wrapper (`mvnw` et `mvnw.cmd`) est fourni.

Prérequis pour lancer l’application :

- JDK 21 ;
- MongoDB accessible ;
- une clé JWT Base64 configurée dans `JWT_SECRET`.

Docker est facultatif. Git et un client HTTP sont utiles pour récupérer et utiliser le projet.

## Récupérer le projet

```bash
git clone https://github.com/Erico-Labare/cc_java_mongo.git
cd cc_java_mongo
```

## Démarrer MongoDB

Une configuration locale est fournie dans [`compose.yaml`](compose.yaml). Elle démarre MongoDB sur le port `27017` et Mongo Express sur `http://localhost:8082` :

```powershell
docker compose up -d
```

Mongo Express y est configuré sans authentification HTTP : réservez cette configuration à un environnement local de confiance. Pour arrêter les services en conservant les données du volume :

```powershell
docker compose stop
```

L’application utilise par défaut `mongodb://localhost:27017/medicaldb`. Elle peut aussi utiliser une instance déjà installée ou une autre URI via `MONGODB_URI`.

## Configuration

Les propriétés par défaut se trouvent dans [`src/main/resources/application.yaml`](src/main/resources/application.yaml).

| Variable | Valeur par défaut | Rôle |
|---|---|---|
| `MONGODB_URI` | `mongodb://localhost:27017/medicaldb` | URI MongoDB |
| `JWT_SECRET` | aucune | Clé Base64 requise pour signer les JWT |
| `BOOTSTRAP_USER_ENABLED` | `false` | Active la création d’un administrateur initial |
| `APP_ADMIN_USERNAME` | `admin` | Identifiant du compte bootstrap |
| `APP_ADMIN_PASSWORD` | aucune | Mot de passe du compte bootstrap, encodé avec BCrypt |

Générez une clé locale de 32 octets, encodée en Base64, puis définissez les variables dans le terminal utilisé pour lancer l’application. Exemple PowerShell :

```powershell
$bytes = [byte[]]::new(32)
[System.Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
$env:BOOTSTRAP_USER_ENABLED = 'true'
$env:APP_ADMIN_USERNAME = 'admin'
$env:APP_ADMIN_PASSWORD = Read-Host 'Mot de passe local de l’administrateur'
```

Le bootstrap est désactivé par défaut. Pour créer automatiquement un premier compte, activez-le et définissez un mot de passe non vide. Le compte n’est créé que si cet identifiant n’existe pas déjà. Ne partagez pas de clé JWT ni de mot de passe dans le dépôt. Le fichier `.gitignore` ignore `/.env.local` uniquement ; Spring Boot ne charge pas automatiquement ce fichier.

Sous Linux/macOS, définissez les mêmes variables dans le shell, par exemple avec `export JWT_SECRET=...`, puis utilisez `./mvnw` au lieu de `.\mvnw.cmd`.

## Lancer l’application

Depuis la racine du dépôt, sous Windows PowerShell :

```powershell
.\mvnw.cmd spring-boot:run
```

Sous Linux/macOS :

```bash
./mvnw spring-boot:run
```

L’API écoute sur `http://localhost:8081`. Le contrôle de santé est public : `http://localhost:8081/actuator/health`. L’endpoint `/actuator/info` est exposé par Actuator, mais nécessite une authentification dans la configuration de sécurité actuelle.

Pour construire le JAR et exécuter les tests :

```powershell
.\mvnw.cmd clean verify
```

Le JAR est produit dans `target/cc_java_mongo-0.0.1-SNAPSHOT.jar`. Son lancement requiert les mêmes paramètres MongoDB et JWT :

```powershell
java -jar target/cc_java_mongo-0.0.1-SNAPSHOT.jar
```

## Authentification

La connexion est publique :

```http
POST /api/auth/login
Content-Type: application/json

{"username":"admin","password":"<mot-de-passe-local>"}
```

Une réponse réussie contient `token`, `tokenType` (`Bearer`) et `expiresIn` en millisecondes. La durée par défaut est de 3 600 000 ms. Envoyez ensuite le jeton dans l’en-tête `Authorization` des appels protégés :

```http
Authorization: Bearer <JWT>
```

Dans Swagger UI, utilisez **Authorize** et saisissez le jeton Bearer. L’interface est disponible à `http://localhost:8081/swagger-ui/index.html` et la spécification JSON à `http://localhost:8081/v3/api-docs`. Ces routes sont publiques ; les autres routes nécessitent une authentification.

Les rôles sont `PATIENT`, `MEDECIN` et `ADMIN`. Les restrictions d’accès aux dossiers sont appliquées dans les services :

- un patient accède à son propre dossier et à ses consultations ;
- un médecin accède aux patients qui lui sont rattachés et à ses consultations ;
- un administrateur peut gérer les données métier ;
- un patient ne peut pas gérer le catalogue des médecins ou médicaments.

Le code réserve également `/api/admin/**` au rôle ADMIN, mais aucun contrôleur d’administration correspondant n’est actuellement présent.

CORS autorise les origines de développement `http://localhost:3000`, `http://localhost:4200` et `http://localhost:5173`, sans cookies. Les méthodes autorisées sont `GET`, `POST`, `PUT`, `PATCH`, `DELETE` et `OPTIONS`.

## API REST

Toutes les routes ci-dessous, hors connexion, Swagger/OpenAPI et `/actuator/health`, nécessitent un JWT. Les créations renvoient `201 Created`, les suppressions indiquées renvoient `204 No Content`, et les opérations de lecture ou mise à jour renvoient normalement `200 OK`. Les erreurs métier et de validation sont documentées plus bas.

| Méthode | Route | Accès et comportement |
|---|---|---|
| `POST` | `/api/auth/login` | Connexion publique, renvoie le JWT |
| `GET` | `/api/patients` | Patients visibles par le rôle ; `nom` facultatif, pagination par `page`, `size`, `sort` (défaut : taille 10, tri `nom`) |
| `GET` | `/api/patients/{numeroSecuriteSociale}` | Détail accessible selon le rattachement et le rôle |
| `POST` | `/api/patients` | ADMIN ; corps `PatientRequest` |
| `PUT` | `/api/patients/{numeroSecuriteSociale}` | ADMIN ou patient sur son propre dossier ; met à jour le nom |
| `DELETE` | `/api/patients/{numeroSecuriteSociale}` | ADMIN ; `204 No Content` |
| `GET` | `/api/medecins` | MEDECIN ou ADMIN ; `nom` facultatif, pagination (défaut : taille 10, tri `nom`) |
| `GET` | `/api/medecins/{matricule}` | MEDECIN ou ADMIN |
| `POST` | `/api/medecins` | ADMIN ; corps `MedecinRequest` |
| `PUT` | `/api/medecins/{matricule}` | ADMIN ; met à jour le nom |
| `DELETE` | `/api/medecins/{matricule}` | ADMIN ; `204 No Content` |
| `GET` | `/api/medicaments` | Tout utilisateur authentifié |
| `GET` | `/api/medicaments/{code}` | Tout utilisateur authentifié |
| `POST` | `/api/medicaments` | ADMIN ; corps `MedicamentRequest` |
| `PUT` | `/api/medicaments/{code}` | ADMIN ; met à jour le libellé |
| `DELETE` | `/api/medicaments/{code}` | ADMIN ; `204 No Content` |
| `GET` | `/api/consultations` | Liste filtrée selon patient, médecin ou ADMIN ; pagination (défaut : taille 10, tri `date`) |
| `GET` | `/api/consultations/patient/{patientId}` | Consultations accessibles pour ce patient ; pagination (défaut : taille 10, tri `date`) |
| `GET` | `/api/consultations/{numero}` | Détail soumis aux contrôles d’accès du service |
| `POST` | `/api/consultations` | MEDECIN (pour un patient rattaché) ou ADMIN ; consultation future |
| `PUT` | `/api/consultations/{numero}` | MEDECIN autorisé ou ADMIN ; consultation future |
| `DELETE` | `/api/consultations/{numero}` | MEDECIN autorisé ou ADMIN ; consultation future ; `204 No Content` |
| `PUT` | `/api/consultations/{numero}/prescriptions` | MEDECIN autorisé ou ADMIN ; consultation future |
| `POST` | `/api/consultations/{numero}/document` | MEDECIN associé ou ADMIN ; fichier multipart dans le champ `file` |
| `GET` | `/api/consultations/{numero}/document` | Téléchargement selon le rattachement au patient ou médecin et le rôle ADMIN |

Les paramètres `page`, `size` et `sort` suivent la pagination Spring Data. Exemple : `GET /api/patients?nom=Martin&page=0&size=5&sort=nom,asc`.

Exemple de création de patient (données fictives) :

```http
POST /api/patients
Content-Type: application/json
Authorization: Bearer <JWT>

{"numeroSecuriteSociale":"000000000000000","nom":"Martin"}
```

Le numéro doit contenir 15 chiffres et le nom ne doit pas dépasser 100 caractères. Les données de cet exemple ne sont pas destinées à représenter une personne réelle.

Exemple de consultation :

```json
{
  "numero": "CONS-EXEMPLE-001",
  "date": "2030-05-20T14:30:00",
  "patientId": "000000000000000",
  "medecinId": "MED001",
  "prescriptions": [
    {"medicamentId": "MEDICAMENT001", "nbPrises": 2}
  ]
}
```

La date doit être future au moment de l’appel. Les identifiants référencés doivent déjà exister ; `nbPrises` doit être positif. `date` est un `LocalDateTime` au format ISO sans fuseau horaire. Un nouvel upload de document remplace le fichier associé à la consultation.

### Validation et erreurs

Les DTO valident les champs requis et leurs contraintes (formats et tailles). Les erreurs de validation ou de corps JSON mal formé renvoient `400`; une ressource absente renvoie `404`; les identifiants de connexion invalides renvoient `401`; un accès métier refusé renvoie `403`. Les erreurs traitées par `GlobalExceptionHandler` contiennent notamment un horodatage, un statut et un message. Les erreurs 401/403 générées directement par Spring Security peuvent avoir un corps différent.

## Données de démonstration

Un jeu de démonstration est fourni par `DemoFixtureRunner`, mais il n’est pas chargé au démarrage normal. Il crée des comptes ADMIN, MEDECIN et PATIENT ainsi que des patients, médecins, médicaments et consultations fictifs. Les identifiants des comptes sont dans le runner ; leur mot de passe de démonstration y est défini et ne doit être utilisé que dans un environnement local isolé. Les identifiants `DEMO-PAT-*` sont synthétiques et ne respectent pas le format de numéro de sécurité sociale de l’API. Les valeurs `nbPrises` illustrent uniquement le champ technique et ne sont pas des posologies médicales.

Le script [`scripts/reset-demo-fixture.ps1`](scripts/reset-demo-fixture.ps1) **efface et recrée** les collections `patients`, `medecins`, `medicaments`, `consultations` et `utilisateurs` de la base `medicaldb`. Il conserve `fs.files` et `fs.chunks` de GridFS ; des fichiers peuvent alors ne plus être référencés. L’opération n’est pas transactionnelle.

Ne lancez ce script que si le remplacement de ces données est souhaité. Il vérifie la base cible et demande la saisie exacte `RESET_DEMO_DATA` avant de continuer. Prérequis : JDK 21, MongoDB, PowerShell et Maven Wrapper. La cible par défaut est `mongodb://localhost:27017/medicaldb` ; `MONGODB_URI` peut sélectionner une autre instance de cette base. Le script lance `mvnw.cmd -DskipTests package`, puis le runner avec le profil `demo-fixture` et l’activation du fixture.

## Tests et documentation

Les tests automatisés se lancent avec :

```powershell
.\mvnw.cmd test
```

Pour la compilation, les tests et les rapports de vérification :

```powershell
.\mvnw.cmd clean verify
```

Les tests unitaires des services utilisent Mockito. Les tests Spring d’intégration se connectent à MongoDB sur `localhost:27017` et utilisent les bases dédiées `cc_java_mongo_context_test`, `cc_java_mongo_jwt_test` et `cc_java_mongo_roles_test`. Une instance MongoDB locale doit donc être disponible pour la suite complète. JaCoCo produit son rapport lors de `verify` dans `target/site/jacoco/index.html`.

Générer la Javadoc :

```powershell
.\mvnw.cmd javadoc:javadoc
```

La sortie Maven est `target/site/apidocs/index.html`. Une copie HTML a été produite dans [`docs/apidocs/index.html`](docs/apidocs/index.html) ; ces fichiers doivent être ajoutés à Git pour être consultables sur le dépôt distant. La configuration Maven active `doclint` avec `all,-missing` : les contrôles de syntaxe restent actifs, tandis que les commentaires absents ne font pas échouer la génération.

Une configuration facultative SonarQube est fournie dans [`compose-sonarqube.yml`](compose-sonarqube.yml). Démarrage local :

```powershell
docker compose -f compose-sonarqube.yml up -d
```

Après avoir créé le projet et un jeton dans SonarQube et défini `SONAR_TOKEN`, l’analyse Maven peut être lancée ainsi :

```powershell
.\mvnw.cmd clean verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar `
  "-Dsonar.projectKey=CC-Java-MongoDB" `
  "-Dsonar.projectName=CC Java MongoDB" `
  "-Dsonar.host.url=http://localhost:9000" `
  "-Dsonar.token=$env:SONAR_TOKEN"
```

Cette analyse nécessite un serveur SonarQube démarré et un jeton valide. Elle n’est pas nécessaire pour lancer l’API.

## Architecture

Le code applicatif se trouve dans `src/main/java` sous les packages :

- `config` : configuration Spring, sécurité, bootstrap utilisateur et OpenAPI ;
- `controller` : endpoints REST ;
- `dto` : formats d’entrée et de sortie, avec validation ;
- `exception` : exceptions et réponses d’erreur ;
- `fixture` : initialisation explicite des données de démonstration ;
- `model` : documents MongoDB et objets métier ;
- `repository` : accès MongoDB via Spring Data ;
- `security` : JWT et utilitaires d’accès ;
- `service` : règles métier et contrôle d’accès.

Les principales collections métier sont `patients`, `medecins`, `medicaments`, `consultations` et `utilisateurs`. Les prescriptions sont intégrées au document MongoDB de consultation ; les fichiers sont stockés par GridFS dans `fs.files` et `fs.chunks`.

Les tests sont situés dans `src/test/java`. Le dépôt ne définit pas de fichier de licence à la racine.
