package com.lactaoen.blackjack.model;

public enum Result {
    WIN(1),
    LOSE(-1),
    PUSH(0);

    private int value;

    Result(int value) {
        this.value = value;
    }

    public static Result getByValue(int value) {
        for (Result r : Result.values()) {
            if (r.value == value) return r;
        }
        return null;
    }
}
