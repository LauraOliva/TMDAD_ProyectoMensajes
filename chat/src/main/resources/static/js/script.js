const messageWindow = document.getElementById("messages");
const commandWindow = document.getElementById("commands");
 
const sendButton = document.getElementById("send");
const messageInput = document.getElementById("message");

const sendCommandButton = document.getElementById("sendCommand");
const commandInput = document.getElementById("command");
 
const socket = new WebSocket("ws://localhost:8080/socket");
socket.binaryType = "arraybuffer";
 
socket.onopen = function (event) {
    addMessageToWindow("Connected");
    addCommandToWindow("Connected");
};
 
socket.onmessage = function (event) {
	var message = JSON.parse(`${event.data}`);
	
	if (message.type === 'notification') {
		addCommandToWindow(message.content);
	}
	else if(message.type === 'chat'){		
		addMessageToWindow(message.content);
	}
	else if(message.type === 'kick'){
		var msg = {
			content : "",
			type : 'KICK'
		};
		sendMessage(JSON.stringify(msg));
		addMessageToWindow("Has sido expulsado de la sala");
	}

};
 
sendCommandButton.onclick = function(event){
	var messageContent = commandInput.value;
	if (messageContent) {
		var command = {
			content : messageContent,
			type : 'COMMAND'
		};
		sendMessage(JSON.stringify(command));
		addCommandToWindow(messageContent);
		commandInput.value = "";
	}
	event.preventDefault();	
}
 
sendButton.onclick = function (event) {
	var messageContent = messageInput.value;
	if (messageContent) {
		var message = {
			content : messageContent,
			type : 'CHAT'
		};
		sendMessage(JSON.stringify(message));
		messageInput.value = "";
	}
	event.preventDefault();	
};

function sendMessage(message) {
    socket.send(message);
}
 
function addMessageToWindow(message) {
    messageWindow.innerHTML += `<div>${message}</div>`;
}

function addCommandToWindow(message) {
    commandWindow.innerHTML += `<div>${message}</div>`;
}