package com.lactaoen.blackjack.model.wrapper;

public class AutoPilotWrapper {

    private boolean isAuto;

    public AutoPilotWrapper() {
    }

    public AutoPilotWrapper(boolean isAuto) {
        this.isAuto = isAuto;
    }

    public boolean isAuto() {
        return isAuto;
    }

    public void setAuto(boolean isAuto) {
        this.isAuto = isAuto;
    }

    public String getMessageType() {
        return "autoPilotWrapper";
    }
}
