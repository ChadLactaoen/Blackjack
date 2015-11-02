package com.lactaoen.blackjack.controller;

import com.lactaoen.blackjack.model.*;
import com.lactaoen.blackjack.model.wrapper.*;
import com.lactaoen.blackjack.service.BlackjackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.List;

@Controller
public class BlackjackController {

    @Autowired
    private BlackjackService blackjackService;

    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public String greeting() throws Exception {
        return "Hello";
    }

    @MessageMapping("/register")
    @SendToUser("/topic/player")
    public Player register(RegistrationWrapper reg) {
        return blackjackService.registerPlayer(reg.getName());
    }

    @MessageMapping("/unregister")
    @SendToUser("/topic/player")
    public Player unregister(UnregistrationWrapper reg) {
        return blackjackService.unregisterPlayer(reg.getPlayerId());
    }

    @MessageMapping("/players")
    @SendToUser("/topic/player")
    public List<Player> getRegisteredPlayers() {
        return blackjackService.getPlayers();
    }

    @MessageMapping("/bet")
    @SendTo("/topic/game")
    public GameInfoWrapper placeBet(BetWrapper bet) throws IOException {
        return blackjackService.placeBet(bet.getPlayerId(), bet.getBetAmount());
    }

    @MessageMapping("/action")
    @SendTo("/topic/game")
    public GameInfoWrapper submitAction(ActionWrapper action) {
        return blackjackService.processAction(action.getPlayerId(), action.getHandNum(), action.getAction());
    }

    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public String handleErrors(IllegalStateException ex) {
        return ex.getMessage();
    }
}
