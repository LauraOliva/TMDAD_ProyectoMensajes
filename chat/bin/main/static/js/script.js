
document.querySelector('#welcomeForm').addEventListener('submit', connect, true)

const messageWindow = document.getElementById("messages");
const commandWindow = document.getElementById("commands");
 
const sendButton = document.getElementById("send");
const messageInput = document.getElementById("message");

const sendFileButton = document.getElementById("sendFile");

const sendCommandButton = document.getElementById("sendCommand");
const commandInput = document.getElementById("command");

var socket = new WebSocket("ws://192.168.1.134:8080/msg");
socket.binaryType = "arraybuffer";	

function connect(event) {
	name = document.querySelector('#name').value.trim();
	if (name) {
		var message = {
			content : name,
			type : 'verify'
		};
		sendMessage(JSON.stringify(message));
		messageInput.value = "";
		document.querySelector('#welcome-page').classList.add('hidden');
		document.querySelector('#dialogue-page').classList.remove('hidden');
		
	    messageWindow.scrollTop = messageWindow.scrollHeight;
		commandWindow.scrollTop = commandWindow.scrollHeight;
			
		
	}
	event.preventDefault();
}
 
socket.onopen = function (event) {
    addCommandToWindow("Connected");
};
 
socket.onmessage = function (event) {

	if (event.data instanceof ArrayBuffer) {
            addMessageToWindow('Got Image:');
            addImageToWindow(event.data);
    } else {
        
		var message = JSON.parse(`${event.data}`);
		
		if (message.type === 'notification') {
			addCommandToWindow(message.content);
		}
		else if (message.type === 'broadcast') {
			addCommandToWindow(message.content);
		}
		else if(message.type === 'chat'){		
			addMessageToWindow(message.content);
		}
		else if(message.type === 'kick'){
			var msg = {
				content : "",
				type : 'kick'
			};
			sendMessage(JSON.stringify(msg));
			addMessageToWindow("Has sido expulsado de la sala");
		}
		else if(message.type === 'clean'){
			cleanMessageWindow();
		}
	}

};

 
sendCommandButton.onclick = function(event){
	var messageContent = commandInput.value;
	if (messageContent) {
		var command = {
			content : messageContent,
			type : 'command'
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
			type : 'chat'
		};
		sendMessage(JSON.stringify(message));
		messageInput.value = "";
	}
	event.preventDefault();	
};

function sendMessage(message) {
    socket.send(message);
}
 
function cleanMessageWindow(){
	messageWindow.innerHTML = ``;
}
 
function addMessageToWindow(message) {
    messageWindow.innerHTML += `<div>${message}</div>`;
    messageWindow.scrollTop = messageWindow.scrollHeight;
}

function addCommandToWindow(message) {
    commandWindow.innerHTML += `<div>${message}</div>`;
	commandWindow.scrollTop = commandWindow.scrollHeight;
}

sendFileButton.onclick = function (event) {

    var file = document.getElementById('filename').files[0];
    if(file){
		
		uploadFile(file)
	}
	event.preventDefault();	
};

function sendFile(file) {

    var reader = new FileReader();
    var rawData = new Blob();            
    reader.loadend = function() { }
    reader.onload = function(e) {
        rawData = e.target.result;
        socket.send(rawData);
    }
    reader.readAsArrayBuffer(file);

}

function uploadFile(file) {
    var formData = new FormData();
    formData.append("file", file);

    var xhr = new XMLHttpRequest();
    xhr.open("POST", "/uploadFile");

    xhr.onload = function() {
        console.log(xhr.responseText);
        var response = JSON.parse(xhr.responseText);
        if(xhr.status == 200) {

            var res = "<a href='" + response.fileDownloadUri + "' target='_blank'>" + "Descargar fichero" + "</a>";

            var message = {
					content : res,
					type : 'chat'
				};
				sendMessage(JSON.stringify(message));
        }
    }

    xhr.send(formData);
}