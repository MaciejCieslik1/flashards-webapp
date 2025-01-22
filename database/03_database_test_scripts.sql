# =============================================================================
# CREATE SEQUENCE customer_seq
#     START WITH 1
#     INCREMENT BY 1
#     MINVALUE 1
#     MAXVALUE 5000
#     CYCLE
#     CACHE 10;
#
# CREATE SEQUENCE folder_seq
#     START WITH 1
#     INCREMENT BY 1
#     MINVALUE 1
#     MAXVALUE 9999
#     CYCLE
#     CACHE 10;
#
# CREATE SEQUENCE authority_seq
#     START WITH 1
#     INCREMENT BY 1
#     MINVALUE 1
#     MAXVALUE 999
#     CYCLE
#     CACHE 10;
#
# CREATE SEQUENCE folder_access_level_seq
#     START WITH 1
#     INCREMENT BY 1
#     MINVALUE 1
#     MAXVALUE 9999
#     CYCLE
#     CACHE 20;
#
# CREATE SEQUENCE friendship_seq
#     START WITH 1
#     INCREMENT BY 1
#     MINVALUE 1
#     MAXVALUE 999
#     CYCLE
#     CACHE 10;
#
# CREATE SEQUENCE sent_verification_code_seq
#     START WITH 1
#     INCREMENT BY 1
#     MINVALUE 1
#     MAXVALUE 9999
#     CYCLE
#     CACHE 20;
#
# CREATE SEQUENCE folder_parent_seq
#     START WITH 1
#     INCREMENT BY 1
#     MINVALUE 1
#     MAXVALUE 9999
#     CYCLE
#     CACHE 20;
#
# CREATE SEQUENCE authorities_customers_seq
#     START WITH 1
#     INCREMENT BY 1
#     MINVALUE 1
#     MAXVALUE 9999
#     CYCLE
#     CACHE 20;
#
# CREATE SEQUENCE access_level_folder_seq
#     START WITH 1
#     INCREMENT BY 1
#     MINVALUE 1
#     MAXVALUE 999
#     CYCLE
#     CACHE 10;
#
# CREATE SEQUENCE friendship_notifications_seq
#     START WITH 1
#     INCREMENT BY 1
#     MINVALUE 1
#     MAXVALUE 999
#     CYCLE
#     CACHE 10;
#
# CREATE SEQUENCE notifications_seq
#     START WITH 1
#     INCREMENT BY 1
#     MINVALUE 1
#     MAXVALUE 9999
#     CYCLE
#     CACHE 20;
#
# CREATE SEQUENCE user_statistics_seq
#     START WITH 1
#     INCREMENT BY 1
#     MINVALUE 1
#     MAXVALUE 999
#     CYCLE
#     CACHE 10;
#
# CREATE SEQUENCE flashcard_progress_seq
#     START WITH 1
#     INCREMENT BY 1
#     MINVALUE 1
#     MAXVALUE 9999
#     CYCLE
#     CACHE 20;
#
# CREATE SEQUENCE user_preferences_seq
#     START WITH 1
#     INCREMENT BY 1
#     MINVALUE 1
#     MAXVALUE 999
#     CYCLE
#     CACHE 10;
#
#
# CREATE SEQUENCE review_logs_seq
#     START WITH 1
#     INCREMENT BY 1
#     MINVALUE 1
#     MAXVALUE 9999
#     CYCLE
#     CACHE 20;
#
# CREATE SEQUENCE folders_decks_seq
#     START WITH 1
#     INCREMENT BY 1
#     MINVALUE 1
#     MAXVALUE 9999
#     CYCLE
#     CACHE 20;
#
# CREATE SEQUENCE flashcard_seq
#     START WITH 1
#     INCREMENT BY 1
#     MINVALUE 1
#     MAXVALUE 99999
#     CYCLE
#     CACHE 30;
#
# CREATE SEQUENCE decks_seq
#     START WITH 1
#     INCREMENT BY 1
#     MINVALUE 1
#     MAXVALUE 9999
#     CYCLE
#     CACHE 20;

# =============================================================================

DELIMITER //

-- TRIGGERS
DROP TRIGGER IF EXISTS after_insert_customer_statistics;

CREATE TRIGGER after_insert_customer_statistics
    AFTER INSERT ON Customers
    FOR EACH ROW
BEGIN
    INSERT INTO User_Statistics (user_id, last_login)
    VALUES (NEW.id, NOW());
END;

DROP TRIGGER IF EXISTS before_insert_notification;

CREATE TRIGGER before_insert_notification
    BEFORE INSERT ON Notifications
    FOR EACH ROW
BEGIN
    SET NEW.creation_date = NOW();
END;

DROP TRIGGER IF EXISTS after_insert_friendship_notification;

CREATE TRIGGER after_insert_friendship_notification
    AFTER INSERT ON Friendships
    FOR EACH ROW
BEGIN
    IF NEW.accepted = 0 THEN
        INSERT INTO Notifications (user_id, text, creation_date, received_date)
        VALUES (NEW.receiver_id, CONCAT('You have a new friend request from user ID: ', NEW.sender_id), NOW(), NOW());
    END IF;
END;


# =============================================================================
DROP PROCEDURE IF EXISTS count_current_learning;

CREATE PROCEDURE count_current_learning(
    IN userId INT,
    IN deckId INT,
    IN revGapMinutes INT,
    IN lastReviewConstant INT,
    OUT learningCount INT
)
BEGIN
    SELECT COUNT(*)
    INTO learningCount
    FROM Flashcards fl
             JOIN Decks d ON fl.deck_id = d.id
             JOIN Flashcards_Progresses fp on fl.id = fp.flashcard_id
             JOIN Review_Logs rl on rl.id = fp.last_review_id

    WHERE d.id = deckId AND
        fp.user_id = userId AND
        TIMESTAMPDIFF(MINUTE,fp.next_review,  rl.when ) <= revGapMinutes AND # time between last review and next review is less than gap
        TIMESTAMPDIFF(MINUTE, fp.next_review, NOW()) <= lastReviewConstant AND # next_review scheduled around now timestampt
        TIMESTAMPDIFF(MINUTE, fp.next_review, NOW()) >= -lastReviewConstant;
END //

# =============================================================================
DROP PROCEDURE IF EXISTS count_due_in_learning;

CREATE PROCEDURE count_due_in_learning(
    IN userId INT,
    IN deckId INT,
    IN revGapMinutes INT,
    IN lastReviewConstant INT,
    OUT learningCount INT
)
BEGIN
    SELECT COUNT(*)
    INTO learningCount
    FROM Flashcards fl
             JOIN Decks d ON fl.deck_id = d.id
             JOIN Flashcards_Progresses fp on fl.id = fp.flashcard_id
             JOIN Review_Logs rl on rl.id = fp.last_review_id

    WHERE d.id = deckId AND
        fp.user_id = userId AND
      # od pierwszego odejmuje drugi
        TIMESTAMPDIFF(MINUTE,fp.next_review,  rl.when ) <= revGapMinutes AND # time between last review and next review is less than gap
        TIMESTAMPDIFF(MINUTE, fp.next_review, NOW()) >= -lastReviewConstant AND
        next_review <= NOW(); # that makes a flashcard due

    # time between  now and ne

END //

# =============================================================================
DROP PROCEDURE IF EXISTS count_due_to_review;

# cards that are due but are not in learning
CREATE PROCEDURE count_due_to_review(
    IN userId INT,
    IN deckId INT,
    IN revGapMinutes INT,
    IN lastReviewConstant INT,
    OUT learningCount INT
)
BEGIN
    SELECT COUNT(*)
    INTO learningCount
    FROM Flashcards fl
             JOIN Decks d ON fl.deck_id = d.id
             JOIN Flashcards_Progresses fp on fl.id = fp.flashcard_id
             JOIN Review_Logs rl on rl.id = fp.last_review_id

    WHERE d.id = deckId AND
        fp.user_id = userId AND
      # simply negate contition that makes flashcard in learning
        (not (TIMESTAMPDIFF(MINUTE,fp.next_review,  rl.when ) <= revGapMinutes AND
              TIMESTAMPDIFF(MINUTE, fp.next_review, NOW()) >= -lastReviewConstant)) AND
        next_review <= NOW();

    # time between  now and ne

END //


# =============================================================================
DROP PROCEDURE IF EXISTS get_due_flashcards;

CREATE PROCEDURE get_due_flashcards(
    IN userId INT,
    IN deckId INT,
    IN howMany INT

)
BEGIN

    SELECT fl.*
    FROM Flashcards fl
             JOIN Decks d ON fl.deck_id = d.id
             JOIN Flashcards_Progresses fp on fl.id = fp.flashcard_id
             JOIN Review_Logs rl on rl.id = fp.last_review_id

    WHERE d.id = deckId AND
        fp.user_id = userId AND
        next_review <= NOW()

    LIMIT howMany;
    # time between  now and ne

END //

# =============================================================================
DROP PROCEDURE IF EXISTS get_new_flashcard;

CREATE PROCEDURE get_new_flashcard(
    IN userId INT,
    IN deckId INT,
    IN howMany INT
)
BEGIN
    SELECT DISTINCT fl.* FROM Flashcards fl
                                  LEFT JOIN Review_Logs rl ON fl.id = rl.flashcard_id
    WHERE deck_id = deckId AND
        (rl.user_id IS NULL or rl.user_id != userId)
    LIMIT howMany;
END //

# =============================================================================
DROP PROCEDURE IF EXISTS get_early_review;

CREATE PROCEDURE get_early_review(
    IN userId INT,
    IN deckId INT,
    IN howMany INT,
    IN revGapMinutes INT,
    IN lastReviewConstant INT
)
BEGIN


    SELECT fl.*
    FROM Flashcards fl
             JOIN Decks d ON fl.deck_id = d.id
             JOIN Flashcards_Progresses fp on fl.id = fp.flashcard_id
             JOIN Review_Logs rl on rl.id = fp.last_review_id

    WHERE d.id = deckId AND
        fp.user_id = userId AND
        next_review > NOW() AND # card is not due
        TIMESTAMPDIFF(MINUTE,fp.next_review,  rl.when ) <= revGapMinutes AND # time between last review and next review is less than gap
        TIMESTAMPDIFF(MINUTE, fp.next_review, NOW()) <= lastReviewConstant  # next_review scheduled around now timestampt

    LIMIT howMany;
    # time between  now and ne

END //

# =============================================================================
DROP PROCEDURE IF EXISTS get_due_in_learning;

CREATE PROCEDURE get_due_in_learning(
    IN userId INT,
    IN deckId INT,
    IN howMany INT,
    IN revGapMinutes INT,
    IN lastReviewConstant INT
)
BEGIN
    SELECT fl.*
    FROM Flashcards fl
             JOIN Decks d ON fl.deck_id = d.id
             JOIN Flashcards_Progresses fp on fl.id = fp.flashcard_id
             JOIN Review_Logs rl on rl.id = fp.last_review_id

    WHERE d.id = deckId AND
        fp.user_id = userId AND
        TIMESTAMPDIFF(MINUTE,fp.next_review,  rl.when ) <= revGapMinutes AND # time between last review and next review is less than gap
        TIMESTAMPDIFF(MINUTE, fp.next_review, NOW()) >= -lastReviewConstant AND
        next_review <= NOW() # that makes a flashcard due
    LIMIT howMany;
    # time between  now and ne

END //
# =============================================================================
DROP PROCEDURE IF EXISTS get_due_to_review;

CREATE PROCEDURE get_due_to_review(
    IN userId INT,
    IN deckId INT,
    IN howMany INT,
    IN revGapMinutes INT,
    IN lastReviewConstant INT
)
BEGIN
    SELECT fl.*
    FROM Flashcards fl
             JOIN Decks d ON fl.deck_id = d.id
             JOIN Flashcards_Progresses fp on fl.id = fp.flashcard_id
             JOIN Review_Logs rl on rl.id = fp.last_review_id

    WHERE d.id = deckId AND
        fp.user_id = userId AND
        (not(TIMESTAMPDIFF(MINUTE,fp.next_review,  rl.when ) <= revGapMinutes AND # time between last review and next review is less than gap
             TIMESTAMPDIFF(MINUTE, fp.next_review, NOW()) >= -lastReviewConstant)) AND
        next_review <= NOW() # that makes a flashcard due
    LIMIT howMany;
    # time between  now and ne

END //

# =============================================================================
DROP PROCEDURE IF EXISTS get_last_used_decks;

CREATE PROCEDURE get_last_used_decks(
    IN userId INT,
    IN howMany INT
)
BEGIN
    SELECT d.*
    FROM Decks d
             join Flashcards fl on d.id = fl.deck_id
             join Review_Logs rl on fl.id = rl.flashcard_id
    WHERE rl.user_id = userId
    Order by rl.when desc
    limit howMany;
END //


# =============================================================================
DROP PROCEDURE IF EXISTS get_deck_progress;

CREATE PROCEDURE get_deck_progress( IN userId INT,
                                    IN deckId INT,
                                    OUT progress FLOAT
)
BEGIN
    DECLARE total_flashcards INT;
    DECLARE learned_flashcards INT;

    SELECT count(*) INTO total_flashcards
    FROM Flashcards fl
    where deck_id = deckId;

    SELECT count(*) INTO learned_flashcards
    from Flashcards fl
             join Flashcards_Progresses fp on fl.id = fp.flashcard_id
             join Review_Logs rl on fp.last_review_id = rl.id
    where fl.deck_id = deckId and fp.user_id = userId
      and fp.next_review is not null
      and DATEDIFF(fp.next_review, rl.`when`) > 30;

    IF total_flashcards = 0
    THEN
        SET progress = 0;
    ELSE
        SET progress = learned_flashcards / total_flashcards;
    END IF;

END //

# =============================================================================
DROP PROCEDURE IF EXISTS count_decks_new_cards;

CREATE PROCEDURE count_decks_new_cards(
    IN userId INT,
    IN deckId INT,
    OUT newCardCount INT
)
BEGIN
    SELECT COUNT(DISTINCT fl.id)
    INTO newCardCount
    FROM Flashcards fl
             LEFT JOIN Review_Logs rl ON fl.id = rl.flashcard_id
    WHERE deck_id = deckId AND rl.user_id = userId AND rl.id IS NULL;
    #     SELECT COUNT(*)
#     INTO newCardCount
#     FROM Flashcards fl
#
#     LEFT JOIN Review_Logs rl ON fl.id = rl.flashcard_id
#     WHERE deck_id = deckId AND
#         (rl.user_id IS NULL or rl.user_id != userId);
END //

# =============================================================================
DROP PROCEDURE IF EXISTS count_all_deck_due_cards;

CREATE PROCEDURE count_all_deck_due_cards(
    IN userId INT,
    IN deckId INT,
    OUT dueCardCount INT
)
BEGIN
    SELECT COUNT(*)
    INTO dueCardCount
    FROM Flashcards fl
             JOIN Flashcards_Progresses fp ON fl.id = fp.flashcard_id
    WHERE fl.deck_id = deckId
      AND fp.user_id = userId
      AND fp.next_review <= NOW();
END //

# =============================================================================
DROP PROCEDURE IF EXISTS count_all_deck_cards;

CREATE PROCEDURE count_all_deck_cards(
    IN userId INT,
    IN deckId INT,
    OUT totalCardCount INT
)
BEGIN
    SELECT COUNT(*)
    INTO totalCardCount
    FROM Flashcards fl
    WHERE fl.deck_id = deckId;
END //

# =============================================================================
DROP PROCEDURE IF EXISTS get_all_user_folders;

CREATE PROCEDURE get_all_user_folders(
    IN userId INT
)
BEGIN
    SELECT f.*
    FROM Folders f
             JOIN Access_Levels_Folders alf ON f.id = alf.folder_id
             JOIN Folder_Access_Level fal ON alf.access_level_id = fal.id
    WHERE fal.customer_id = userId;
END //

# =============================================================================
DROP PROCEDURE IF EXISTS get_github_style_chart_data;

CREATE PROCEDURE get_github_style_chart_data(
    IN userId INT
)
BEGIN
    SELECT
        DATE(`when`) AS activity_date
    FROM
        Review_Logs
    WHERE
        user_id = userId
      AND `when` > DATE_SUB(NOW(), INTERVAL 1 YEAR)
    GROUP BY
        DATE(`when`);
END //

# =============================================================================
DROP PROCEDURE IF EXISTS find_customers_last_review;

CREATE PROCEDURE find_customers_last_review(
    IN userId INT,
    OUT lastReviewDate DATE
)
BEGIN
    SELECT MAX(`when`)
    INTO lastReviewDate
    FROM Review_Logs
    WHERE user_id = userId;
END //

# =============================================================================
DROP PROCEDURE IF EXISTS count_all_new_cards;


CREATE PROCEDURE count_all_new_cards(
    IN userId INT,
    OUT result INT
)
BEGIN
    SELECT COUNT(DISTINCT fl.id)
    INTO result
    FROM Flashcards fl
             LEFT JOIN Review_Logs rl ON fl.id = rl.flashcard_id
    WHERE rl.user_id = userId AND rl.id IS NULL;
END //

# =============================================================================
DROP PROCEDURE IF EXISTS count_all_due_cards;

CREATE PROCEDURE count_all_due_cards(
    IN userId INT,
    OUT result INT
)
BEGIN
    SELECT COUNT(*)
    INTO result
    FROM Flashcards fl
             JOIN Flashcards_Progresses fp ON fl.id = fp.flashcard_id
    WHERE
        fp.user_id = userId
      AND fp.next_review <= NOW();
END //

# =============================================================================
DROP PROCEDURE IF EXISTS count_all_cards;

CREATE PROCEDURE count_all_cards(
    IN userId INT,
    OUT result INT
)
BEGIN
    SELECT COUNT(*)
    INTO result
    FROM Flashcards fl;
END //

# =============================================================================

DELIMITER $$
DROP FUNCTION IF EXISTS count_decks_new_cards;

-- Funkcja zliczająca nowe karty
CREATE FUNCTION count_decks_new_cards (userId INT, deckId INT)
    RETURNS INT
    DETERMINISTIC
BEGIN
    DECLARE newCardCount INT;

    SELECT COUNT(DISTINCT fl.id)
    INTO newCardCount
    FROM Flashcards fl
             LEFT JOIN Review_Logs rl ON fl.id = rl.flashcard_id
    WHERE fl.deck_id = deckId
      AND (rl.user_id IS NULL OR rl.user_id != userId);

    RETURN newCardCount;
END $$

DROP FUNCTION IF EXISTS get_deck_progress;
-- Funkcja obliczająca postęp w danym decku
CREATE FUNCTION get_deck_progress(userId INT, deckId INT)
    RETURNS FLOAT
    DETERMINISTIC
BEGIN
    DECLARE total_flashcards INT;
    DECLARE learned_flashcards INT;
    DECLARE progress FLOAT;

    -- Zlicz całkowitą liczbę kart w decku
    SELECT COUNT(*)
    INTO total_flashcards
    FROM Flashcards fl
    WHERE fl.deck_id = deckId;

    -- Zlicz liczbę nauczonych kart przez użytkownika
    SELECT COUNT(*)
    INTO learned_flashcards
    FROM Flashcards fl
             JOIN Flashcards_Progresses fp ON fl.id = fp.flashcard_id
             JOIN Review_Logs rl ON fp.last_review_id = rl.id
    WHERE fl.deck_id = deckId
      AND fp.user_id = userId
      AND fp.next_review IS NOT NULL
      AND DATEDIFF(fp.next_review, rl.`when`) > 30;

    -- Oblicz postęp
    IF total_flashcards = 0 THEN
        SET progress = 0;
    ELSE
        SET progress = learned_flashcards / total_flashcards;
    END IF;

    -- Zwróć wynik
    RETURN progress;
END $$

-- Funkcja obliczająca całkowity czas nauki

DROP FUNCTION IF EXISTS calculate_study_time;


DELIMITER $$

CREATE FUNCTION calculate_study_time(userId INT)
    RETURNS FLOAT
    DETERMINISTIC
BEGIN
    DECLARE total_time INT DEFAULT 0;
    DECLARE study_time_in_seconds INT;
    DECLARE done INT DEFAULT 0;
    DECLARE when_time DATETIME;
    DECLARE user_answer VARCHAR(255);

    DECLARE rec_cursor CURSOR FOR
        SELECT rl.`when`, rl.user_answer
        FROM Review_Logs rl
        WHERE rl.user_id = userId
          AND rl.`when` >= NOW() - INTERVAL 7 DAY;

    -- Declare the handler for the end of the cursor
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

    OPEN rec_cursor;

    read_loop: LOOP
        FETCH rec_cursor INTO when_time, user_answer;
        IF done THEN
            LEAVE read_loop;
        END IF;

        SET study_time_in_seconds = 600;
        SET total_time = total_time + study_time_in_seconds;
    END LOOP;

    CLOSE rec_cursor;

    RETURN total_time / 3600;  -- Return time in hours
END $$

DELIMITER ;

-- Funkcja obliczająca najdłuższą serię dni nauki
DROP FUNCTION IF EXISTS calculate_longest_learning_streak;

DELIMITER $$

CREATE FUNCTION calculate_longest_learning_streak(userId INT)
    RETURNS INT
    DETERMINISTIC
BEGIN
    DECLARE longest_streak INT DEFAULT 0;
    DECLARE current_streak INT DEFAULT 0;
    DECLARE prev_date DATE DEFAULT NULL;
    DECLARE current_review_date DATE DEFAULT NULL;  -- Zmieniono nazwę zmiennej
    DECLARE done INT DEFAULT 0;  -- Declare the 'done' variable

    DECLARE rec_cursor CURSOR FOR
        SELECT DISTINCT DATE(rl.`when`) AS review_date
        FROM Review_Logs rl
        WHERE rl.user_id = userId
        ORDER BY review_date;

    -- Declare the handler for the end of the cursor
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

    OPEN rec_cursor;

    read_loop: LOOP
        FETCH rec_cursor INTO current_review_date;  -- Używamy nowej zmiennej
        IF done THEN
            LEAVE read_loop;
        END IF;

        -- If there is a previous date, check if it's consecutive
        IF prev_date IS NOT NULL AND current_review_date = prev_date + INTERVAL 1 DAY THEN
            SET current_streak = current_streak + 1;
        ELSE
            SET current_streak = 1;
        END IF;

        -- Update the longest streak if the current streak is longer
        IF current_streak > longest_streak THEN
            SET longest_streak = current_streak;
        END IF;

        -- Move the previous date forward
        SET prev_date = current_review_date;
    END LOOP;

    CLOSE rec_cursor;

    RETURN longest_streak;
END $$

DELIMITER ;

-- Funkcja zliczająca karty nauczone w ciągu ostatnich 30 dni
DROP FUNCTION IF EXISTS calculate_flashcards_learned_last_30_days;

DELIMITER $$

CREATE FUNCTION calculate_flashcards_learned_last_30_days(userId INT)
    RETURNS INT
    DETERMINISTIC
BEGIN
    DECLARE flashcards_learned INT DEFAULT 0;

    SELECT COUNT(DISTINCT fp.flashcard_id)
    INTO flashcards_learned
    FROM Flashcards_Progresses fp
             JOIN Review_Logs rl ON fp.last_review_id = rl.id
    WHERE fp.user_id = userId
      AND fp.next_review IS NOT NULL
      AND DATEDIFF(CURRENT_DATE, rl.`when`) <= 30;

    RETURN flashcards_learned;
END $$

DELIMITER ;

-- TRIGGERS
-- after_insert_customer_statistics
INSERT INTO Customers (email, password_hash, username, profile_creation_date, root_folder_id)
VALUES ('test_user@example.com', 'hashedpassword', 'test_user', NOW(), 1);
-- verify the trigger
SELECT * FROM User_Statistics WHERE user_id = LAST_INSERT_ID();

-- before_insert_notification
INSERT INTO Notifications (user_id, received, text, creation_date, received_date)
VALUES (1, 0, 'Test Notification', '2025-01-22 00:30:59', '2025-01-22 00:30:59');
-- verify the trigger
SELECT creation_date FROM Notifications WHERE id = LAST_INSERT_ID();

-- after_insert_customer_folder
INSERT INTO Customers (email, password_hash, username, profile_creation_date, root_folder_id)
VALUES ('another_user@example.com', 'anotherhashedpassword', 'another_user', NOW(), 1);
-- verify the trigger
SELECT * FROM Folders WHERE name = CONCAT('Root Folder - ', 'another_user');
SELECT root_folder_id FROM Customers WHERE id = LAST_INSERT_ID();

-- after_insert_friendship_notification
INSERT INTO Friendships (sender_id, receiver_id, accepted)
VALUES (1, 2, 0);

-- verify the trigger
SELECT * FROM Notifications WHERE user_id = 2 AND text LIKE '%friend request from user ID: 1%';

-- FUNCTIONS TESTING

-- count_decks_new_cards
-- Test case 1: user 1, deck 1
SELECT count_decks_new_cards(1, 1) AS 'New Cards Count for user 1 and deck 1';

-- Test case 2: user 2, deck 1
SELECT count_decks_new_cards(1, 2) AS 'New Cards Count for user 1 and deck 2';

-- get_deck_progress_func
-- Test case 1: user with 50% progress in a deck
SELECT get_deck_progress(1, 1) AS 'Deck Progress for user 1 and deck 1';

-- Test case 2: user has not learned any flashcards in a deck
SELECT get_deck_progress(1, 2) AS 'Deck Progress for user 2 and deck 1';

-- Test case 3: deck with no flashcards
SELECT get_deck_progress(1, 999) AS 'Deck Progress for user 1 and deck 999';

-- calculate_study_time
-- Test case 1: user with review logs in the last 7 days
SELECT calculate_study_time(1) AS 'Study Time for user 1';

-- Test case 2: user with no review logs in the last 7 days
SELECT calculate_study_time(2) AS 'Study Time for user 2';

-- calculate_longest_learning_streak
-- Test case 1: user with a streak of consecutive days
SELECT calculate_longest_learning_streak(1) AS 'Longest Learning Streak for user 1';

-- Test case 2: user with no review logs
SELECT calculate_longest_learning_streak(2) AS 'Longest Learning Streak for user 2';

-- calculate_flashcards_learned_last_30_days
-- Test case 1: user who has reviewed some flashcards in the last 30 days
SELECT calculate_flashcards_learned_last_30_days(1) AS 'Flashcards Learned in Last 30 Days for user 1';

-- Test case 2: user who has not reviewed any flashcards in the last 30 days
SELECT calculate_flashcards_learned_last_30_days(2) AS 'Flashcards Learned in Last 30 Days for user 2';


-- PROCEDURES TESTING

-- Test for count_current_learning
DELIMITER //

-- Test for count_current_learning
DROP PROCEDURE IF EXISTS test_count_current_learning;

CREATE PROCEDURE test_count_current_learning()
BEGIN
    DECLARE result INT;
    CALL count_current_learning(1, 1, 30, 60, result);
    SELECT 'Test count_current_learning:' AS Description, result AS Result;
END //

-- Test for count_due_in_learning
DROP PROCEDURE IF EXISTS test_count_due_in_learning;

CREATE PROCEDURE test_count_due_in_learning()
BEGIN
    DECLARE result INT;
    CALL count_due_in_learning(1, 1, 30, 60, result);
    SELECT 'Test count_due_in_learning:', result;
END //

-- Test for count_due_to_review
DROP PROCEDURE IF EXISTS test_count_due_to_review;

DROP PROCEDURE IF EXISTS test_count_due_to_review;

CREATE PROCEDURE test_count_due_to_review()
BEGIN
    DECLARE result INT;
    CALL count_due_to_review(1, 1, 30, 60, result);
    SELECT 'Test count_due_to_review:', result;
END //

-- Test for get_due_flashcards
DROP PROCEDURE IF EXISTS test_get_due_flashcards;

CREATE PROCEDURE test_get_due_flashcards()
BEGIN
    CALL get_due_flashcards(1, 1, 5);
END //

-- Test for get_new_flashcard
DROP PROCEDURE IF EXISTS test_get_new_flashcard;

CREATE PROCEDURE test_get_new_flashcard()
BEGIN
    CALL get_new_flashcard(1, 1, 5);
END //

-- Test for get_early_review
DROP PROCEDURE IF EXISTS test_get_early_review;

CREATE PROCEDURE test_get_early_review()
BEGIN
    CALL get_early_review(1, 1, 5, 30, 60);
END //

-- Test for get_due_in_learning
DROP PROCEDURE IF EXISTS test_get_due_in_learning;

CREATE PROCEDURE test_get_due_in_learning()
BEGIN
    CALL get_due_in_learning(1, 1, 5, 30, 60);
END //

-- Test for get_due_to_review
DROP PROCEDURE IF EXISTS test_get_due_to_review;

CREATE PROCEDURE test_get_due_to_review()
BEGIN
    CALL get_due_to_review(1, 1, 5, 30, 60);
END //

-- Test for get_last_used_decks
DROP PROCEDURE IF EXISTS test_get_last_used_decks;

CREATE PROCEDURE test_get_last_used_decks()
BEGIN
    CALL get_last_used_decks(1, 5);
END //

-- Test for get_deck_progress
DROP PROCEDURE IF EXISTS test_get_deck_progress;

CREATE PROCEDURE test_get_deck_progress()
BEGIN
    DECLARE progress FLOAT;
    CALL get_deck_progress(1, 1, progress);
    SELECT 'Test get_deck_progress:', progress;
END //

-- Test for count_decks_new_cards
DROP PROCEDURE IF EXISTS test_count_decks_new_cards;

CREATE PROCEDURE test_count_decks_new_cards()
BEGIN
    DECLARE result INT;
    CALL count_decks_new_cards(1, 1, result);
    SELECT 'Test count_decks_new_cards:', result;
END //

-- Test for count_all_deck_due_cards
DROP PROCEDURE IF EXISTS test_count_all_deck_due_cards;

CREATE PROCEDURE test_count_all_deck_due_cards()
BEGIN
    DECLARE result INT;
    CALL count_all_deck_due_cards(1, 1, result);
    SELECT 'Test count_all_deck_due_cards:', result;
END //

-- Test for count_all_deck_cards
DROP PROCEDURE IF EXISTS test_count_all_deck_cards;

CREATE PROCEDURE test_count_all_deck_cards()
BEGIN
    DECLARE result INT;
    CALL count_all_deck_cards(1, 1, result);
    SELECT 'Test count_all_deck_cards:', result;
END //

-- Test for get_all_user_folders
DROP PROCEDURE IF EXISTS test_get_all_user_folders;

CREATE PROCEDURE test_get_all_user_folders()
BEGIN
    CALL get_all_user_folders(1);
END //

-- Test for get_github_style_chart_data
DROP PROCEDURE IF EXISTS test_get_github_style_chart_data;

CREATE PROCEDURE test_get_github_style_chart_data()
BEGIN
    CALL get_github_style_chart_data(1);
END //

-- Test for find_customers_last_review
DROP PROCEDURE IF EXISTS test_find_customers_last_review;

CREATE PROCEDURE test_find_customers_last_review()
BEGIN
    DECLARE lastReviewDate DATE;
    CALL find_customers_last_review(1, lastReviewDate);
    SELECT 'Test find_customers_last_review:', lastReviewDate;
END //

-- Test for count_all_new_cards
DROP PROCEDURE IF EXISTS test_count_all_new_cards;

CREATE PROCEDURE test_count_all_new_cards()
BEGIN
    DECLARE result INT;
    CALL count_all_new_cards(1, result);
    SELECT 'Test count_all_new_cards:', result;
END //

-- Test for count_all_due_cards
DROP PROCEDURE IF EXISTS test_count_all_due_cards;

CREATE PROCEDURE test_count_all_due_cards()
BEGIN
    DECLARE result INT;
    CALL count_all_due_cards(1, result);
    SELECT 'Test count_all_due_cards:', result;
END //

-- Test for count_all_cards
DROP PROCEDURE IF EXISTS test_count_all_cards;

CREATE PROCEDURE test_count_all_cards()
BEGIN
    DECLARE result INT;
    CALL count_all_cards(1, result);
    SELECT 'Test count_all_cards:', result;
END //

DELIMITER ;

CALL test_count_current_learning();
CALL test_count_due_in_learning();
CALL test_count_due_to_review();
CALL test_get_due_flashcards();
CALL test_get_new_flashcard();
CALL test_get_early_review();
CALL test_get_due_in_learning();
CALL test_get_due_to_review();
CALL test_get_last_used_decks();
CALL test_get_deck_progress();
CALL test_count_decks_new_cards();
CALL test_count_all_deck_due_cards();
CALL test_count_all_deck_cards();
CALL test_get_all_user_folders();
CALL test_get_github_style_chart_data();
CALL test_find_customers_last_review();
CALL test_count_all_new_cards();
CALL test_count_all_due_cards();
CALL test_count_all_cards();