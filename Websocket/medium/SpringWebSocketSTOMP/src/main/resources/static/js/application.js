/**
 * 원문에서는 stompjs를 사용했지만, 더 이상 지원하지 않아 stompjs를 대신 사용
 *
 * https://github.com/stomp-js/stompjs/tree/develop
 * https://stomp-js.github.io/guide/stompjs/using-stompjs-v5.html
 */
let stompClient = null;
const headers = { ack: 'client' };

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    $("#send").prop("disabled", !connected);

    if (connected) {
        $("#conversation").show();
    } else {
        $("#conversation").hide();
    }

    $('#output').val('');
    $("#responses").html("");
}

function connect() {
    const stompConfig = {
        // WebSocket Server URL
        brokerURL: '/websocket-stomp',

        // when connection dropped try to reconnect after 5000ms
        reconnectDelay: 5000,
        // client will listen to heartbeats from the server every 4000ms
        heartbeatIncoming: 4000,
        // client will send heartbeats every 4000ms
        heartbeatOutgoing: 4000,

        debug: function (str) {
            console.log('STOMP: ' + str)
        },

        onConnect: function (frame) {
            // Do something, all subscribes must be done is this callback
            // This is needed because this will be executed after a (re)connect

            console.log('Client connected: ' + frame);
            setConnected(true);

            stompClient.subscribe('/app/subscribe', function (response) {
                log(response, 'table-success');

                // acknowledge the message by sending the ACK frame
                response.ack();
            }, headers); // to enable client acknowledgment

            stompClient.subscribe('/queue/responses', function (response) {
                log(response, 'table-success');
            });

            stompClient.subscribe('/queue/errors', function (response) {
                log(response, 'table-danger');

                console.log('Client unsubscribes: subscription');
                subscription.unsubscribe();
            });

            stompClient.subscribe('/topic/periodic', function (response) {
                log(response, 'table-info');
            });
        },

        onStompError: function (frame) {
            // Will be invoked in case of error encountered at Broker
            // Bad login/passcode typically will cause an error
            // Complaint brokers will set `message` header with a brief message. Body may contain details.
            // Compliant brokers will terminate the connection after any error
            console.log('Broker reported error: ' + frame.headers['message']);
            console.log('Additional details: ' + frame.body);
            setConnected(false);
        },

        onDisconnect: function (frame) {
            console.log('Client disconnected: ' + frame);
            setConnected(false);
        }

        // SockJS 를 사용할 경우
        // webSocketFactory: function () {
        //     console.log('Using SockJS');
        //     return new SockJS('http://localhost:8080/websocket-sockjs-stomp');
        // }
    }

    // stompjs 사용: webstomp.over -> Stomp.over
    stompClient = new StompJs.Client(stompConfig);

    stompClient.activate();
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.deactivate();
        setConnected(false);
    }
}

function send() {
    const output = $("#output").val();
    console.log("Client sends: " + output);

    // sent message with acknowledge with a transaction
    // https://stomp-js.github.io/guide/stompjs/using-stompjs-v5.html#transactions
    const tx = stompClient.begin();
    stompClient.publish({destination: '/app/request', headers: { transaction: tx.id }, body: output});
    tx.commit();
}

function log(response, clazz) {
    const input = response.body;
    console.log("Client received: " + input);
    $("#responses").append("<tr class='" + clazz + "'><td>" + input + "</td></tr>");
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $("#connect").click(function () {
        connect();
    });
    $("#disconnect").click(function () {
        disconnect();
    });
    $("#send").click(function () {
        send();
    });
});
