-- DESTRUCTIVE: removes the exact disposable account named below and all rows
-- linked to it through ON DELETE CASCADE. It does not drop tables or schema.
-- Change nothing here unless you have verified that this test account contains
-- no data you need. Never run this against a real player's account.
USE sourcerer_crusade;

START TRANSACTION;
DELETE FROM players WHERE player_name = 'Workbench Test Player';
COMMIT;
