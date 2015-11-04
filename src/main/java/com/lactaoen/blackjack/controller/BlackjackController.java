package com.lactaoen.blackjack.controller;

import com.lactaoen.blackjack.exception.BlackjackErrorCode;
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
import java.util.List;
import java.util.Map;

@Controller
public class BlackjackController {

    @Autowired
    private BlackjackService blackjackService;

    @MessageMapping("/connect")
    @SendTo("/topic/greetings")
    public String greeting() throws Exception {
        return "Hello";
    }

    @MessageMapping("/register")
    @SendToUser("/topic/player")
    public PlayerInfo register(RegistrationWrapper reg) throws BlackjackException {
        return new PlayerInfo().transpose(blackjackService.registerPlayer(reg.getName()));
    }

    @MessageMapping("/unregister")
    @SendToUser("/topic/player")
    public Player unregister(UnregistrationWrapper reg) throws BlackjackException {
        return blackjackService.unregisterPlayer(reg.getPlayerId());
    }

    @MessageMapping("/players")
    @SendTo("/topic/players")
    public List<Player> getRegisteredPlayers() {
        return blackjackService.getPlayers();
    }

    @MessageMapping("/bet")
    @SendTo("/topic/game")
    public GameInfoWrapper placeBet(BetWrapper bet) throws BlackjackException {
        return blackjackService.placeBet(bet.getPlayerId(), bet.getBetAmount());
    }

    @MessageMapping("/action")
    @SendTo("/topic/game")
    public GameInfoWrapper submitAction(ActionWrapper action) throws BlackjackException {
        return blackjackService.processAction(action.getPlayerId(), action.getHandNum(), action.getAction());
    }

    @MessageExceptionHandler
    @SendToUser("/topic/errors")
    public Map<String, Object> handleErrors(BlackjackException ex) {
        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("errorCode", ex.getErrorCode());
        errorMap.put("message", ex.getErrorCode().getMessage());
        return errorMap;
    }
}
