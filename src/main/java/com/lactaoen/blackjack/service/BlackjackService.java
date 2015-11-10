package com.lactaoen.blackjack.service;

import com.lactaoen.blackjack.exception.BlackjackErrorCode;
import com.lactaoen.blackjack.exception.BlackjackException;
import com.lactaoen.blackjack.model.*;
import com.lactaoen.blackjack.model.wrapper.GameInfoWrapper;
import com.lactaoen.blackjack.model.wrapper.PlayerHandWrapper;
import com.lactaoen.blackjack.model.wrapper.PlayerListWrapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class BlackjackService {

    private Game game;

    public BlackjackService() {
        game = new Game();
    }

    /**
     * Get all players.
     *
     * @return A List of all players currently registered to the game.
     */
    public List<Player> getPlayers() {
        return game.getPlayers();
    }

    public PlayerListWrapper getPlayersForTrebek() {
        List<PlayerInfo> playerList = new ArrayList<>();
        for (Player p : game.getPlayers()) {
            playerList.add(new PlayerInfo().transpose(p));
        }
        return new PlayerListWrapper(playerList);
    }

    /**
     * Places a bet for the upcoming hand.
     *
     * @param playerId The secret key of the Player.
     * @param betAmount The amount to deduct from the Player's chip count and add to the next hand.
     * @return The status of the Game.
     * @throws BlackjackException Thrown if the bet can't be placed for any reason.
     */
    public GameInfoWrapper placeBet(String playerId, int betAmount) throws BlackjackException, InterruptedException {
        // Player can't place a bet if the action on the current hand is not finished
        if (!game.isActionDone()) throw new BlackjackException(BlackjackErrorCode.BJ105);

        // Hacky. Trebek will call this only to redraw the UI after he's kicked someone out of the game
        if (playerId.equals("Hello, my name is Inigo Montoya.")) {
            return new GameInfoWrapper(game.getPlayers(), game.getDealer(), null, Round.BETTING_ROUND, null, game.getDeck().getDeckSize());
        }

        // Validate bet is increment of 10
        if (betAmount < 10 || betAmount % 10 != 0) throw new BlackjackException(BlackjackErrorCode.BJ110);

        Optional<Player> player = game.getPlayers().stream().filter(p -> p.getPlayerId().equals(playerId)).findFirst();

        if (player.isPresent()) {
            // Player can't place bet if he's inactive
            if (!player.get().isActive()) throw new BlackjackException(BlackjackErrorCode.BJ570);

            // Make sure player hasn't already placed a bet for this current hand
            if (player.get().getHands().stream().filter(h -> h.getResult() == null).count() > 0 || player.get().getNextBet() != null) {
                throw new BlackjackException(BlackjackErrorCode.BJ102);
            }

            // Validate the player has enough to make the desired bet amount
            if (player.get().getChips() - betAmount < 0) throw new BlackjackException(BlackjackErrorCode.BJ101);

            player.get().setNextBet(betAmount);
        } else {
            throw new BlackjackException(BlackjackErrorCode.BJ550);
        }

        // If all players have placed their bet for this hand and we're on auto-pilot, then deal out the cards
        if (game.isBettingRoundDone() && game.getDealer().isAuto()) {
            // Sleep so that we can see the result from the last hand
            Thread.sleep(1500);
            game.getPlayers().stream().forEach(Player::removeOldHands);
            game.getPlayers().stream().filter(Player::isActive).forEach(Player::moveBetToNewHand);
            game.getDealer().getHands().clear();
            game.getDealer().addHand(new Hand());
            return startNewHand();
        }

        return new GameInfoWrapper(game.getPlayers(), game.getDealer(), null, Round.BETTING_ROUND, null, game.getDeck().getDeckSize());
    }

    /**
     * Attempts to add a new player to the game.
     *
     * @param name The name of the player.
     * @return The Player object created after registering.
     * @throws BlackjackException Thrown if there is no more room for another player in the Game.
     */
    public Player registerPlayer(String name) throws BlackjackException {
        if (game.getPlayers().size() < 4) {
            return game.addPlayer(name);
        }

        throw new BlackjackException(BlackjackErrorCode.BJ500);
    }

    /**
     * Unregisters a player from the game.
     *
     * @param playerId The secret key of the Player wanting to be unregistered from the Game.
     * @return The Player object who will be unregistered.
     * @throws BlackjackException Thrown if the player id is not found.
     */
    public PlayerInfo unregisterPlayer(String playerId) throws BlackjackException {
        if (!game.isActionDone()) {
            // If the player tries to unregister during a hand, allow them to do it, but set the player to inactive
            // We can kick them out later
            game.getPlayers().stream().filter(p -> p.getPlayerId().equals(playerId)).findFirst().ifPresent(pl -> pl.setActive(false));
            throw new BlackjackException(BlackjackErrorCode.BJ930);
        }

        Player player = null;
        List<Player> players = game.getPlayers();
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getPlayerId().equals(playerId)) {
                player = players.get(i);
                players.remove(i);
                break;
            }
        }

        // Move people to first available seat as necessary (i.e., if seat 1 leaves, player in seat 2 now moves to seat 1)
        for (int i = 0; i < players.size(); i++) {
            players.get(i).setSeatNum(i+1);
        }

        if (player == null) {
            throw new BlackjackException(BlackjackErrorCode.BJ920);
        }
        return new PlayerInfo().transpose(player);
    }

    /**
     * Processes a Player's action on his current hand.
     *
     * @param playerId The secret key of the Player.
     * @param handNum The hand number (0-indexed) in the Player's list of hands on which the action will be applied.
     * @param action The action to be executed.
     * @return The status of the Game after the action is executed.
     * @throws BlackjackException Thrown if the action can't be processed for any reason.
     */
    public GameInfoWrapper processAction(String playerId, int handNum, Action action) throws BlackjackException {
        // Check that we are not in the betting round
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
                                performDouble(player.get(), hand);
                            } else {
                                throw new BlackjackException(BlackjackErrorCode.BJ720);
                            }
                            break;
                        case SURRENDER:
                            if (canPerformSurrender(hand)) {
                                performSurrender(player.get(), hand);
                            } else {
                                throw new BlackjackException(BlackjackErrorCode.BJ730);
                            }
                            break;
                        case SPLIT:
                            if (canPerformSplit(player.get(), hand)) {
                                performSplit(player.get(), hand);
                            } else {
                                throw new BlackjackException(BlackjackErrorCode.BJ740);
                            }
                            break;
                    }
                } else {
                    // Thrown when the player is acting out of turn
                    throw new BlackjackException(BlackjackErrorCode.BJ799);
                }
            } catch (IndexOutOfBoundsException ex) {
                // Hand number param was not a valid hand number
                throw new BlackjackException(BlackjackErrorCode.BJ701);
            }
        } else {
            // Could not find player for the given player id
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

    // Used by the Trebek admin panel. Not for public use.
    public GameInfoWrapper manualHandStart() throws BlackjackException {
        if (!game.isBettingRoundDone()) throw new BlackjackException(BlackjackErrorCode.BJ914);
        if (game.getDealer().isAuto()) throw new BlackjackException(BlackjackErrorCode.BJ915);

        game.getPlayers().stream().forEach(Player::removeOldHands);
        game.getPlayers().stream().filter(Player::isActive).forEach(Player::moveBetToNewHand);
        game.getDealer().getHands().clear();
        game.getDealer().addHand(new Hand());
        return startNewHand();
    }

    public GameInfoWrapper getCurrentGameInfo() {
        boolean isInProgress = !game.isActionDone();
        return new GameInfoWrapper(game.getPlayers(), isInProgress ? null: game.getDealer(), game.getDealerUpCard(),
                isInProgress ? Round.HAND_IN_PROGRESS : Round.BETTING_ROUND, null, game.getDeckSize());
    }

    public boolean toggleAutoPilot() {
        game.getDealer().toggleAuto();
        return game.getDealer().isAuto();
    }

    public void togglePlayerActive(String playerId) throws BlackjackException {
        Optional<Player> player = game.getPlayers().stream().filter(p -> p.getPlayerId().equals(playerId)).findFirst();

        if (player.isPresent()) {
            // UI will only get redrawn on the next event message.
            player.get().toggleIsActive();
        } else {
            throw new BlackjackException(BlackjackErrorCode.BJ921);
        }
    }

    public PlayerHandWrapper getActivePlayerHandNum() throws BlackjackException {
        for (Player p : game.getPlayers()) {
            for (int i = 0; i < p.getHands().size(); i++) {
                if (p.getHands().get(i).isTurn()) {
                    return new PlayerHandWrapper(p.getPlayerId(), i);
                }
            }
        }
        throw new BlackjackException(BlackjackErrorCode.BJ940);
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

    private Result compareHandVals(int playerHandVal, int dealerHandVal) {
        if (playerHandVal > 21) {
            return Result.LOSE;
        } else if (dealerHandVal > 21) {
            return Result.WIN;
        } else {
            return Result.getByValue(Integer.compare(playerHandVal, dealerHandVal));
        }
    }

    private void evaluateHands() {
        // Evaluates all hands relative to the dealer
        int dealerHandValue = game.getDealerHand().getHandValue();

        for (Player p : game.getPlayers()) {
            p.getHands().stream().forEach(hand -> {
                Result result = compareHandVals(hand.getHandValue(), dealerHandValue);
                hand.setResult(result);

                if (hand.getHandStatus() == HandStatus.BLACKJACK) {
                    // Award 3:2 payout for blackjack
                    p.incrementChipCount((int)((hand.getBetAmount()*1.5) + hand.getBetAmount()));
                } else if (result == Result.WIN) {
                    // Award the player as well as give him the bet back
                    p.incrementChipCount(hand.getBetAmount()*2);
                } else if (result == Result.PUSH) {
                    // Give him back his original bet
                    p.incrementChipCount(hand.getBetAmount());
                }
            });
        }

        // Set players to inactive if they no longer have enough chips to play
        game.getPlayers().stream().filter(p -> p.getChips() < 10).forEach(p -> p.setActive(false));
    }

    private boolean isDealerNeedToHit() {
        // Dealer only needs to hit iff hand value is less that 17 and there's at least one player who hasn't busted, surrendered, or has blackjack
        return game.getDealerHand().getHandValue() < 17 && game.getAllHands().stream().filter(h -> h.getHandStatus() == HandStatus.IN_PLAY).count() > 0;
    }

    private void performDealerAction() {
        while (isDealerNeedToHit()) {
            game.getDealerHand().addCard(game.dealCard());
        }
    }

    private void performDouble(Player player, Hand hand) {
        player.decrementChipCount(hand.getBetAmount());
        hand.setBetAmount(hand.getBetAmount()*2);
        hand.addCard(game.dealCard());
        game.switchCurrentHandOffAndTurnNextHandOn();
    }

    private void performSplit(Player player, Hand hand) {
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

    }

    private void performSurrender(Player player, Hand hand) {
        player.incrementChipCount(hand.getBetAmount() / 2);
        hand.setHandStatus(HandStatus.SURRENDER);
        game.switchCurrentHandOffAndTurnNextHandOn();
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
}
