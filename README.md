# Liquid War - Projet CPOO5

Un jeu de stratégie en temps réel inspiré du jeu classique "Liquid War", développé en Java avec JavaFX.

## Description du jeu

Liquid War est un jeu où deux équipes de particules s'affrontent. Chaque joueur contrôle un curseur qui attire ses particules. Lorsque des particules de différentes équipes se rencontrent, elles combattent et la particule avec le plus d'énergie convertit l'autre à son équipe. L'objectif est de convertir toutes les particules adverses.

### Modes de jeu

- **Joueur vs IA** : Affrontez une intelligence artificielle avec 3 niveaux de difficulté (Facile, Moyen, Difficile)
- **Multijoueur Local** : Deux joueurs sur le même clavier

### Contrôles

| Joueur | Contrôle |
|--------|----------|
| Joueur 1 (Rouge) | Souris |
| Joueur 2 (Bleu) | Flèches directionnelles |
| Pause | Échap |

---

## Architecture du projet

Le projet suit une architecture **MVC (Model-View-Controller)** adaptée avec une séparation claire des responsabilités.

```
src/main/java/fr/uparis/liquidwar/
├── Main.java                 # Point d'entrée de l'application
├── algorithm/                # Logique de jeu et IA (Controller)
│   ├── AIController.java
│   ├── GradientCalculator.java
│   └── ParticleMovement.java
├── model/                    # Données et état du jeu (Model)
│   ├── Board.java
│   ├── Gradient.java
│   ├── Particle.java
│   ├── Position.java
│   └── Team.java
├── view/                     # Interface graphique (View)
│   ├── GamePanel.java
│   ├── MainWindow.java
│   └── MenuWindow.java
└── util/                     # Utilitaires
    └── Direction.java
```

### Model (`model/`)

Contient les classes représentant l'état du jeu :

| Classe | Description |
|--------|-------------|
| `Board` | Plateau de jeu (grille 2D avec obstacles) |
| `Particle` | Unité de jeu avec position, énergie et équipe |
| `Team` | Équipe avec couleur, particules et position du curseur |
| `Position` | Position (x, y) immuable avec méthodes utilitaires |
| `Gradient` | Carte des distances pour le pathfinding |

### View (`view/`)

Gère l'affichage et les interactions utilisateur :

| Classe | Description |
|--------|-------------|
| `MenuWindow` | Menu principal (choix du mode et difficulté) |
| `MainWindow` | Fenêtre de jeu avec boucle de rendu et gestion des entrées |
| `GamePanel` | Canvas de rendu du plateau et des particules |

### Algorithm (`algorithm/`)

Contient la logique de jeu et l'intelligence artificielle :

| Classe | Description |
|--------|-------------|
| `GradientCalculator` | Calcul BFS des distances vers le curseur |
| `ParticleMovement` | Déplacement des particules selon le gradient |
| `AIController` | Intelligence artificielle pour l'équipe adverse |

---

## Algorithmes clés

### Calcul du Gradient (BFS)

Les particules se déplacent vers leur curseur en suivant un **champ de gradient**. Ce gradient est calculé par un algorithme **BFS (Breadth-First Search)** :

1. Le curseur est la source avec distance 0
2. Propagation aux cellules voisines avec coût 10 (cardinal) ou 14 (diagonal)
3. Les obstacles sont marqués comme infranchissables
4. Chaque particule suit la direction de gradient décroissant

### Intelligence Artificielle

L'IA adapte sa stratégie selon le niveau de difficulté :

| Difficulté | Stratégie | Fréquence de mise à jour |
|------------|-----------|--------------------------|
| **Facile** | Défensive (reste près de ses particules) | ~0.5s |
| **Moyen** | Équilibrée (adapte selon l'avantage) | ~0.25s |
| **Difficile** | Agressive (cible les ennemis, chasse les isolés) | ~0.08s |

---

## Commandes Gradle

```bash
# Lancer le jeu
./gradlew run

# Exécuter les tests
./gradlew test

# Compiler le projet
./gradlew build
```

---

## Tests

Les tests unitaires couvrent :
- Les modèles (`Board`, `Particle`, `Team`, `Position`, `Gradient`)
- Les algorithmes (`GradientCalculator`, `ParticleMovement`, `AIController`)
- Les utilitaires (`Direction`)

Rapport de tests : `build/reports/tests/test/index.html`

---

## Technologies utilisées

- **Java 21**
- **JavaFX** - Interface graphique
- **JUnit 5** - Tests unitaires
- **Gradle** - Build system

---
