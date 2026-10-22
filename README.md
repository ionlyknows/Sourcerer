# Sourcerer: Crusade

A Java Swing Python learning game. The original seven authored campaign stages,
standalone lessons, guided tutorial, syntax dictionary, tile and typed-answer
challenges, combat, stars, and battle screens remain in the game. The campaign
now has 100 stages, including a harder Python challenge at each tenth stage.

## Build and run

Requires JDK 21 or later and Maven.

```powershell
mvn clean test
mvn package
mvn exec:java
```

Run with no database settings to keep local progress in
`sourcerer_progress.properties` in the launch directory. To use MySQL
persistence, first follow [the Workbench setup guide](docs/database-setup.md),
then set `SOURCERER_DB_URL`, `SOURCERER_DB_USER`, and `SOURCERER_DB_PASSWORD`
before running `mvn exec:java`. Database mode uses one profile named
`Sourcerer`; connection or schema errors are reported and do not silently
replace saved data.

## Gameplay

- Complete a stage to unlock the next stage and record up to three stars.
- Ordinary first clears award 25 coins; boss first clears award 60 coins.
- A first clear also grants one Healing Potion. Replays do not grant first-clear
  coins or potions again.
- Every tenth stage is a boss with an additional multi-concept Python puzzle.
- The Potion Store is available from the world map. Healing costs 25 coins and
  restores up to 30 HP; Mana costs 20 coins and restores up to 25 MP.
- Character level, XP, coins, inventory, campaign stars, boss defeats, and the
  guided tutorial flag persist locally or in the configured database.

XP follows the existing level-up rule: the first level requires 50 XP; each
later level requires 25 more XP than the previous threshold. World stage and
character level are separate values.

## Project layout

Java sources use Maven's `src/main/java/sourcerer` layout. Tests are in
`src/test/java`. SQL setup and verification scripts are in `sql/`; project
guides and verification notes are in `docs/`.

The MySQL scripts are manual setup steps. The application never executes schema
creation or reset SQL automatically. See [database setup](docs/database-setup.md)
and [gameplay and manual checks](docs/gameplay-guide.md).
