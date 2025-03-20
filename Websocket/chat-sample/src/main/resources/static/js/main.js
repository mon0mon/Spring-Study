'use strict';

const usernamePage = document.querySelector('#username-page');
const chatPage = document.querySelector('#chat-page');
const usernameForm = document.querySelector('#usernameForm');
const messageForm = document.querySelector('#messageForm');
const messageInput = document.querySelector('#message');
const chatArea = document.querySelector('#chat-messages');
const logout = document.querySelector('#logout');

let stompClient = null;
let email = null;
let selectedRoomId = null;
let accessToken = null;

// 쿠키에서 특정 이름의 값을 가져오는 헬퍼 함수
function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
}

// 로그인 및 STOMP 연결
function login(event) {
    email = document.querySelector('#email').value.trim();
    const password = document.querySelector('#password').value.trim();

    if (email && password) {
        // 로그인 API 호출 (/users/login 엔드포인트로 자격증명 전송)
        fetch('/users/login', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({email: email, password: password})
        })
            .then(response => {
                if(response.ok) return response.json();
                else throw new Error('Login failed');
            })
            .then(data => {
                accessToken = data.accessToken;
                // accessToken을 cookie에 저장
                document.cookie = "accessToken=" + accessToken + "; path=/";
                findAndDisplayChatRooms().then(r => console.log("Chat rooms loaded"));
                initChat();
            })
            .catch(error => {
                console.error('Login error:', error);
                // 로그인 실패시 fallback으로 token 없이 진행
                initChat();
            });
    }
    event.preventDefault();
}

// 채팅 초기화 및 STOMP 연결
function initChat() {
    usernamePage.classList.add('hidden');
    chatPage.classList.remove('hidden');

    const stompConfig = {
        // WebSocket Server URL
        brokerURL: '/ws/stomp',

        // when connection dropped try to reconnect after 5000ms
        reconnectDelay: 5000,
        // client will listen to heartbeats from the server every 4000ms
        heartbeatIncoming: 4000,
        // client will send heartbeats every 4000ms
        heartbeatOutgoing: 4000,

        debug: function (str) {
            console.log('STOMP: ' + str)
        },

        connectHeaders: {
            Authorization: 'Bearer ' + accessToken
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

// STOMP 연결 성공 후 처리
function onConnected() {
    // 유저 전용 채널 구독
    stompClient.subscribe(`/user/${email}/queue/messages`, onMessageReceived);
    // 공용 채널 구독 (필요시)
    stompClient.subscribe(`/user/public`, onMessageReceived);

    // 서버에 연결 유저 등록
    stompClient.send("/app/user.addUser",
        {},
        JSON.stringify({username: email, status: 'ONLINE'})
    );
    document.querySelector('#connected-user-username').textContent = email;
}

// 채팅방 목록 가져오기 (/rooms 엔드포인트)
async function findAndDisplayChatRooms() {
    const headers = {};
    const token = getCookie("accessToken");
    if (token) {
        headers['Authorization'] = 'Bearer ' + token;
    }
    const response = await fetch('/rooms', { headers });

    // 403 응답인 경우, 쿠키에서 accessToken 삭제 후 로그인 화면 표시
    if (response.status === 403) {
        // 쿠키 삭제 (만료일을 과거로 설정)
        document.cookie = "accessToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/";
        // 로그인 페이지 보이기, 채팅 페이지 숨기기
        showLoginPage();
        return;
    }

    showChatPage();
    const chatRooms = await response.json();
    const connectedUsersList = document.getElementById('connectedUsers');
    connectedUsersList.innerHTML = '';

    chatRooms.forEach(room => {
        appendRoomElement(room, connectedUsersList);
    });
}

// 채팅방 목록에 각 방을 추가
function appendRoomElement(room, listElement) {
    const listItem = document.createElement('li');
    listItem.classList.add('list-group-item', 'list-group-item-action');
    listItem.setAttribute('data-room-id', room.id);
    listItem.textContent = room.name;
    listItem.addEventListener('click', roomItemClick);
    listElement.appendChild(listItem);
}

// 채팅방 선택 시 처리
function roomItemClick(event) {
    // 이전에 활성화된 항목 해제
    document.querySelectorAll('#connectedUsers li').forEach(item => {
        item.classList.remove('active');
    });
    const clickedRoom = event.currentTarget;
    clickedRoom.classList.add('active');
    selectedRoomId = clickedRoom.getAttribute('data-room-id');
    messageForm.classList.remove('hidden');
    fetchAndDisplayRoomChat();
}

// 선택된 채팅방의 메시지 내역 불러오기
async function fetchAndDisplayRoomChat() {
    const headers = {};
    const token = getCookie("accessToken");
    if(token) {
        headers['Authorization'] = 'Bearer ' + token;
    }
    const response = await fetch(`/rooms/${selectedRoomId}/messages`, {headers});
    const roomChat = await response.json();
    chatArea.innerHTML = '';
    roomChat.forEach(chat => {
        displayMessage(chat.sender, chat.content);
    });
    chatArea.scrollTop = chatArea.scrollHeight;
}

// 채팅 메시지 DOM 추가
function displayMessage(sender, content) {
    const messageContainer = document.createElement('div');
    messageContainer.classList.add('message');
    if (sender === email) {
        messageContainer.classList.add('sender');
    } else {
        messageContainer.classList.add('receiver');
    }
    const messageText = document.createElement('p');
    messageText.textContent = content;
    messageContainer.appendChild(messageText);
    chatArea.appendChild(messageContainer);
}

// STOMP 메시지 수신 처리
function onMessageReceived(payload) {
    console.log('Message received', payload);
    const message = JSON.parse(payload.body);
    // 현재 선택된 채팅방의 메시지라면 표시
    if (selectedRoomId && message.roomId === selectedRoomId) {
        displayMessage(message.sender, message.content);
        chatArea.scrollTop = chatArea.scrollHeight;
    }
}

// 메시지 전송
function sendMessage(event) {
    const messageContent = messageInput.value.trim();
    if (messageContent && stompClient && selectedRoomId) {
        const chatMessage = {
            sender: email,
            roomId: selectedRoomId,
            content: messageContent,
            timestamp: new Date()
        };
        stompClient.send("/app/chat", {}, JSON.stringify(chatMessage));
        displayMessage(email, messageContent);
        messageInput.value = '';
    }
    chatArea.scrollTop = chatArea.scrollHeight;
    event.preventDefault();
}

// STOMP 연결 에러 처리
function onError() {
    console.error('Could not connect to WebSocket server. Please refresh this page to try again!');
}

// 로그아웃 처리
function onLogout() {
    stompClient.send("/app/user.disconnectUser",
        {},
        JSON.stringify({username: email, status: 'OFFLINE'})
    );
    window.location.reload();
}

function showLoginPage() {
    document.getElementById("login-page").classList.remove("hidden");
    document.getElementById("chat-page").classList.add("hidden");
}

function showChatPage() {
    document.getElementById("login-page").classList.add("hidden");
    document.getElementById("chat-page").classList.remove("hidden");
}

usernameForm.addEventListener('submit', login, true);
messageForm.addEventListener('submit', sendMessage, true);
logout.addEventListener('click', onLogout, true);
window.onbeforeunload = () => onLogout();
