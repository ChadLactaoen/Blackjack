package com.lactaoen.blackjack.service;

import com.lactaoen.blackjack.exception.BlackjackErrorCode;
import com.lactaoen.blackjack.exception.BlackjackException;
import com.lactaoen.blackjack.model.*;
import com.lactaoen.blackjack.model.wrapper.GameInfoWrapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class BlackjackService {

    private Game game;

    public BlackjackService() {
        game = new Game();
    }

    public List<Player> getPlayers() {
        return game.getPlayers();
    }

    public Player registerPlayer(String name) throws BlackjackException {
        // TODO Replace magic number here
        if (game.getPlayers().size() < 4) {
            return game.addPlayer(name);
        }

        throw new BlackjackException(BlackjackErrorCode.BJ500);
    }

    public Player unregisterPlayer(String playerId) throws BlackjackException {
        Player player = null;
        List<Player> players = game.getPlayers();
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getPlayerId().equals(playerId)) {
                player = players.get(i);
                players.remove(i);
                break;
            }
        }

        if (player == null) {
            throw new BlackjackException(BlackjackErrorCode.BJ550);
        }
        return player;
    }

    public GameInfoWrapper placeBet(String playerId, int betAmount) throws BlackjackException {
        if (!game.isActionDone()) {
            throw new BlackjackException(BlackjackErrorCode.BJ105);
        }

        if (betAmount < 10 || betAmount % 10 != 0) throw new BlackjackException(BlackjackErrorCode.BJ110);

        Optional<Player> player = game.getPlayers().stream().filter(p -> p.getPlayerId().equals(playerId)).findFirst();

        if (player.isPresent()) {
            // Make sure player hasn't already placed a bet for this current hand
            if (player.get().getHands().stream().filter(h -> h.getResult() == null).count() > 0 || player.get().getNextBet() != null) {
                throw new BlackjackException(BlackjackErrorCode.BJ102);
            }

            // Validate the player has enough to make the desired bet amount
            if (player.get().getChips() - betAmount < 0) throw new BlackjackException(BlackjackErrorCode.BJ101);

            // Decrement chip stack by the bet amount and place it on a new hand
            player.get().decrementChipCount(betAmount);
            player.get().setNextBet(betAmount);
        }

        // If all players have placed their bet for this hand, then deal out the cards
        if (game.isBettingRoundDone()) {
            game.getPlayers().stream().forEach(Player::removeOldHands);
            game.getPlayers().stream().forEach(Player::moveBetToNewHand);
            game.getDealer().getHands().clear();
            game.getDealer().addHand(new Hand());
            return startNewHand();
        }

        return new GameInfoWrapper(game.getPlayers(), null, null, Round.BETTING_ROUND, null, game.getDeck().getDeckSize());
    }

    public GameInfoWrapper processAction(String playerId, int handNum, Action action) throws BlackjackException {
        if (game.isActionDone()) throw new BlackjackException(BlackjackErrorCode.BJ700);

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
                            if (hand.getHandValue() > 21) {
                                hand.setHandStatus(HandStatus.BUST);
                                game.switchCurrentHandOffAndTurnNextHandOn();
                            }
                            break;
                        case DOUBLE:
                            if (canPerformDouble(player.get(), hand)) {
                                player.get().decrementChipCount(hand.getBetAmount());
                                hand.setBetAmount(hand.getBetAmount()*2);
                                hand.addCard(game.dealCard());
                                game.switchCurrentHandOffAndTurnNextHandOn();
                            } else {
                                throw new BlackjackException(BlackjackErrorCode.BJ720);
                            }
                            break;
                        case SURRENDER:
                            if (canPerformSurrender(hand)) {
                                player.get().incrementChipCount(hand.getBetAmount()/2);
                                hand.setHandStatus(HandStatus.SURRENDER);
                                game.switchCurrentHandOffAndTurnNextHandOn();
                            } else {
                                throw new BlackjackException(BlackjackErrorCode.BJ730);
                            }
                            break;
                        case SPLIT:
                            performSplit(player.get(), hand);
                            break;
                    }
                }
            } catch (ArrayIndexOutOfBoundsException ex) {
                throw new BlackjackException(BlackjackErrorCode.BJ701);
            }
        } else {
            throw new BlackjackException(BlackjackErrorCode.BJ550);
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
        game.getPlayers().stream().filter(Player::isActive).forEach(Player::incrementHandsPlayed);
        game.dealNewHand();

        // Check if dealer has blackjack. If she does, then the hand is over
        if (game.getDealerHand().getHandValue() == 21) {
            evaluateHands();
            return new GameInfoWrapper(game.getPlayers(), game.getDealer(), game.getDealerUpCard(), Round.BETTING_ROUND, null, game.getDeckSize());
        } else {
            // This needs to check if anyone has any blackjacks so we can set the hand status appropriately
            game.getPlayers().stream().forEach(p -> p.getHands().stream().forEach(h -> {if(h.isBlackjack()) h.setHandStatus(HandStatus.BLACKJACK);}));

            // Start the game as usual. Check for blackjack though, that's an automatic win
            try {
                game.getAllHands().stream().filter(h -> h.getHandStatus() != HandStatus.BLACKJACK).findFirst().get().setTurn(true);
            } catch (NoSuchElementException ex) {
                // This handles the case if all current players already have blackjack, then the hand is over
                evaluateHands();
                return new GameInfoWrapper(game.getPlayers(), game.getDealer(), game.getDealerUpCard(), Round.BETTING_ROUND, null, game.getDeckSize());
            }

            return new GameInfoWrapper(game.getPlayers(), null, game.getDealerUpCard(), Round.HAND_IN_PROGRESS, null, game.getDeckSize());
        }
    }

    /** Private util methods that perform logic operations on the Game object. **/

    private void performSplit(Player player, Hand hand) throws BlackjackException {
        if (canPerformSplit(player, hand)) {
            player.decrementChipCount(hand.getBetAmount());
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
        } else {
            throw new BlackjackException(BlackjackErrorCode.BJ740);
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

    private boolean canPerformSurrender(Hand hand) {
        // Player can only surrender if the hand has two cards in it
        return hand.getCards().size() == 2;
    }

    private void performDealerAction() {
        while (dealerNeedsToHit()) {
            game.getDealerHand().addCard(game.dealCard());
        }
    }

    private boolean dealerNeedsToHit() {
        // Dealer only needs to hit iff hand value is less that 17 and there's at least one player who hasn't busted, surrendered, or has blackjack
        return game.getDealerHand().getHandValue() < 17 && game.getAllHands().stream().filter(h -> h.getHandStatus() == HandStatus.IN_PLAY).count() > 0;
    }

    private void evaluateHands() {
        int dealerHandValue = game.getDealerHand().getHandValue();

        for (Player p : game.getPlayers()) {
            p.getHands().stream().forEach(hand -> {
                Result result = getResult(hand.getHandValue(), dealerHandValue);
                hand.setResult(result);

                if (hand.getHandStatus() == HandStatus.BLACKJACK) {
                    // TODO Decide here if we want to make Blackjack automatic wins. Otherwise, need to implement logic
                    // Award 3:2 payout for blackjack
                    p.incrementChipCount((int)(hand.getBetAmount()*1.5) + hand.getBetAmount());
                } else if (result == Result.WIN) {
                    // Award the player as well as give him the bet back
                    p.incrementChipCount(hand.getBetAmount()*2);
                } else if (result == Result.PUSH) {
                    // Give him back his original bet
                    p.incrementChipCount(hand.getBetAmount());
                }
            });
        }

        // Boot out players who don't have enough chips
    }

    private Result getResult(int playerHandVal, int dealerHandVal) {
        if (playerHandVal > 21) {
            return Result.LOSE;
        } else if (dealerHandVal > 21) {
            return Result.WIN;
        } else {
            return Result.getByValue(Integer.compare(playerHandVal, dealerHandVal));
        }
    }
}
