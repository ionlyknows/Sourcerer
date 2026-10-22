-- Creates the application schema only when it does not already exist.
-- Existing schemas and player data are preserved.
CREATE DATABASE IF NOT EXISTS sourcerer_crusade
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE sourcerer_crusade;
