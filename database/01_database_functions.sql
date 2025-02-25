use my_database;

DELIMITER //

# =============================================================================

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

CREATE PROCEDURE count_decks_new_cards(
    IN userId INT,
    IN deckId INT,
    OUT newCardCount INT
)
BEGIN
    SET newCardCount = 0;
    SELECT COUNT(*)
    INTO newCardCount
    FROM (
             SELECT fl.id
             FROM Flashcards fl
                      JOIN Review_Logs rl ON fl.id = rl.flashcard_id
             WHERE fl.deck_id = deckId AND rl.user_id = userId
             GROUP BY fl.id
             HAVING COUNT(rl.id) = 1
         ) AS subquery;
END //

# =============================================================================

CREATE PROCEDURE count_all_deck_due_cards(
    IN userId INT,
    IN deckId INT,
    OUT dueCardCount INT
)
BEGIN
    SELECT COUNT(*)
    INTO dueCardCount
    FROM (
            SELECT fl.id
            FROM Flashcards fl
               JOIN Flashcards_Progresses fp ON fl.id = fp.flashcard_id
               JOIN Review_Logs rl ON fl.id = rl.flashcard_id
            WHERE fl.deck_id = deckId AND fp.user_id = userId AND fp.next_review <= NOW()
            GROUP BY fl.id
            HAVING COUNT(rl.id) > 1
        ) AS subquery;
END //

# =============================================================================

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
# CREATE PROCEDURE count_all_new_cards(
#     IN userId INT,
#     OUT result INT
# )
# BEGIN
# SELECT COUNT(*)
# INTO result
# FROM Flashcards fl
# LEFT JOIN Review_Logs rl ON fl.id = rl.flashcard_id
# WHERE
#     rl.user_id = userId AND
#     rl.id IS NULL;
# END //

CREATE PROCEDURE count_all_new_cards(
    IN userId INT,
    OUT result INT
)
BEGIN
    SET result = 0;
    SELECT COUNT(*)
    INTO result
    FROM (
             SELECT fl.id
             FROM Flashcards fl
                      JOIN Review_Logs rl ON fl.id = rl.flashcard_id
             WHERE rl.user_id = userId
             GROUP BY fl.id
             HAVING COUNT(rl.id) = 1
         ) AS subquery;
END //

# =============================================================================

CREATE PROCEDURE count_all_due_cards(
    IN userId INT,
    OUT result INT
)
BEGIN
SELECT COUNT(*)
INTO result
FROM (
        SELECT fl.id
        FROM Flashcards fl
            JOIN Flashcards_Progresses fp ON fl.id = fp.flashcard_id
            JOIN Review_Logs rl ON fl.id = rl.flashcard_id
        WHERE fp.user_id = userId AND fp.next_review <= NOW()
        GROUP BY fl.id
        HAVING COUNT(rl.id) > 1
     ) AS subquery;
END //


# =============================================================================

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
