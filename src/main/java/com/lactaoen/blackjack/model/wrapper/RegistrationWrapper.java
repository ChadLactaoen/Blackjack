package com.lactaoen.blackjack.model.wrapper;

public class RegistrationWrapper {

    private String name;

    public RegistrationWrapper() {
    }

    public RegistrationWrapper(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
