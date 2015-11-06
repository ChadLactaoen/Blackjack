package com.lactaoen.blackjack.exception;

public enum BlackjackErrorCode {
    // Error codes in the 100's represent betting errors
    BJ101("Bet amount exceeds current chip count."),
    BJ102("You have already made a bet for the current hand."),
    BJ105("There is already a hand currently in progress. You cannot bet right now."),
    BJ110("Bet must be in increments of 10."),

    // Error codes in the 500's represent player errors
    BJ500("Game is currently at player capacity. Cannot register."),
    BJ550("No player found for given player id."),
    BJ570("Your status is currently inactive."),

    // Error codes in the 700's represent in-game action errors
    BJ700("You cannot perform any actions on the current hand as it is now the betting round."),
    BJ701("Hand number provided does not represent an actual hand number."),
    BJ720("Hand is not eligible for a double down."),
    BJ730("Hand is not eligible for a surrender."),
    BJ740("Hand is not eligible for a split."),
    BJ799("Hand is currently not able to be acted on. You are acting out of turn."),

    // Error codes in the 900's correspond to Trebek admin errors
    BJ914("Trebek cannot manually start a hand as people are still betting."),
    BJ915("Trebek cannot manually start a hand as the game is on auto-pilot."),
    BJ920("Invalid player id. Cannot kick player."),
    BJ921("Invalid player id. Cannot inactivate/activate player."),
    BJ930("Hand is currently in progress and cannot kick player. Setting player as inactive instead."),
    BJ940("No hand currently active.");

    private String message;

    BlackjackErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
