package com.lactaoen.blackjack.controller;

import com.lactaoen.blackjack.service.BlackjackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

/**
 * Trebek controller handles all the admin operations, such as booting people and
 * starting hands.
 */
@Controller
public class TrebekController {

    public void kickPlayer(String playerId) {
        // TODO
    }
}
