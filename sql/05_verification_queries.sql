USE sourcerer_crusade;

-- Player account and character statistics.
SELECT player_id, player_name, xp, character_level, coins, bosses_defeated, tutorial_done
FROM players
ORDER BY player_id;

-- Inventory by player (Potion enum codes currently used by the project: HEALING, MANA).
SELECT p.player_name, i.potion_code, i.quantity
FROM inventory AS i
JOIN players AS p ON p.player_id = i.player_id
ORDER BY p.player_name, i.potion_code;

-- Stage definitions and boss markers (expect 100 rows and 10 bosses).
SELECT stage_type, COUNT(*) AS stage_count
FROM stages
GROUP BY stage_type
ORDER BY stage_type;
SELECT stage_id, stage_name, stage_type FROM stages ORDER BY stage_id;

-- Per-player unlocked/completed progress.
SELECT p.player_name, pp.stage_id, pp.unlocked, pp.completed, pp.stars, pp.completed_at
FROM player_progress AS pp
JOIN players AS p ON p.player_id = pp.player_id
ORDER BY p.player_name, pp.stage_id;

-- Individual boss defeats and totals recorded for each player.
SELECT p.player_name, bd.stage_id, bd.defeated_at
FROM boss_defeats AS bd
JOIN players AS p ON p.player_id = bd.player_id
ORDER BY p.player_name, bd.stage_id;
SELECT p.player_name, p.bosses_defeated, COUNT(bd.stage_id) AS recorded_boss_defeats
FROM players AS p
LEFT JOIN boss_defeats AS bd ON bd.player_id = p.player_id
GROUP BY p.player_id, p.player_name, p.bosses_defeated
ORDER BY p.player_name;

-- Idempotency/reward/purchase audit history.
SELECT p.player_name, oh.operation_id, oh.operation_type, oh.reference_id, oh.created_at, oh.details
FROM operation_history AS oh
JOIN players AS p ON p.player_id = oh.player_id
ORDER BY oh.created_at DESC;
