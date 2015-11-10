var stompClient = null;
var playerId = null;

function setConnected(connected) {
    document.getElementById('connect').disabled = connected;
    document.getElementById('disconnect').disabled = !connected;
}

function connect() {
    var socket = new SockJS('/connect');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function(frame) {
        setConnected(true);
        console.log('Connected: ' + frame);

        // Handling when user info is sent to me
        stompClient.subscribe('/user/queue/player', function(message) {
            var obj = JSON.parse(message.body);
            playerId = obj.playerId;

            // If it's not null, get the rest of the players
            if (obj != null) {
                stompClient.send('/app/players', {}, {});
            }
        });

        // Handles calling getting players. Should only happen twice, max
        stompClient.subscribe('/user/queue/players', function(message) {
            populateUI(message.body);
        });

        // Handling game info when it's send to me
        stompClient.subscribe('/topic/game', function(message) {
            populateUI(message.body);
        });

        // Handling errors
        stompClient.subscribe('/user/queue/errors', function(message) {
            var error = JSON.parse(message.body);
            $('#alerts').html('<div class="alert alert-danger alert-dismissible" role="alert">'
                + '<button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>'
                + '<strong>Error Code ' + error.errorCode + ': </strong>' + error.message
                + '</div>');
        });
    });
    stompClient.send('/app/players', {}, {});
}

function disconnect() {
    if (stompClient != null) {
        if (playerId != null) {
            stompClient.send("/app/unregister", {}, JSON.stringify({ 'playerId': playerId}));
        }
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

function populateUI(gameObj) {
    gameObj = JSON.parse(gameObj);
    var handStatus = gameObj.gameStatus == 'BETTING_ROUND' ? 'Betting Round' : 'Hand in Progress';
    var extraClass = gameObj.gameStatus == 'BETTING_ROUND' ? 'text-primary' : 'text-warning';

    clearUI();
    populateDealerHand(gameObj);
    populatePlayerHands(gameObj);

    $('#hand-status').html('<h4 class="text-right ' + extraClass + '">' + handStatus + '</h4>');
    $('#deck-card-count').html('<p class="text-right">Cards Left in Deck: ' + gameObj.cardsLeftInDeck + '</p>');
}

function getSuitSymbol(suit) {
    switch(suit) {
        case 'SPADES':
            return '♠';
        case 'HEARTS':
            return '♥';
        case 'DIAMONDS':
            return '♦';
        case 'CLUBS':
            return '♣';
    }
}

function populateDealerHand(gameObj) {
    // Populate the dealer hand UI
    var isActionDone = gameObj.dealer !== null;
    var dealerUI = $('#dealer-hand-display');
    var dealerHandValue = $('#dealer-hand-value');

    if (isActionDone) {
        // Only time dealer won't have a hand and gets here is on first player registration and no hand has been played
        if (gameObj.dealer.hands.length > 0) {
            for (var i = 0; i < gameObj.dealer.hands[0].cards.length; i++) {
                dealerUI.append(prettifyCard(gameObj.dealer.hands[0].cards[i]));
            }
            dealerHandValue.append('<p>(' + gameObj.dealer.hands[0].handValue + ')</p>')
        }
    } else {
        dealerUI.append(prettifyCard(gameObj.dealerUpCard));
        dealerHandValue.append('<p>(' + gameObj.dealerUpCard.cardValue + ')</p>');
    }

}

function populatePlayerHands(gameObj) {
    var players = gameObj.players;
    var turnSeatNum = null;
    var turnHandNum = null;

    for (var i = 0; i < players.length; i++) {
        // If it's the betting round, add a checkmark next to the seat num to indicate they've put their next bet in
        if (gameObj.gameStatus == 'BETTING_ROUND' && players[i].betInForNextRound) {
            $('.seat-label[seat-num=' + players[i].seatNum + ']').html('<h3>' + players[i].seatNum + '</h3><div class="glyphicon glyphicon-ok-circle text-success"></div>');
        } else {
            $('.seat-label[seat-num=' + players[i].seatNum + ']').html('<h3>' + players[i].seatNum + '</h3>');
        }
        $('.player-name[seat-num=' + (i+1) + ']').html('<h3>' + players[i].name + ' - $' + players[i].chips + '</h3>');
        $('.hand-count[seat-num=' + (i+1) + ']').html('<p>Hands Played: ' + players[i].handsPlayed + '</p>');
        var isActive = players[i].active;
        $('.active-status[seat-num=' + (i+1) + ']').html('<p>Player Status: '
            + (isActive ? '<span class="text-success"><strong>Active</strong></span>' : '<span class="text-danger"><strong>Inactive</strong></span>') + '</p>');

        if (players[i].hands.length > 0) {
            for (var j = 0; j < players[i].hands.length; j++) {
                if (players[i].hands[j].turn) {
                    turnSeatNum = i + 1;
                    turnHandNum = j + 1;
                }

                for (var k = 0; k < players[i].hands[j].cards.length; k++) {
                    var card = players[i].hands[j].cards[k];
                    $('.hand-row div[hand-num=' + (j+1) + '][seat-num=' + (i+1) + '] h3').append(prettifyCard(card));
                }
                $('.value-row div[hand-num=' + (j+1) + '][seat-num=' + (i+1) + ']').html('<p class="text-center">(' + players[i].hands[j].handValue + ')</p>');
                $('.bet-row div[hand-num=' + (j+1) + '][seat-num=' + (i+1) + ']').html('<p class="text-center">$' + players[i].hands[j].betAmount + '</p>');

                // Fill in the result row if the hand is over
                if (players[i].hands[j].result !== null) {
                    // Check for blackjack status or surrender
                    if (players[i].hands[j].handStatus == 'BLACKJACK') {
                        $('.result-row div[hand-num=' + (j+1) + '][seat-num=' + (i+1) + ']').html('<p class="text-center text-success"><strong>BLACKJACK</strong></p>');
                    } else if (players[i].hands[j].handStatus == 'SURRENDER') {
                        $('.result-row div[hand-num=' + (j+1) + '][seat-num=' + (i+1) + ']').html('<p class="text-center text-warning"><strong>SURRENDER</strong></p>');
                    } else {
                        var result = players[i].hands[j].result;
                        var extraClass = result == 'LOSE' || result == 'WIN' ? (result == 'WIN' ? 'text-success' : 'text-danger') : '';
                        $('.result-row div[hand-num=' + (j+1) + '][seat-num=' + (i+1) + ']').html('<p class="text-center '
                            + extraClass + '"><strong>' + result + '<strong></strong></p>');
                    }
                }
            }
        }
    }

    if (turnSeatNum && turnHandNum) {
        $('.hand-row div[seat-num=' + turnSeatNum + '][hand-num=' + turnHandNum + ']').addClass('active');
    }
}

function clearUI() {
    $('#dealer-hand-display').empty();
    $('#dealer-hand-value').empty();
    $('.hand h3').empty();
    $('.hand p').empty();
    $('.result-row div').empty();
    $('.seat-label').empty();
    $('.hand-count').empty();
    $('.player-name').empty();
    $('.active-status').empty();
    $('#hand-status').empty();
    $('#deck-card-count').empty();
    $('.active').removeClass('active');
}

function prettifyCard(card) {
    if (card.suit === "HEARTS" || card.suit === "DIAMONDS") {
        return '<span class="text-danger">' + card.alias + '' + getSuitSymbol(card.suit) + '</span>';
    } else {
        return '<span>' + card.alias + '' + getSuitSymbol(card.suit) + '</span>'
    }
}

function createActionObject(action) {
    var obj = {};
    obj["playerId"] = playerId;
    obj["action"] = action;
    obj["handNum"] = parseInt($('.active').attr('hand-num')) - 1;
    return obj;
}