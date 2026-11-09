# Fast Delivery

**Fast Delivery** est une application Android de démonstration pour la gestion et le suivi de livraisons. Elle propose une interface dédiée aux livreurs, avec des commandes, des itinéraires sur carte et des écrans de suivi d’activité.

> Le projet est en cours de développement. Les données de commandes et de statistiques sont actuellement générées localement pour la démonstration. L’intégration Firebase est prévue/en cours et n’est pas encore configurée dans cette version.

## Fonctionnalités

- Création de compte, connexion et récupération de mot de passe avec stockage local.
- Tableau de bord avec commandes disponibles et commande active.
- Carte avec points de collecte, destination et itinéraire routier.
- Historique des livraisons et statistiques.
- Profil, paramètres, choix de langue et thème sombre.
- Écrans de notifications et suivi des étapes de livraison.

## Technologies

- Kotlin
- Jetpack Compose et Material 3
- Navigation Compose
- Google Maps SDK et Maps Compose
- OSRM pour le calcul d’itinéraires, avec Google Routes comme solution de repli
- ZXing Core

## Prérequis

- Android Studio avec prise en charge de la configuration Gradle du projet.
- JDK 11 ou version compatible avec Gradle.
- Android SDK Platform 37.
- Une clé API Google Maps avec Maps SDK for Android activé.

L’application cible Android 15/API 35 ou ultérieur (targetSdk 37) et prend en charge Android 10/API 29 ou ultérieur (minSdk 29).

## Configuration

1. Clonez le dépôt et ouvrez-le dans Android Studio :

   ```bash
   git clone https://github.com/Aymen-ELYAAKOUBI/Fast-Delevery-App.git
   ```

2. Dans le fichier local.properties à la racine du projet, ajoutez votre propre clé Maps :

   ```properties
   MAPS_API_KEY=VOTRE_CLE_API_GOOGLE_MAPS
   ```

   Ce fichier est ignoré par Git. Ne publiez pas votre clé API. Dans Google Cloud, restreignez-la à l’application Android (nom du package et empreinte SHA-1) et aux API nécessaires.

3. Synchronisez Gradle, puis lancez l’application sur un appareil ou un émulateur Android 10 ou ultérieur.

Pour générer un APK de débogage depuis Windows :

```powershell
.\gradlew.bat assembleDebug
```

## Données et services

Les comptes sont enregistrés localement sur l’appareil avec SharedPreferences. Les commandes de démonstration et certaines statistiques sont générées dans l’application. Cette version ne fournit pas encore de backend partagé entre utilisateurs.

Les itinéraires sont demandés à OSRM. Le code prévoit Google Routes comme solution de repli lorsque la clé Maps configurée le permet. Une connexion Internet est nécessaire pour charger les cartes et les itinéraires.

## Firebase

L’intégration Firebase est un chantier en cours. L’application n’utilise pas encore Firebase Authentication, Cloud Firestore ni un backend Firebase dans la version actuelle. Cette section pourra être mise à jour à mesure que ces services seront intégrés.

## Structure du projet

```text
app/src/main/java/com/example/suiverliverson/
├── AppNav.kt                 # Navigation entre les écrans
├── HomeScreen.kt             # Tableau de bord et commandes
├── MapScreen.kt              # Carte et suivi d’itinéraire
├── RouteService.kt           # Récupération et traitement des itinéraires
├── DeliveryManager.kt         # État du parcours de livraison
├── AccountStore.kt            # Comptes stockés localement
├── HistoryScreen.kt           # Historique
├── StatsScreen.kt             # Statistiques
├── ProfileScreen.kt           # Profil
├── SettingsScreen.kt          # Paramètres
└── ...
```

## Contribution

Les retours et contributions sont les bienvenus. Pour proposer une amélioration, ouvrez une issue ou soumettez une pull request en décrivant le changement.

## Auteur

**Aymen Elyaakoubi**

- Portfolio : https://my-portfolio-beta-eight-27.vercel.app/
- GitHub : https://github.com/Aymen-ELYAAKOUBI

## Licence

Aucune licence n’est actuellement déclarée. Contactez l’auteur avant de réutiliser ou redistribuer le code.
