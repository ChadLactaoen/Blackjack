var stompClient = null;
var playerId = null;

function setConnected(connected) {
    document.getElementById('connect').disabled = connected;
    document.getElementById('disconnect').disabled = !connected;
    document.getElementById('conversationDiv').style.visibility = connected ? 'visible' : 'hidden';
    document.getElementById('bettingCenter').style.visibility = connected ? 'visible' : 'hidden';
    document.getElementById('actionButtons').style.visibility = connected ? 'visible' : 'hidden';
    document.getElementById('response').innerHTML = '';
}

function connect() {
    var socket = new SockJS('/hello');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function(frame) {
        setConnected(true);
        console.log('Connected: ' + frame);

        // Handling when user info is sent to me
        stompClient.subscribe('/user/topic/player', function(message) {
            var obj = JSON.parse(message.body);
            playerId = obj.playerId;

            // If it's not null, display my information
            if (obj != null) {
                displayPlayerInfo(obj);
            }
        });

        // Handling game info when it's send to me
        stompClient.subscribe('/topic/game', function(message) {
            var gameObj = JSON.parse(message.body);

            populateDealerHand(gameObj);
            populatePlayerHands(gameObj);
        });
    });
}

function disconnect() {
    if (stompClient != null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

function sendName() {
    var name = document.getElementById('name').value;
    stompClient.send("/app/register", {}, JSON.stringify({ 'name': name }));
}

function displayPlayerInfo(obj) {
    var div = $('.player-name[seat-num=' + obj.seatNum + ']');

    $('.seat-label[seat-num=' + obj.seatNum + ']').html('<h3>' + obj.seatNum + '</h3>');
    div.html('<h3>' + obj.name + ' - $' + obj.chips + '</h3>');

    // Remove old data from this div
    while (div.firstChild) {
        div.removeChild(div.firstChild);
    }
}

function placeBet() {
    var betAmount = document.getElementById('bet').value;
    // document.getElementById('bet').value = null;

    var obj = {};
    obj["playerId"] = playerId;
    obj["betAmount"] = parseInt(betAmount);

    stompClient.send("/app/bet", {}, JSON.stringify({ 'playerId': playerId, 'betAmount': parseInt(betAmount)}));
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

    dealerUI.empty();
    dealerHandValue.empty();

    if (isActionDone) {
        for (var i = 0; i < gameObj.dealer.hands[0].cards.length; i++) {
            dealerUI.append(prettifyCard(gameObj.dealer.hands[0].cards[i]));
        }
        dealerHandValue.append('<p>(' + gameObj.dealer.hands[0].handValue + ')</p>')
    } else {
        dealerUI.append(prettifyCard(gameObj.dealerUpCard));
        dealerHandValue.append('<p>(' + gameObj.dealerUpCard.cardValue + ')</p>');
    }

}

function populatePlayerHands(gameObj) {
    var players = gameObj.players;
    var turnSeatNum = null;
    var turnHandNum = null;

    $('.hand h3').empty();
    $('.result-row div').empty();
    $('.active').removeClass('active');

    for (var i = 0; i < players.length; i++) {
        $('.seat-label[seat-num=' + players[i].seatNum + ']').html('<h3>' + players[i].seatNum + '</h3>');
        $('.player-name[seat-num=' + (i+1) + ']').html('<h3>' + players[i].name + ' - $' + players[i].chips + '</h3>');
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

            console.log(players[i].hands[j].result !== null);

            // Fill in the result row if the hand is over
            if (players[i].hands[j].result !== null) {
                // Check for blackjack status or surrender
                if (players[i].hands[j].handStatus == 'BLACKJACK') {
                    $('.result-row div[hand-num=' + (j+1) + '][seat-num=' + (i+1) + ']').html('<p class="text-center">BLACKJACK</p>');
                } else if (players[i].hands[j].handStatus == 'SURRENDER') {
                    $('.result-row div[hand-num=' + (j+1) + '][seat-num=' + (i+1) + ']').html('<p class="text-center">SURRENDER</p>');
                } else {
                    $('.result-row div[hand-num=' + (j+1) + '][seat-num=' + (i+1) + ']').html('<p class="text-center">' + players[i].hands[j].result + '</p>');
                }
            }
        }
    }

    if (turnSeatNum && turnHandNum) {
        $('.hand-row div[seat-num=' + turnSeatNum + '][hand-num=' + turnHandNum + ']').addClass('active');
    }
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

function handleHit() {
    stompClient.send("/app/action", {}, JSON.stringify(createActionObject("HIT")));
}

function handleStand() {
    stompClient.send("/app/action", {}, JSON.stringify(createActionObject("STAND")));
}

function handleDouble() {
    stompClient.send("/app/action", {}, JSON.stringify(createActionObject("DOUBLE")));
}

function handleSplit() {
    stompClient.send("/app/action", {}, JSON.stringify(createActionObject("SPLIT")));
}

function handleSurrender() {
    stompClient.send("/app/action", {}, JSON.stringify(createActionObject("SURRENDER")));
}