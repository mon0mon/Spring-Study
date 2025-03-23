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
let isConnected = true;
let isScrolledToBottom = true; // 스크롤이 최하단에 있는지 여부

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
                document.coookie = "user=" + encodeURIComponent(JSON.stringify(data.user)) + "; path=/";
                showAlert('login', 'success'); // aria-label: Success
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

        // 채팅방 이름
        const roomName = document.createElement('span');
        roomName.textContent = room.name;

        // 참여자 수
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

    // 스크롤 이벤트 리스너 추가
    chatArea.addEventListener('scroll', handleScroll);

    setConnected(true)
}

// 스크롤 이벤트 핸들러
function handleScroll() {
    // 스크롤이 최하단에 있는지 확인
    const isAtBottom = chatArea.scrollHeight - chatArea.clientHeight <= chatArea.scrollTop + 1;
    isScrolledToBottom = isAtBottom;
}

// 연결 상태 토글
function toggleConnection() {
    isConnected = !isConnected;

    setConnected(isConnected);
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
    scrollToBottom();
}

// 채팅 메시지 DOM 추가
function displayMessage(message) {
    const isCurrentUser = message.sender === auth?.name;

    const messageContainer = document.createElement('div');
    messageContainer.classList.add('message-container');
    messageContainer.classList.add(isCurrentUser ? 'sender' : 'receiver');

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
        // 내가 보낸 메시지는 프로필 이미지 없이 내용과 시간만 표시
        messageContainer.appendChild(messageContent);
        messageContainer.appendChild(messageTime);
    } else {
        // 다른 사용자가 보낸 메시지는 프로필 이미지, 내용, 시간 표시
        const profileImg = document.createElement('img');
        profileImg.src = message.senderImage || 'img/user_icon.png';
        profileImg.alt = message.sender;
        profileImg.classList.add('user-profile-img');

        messageContainer.appendChild(profileImg);
        messageContainer.appendChild(messageContent);
        messageContainer.appendChild(messageTime);
    }

    // 현재 스크롤 위치 확인
    const wasScrolledToBottom = isScrolledToBottom;

    // 메시지 추가
    chatArea.appendChild(messageContainer);

    // 스크롤이 최하단에 있었다면 자동 스크롤
    if (wasScrolledToBottom) {
        scrollToBottom();
    }
}

// 스크롤을 최하단으로 이동
function scrollToBottom() {
    chatArea.scrollTop = chatArea.scrollHeight;
    isScrolledToBottom = true;
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

    if (stompClient && stompClient.connected) {
        // STOMP를 통한 메시지 전송
        const chatMessage = {
            type: "SEND",
            content: messageContent,
        };

        const tx = stompClient.begin();
        stompClient.publish({
            destination: `/app/chat/${selectedRoomId}`,
            headers: {transaction: tx.id, 'Authorization': 'Bearer ' + accessToken},
            body: JSON.stringify(chatMessage)
        });
        tx.commit();
    } else {
        console.error('Error sending message');
    }

    // 입력 필드 초기화
    messageInput.value = '';
}

// STOMP 연결
function connectStomp() {
    if (!stompClient) {
        const headers = {
            Authorization: 'Bearer ' + accessToken
        };

        const stompConfig = {
            brokerURL: '/ws/stomp',
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
            connectHeaders: headers,

            onConnect: function (frame) {
                console.log('Client connected: ' + frame);
                setConnected(true);
                showAlert('STOMP Connected Successfully', 'info'); // aria-label: Info

                // 주기적 메시지 구독
                stompClient.subscribe('/topic/periodic', function (response) {
                    const data = JSON.parse(response.body);
                    console.log(`periodic message : ${response.body}`);
                }, headers);

                // 에러 메시지 구독
                if (auth && auth.id) {
                    stompClient.subscribe(`/user/${auth.id}/queue/errors`, function (response) {
                        console.error('Error message from server: ' + response.body);
                    }, headers);
                }

                // 채팅방 메시지 구독
                chatRooms.forEach(room => {
                    stompClient.subscribe(`/topic/chat/${room.id}`, function (response) {
                        onMessageReceived(response);
                    }, headers);
                });
            },

            onStompError: function (frame) {
                console.log('Broker reported error: ' + frame.headers['message']);
                console.log('Additional details: ' + frame.body);
                setConnected(false);
                showAlert('STOMP Error', 'danger'); // aria-label: Danger
            },

            onDisconnect: function (frame) {
                console.log('Client disconnected: ' + frame);
                setConnected(false);
                showAlert('STOMP Disconnected Successfully', 'info'); // aria-label: Info
            }
        };

        stompClient = new StompJs.Client(stompConfig);
        stompClient.activate();
    }
}

// STOMP 연결 해제
function disconnectStomp() {
    if (stompClient) {
        stompClient.deactivate();
        stompClient = null;
    }
}

// 연결 상태 설정
function setConnected(connected) {
    isConnected = connected;

    if (connected) {
        connectStomp();
        connectBtn.classList.add('hidden');
        disconnectBtn.classList.remove('hidden');
    } else {
        disconnectStomp();
        connectBtn.classList.remove('hidden');
        disconnectBtn.classList.add('hidden');
    }
}

// STOMP 메시지 수신 처리
function onMessageReceived(payload) {
    console.log('Message received', payload);
    const message = JSON.parse(payload.body);

    // 현재 선택된 채팅방의 메시지라면 표시
    if (selectedRoomId && message.chatRoomId == selectedRoomId) {
        displayMessage(message);
    } else {
        // 다른 채팅방의 메시지인 경우 해당 채팅방의 읽지 않은 메시지 수 증가
        updateUnreadCount(message.chatRoomId);
    }
}

// 읽지 않은 메시지 수 업데이트
function updateUnreadCount(roomId) {
    const room = chatRooms.find(r => r.id === roomId);
    if (room) {
        room.unreadCount = (room.unreadCount || 0) + 1;

        // UI 업데이트
        const roomElement = document.querySelector(`#connectedUsers .list-group-item[data-room-id="${roomId}"]`);
        if (roomElement) {
            let unreadBadge = roomElement.querySelector('.unread-badge');

            if (!unreadBadge) {
                unreadBadge = document.createElement('span');
                unreadBadge.classList.add('unread-badge');
                roomElement.appendChild(unreadBadge);
            }

            unreadBadge.textContent = room.unreadCount;
        }
    }
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
    // STOMP 연결 해제
    disconnectStomp();

    // 쿠키 삭제
    deleteCookie();
    clearData();
    showLoginPage();

    showAlert('logout', 'success'); // aria-label: Success
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

// Bootstrap Alert 생성 함수 (SVG 아이콘 포함)
function showAlert(message, type) {
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type} d-flex align-items-center alert-dismissible fade show`;
    alertDiv.setAttribute('role', 'alert');

    let ariaLabel;
    let iconId;
    if (type === 'success') {
        ariaLabel = 'Success:';
        iconId = 'check-circle-fill';
    } else if (type === 'info') {
        ariaLabel = 'Info:';
        iconId = 'info-fill';
    } else if (type === 'danger') {
        ariaLabel = 'Danger:';
        iconId = 'exclamation-triangle-fill';
    } else {
        // 기본값 설정
        ariaLabel = 'Info:';
        iconId = 'info-fill';
    }
    alertDiv.setAttribute('aria-label', ariaLabel);

    // SVG 아이콘 생성
    const svgElem = document.createElementNS("http://www.w3.org/2000/svg", "svg");
    svgElem.classList.add("bi", "flex-shrink-0", "me-2");
    svgElem.setAttribute("role", "img");
    svgElem.setAttribute("aria-label", ariaLabel);
    svgElem.setAttribute("height", "16")
    svgElem.setAttribute("width", "16")

    // <use> 태그 생성 및 심볼 참조
    const useElem = document.createElementNS("http://www.w3.org/2000/svg", "use");
    useElem.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href", `#${iconId}`);
    svgElem.appendChild(useElem);

    // 메시지 컨테이너 생성
    const messageDiv = document.createElement("div");
    messageDiv.textContent = message;

    // SVG 아이콘과 메시지 컨테이너를 alertDiv에 추가
    alertDiv.appendChild(svgElem);
    alertDiv.appendChild(messageDiv);

    // dismiss 버튼 추가
    const closeButton = document.createElement('button');
    closeButton.type = 'button';
    closeButton.className = 'btn-close';
    closeButton.setAttribute('data-bs-dismiss', 'alert');
    closeButton.setAttribute('aria-label', 'Close');
    alertDiv.appendChild(closeButton);

    // alert-container에 추가 (없으면 document.body에 추가)
    const container = document.getElementById('alert-container') || document.body;
    container.appendChild(alertDiv);

    // 5초 후에 alert 자동 제거
    setTimeout(() => {
        alertDiv.classList.remove('show');
        alertDiv.classList.add('hide');
        setTimeout(() => alertDiv.remove(), 500);
    }, 5000);
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
