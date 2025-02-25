CREATE DATABASE IF NOT EXISTS my_database;
use my_database;

-- manage creating new customer
DROP USER if exists 'springstudent'@'%' ;
CREATE USER 'springstudent'@'%' IDENTIFIED BY 'springstudent';
GRANT ALL PRIVILEGES ON * . * TO 'springstudent'@'%';

-- things from new chart
CREATE TABLE `Customers`(
                            `id` INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                            `email` VARCHAR(255) NOT NULL,
                            `password_hash` VARCHAR(255) NOT NULL,
                            `username` VARCHAR(255) NOT NULL,
                            `account_expired` BOOLEAN NOT NULL DEFAULT '0',
                            `account_locked` BOOLEAN NOT NULL DEFAULT '0',
                            `credentials_expired` BOOLEAN NOT NULL DEFAULT '0',
                            `enabled` BOOLEAN NOT NULL DEFAULT '1',
                            `profile_creation_date` DATETIME NOT NULL,
                            `profile_picture_path` VARCHAR(255) NULL DEFAULT '/static/profile_pictures/default.jpg',
                            `root_folder_id` INT UNSIGNED NOT NULL,
                            `bio` VARCHAR(255) NULL DEFAULT 'Hi! This is my bio, I am using Flashcards app!'
);
ALTER TABLE
    `Customers` ADD UNIQUE `customers_email_unique`(`email`);
CREATE TABLE `Flashcards`(
                             `id` INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                             `deck_id` INT UNSIGNED NOT NULL,
                             `front` VARCHAR(255) NOT NULL,
                             `back` VARCHAR(255) NOT NULL
);
CREATE TABLE `Folders`(
                          `id` INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                          `name` VARCHAR(255) NOT NULL
);
CREATE TABLE `Decks`(
                        `id` INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                        `name` VARCHAR(255) NOT NULL
);
CREATE TABLE `Flashcards_Progresses`(
                                        `id` INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                        `flashcard_id` INT UNSIGNED NOT NULL,
                                        `user_id` INT UNSIGNED NOT NULL,
                                        `next_review` DATETIME NOT NULL,
                                        `last_review_id` INT UNSIGNED NOT NULL
);
CREATE TABLE `User_Preferences`(
                                   `id` INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                   `user_id` INT UNSIGNED NOT NULL,
                                   `dark_mode` BOOLEAN NOT NULL DEFAULT '1',
                                   `language` INT NOT NULL,
                                   `reminder_time` TIME NOT NULL,
                                   `timezone` INT NOT NULL,
                                   `study_reminders` INT NOT NULL
);
CREATE TABLE `User_Statistics`(
                                  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                  `user_id` INT UNSIGNED NOT NULL,
                                  `total_time_spent` INT NOT NULL DEFAULT '0',
                                  `login_count` INT NOT NULL DEFAULT '0',
                                  `last_login` DATETIME NOT NULL,
                                   `total_days_learning` INT NOT NULL DEFAULT '0',
                                   `days_learning_streak` INT NOT NULL DEFAULT '0',
                                    `longest_learning_streak` INT NOT NULL DEFAULT '0'

);
CREATE TABLE `Review_Logs`(
                              `id` INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                              `flashcard_id` INT UNSIGNED NOT NULL,
                              `user_id` INT UNSIGNED NOT NULL,
                              `when` DATETIME,
                              `user_answer` TINYINT
);
CREATE TABLE `Folders_Decks`(
                                `id` INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                `folder_id` INT UNSIGNED NOT NULL,
                                `deck_id` INT UNSIGNED NOT NULL
);
CREATE TABLE `Friendships`(
                              `id` INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                              `sender_id` INT UNSIGNED NOT NULL,
                              `receiver_id` INT UNSIGNED NOT NULL,
                              `accepted` BOOLEAN NOT NULL DEFAULT '0'
);
CREATE TABLE `Notifications`(
                                `id` INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                `user_id` INT UNSIGNED NOT NULL,
                                `received` BOOLEAN NOT NULL DEFAULT '0',
                                `text` VARCHAR(255) NOT NULL,
                                `creation_date` DATETIME NOT NULL,
                                `received_date` DATETIME NOT NULL
);
CREATE TABLE `Friendships_Notifications`(
                                            `id` INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                            `friendship_id` INT UNSIGNED NOT NULL,
                                            `notification_id` INT UNSIGNED NOT NULL
);
CREATE TABLE `Folder_Parent`(
                                `id` INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                `parent_folder_id` INT UNSIGNED NOT NULL,
                                `child_folder_id` INT UNSIGNED NOT NULL
);
CREATE TABLE `Folder_Access_Level`(
                                      `id` INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                      `customer_id` INT UNSIGNED NOT NULL,
                                      `access_level` TINYINT NOT NULL
);
CREATE TABLE `Access_Levels_Folders`(
                                        `id` INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                        `access_level_id` INT UNSIGNED NOT NULL,
                                        `folder_id` INT UNSIGNED NOT NULL
);

CREATE TABLE `Sent_Verification_Codes`(
                                        `id` INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                        `customer_id` INT UNSIGNED NOT NULL,
                                        `expiration_date` DATETIME NOT NULL,
                                        `code` VARCHAR(255) NOT NULL
);

ALTER TABLE
    `Sent_Verification_Codes` ADD CONSTRAINT `sent_verification_tokens_user_id_foreign` FOREIGN KEY(`customer_id`) REFERENCES `Customers`(`id`);

ALTER TABLE
    `User_Preferences` ADD CONSTRAINT `user_preferences_user_id_foreign` FOREIGN KEY(`user_id`) REFERENCES `Customers`(`id`);
ALTER TABLE
    `Customers` ADD CONSTRAINT `customers_root_folder_id_foreign` FOREIGN KEY(`root_folder_id`) REFERENCES `Folders`(`id`);
ALTER TABLE
    `Review_Logs` ADD CONSTRAINT `review_logs_flashcard_id_foreign` FOREIGN KEY(`flashcard_id`) REFERENCES `Flashcards`(`id`);
ALTER TABLE
    `Folder_Parent` ADD CONSTRAINT `folder_parent_child_folder_id_foreign` FOREIGN KEY(`child_folder_id`) REFERENCES `Folders`(`id`);
ALTER TABLE
    `Flashcards` ADD CONSTRAINT `flashcards_deck_id_foreign` FOREIGN KEY(`deck_id`) REFERENCES `Decks`(`id`);
ALTER TABLE
    `Notifications` ADD CONSTRAINT `notifications_user_id_foreign` FOREIGN KEY(`user_id`) REFERENCES `Customers`(`id`);
ALTER TABLE
    `Access_Levels_Folders` ADD CONSTRAINT `access_levels_folders_folder_id_foreign` FOREIGN KEY(`folder_id`) REFERENCES `Folders`(`id`);
ALTER TABLE
    `User_Statistics` ADD CONSTRAINT `user_statistics_user_id_foreign` FOREIGN KEY(`user_id`) REFERENCES `Customers`(`id`);
ALTER TABLE
    `Folders_Decks` ADD CONSTRAINT `folders_decks_deck_id_foreign` FOREIGN KEY(`deck_id`) REFERENCES `Decks`(`id`);
ALTER TABLE
    `Friendships` ADD CONSTRAINT `friendships_receiver_id_foreign` FOREIGN KEY(`receiver_id`) REFERENCES `Customers`(`id`);
ALTER TABLE
    `Folder_Access_Level` ADD CONSTRAINT `folder_access_level_customer_id_foreign` FOREIGN KEY(`customer_id`) REFERENCES `Customers`(`id`);
ALTER TABLE
    `Flashcards_Progresses` ADD CONSTRAINT `flashcards_progresses_flashcard_id_foreign` FOREIGN KEY(`flashcard_id`) REFERENCES `Flashcards`(`id`);
ALTER TABLE
    `Friendships_Notifications` ADD CONSTRAINT `friendships_notifications_friendship_id_foreign` FOREIGN KEY(`friendship_id`) REFERENCES `Friendships`(`id`);
ALTER TABLE
    `Folder_Parent` ADD CONSTRAINT `folder_parent_parent_folder_id_foreign` FOREIGN KEY(`parent_folder_id`) REFERENCES `Folders`(`id`);
ALTER TABLE
    `Friendships` ADD CONSTRAINT `friendships_sender_id_foreign` FOREIGN KEY(`sender_id`) REFERENCES `Customers`(`id`);
ALTER TABLE
    `Friendships_Notifications` ADD CONSTRAINT `friendships_notifications_notification_id_foreign` FOREIGN KEY(`notification_id`) REFERENCES `Notifications`(`id`);
ALTER TABLE
    `Folders_Decks` ADD CONSTRAINT `folders_decks_folder_id_foreign` FOREIGN KEY(`folder_id`) REFERENCES `Folders`(`id`);
ALTER TABLE
    `Access_Levels_Folders` ADD CONSTRAINT `access_levels_folders_access_level_id_foreign` FOREIGN KEY(`access_level_id`) REFERENCES `Folder_Access_Level`(`id`);
ALTER TABLE
    `Review_Logs` ADD CONSTRAINT `review_logs_user_id_foreign` FOREIGN KEY(`user_id`) REFERENCES `Customers`(`id`);
ALTER TABLE
    `Flashcards_Progresses` ADD CONSTRAINT `flashcards_progresses_user_id_foreign` FOREIGN KEY(`user_id`) REFERENCES `Customers`(`id`);
ALTER TABLE
    `Flashcards_Progresses` ADD CONSTRAINT `flashcards_progresses_last_review_id_foreign` FOREIGN KEY(`last_review_id`) REFERENCES `Review_Logs`(`id`);


-- things from old chart
CREATE TABLE `Authorities`(
                              `id` INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                              `name` VARCHAR(255) NOT NULL
);
CREATE TABLE `Authorities_Customers`(
                                        `id` INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                        `authority_id` INT UNSIGNED NOT NULL,
                                        `customer_id` INT UNSIGNED NOT NULL
);
ALTER TABLE
    `Authorities_Customers` ADD CONSTRAINT `authorities_customers_customer_id_foreign` FOREIGN KEY(`customer_id`) REFERENCES `Customers`(`id`);

ALTER TABLE
    `Authorities_Customers` ADD CONSTRAINT `authorities_customers_authority_id_foreign` FOREIGN KEY(`authority_id`) REFERENCES `Authorities`(`id`);

-- triggers
# DELIMITER //
#
# CREATE TRIGGER before_insert_disable_fk
#     BEFORE INSERT ON Folders
#     FOR EACH ROW
# BEGIN
#     SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS;
#     SET FOREIGN_KEY_CHECKS = 0;
# END //
#
# CREATE TRIGGER after_insert_enable_fk
#     AFTER INSERT ON Folders
#     FOR EACH ROW
# BEGIN
#
#     SET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS;
#     IF NEW.access_specyfing_folder_id IS NULL THEN
#         SET NEW.access_specyfing_folder_id = NEW.id;
#     END IF;
# END //

# CREATE TRIGGER before_insert_folder
#     AFTER INSERT ON Folders
#     FOR EACH ROW
# BEGIN
#     IF NEW.access_specyfing_folder_id IS NULL THEN
#         SET NEW.access_specyfing_folder_id = NEW.id;
#     END IF;
# END //

# DELIMITER ;