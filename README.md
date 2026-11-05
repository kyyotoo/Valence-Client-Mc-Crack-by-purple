# Valence Client — Version Crack

> Client Minecraft Fabric axé sur le visuel : HUD personnalisé, ciel dynamique, paroles synchronisées, styles et performances.

⚠️ **Ce dépôt est une version modifiée (crack) du client Valence.** La vérification de licence a été retirée : aucune clé n'est nécessaire pour utiliser le client. Voir la section [Avertissement](#avertissement).

---

## Sommaire

- [Fonctionnalités](#fonctionnalités)
- [Prérequis](#prérequis)
- [Installation](#installation)
- [Compilation depuis les sources](#compilation-depuis-les-sources)
- [Utilisation](#utilisation)
- [Raccourcis clavier](#raccourcis-clavier)
- [Structure du projet](#structure-du-projet)
- [Avertissement](#avertissement)
- [Crédits](#crédits)

---

## Fonctionnalités

### Interface & HUD
- HUD personnalisé (barre de vie, hotbar en verre, île d'informations)
- Crosshair custom
- Thème glassmorphism avec animations fluides
- Écran de menu complet (overview, HUD, lyrics, sky, performance, settings, styles)

### Ciel dynamique
- Plusieurs presets de ciel (classic, bright skies, night skies, space skies)
- Aurora, nébuleuses, étoiles, nuages, cristaux, anneaux de Saturne
- Rendu temps réel avec effets de glow

### Paroles & musique
- Intégration Spotify (contrôle play/pause/next/prev)
- Paroles synchronisées (lyrics provider, NetEase, local media)
- Traduction des paroles
- Favoris
- Affichage des paroles dans le ciel (SkyLyrics) et à l'écran

### Styles & personnalisation
- Gestionnaire de styles (StyleManager)
- Plusieurs catégories de styles et effets
- Packs de styles (StylePacks)
- Polices personnalisées (Poppins, Bebas Neue, Orbitron, etc.)

### Performances
- Optimisations FPS (FpsBoost, AutoPerf, Occlusion)
- Modes de rendu allégés

### Cosmétiques
- Accessoires, Death Cards, Death FX

---

## Prérequis

- **Minecraft** : version compatible avec le mod (voir `fabric.mod.json`)
- **Fabric Loader** : version correspondante
- **Fabric API** : requis
- **Java** : JDK 21 (ou selon la version de Minecraft ciblée)

Pour compiler depuis les sources :
- **JDK 17+** (21 recommandé)
- **Gradle** (fourni via le wrapper `gradlew`)

---

## Installation

### Méthode 1 : Jar précompilé

1. Téléchargez le fichier `valence-<version>.jar` depuis la section **Releases** (ou `build/libs/` si vous avez compilé vous-même).
2. Placez-le dans le dossier `mods/` de votre installation Minecraft Fabric.
3. Lancez Minecraft avec le profil Fabric.

⚠️ Ne prenez **pas** le fichier `-sources.jar` ni `-dev.jar`.

### Méthode 2 : Compiler depuis les sources

Voir la section suivante.

---

## Compilation depuis les sources

### 1. Cloner le dépôt

```bash
git clone https://github.com/kyyotoo/Valence-Client-Mc-Crack-by-purple.git
cd Valence-Client-Mc-Crack-by-purple
