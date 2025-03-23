'use strict';

// DOM 요소
const loginPage = document.getElementById('login-page');
const chatPage = document.getElementById('chat-page');
const usernameForm = document.getElementById('usernameForm');
const messageForm = document.getElementById('messageForm');
const messageInput = document.getElementById('message');
const chatArea = document.getElementById('chat-messages');
const logoutBtn = document.getElementById('logout');
const connectBtn = document.getElementById('connect');
const disconnectBtn = document.getElementById('disconnect');
const currentRoomElement = document.getElementById('current-room');
const currentRoomParticipantCount = document.getElementById('current-room-participants-count');

// 상태 변수
let stompClient = null;
let selectedRoomId = null;
let accessToken = null;
let auth = null;
let chatRooms = [];
let isConnected = false;

// 쿠키에서 특정 이름의 값을 가져오는 헬퍼 함수
function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
}

// 로그인 및 STOMP 연결
function login(event) {
    event.preventDefault();
    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value.trim();

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
            });
    }
}

// 채팅방 목록 표시
function displayChatRooms(rooms) {
    const connectedUsersList = document.getElementById('connectedUsers');
    connectedUsersList.innerHTML = '';

    rooms.forEach(room => {
        const listItem = document.createElement('li');
        listItem.classList.add('list-group-item');
        listItem.setAttribute('data-room-id', room.id);

        // 채팅방 정보 (이름, 참여자 수)
        const roomInfo = document.createElement('div');
        roomInfo.classList.add('room-info');

        const roomName = document.createElement('span');
        roomName.textContent = room.name;

        const participantsCount = document.createElement('span');
        participantsCount.classList.add('ms-2', 'text-muted');
        participantsCount.textContent = room.participants.length;

        roomInfo.appendChild(roomName);
        roomInfo.appendChild(participantsCount);

        // 읽지 않은 메시지 수
        if (room.unreadCount > 0) {
            const unreadBadge = document.createElement('span');
            unreadBadge.classList.add('unread-badge');
            unreadBadge.textContent = room.unreadCount;
            listItem.appendChild(roomInfo);
            listItem.appendChild(unreadBadge);
        } else {
            listItem.appendChild(roomInfo);
        }

        listItem.addEventListener('click', () => selectChatRoom(room));
        connectedUsersList.appendChild(listItem);
    });

    // 첫 번째 채팅방 선택
    if (rooms.length > 0 && !selectedRoomId) {
        selectChatRoom(rooms[0]);
    }
}

// 채팅방 선택
function selectChatRoom(room) {
    // 이전에 선택된 채팅방의 활성 상태 제거
    document.querySelectorAll('#connectedUsers .list-group-item').forEach(item => {
        item.classList.remove('active');
    });

    // 선택된 채팅방 활성화
    const roomElement = document.querySelector(`#connectedUsers .list-group-item[data-room-id="${room.id}"]`);
    if (roomElement) {
        roomElement.classList.add('active');
    }

    selectedRoomId = room.id;
    currentRoomElement.textContent = room.name;
    currentRoomParticipantCount.textContent = room.participants.length;

    // 채팅방의 메시지 불러오기
    fetchAndDisplayRoomChat(room.id);

    // 읽지 않은 메시지 수 초기화
    if (room.unreadCount > 0) {
        room.unreadCount = 0;
        const unreadBadge = roomElement.querySelector('.unread-badge');
        if (unreadBadge) {
            unreadBadge.remove();
        }
    }
}

// 채팅 초기화 및 STOMP 연결
function initChat() {
    showChatPage();

    // 실제 환경에서는 WebSocket 연결 코드 사용
    // 테스트를 위해 연결 상태 토글 기능만 구현
    connectBtn.addEventListener('click', toggleConnection);
    disconnectBtn.addEventListener('click', toggleConnection);

    toggleConnection()
}

// 연결 상태 토글
function toggleConnection() {
    isConnected = !isConnected;

    if (isConnected) {
        connectStomp();
        setConnected(true);
    } else {
        disconnectStomp();
        setConnected(false);
    }
}

// connectBtn 상태 변경
function setConnected(connected) {
    isConnected = connected;
    connectBtn.classList.toggle('hidden', connected);
    disconnectBtn.classList.toggle('hidden', !connected);
}

// STOMP 연결
function connectStomp() {
    const headers = {
        Authorization: 'Bearer ' + accessToken
    };

    const stompConfig = {
        // WebSocket Server URL
        brokerURL: '/ws/stomp',

        // when connection dropped try to reconnect after 5000ms
        reconnectDelay: 0,
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

            stompClient.subscribe(`/user/${auth.id}/queue/errors`, function (response) {
                console.error('Error message from server: ' + response.body);
            })

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
    }

    // stompjs 사용: webstomp.over -> Stomp.over
    stompClient = new StompJs.Client(stompConfig);

    stompClient.activate();
}

// Stomp 연결 종료
function disconnectStomp() {
    if (stompClient) {
        stompClient.deactivate();
    }
}

// STOMP 메시지 수신 처리
function onMessageReceived(payload) {
    console.log('Message received', payload);
    const message = JSON.parse(payload.body);
    // 현재 선택된 채팅방의 메시지라면 표시
    if (selectedRoomId && message.chatRoomId == selectedRoomId) {
        displayMessage(message.sender, message.content);
        chatArea.scrollTop = chatArea.scrollHeight;
    }
}

// 채팅방 목록 가져오기 (/rooms 엔드포인트)
async function findAndDisplayChatRooms() {
    const headers = {};
    const token = getCookie("accessToken");
    if (token) {
        headers['Authorization'] = 'Bearer ' + token;
    }

    try {
        const response = await fetch('/rooms', {headers});

        // 403 응답인 경우, 쿠키에서 accessToken 삭제 후 로그인 화면 표시
        if (response.status === 403) {
            deleteCookie();
            showLoginPage();
            return;
        }

        // 응답 JSON이 { chatRooms: [...] } 형태이므로 chatRooms 프로퍼티를 추출
        const data = await response.json();
        chatRooms = data.chatRooms;
        displayChatRooms(chatRooms);

        showChatPage();
    } catch (error) {
        console.error('Error fetching chat rooms:', error);
    }
}

// 선택된 채팅방의 메시지 내역 불러오기
async function fetchAndDisplayRoomChat(roomId) {
    try {
        const headers = {};
        const token = getCookie("accessToken");
        if (token) {
            headers['Authorization'] = 'Bearer ' + token;
        }

        const response = await fetch(`/chat/${roomId}/history`, {headers});
        const roomChat = await response.json();

        displayChatMessages(roomChat.messages);
    } catch (error) {
        console.error('Error fetching chat history:', error);
    }
}

// 채팅 메시지 표시
function displayChatMessages(messages) {
    chatArea.innerHTML = '';

    messages.forEach(message => {
        displayMessage(message);
    });

    // 스크롤을 최하단으로 이동
    chatArea.scrollTop = chatArea.scrollHeight;
}

// 채팅 메시지 DOM 추가
function displayMessage(message) {
    const isCurrentUser = message.sender === auth?.name;

    const messageContainer = document.createElement('div');
    messageContainer.classList.add('message-container');
    messageContainer.classList.add(isCurrentUser ? 'sender' : 'receiver');

    // 프로필 이미지
    const profileImg = document.createElement('img');
    profileImg.src = message.senderImage || 'img/user_icon.png';
    profileImg.alt = message.sender;
    profileImg.classList.add('user-profile-img');

    // 메시지 내용 컨테이너
    const messageContent = document.createElement('div');
    messageContent.classList.add('message-content');

    // 발신자 이름 (현재 사용자가 아닌 경우에만 표시)
    if (!isCurrentUser) {
        const senderName = document.createElement('div');
        senderName.classList.add('message-sender');
        senderName.textContent = message.sender;
        messageContent.appendChild(senderName);
    }

    // 메시지 내용
    const messageBubble = document.createElement('div');
    messageBubble.classList.add('message-bubble');
    messageBubble.textContent = message.content;
    messageContent.appendChild(messageBubble);

    // 시간
    const messageTime = document.createElement('div');
    messageTime.classList.add('message-time');
    messageTime.textContent = formatTime(message.timestamp);

    // 메시지 컨테이너에 요소 추가
    if (isCurrentUser) {
        messageContainer.appendChild(messageContent);
        messageContainer.appendChild(messageTime);
    } else {
        messageContainer.appendChild(profileImg);
        messageContainer.appendChild(messageContent);
        messageContainer.appendChild(messageTime);
    }

    chatArea.appendChild(messageContainer);
}

// 시간 포맷팅 (AM/PM)
function formatTime(timestamp) {
    const date = new Date(timestamp);
    return date.toLocaleString('en-US', {
        hour: 'numeric',
        minute: '2-digit',
        hour12: true
    });
}

// 메시지 전송
function sendMessage(event) {
    event.preventDefault();

    const messageContent = messageInput.value.trim();
    if (!messageContent || !selectedRoomId) return;

    // 실제 환경에서는 서버로 메시지 전송
    // 테스트를 위해 즉시 메시지 표시
    const newMessage = {
        id: Date.now().toString(),
        sender: auth?.name || 'User1',
        content: messageContent,
        timestamp: Date.now(),
        senderImage: 'img/user_icon.png'
    };

    displayMessage(newMessage);

    // 입력 필드 초기화
    messageInput.value = '';

    // 스크롤을 최하단으로 이동
    chatArea.scrollTop = chatArea.scrollHeight;
}

// 로그인 페이지 표시
function showLoginPage() {
    loginPage.classList.remove('hidden');
    chatPage.classList.add('hidden');
}

// 채팅 페이지 표시
function showChatPage() {
    loginPage.classList.add('hidden');
    chatPage.classList.remove('hidden');

    // 현재 사용자 정보 표시
    if (auth) {
        document.getElementById('connected-user-username').textContent = auth.name;
    } else {
        // 쿠키에서 사용자 정보 가져오기
        const userCookie = getCookie('user');
        if (userCookie) {
            auth = JSON.parse(decodeURIComponent(userCookie));
            document.getElementById('connected-user-username').textContent = auth.name;
        }
    }
}

// 로그아웃 처리
function onLogout() {
    // 쿠키 삭제
    deleteCookie();
    clearData();
    showLoginPage();
}

// 쿠키 삭제
function deleteCookie() {
    document.cookie = "accessToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
    document.cookie = "user=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
}

// 데이터 초기화
function clearData() {
    chatRooms = [];
    selectedRoomId = null;
    auth = null;
    chatArea.innerHTML = '';
}

// 이벤트 리스너
usernameForm.addEventListener('submit', login);
messageForm.addEventListener('submit', sendMessage);
logoutBtn.addEventListener('click', onLogout);

// 페이지 로드 시 실행
window.addEventListener('load', function() {
    const token = getCookie("accessToken");
    if (token) {
        findAndDisplayChatRooms();
        initChat();
    } else {
        showLoginPage();
    }
});
