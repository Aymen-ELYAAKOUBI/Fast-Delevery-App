# Fast Delivery App

Application Android de livraison construite avec Kotlin et Jetpack Compose. Le projet propose des parcours distincts pour les clients, les livreurs et les entreprises, avec Firebase Authentication et Cloud Firestore.

> **État : MVP en développement.** La connexion Firebase et la lecture du tableau de bord ont été vérifiées sur un appareil. Le cycle complet d’une commande (création, acceptation par un livreur, collecte et livraison) doit encore être validé avec deux comptes de test. Les tarifs, distances et certaines étapes de carte restent des valeurs de démonstration : ne pas utiliser pour une activité commerciale réelle.

## Fonctionnalités

### Client
- Créer un compte et se connecter avec une adresse e-mail et un mot de passe.
- Envoyer une demande avec les adresses de collecte et de livraison, le nom et le téléphone du destinataire, et une description du colis.
- Suivre le statut de ses demandes dans une liste mise à jour depuis Firestore.

### Livreur
- Consulter les demandes en attente dans la file partagée.
- Accepter une demande; la réservation est effectuée côté Firestore pour éviter qu’elle soit attribuée à plusieurs livreurs.
- Faire avancer le statut de la commande : en attente, acceptée, récupérée, livrée.
- Consulter l’interface de carte et l’historique existants.

### Entreprise
- Créer des demandes de livraison depuis son compte.
- Consulter les compteurs et les statuts des demandes créées par ce compte.

Chaque compte ne voit que les commandes qu’il possède. Un livreur voit les demandes en attente et celles qui lui sont attribuées. Les comptes livreur et entreprise restent en attente tant qu’un administrateur ne les a pas approuvés dans Firestore. Les comptes entreprise ne disposent pas encore de gestion d’équipe ni de rôles administratifs.

## Technologies

- Kotlin, Jetpack Compose et Material 3
- Navigation Compose
- Firebase Authentication (e-mail/mot de passe)
- Cloud Firestore avec la base nommée `fast-delivery-app`
- Google Maps SDK et Maps Compose
- OSRM pour les itinéraires, avec Google Routes comme solution de repli configurée dans le projet
- ZXing Core

## Prérequis

- Android Studio avec le JDK intégré recommandé.
- Android SDK Platform 37.
- Une clé Google Maps avec Maps SDK for Android activé.
- Un appareil ou émulateur Android 10 / API 29 ou ultérieur.

Configuration Android du module : `minSdk 29`, `targetSdk 37`.

## Démarrage

1. Clonez le dépôt et ouvrez le projet dans Android Studio :

   ```bash
   git clone https://github.com/Aymen-ELYAAKOUBI/Fast-Delevery-App.git
   ```

2. Ajoutez votre clé Google Maps dans le fichier `local.properties` à la racine :

   ```properties
   MAPS_API_KEY=VOTRE_CLE_API_GOOGLE_MAPS
   ```

   `local.properties` est ignoré par Git. Restreignez la clé dans Google Cloud à l’application Android (package et empreinte SHA-1) et aux API nécessaires. Ne mettez pas cette clé dans le dépôt.

3. Vérifiez que le fichier `app/google-services.json` correspond à votre projet Firebase et au package `com.example.suiverliverson`. Si vous utilisez votre propre projet Firebase, remplacez ce fichier par la configuration Android de ce projet.

4. Synchronisez Gradle dans Android Studio, puis lancez la configuration `app`.

Pour construire l’APK de débogage :

- Windows : `gradlew.bat :app:assembleDebug`
- macOS/Linux : `./gradlew :app:assembleDebug`

## Configuration Firebase

Le projet Firebase utilisé par défaut est `fast-delivery-app-4f971`. La base Firestore nommée est `fast-delivery-app` dans la région `europe-southwest1`.

1. Dans Firebase Console, activez le fournisseur **E-mail/Mot de passe** dans Authentication.
2. Créez ou sélectionnez la base Firestore nommée `fast-delivery-app`.
3. Publiez les règles du dépôt (`firestore.rules`) sur **cette base nommée**. Le fichier `firebase.json` associe explicitement ces règles à la base.
4. Vérifiez dans l’application qu’un compte client peut ouvrir son tableau de bord sans erreur de permission.
5. Pour approuver un livreur ou une entreprise, ouvrez `users/{uid}` dans la base Firestore `fast-delivery-app` et définissez le champ texte `approvalStatus` à `approved`. L’utilisateur doit ensuite se déconnecter puis se reconnecter. Seul un administrateur du projet doit modifier ce champ.

Vous pouvez aussi publier les règles avec Firebase CLI après connexion :

```bash
npx -y firebase-tools@latest deploy --only firestore:rules --project fast-delivery-app-4f971
```

Les règles refusent par défaut les accès non autorisés. Elles permettent au propriétaire de lire ses commandes et limitent les actions du livreur aux demandes en attente ou attribuées à son UID. Toute modification de règles doit être vérifiée avant publication.

## Test manuel du parcours de commande

Utilisez des comptes de test distincts pour le client (ou l’entreprise) et le livreur :

1. Connectez-vous avec le compte client/entreprise et créez une demande de test.
2. Vérifiez que cette demande apparaît dans sa liste avec le statut « en attente ».
3. Connectez-vous avec un compte livreur et vérifiez que la demande apparaît dans la file.
4. Acceptez-la, puis avancez les étapes de collecte et de livraison.
5. Revenez au compte client/entreprise et vérifiez que le statut s’actualise.
6. Vérifiez qu’un autre compte client ne peut pas lire cette commande.

Ne saisissez pas de vraies adresses ou coordonnées de destinataires lors des essais.

## Limites actuelles

- Le prix est une valeur fixe de démonstration; aucune tarification réelle n’est calculée.
- Les coordonnées de livraison et les distances ne sont pas calculées à partir des adresses saisies.
- La carte et la progression du parcours ne constituent pas un suivi GPS temps réel du livreur.
- Les statistiques du livreur comprennent encore des données de démonstration.
- Les entreprises ne peuvent pas inviter des employés ou gérer plusieurs utilisateurs.
- L’approbation des livreurs et entreprises se fait manuellement dans Firebase Console; aucun écran administrateur n’est fourni.
- Les règles Firestore n’ont pas encore été vérifiées avec Firebase Emulator Suite.

## Structure principale

```text
app/src/main/java/com/example/suiverliverson/
├── AppNav.kt                    # Navigation et routage selon le rôle
├── AccountStore.kt              # Firebase Auth et profils de compte
├── OrderRepository.kt           # Création, écoute et attribution des demandes
├── RoleHomeScreen.kt            # Espaces client et entreprise
├── HomeScreen.kt                # Tableau de bord du livreur et file de demandes
├── DeliveryManager.kt           # Étapes de livraison et historique du livreur
├── MapScreen.kt                 # Carte et interface de parcours
└── RouteService.kt              # Récupération des itinéraires
firestore.rules                  # Règles de sécurité Firestore
firebase.json                    # Association règles / base Firestore nommée
```

## Contribution

Les retours et contributions sont les bienvenus. Ouvrez une issue ou une pull request en décrivant clairement le changement proposé.

## Auteur

**Aymen Elyaakoubi**

- Portfolio : https://my-portfolio-beta-eight-27.vercel.app/
- GitHub : https://github.com/Aymen-ELYAAKOUBI

## Licence

Aucune licence n’est actuellement déclarée. Contactez l’auteur avant de réutiliser ou redistribuer le code.
