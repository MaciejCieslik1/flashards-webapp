import React, { useState, useEffect } from "react";
import { useParams } from "react-router-dom";

import Navbar from "../../Navbar/Navbar";
import "./Study.css";
import testDecks from "../../../assets/mockData/testDecks";

const Study = (props) => {
  const { deckId } = useParams(); // Use deckId to identify the deck
  const [currentCardIndex, setCurrentCardIndex] = useState(0);
  const [cards, setCards] = useState([]);
  const [currentDeck, setCurrentDeck] = useState(null); // Null initially
  const [showBack, setShowBack] = useState(false); // State to toggle card sides

  // Fetch the deck dynamically
  useEffect(() => {
    const fetchedDeck =
      testDecks.find((deck) => deck.id === Number(deckId)) || null;
    if (fetchedDeck) {
      setCurrentDeck(fetchedDeck);
      setCards(fetchedDeck.newCards || []);
    }
  }, [deckId]); // Refetch deck when deckId changes

  // Add event listener for Enter key to toggle card side
  useEffect(() => {
    const handleKeyPress = (e) => {
      if (e.key === "Enter") {
        setShowBack((prev) => !prev); // Toggle between front and back
        e.preventDefault(); // Prevent default Enter key behavior
      }
    };
    window.addEventListener("keydown", handleKeyPress);
    return () => window.removeEventListener("keydown", handleKeyPress); // Cleanup on unmount
  }, []);

  const handleProgressUpdate = (interval) => {
    if (!currentDeck) return;

    const updatedCards = cards.map((card, index) =>
      index === currentCardIndex ? { ...card, progressInterval: interval } : card
    );
    setCards(updatedCards);

    const updatedDeck = {
      ...currentDeck,
      newCards: updatedCards,
    };
    setCurrentDeck(updatedDeck);

    if (currentCardIndex < updatedCards.length - 1) {
      setCurrentCardIndex(currentCardIndex + 1);
    } else {
      setCurrentCardIndex(0);
    }

    setShowBack(false); // Reset to front of the card when moving to the next card
  };

  // Guard for missing data
  if (!currentDeck || cards.length === 0) {
    return <div>Loading or no cards available...</div>;
  }

  const currentCard = cards[currentCardIndex] || {};

  return (
    <div className="study">
      <Navbar details={props.details || {}} />
      <div className="study-container">
      <p>You're studying: {currentDeck?.title || "Unknown Deck"}</p>
        {currentCard.front && currentCard.back ? (
          <>
            <div className="card">
              {showBack ? (
                <div className="back">{currentCard.back}</div>
              ) : (
                <div className="front">{currentCard.front}</div>
              )}
            </div>
            <div className="progress-buttons">
              <button onClick={() => handleProgressUpdate("1min")}>Repeat</button>
              <button onClick={() => handleProgressUpdate("10min")}>Hard</button>
              <button onClick={() => handleProgressUpdate("1day")}>Mid</button>
              <button onClick={() => handleProgressUpdate("5day")}>Easy</button>
            </div>
            <button className="edit-button">Edit</button>
          </>
        ) : (
          <p>Card data is incomplete.</p>
        )}
      </div>
    </div>
  );
};

export default Study;

{/*
  FOR TESTING
import React, { useState, useEffect } from "react";
import { useParams } from "react-router-dom";

import Navbar from "../../Navbar/Navbar";
import "./Study.css";
import mockDeck from "../../../assets/mockData/mockDeck";

const Study = (props) => {
  const { deckId } = useParams(); // Still keeping deckId for potential routing
  const [currentCardIndex, setCurrentCardIndex] = useState(0);
  const [cards, setCards] = useState([]);
  const [currentDeck, setCurrentDeck] = useState(null); // Start with null for safety
  const [showBack, setShowBack] = useState(false); // State to track whether to show the back of the card

  // Simulate updating `mockDeck`
  const updateMockDeck = (updatedDeck) => {
    mockDeck.newCards = updatedDeck.newCards;
    console.log("Updated mockDeck:", mockDeck); // For debugging
  };

  // Initialize cards and deck from `mockDeck`
  useEffect(() => {
    setCurrentDeck(mockDeck); // Always use mockDeck
    setCards(mockDeck?.newCards.slice(0, 50) || []); // Limit cards during testing
  }, []); // No dependencies since we're always using mockDeck

  // Add event listener for Enter key to toggle card side
  useEffect(() => {
    const handleKeyPress = (e) => {
      if (e.key === "Enter") {
        setShowBack((prev) => !prev); // Toggle between front and back
        e.preventDefault(); // Prevent other actions bound to Enter
      }
    };
    window.addEventListener("keydown", handleKeyPress);
    return () => window.removeEventListener("keydown", handleKeyPress); // Cleanup listener on unmount
  }, []);

  const handleProgressUpdate = (interval) => {
    if (!currentDeck) return;

    const updatedCards = cards.map((card, index) =>
      index === currentCardIndex ? { ...card, progressInterval: interval } : card
    );
    setCards(updatedCards);

    const updatedDeck = {
      ...currentDeck,
      newCards: updatedCards,
    };
    setCurrentDeck(updatedDeck);
    updateMockDeck(updatedDeck);

    if (currentCardIndex < updatedCards.length - 1) {
      setCurrentCardIndex(currentCardIndex + 1);
    } else {
      setCurrentCardIndex(0);
    }

    setShowBack(false); // Reset to front of the card when moving to the next card
  };

  // Guard for missing data
  if (!currentDeck || cards.length === 0) {
    return <div>Loading or no cards available...</div>;
  }

  const currentCard = cards[currentCardIndex] || {};

  return (
    <div className="study">
    <Navbar details={props.details || {}} /> 
    <div className="study-container">
      <p>You're studying mockDeck</p>
      {currentCard.front && currentCard.back ? (
        <>
          <div className="card">
            {showBack ? (
              <div className="back">{currentCard.back}</div>
            ) : (
              <div className="front">{currentCard.front}</div>
            )}
          </div>
          <div className="progress-buttons">
            <button onClick={() => handleProgressUpdate("1min")}>Repeat</button>
            <button onClick={() => handleProgressUpdate("10min")}>Hard</button>
            <button onClick={() => handleProgressUpdate("1day")}>Mid</button>
            <button onClick={() => handleProgressUpdate("5day")}>Easy</button>
          </div>
          <button className="edit-button">Edit</button>
        </>
      ) : (
        <p>Card data is incomplete.</p>
      )}
    </div>
    </div>
  );
};

export default Study;
*/}