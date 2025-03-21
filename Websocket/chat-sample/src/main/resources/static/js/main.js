'use strict';

const usernamePage = document.querySelector('#username-page');
const chatPage = document.querySelector('#chat-page');
const usernameForm = document.querySelector('#usernameForm');
const messageForm = document.querySelector('#messageForm');
const messageInput = document.querySelector('#message');
const chatArea = document.querySelector('#chat-messages');
const logout = document.querySelector('#logout');

let stompClient = null;
let selectedRoomId = null;
let accessToken = null;
let auth = null;
let chatRooms = [];

// 쿠키에서 특정 이름의 값을 가져오는 헬퍼 함수
function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
}

// 로그인 및 STOMP 연결
function login(event) {
    const email = document.querySelector('#email').value.trim();
    const password = document.querySelector('#password').value.trim();

    if (email && password) {
        // 로그인 API 호출 (/users/login 엔드포인트로 자격증명 전송)
        fetch('/users/login', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({email: email, password: password})
        })
            .then(response => {
                if (response.ok) return response.json();
                else throw new Error('Login failed');
            })
            .then(data => {
                accessToken = data.accessToken;
                // accessToken을 cookie에 저장
                document.cookie = "accessToken=" + accessToken + "; path=/";
                // 추가: user 값을 cookie에 저장 (JSON 문자열 형태로)
                document.cookie = "user=" + encodeURIComponent(JSON.stringify(data.user)) + "; path=/";
                findAndDisplayChatRooms().then(() => {
                    console.log("Chat rooms loaded");
                    initChat();
                });
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
    showChatPage();

    const headers = {
        Authorization: 'Bearer ' + accessToken
    };

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

        connectHeaders: headers,

        onConnect: function (frame) {
            // Do something, all subscribes must be done is this callback
            // This is needed because this will be executed after a (re)connect

            console.log('Client connected: ' + frame);
            setConnected(true);

            stompClient.subscribe('/topic/periodic', function (response) {
                const data = JSON.parse(response.body);
                console.log(`periodic message : ${response.body}`);
                // displayMessage('System', data.message);
            });

            console.log(chatRooms)
            chatRooms.forEach(
                room => {
                    stompClient.subscribe(`/topic/chat/${room.id}`, function (response) {
                        onMessageReceived(response);
                    });
                }
            );
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

// 채팅방 목록 가져오기 (/rooms 엔드포인트)
async function findAndDisplayChatRooms() {
    const headers = {};
    const token = getCookie("accessToken");
    if (token) {
        headers['Authorization'] = 'Bearer ' + token;
    }
    const response = await fetch('/rooms', {headers});

    // 403 응답인 경우, 쿠키에서 accessToken 삭제 후 로그인 화면 표시
    if (response.status === 403) {
        deleteCookie();
        // 로그인 페이지 보이기, 채팅 페이지 숨기기
        showLoginPage();
        return;
    }

    showChatPage();

    // 응답 JSON이 { chatRooms: [...] } 형태이므로 chatRooms 프로퍼티를 추출합니다.
    const data = await response.json();
    chatRooms = data.chatRooms;

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
    if (token) {
        headers['Authorization'] = 'Bearer ' + token;
    }
    const response = await fetch(`/chat/${selectedRoomId}/history`, {headers});
    const roomChat = await response.json();
    chatArea.innerHTML = '';
    roomChat.content.forEach(chat => {
        displayMessage(chat.sender, chat.message, chat.timestamp);
    });
    chatArea.scrollTop = chatArea.scrollHeight;
}

// 채팅 메시지 DOM 추가
function displayMessage(sender, content, timestamp) {
    const messageContainer = document.createElement('div');
    messageContainer.classList.add('message');

    // 현재 사용자인지 여부에 따라 스타일 지정
    if (sender === auth.name) {
        messageContainer.classList.add('sender');
    } else {
        messageContainer.classList.add('receiver');
    }

    // 발신자 요소 (예: 상단에 표시)
    const senderElement = document.createElement('span');
    senderElement.classList.add('message-sender');
    senderElement.textContent = sender;

    // 메시지 내용 요소
    const contentElement = document.createElement('p');
    contentElement.classList.add('message-content');
    contentElement.textContent = content;

    // 채팅 시간 요소 (timestamp가 있으면 파싱, 없으면 현재 시간 사용)
    const timeElement = document.createElement('span');
    timeElement.classList.add('message-timestamp');
    const time = timestamp ? new Date(timestamp) : new Date();
    // 원하는 포맷으로 시간 표시 (여기서는 간단히 toLocaleTimeString() 사용)
    timeElement.textContent = time.toLocaleTimeString();

    // 메시지 컨테이너에 발신자, 내용, 시간 순서로 추가
    messageContainer.appendChild(senderElement);
    messageContainer.appendChild(contentElement);
    messageContainer.appendChild(timeElement);

    const threshold = 10; // 10px 이내면 최하단으로 간주
    const isAtBottom = chatArea.scrollTop >= (chatArea.scrollHeight - chatArea.clientHeight - threshold);

    chatArea.appendChild(messageContainer);
    // 자동 스크롤 조건 확인
    const isOverflowing = chatArea.scrollHeight > chatArea.clientHeight;

    if (isOverflowing && (chatArea.scrollTop === 0 || isAtBottom)) {
        chatArea.scrollTop = chatArea.scrollHeight;
    }
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
            type: "SEND",
            content: messageContent,
        };

        // sent message with acknowledge with a transaction
        // https://stomp-js.github.io/guide/stompjs/using-stompjs-v5.html#transactions
        const tx = stompClient.begin();
        stompClient.publish(
            {
                destination: `/app/chat/${selectedRoomId}`,
                headers: {transaction: tx.id, 'Authorization' : 'Bearer ' + accessToken},
                body: JSON.stringify(chatMessage)
            }
        );
        tx.commit();

        messageInput.value = '';
        displayMessage(auth.name, messageContent);
    }
    chatArea.scrollTop = chatArea.scrollHeight;
    event.preventDefault();
}

function setConnected(connected) {
    document.getElementById("connect").disabled = connected;
    document.getElementById("disconnect").disabled = !connected;

    document.getElementById('message').value = '';
}

// 로그아웃 처리
function onLogout() {
    // 쿠키 삭제 (만료일을 과거로 설정)
    deleteCookie();
    clearData();
    window.location.reload();
}

function showLoginPage() {
    document.getElementById("login-page").classList.remove("hidden");
    document.getElementById("chat-page").classList.add("hidden");
    document.getElementById("messageForm").classList.add("hidden");

    auth = null;
}

function showChatPage() {
    document.getElementById("login-page").classList.add("hidden");
    document.getElementById("chat-page").classList.remove("hidden");
    document.getElementById("messageForm").classList.remove("hidden");

    auth = JSON.parse(decodeURIComponent(getCookie('user')))
    document.querySelector('#connected-user-username').textContent = auth.name;
}

function deleteCookie() {
    // 쿠키 삭제 (만료일을 과거로 설정)
    document.cookie = "accessToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/; user=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/";
}

function clearData() {
    chatRooms = [];
    selectedRoomId = null;
    chatArea.innerHTML = '';
}

usernameForm.addEventListener('submit', login, true);
messageForm.addEventListener('submit', sendMessage, true);
logout.addEventListener('click', onLogout, true);
window.onbeforeunload = () => onLogout();
