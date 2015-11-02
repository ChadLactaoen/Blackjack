package com.lactaoen.blackjack.service;

import com.lactaoen.blackjack.model.*;
import com.lactaoen.blackjack.model.wrapper.GameInfoWrapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BlackjackService {

    private Game game;

    public BlackjackService() {
        game = new Game();
    }

    public List<Player> getPlayers() {
        // Return only a copy of the list of players as we don't want to give them the playerIds
        List<Player> copy = new ArrayList<>();
        copy.addAll(game.getPlayers());
        copy.stream().forEach(p -> {
            p.setPlayerId(null);
            p.setHands(null);
        });
        return copy;
    }

    public Player registerPlayer(String name) {
        if (game.getPlayers().size() < 4) {
            game.addPlayer(name);
            return game.getLastSeatedPlayer();
        }

        return null;
    }

    public Player unregisterPlayer(String playerId) {
        Player player = null;
        List<Player> players = game.getPlayers();
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getPlayerId().equals(playerId)) {
                player = players.get(i);
                players.remove(i);
                break;
            }
        }

        return player;
    }

    public GameInfoWrapper placeBet(String playerId, int betAmount) throws IOException {
        if (betAmount < 10 || betAmount % 10 != 0) throw new IOException("Bet must be in increments of 10.");

        Optional<Player> player = game.getPlayers().stream().filter(p -> p.getPlayerId().equals(playerId)).findFirst();

        if (player.isPresent()) {
            // Make sure player hasn't already placed a bet for this current hand
            if (player.get().getHands().stream().filter(h -> h.getCards().isEmpty()).count() >= 1) {
                throw new IOException("You have already made a bet for the current hand");
            }

            // Validate the player has enough to make the desired bet amount
            if (player.get().getChips() - betAmount < 0) throw new IOException("Bet amount exceeds current chip count.");

            // Decrement chip stack by the bet amount and place it on a new hand
            player.get().decrementChipCount(betAmount);
            player.get().setNextBet(betAmount);
        }

        // If all players have placed their bet for this hand, then deal out the cards
        if (game.isBettingRoundDone()) {
            game.getPlayers().stream().forEach(Player::removeOldHands);
            game.getPlayers().stream().forEach(Player::moveBetToNewHand);
            return startNewHand();
        }

        return new GameInfoWrapper(game.getPlayers(), null, null, Round.BETTING_ROUND, null, game.getDeck().getDeckSize());
    }

    public GameInfoWrapper processAction(String playerId, int handNum, Action action) {
        Optional<Player> player = game.getPlayers().stream().filter(p -> p.getPlayerId().equals(playerId)).findFirst();

        if (player.isPresent()) {
            try {
                Hand hand = player.get().getHands().get(handNum);
                if (hand.isTurn()) {
                    // Execute the action
                    switch (action) {
                        case STAND:
                            game.switchCurrentHandOffAndTurnNextHandOn();
                            break;
                        case HIT:
                            hand.addCard(game.dealCard());
                            break;
                        case DOUBLE:
                            if (canPerformDouble(player.get(), hand)) {
                                hand.addCard(game.dealCard());
                                game.switchCurrentHandOffAndTurnNextHandOn();
                            }
                            break;
                        case SURRENDER:
                            player.get().incrementChipCount(hand.getBetAmount()/2);
                            game.switchCurrentHandOffAndTurnNextHandOn();
                            break;
                        case SPLIT:
                            performSplit(player.get(), hand);
                            break;
                    }
                }
            } catch (ArrayIndexOutOfBoundsException ex) {
                // TODO Implement
            }
        }

        // If all players are done acting, then let the dealer do his thang, evaluate hands, then pay out accordingly
        if (game.isActionDone()) {
            performDealerAction();
            evaluateHands();
            return new GameInfoWrapper(game.getPlayers(), game.getDealer(), game.getDealerUpCard(),
                    Round.BETTING_ROUND, new PlayerAction(player.get().getName(), action), game.getDeckSize());
        }

        // If there is still more action to come, send as Hand still in Progress
        return new GameInfoWrapper(game.getPlayers(), null, game.getDealerUpCard(), Round.HAND_IN_PROGRESS,
                new PlayerAction(player.get().getName(), action), game.getDeckSize());
    }

    private GameInfoWrapper startNewHand() {
        // Add to their hands played counter
        game.getPlayers().stream().forEach(Player::incrementHandsPlayed);
        game.dealNewHand();

        // Check if dealer has blackjack. If she does, then the hand is over
        if (game.getDealerHand().getHandValue() == 21) {
            evaluateHands();
            return new GameInfoWrapper(game.getPlayers(), game.getDealer(), game.getDealerUpCard(), Round.BETTING_ROUND, null, game.getDeckSize());
        } else {
            // Start the game as usual otherwise
            game.getPlayers().get(0).getHands().get(0).setTurn(true);
            return new GameInfoWrapper(game.getPlayers(), null, game.getDealerUpCard(), Round.HAND_IN_PROGRESS, null, game.getDeckSize());
        }
    }

    /** Private util methods that perform logic operations on the Game object. **/

    private void performSplit(Player player, Hand hand) {
        if (canPerformSplit(player, hand)) {
            // Remove one card from the hand and move it to it's own hand
            Card card = hand.getCards().get(1);

            hand.getCards().remove(1);
            Hand newHand = new Hand(hand.getBetAmount());
            newHand.addCard(card);

            // Add new hand to player's list of hands
            player.addHand(newHand);

            // Add card to current hand
            hand.addCard(game.dealCard());

            // Allows Aces to be split only once
            if (card.getRank() == Rank.ACE) {
                newHand.addCard(game.dealCard());
                hand.switchIsTurn();
                // Set new hand to true so we can handle the next hand appropriately
                newHand.switchIsTurn();
                game.switchCurrentHandOffAndTurnNextHandOn();
            }
        }
    }

    private boolean canPerformDouble(Player player, Hand hand) {
        // Player can double only if he has enough chips and on his first two cards
        return player.getChips() >= hand.getBetAmount() && hand.getCards().size() == 2;
    }

    private boolean canPerformSplit(Player player, Hand hand) {
        // Player can split only if he has enough to cover and has no more than 4 hands already in play
        return player.getChips() >= hand.getBetAmount() && player.getHands().size() < 4 && hand.isSplittable();
    }

    private void performDealerAction() {
        // HIT if the dealer's hand value < 17, then evaluate again
        while (game.getDealerHand().getHandValue() < 17) {
            game.getDealerHand().addCard(game.dealCard());
        }
    }

    private void evaluateHands() {
        int dealerHandValue = game.getDealerHand().getHandValue();

        for (Player p : game.getPlayers()) {
            p.getHands().stream().forEach(hand -> {
                Result result = getResult(hand.getHandValue(), dealerHandValue);
                hand.setResult(result);
                if (result == Result.WIN) {
                    // Award the player as well as give him the bet back
                    p.incrementChipCount(hand.getBetAmount()*2);
                }
            });
        }

        // Boot out players who don't have enough chips
    }

    private Result getResult(int playerHandVal, int dealerHandVal) {
        return Result.getByValue(Integer.compare(playerHandVal, dealerHandVal));
    }
}
