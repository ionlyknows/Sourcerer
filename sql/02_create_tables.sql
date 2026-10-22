-- Run after 01_create_database.sql. CREATE TABLE IF NOT EXISTS preserves existing data.
USE sourcerer_crusade;

CREATE TABLE IF NOT EXISTS players (
    player_id BIGINT NOT NULL AUTO_INCREMENT,
    player_name VARCHAR(80) NOT NULL,
    xp BIGINT NOT NULL DEFAULT 0,
    character_level INT NOT NULL DEFAULT 1,
    coins BIGINT NOT NULL DEFAULT 0,
    bosses_defeated INT NOT NULL DEFAULT 0,
    tutorial_done BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (player_id),
    UNIQUE KEY uq_players_name (player_name),
    CONSTRAINT chk_players_xp CHECK (xp >= 0),
    CONSTRAINT chk_players_level CHECK (character_level >= 1),
    CONSTRAINT chk_players_coins CHECK (coins >= 0),
    CONSTRAINT chk_players_bosses CHECK (bosses_defeated >= 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS stages (
    stage_id INT NOT NULL,
    stage_name VARCHAR(120) NOT NULL,
    stage_type ENUM('STANDARD', 'BOSS') NOT NULL DEFAULT 'STANDARD',
    description VARCHAR(500) NOT NULL DEFAULT '',
    PRIMARY KEY (stage_id),
    UNIQUE KEY uq_stages_id_type (stage_id, stage_type),
    CONSTRAINT chk_stages_id CHECK (stage_id BETWEEN 1 AND 100),
    CONSTRAINT chk_stages_boss_position CHECK (
        (stage_type = 'BOSS' AND MOD(stage_id, 10) = 0)
        OR (stage_type = 'STANDARD' AND MOD(stage_id, 10) <> 0)
    )
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS player_progress (
    player_id BIGINT NOT NULL,
    stage_id INT NOT NULL,
    unlocked BOOLEAN NOT NULL DEFAULT FALSE,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    stars TINYINT UNSIGNED NOT NULL DEFAULT 0,
    completed_at TIMESTAMP NULL DEFAULT NULL,
    PRIMARY KEY (player_id, stage_id),
    KEY ix_player_progress_stage (stage_id),
    CONSTRAINT fk_progress_player FOREIGN KEY (player_id) REFERENCES players (player_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_progress_stage FOREIGN KEY (stage_id) REFERENCES stages (stage_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_progress_stars CHECK (stars BETWEEN 0 AND 3),
    CONSTRAINT chk_progress_completion CHECK (completed OR stars = 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS inventory (
    player_id BIGINT NOT NULL,
    potion_code VARCHAR(40) NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    PRIMARY KEY (player_id, potion_code),
    CONSTRAINT fk_inventory_player FOREIGN KEY (player_id) REFERENCES players (player_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_inventory_quantity CHECK (quantity >= 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS boss_defeats (
    player_id BIGINT NOT NULL,
    stage_id INT NOT NULL,
    stage_type ENUM('STANDARD', 'BOSS') NOT NULL DEFAULT 'BOSS',
    defeated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (player_id, stage_id),
    KEY ix_boss_defeats_stage (stage_id),
    CONSTRAINT fk_boss_defeat_player FOREIGN KEY (player_id) REFERENCES players (player_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_boss_defeat_stage FOREIGN KEY (stage_id, stage_type)
        REFERENCES stages (stage_id, stage_type) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_boss_defeat_type CHECK (stage_type = 'BOSS')
) ENGINE=InnoDB;

-- A caller generates one stable operation_id per logical purchase/reward/settlement.
-- Inserting that ID and applying associated updates must happen in one transaction.
CREATE TABLE IF NOT EXISTS operation_history (
    operation_id VARCHAR(64) NOT NULL,
    player_id BIGINT NOT NULL,
    operation_type ENUM('STAGE_REWARD', 'POTION_PURCHASE', 'POTION_USE', 'BOSS_DEFEAT') NOT NULL,
    reference_id VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    details JSON NULL,
    PRIMARY KEY (operation_id),
    KEY ix_operation_player_time (player_id, created_at),
    KEY ix_operation_type_reference (operation_type, reference_id),
    CONSTRAINT fk_operation_player FOREIGN KEY (player_id) REFERENCES players (player_id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;
