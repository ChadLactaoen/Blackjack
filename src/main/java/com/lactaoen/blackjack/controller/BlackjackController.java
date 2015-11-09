package com.lactaoen.blackjack.controller;

import com.lactaoen.blackjack.exception.BlackjackException;
import com.lactaoen.blackjack.model.*;
import com.lactaoen.blackjack.model.wrapper.*;
import com.lactaoen.blackjack.service.BlackjackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

@Controller
public class BlackjackController {

    @Autowired
    private BlackjackService blackjackService;

    @MessageMapping("/register")
    @SendToUser("/queue/player")
    public PlayerInfo register(RegistrationWrapper reg) throws BlackjackException {
        return new PlayerInfo().transpose(blackjackService.registerPlayer(reg.getName()));
    }

    @MessageMapping("/unregister")
    @SendToUser("/queue/player")
    public PlayerInfo unregister(UnregistrationWrapper reg) throws BlackjackException {
        return blackjackService.unregisterPlayer(reg.getPlayerId());
    }

    @MessageMapping("/players")
    @SendToUser("/queue/players")
    public GameInfoWrapper getCurrentGameInfo() {
        return blackjackService.getCurrentGameInfo();
    }

    @MessageMapping("/bet")
    @SendTo("/topic/game")
    public GameInfoWrapper placeBet(BetWrapper bet) throws BlackjackException {
        return blackjackService.placeBet(bet.getPlayerId(), bet.getBetAmount());
    }

    @MessageMapping("/action")
    @SendTo("/topic/game")
    public GameInfoWrapper submitAction(ActionWrapper action) throws BlackjackException, InterruptedException {
        // Waits 1/2 second before sending message to simulate waiting for the next action
        Thread.sleep(500);
        return blackjackService.processAction(action.getPlayerId(), action.getHandNum(), action.getAction());
    }

    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public BlackjackErrorWrapper handleErrors(BlackjackException ex) {
        return new BlackjackErrorWrapper(ex.getErrorCode(), ex.getMessage());
    }
}
