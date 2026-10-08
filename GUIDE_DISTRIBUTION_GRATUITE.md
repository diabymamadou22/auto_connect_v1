# 🇲🇱 Guide Officiel de Distribution 100% Gratuite - AutoConnect Mali

> **Zéro dépense (0 FCFA / 0 €)** : Pas besoin d'acheter un serveur, d'abonnement ou de nom de domaine. Tout est propulsé par des outils officiels gratuits et sécurisés.

---

## 📦 1. Votre APK Release Signé est Prêt !

Le fichier APK de production a été compilé et signé avec succès :
- **Emplacement 1** : `app/build/outputs/apk/release/app-release.apk`
- **Emplacement 2 (prêt pour le web)** : `docs/autoconnect-mali.apk` (Taille : ~17 Mo)

Cet APK est compatible avec tous les smartphones Android (Samsung, Tecno, Infinix, Xiaomi, Huawei, etc.) de la version **Android 7.0 (Nougat) jusqu'à Android 15**.

---

## 🌐 2. Hébergement Gratuit & Lien Public Permanent (GitHub Releases)

La meilleure solution au monde pour héberger un fichier APK gratuitement et sans limite de bande passante est **GitHub Releases** :

### Les étapes (2 minutes) :
1. Sur votre dépôt GitHub, cliquez sur **Releases** (dans la colonne de droite) puis sur **« Draft a new release »** (ou « Create a new release »).
2. Choisissez un tag de version : tapez `v1.0.0` et cliquez sur *Create new tag*.
3. Titre de la release : `AutoConnect Mali v1.0.0 - Version Officielle`
4. Description : listez les nouveautés (ex: *SOS Dépannage, carte des garages, devis en ligne, espace garagiste*).
5. **Glissez-déposez le fichier `app-release.apk`** dans la boîte de fichiers (Attach binaries).
6. Cliquez sur **« Publish release »**.

### 🔗 Votre Lien Public Permanent Direct :
Dès la publication, vous bénéficiez automatiquement d'un lien officiel direct pour télécharger le fichier :
```
https://github.com/<votre-nom-d-utilisateur>/autoconnect-mali/releases/latest/download/autoconnect-mali.apk
```
*Ce lien ne change jamais et pointera toujours vers votre toute dernière version !*

---

## 🚀 3. Page de Téléchargement Gratuite (GitHub Pages)

Le dossier `docs/` de ce projet contient déjà une **page web moderne et responsive** (`docs/index.html`) avec :
- Le bouton de téléchargement direct de l'APK
- Le QR Code interactif haute définition
- Le guide d'installation illustré étape par étape
- Le bouton de partage WhatsApp en 1 clic

### Comment l'activer gratuitement en 3 clics :
1. Sur votre dépôt GitHub, allez dans **Settings** (Paramètres).
2. Dans le menu de gauche, cliquez sur **Pages**.
3. Dans **Build and deployment** > **Branch** :
   - Sélectionnez la branche `main` (ou `master`).
   - Sélectionnez le dossier **/docs**.
4. Cliquez sur **Save**.
5. En 30 secondes, votre site web gratuit est en ligne avec HTTPS sécurisé :
   `https://<votre-nom>.github.io/autoconnect-mali/`

---

## 📱 4. QR Code & Partage Direct de Téléphone à Téléphone

Vous disposez de 3 façons de faire scanner le QR Code :

1. **Directement depuis l'application Android** :
   - Ouvrez le menu latéral (Drawer) d'AutoConnect Mali.
   - Touchez **« Partager & Mises à jour »**.
   - Présentez l'écran de votre téléphone : le client ou le garagiste scanne le QR code avec l'appareil photo de son téléphone et le téléchargement démarre immédiatement !
2. **Depuis la page Web de téléchargement** (`docs/index.html`).
3. **Partage ultra-rapide hors-connexion (Mali)** :
   - Vous pouvez également envoyer l'APK directement via **WhatsApp**, **Xender** ou **Bluetooth** sans consommer de forfait internet.

---

## 📲 5. Guide d'Installation sur les Téléphones Android (Pour vos Utilisateurs)

Puisque l'application est distribuée directement en APK gratuit :

1. **Étape 1 (Téléchargement)** :
   - L'utilisateur clique sur le lien ou scanne le QR code.
   - Si le navigateur (Chrome) affiche : *« Ce type de fichier peut être nocif »*, appuyez sur **« Télécharger quand même »**.
2. **Étape 2 (Autorisation des sources inconnues)** :
   - Ouvrez le fichier téléchargé.
   - Si Android demande : *« Pour votre sécurité, votre téléphone n'est pas autorisé à installer des applications inconnues depuis cette source »*, appuyez sur **Paramètres** puis activez **« Autoriser cette source »**.
3. **Étape 3 (Installation & Play Protect)** :
   - Appuyez sur **« Installer »**.
   - Si Google Play Protect affiche un écran d'avertissement, touchez **« Plus de détails »** puis **« Installer quand même »**.

---

## 🔄 6. Comment Gérer les Mises à Jour (Releases Futures)

Quand vous ajoutez une nouvelle fonctionnalité (ex: v1.1.0) :
1. Modifiez simplement le `versionCode` et `versionName` dans `app/build.gradle.kts`.
2. Recompilez avec `gradle assembleRelease`.
3. Publiez une nouvelle release sur GitHub (`v1.1.0`) avec le nouvel APK.
4. Les utilisateurs qui cliquent sur le bouton **« Vérifier les mises à jour »** dans l'application téléchargeront directement la nouvelle version qui s'installera par-dessus l'ancienne en conservant toutes leurs données !
