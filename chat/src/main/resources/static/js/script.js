
document.querySelector('#welcomeForm').addEventListener('submit', connect, true)

const messageWindow = document.getElementById("messages");
const commandWindow = document.getElementById("commands");
 
const sendButton = document.getElementById("send");
const messageInput = document.getElementById("message");

const title = document.getElementById("title");

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
			type : 'VERIFY'
		};
		sendMessage(JSON.stringify(message));
	}
	event.preventDefault();
}
 
socket.onopen = function (event) {
    addCommandToWindow("Connected");
};
 
socket.onmessage = function (event) {


	var message = JSON.parse(`${event.data}`);
	
	if (message.type === 'NOTIFICATION') {
		addCommandToWindow(message.content);
	}
	else if(message.type === 'VERIFY'){
		if(message.content === 'ok'){
			messageInput.value = "";
			document.querySelector('#welcome-page').classList.add('hidden');
			document.querySelector('#dialogue-page').classList.remove('hidden');
			
		    messageWindow.scrollTop = messageWindow.scrollHeight;
			commandWindow.scrollTop = commandWindow.scrollHeight;
		}
		else{
			title.innerHTML = "Maximo numero de usuario excedido"
		}
	}
	else if(message.type === 'CHAT'){		
		addMessageToWindow(message.content);
	}
	else if(message.type === 'CLEAN'){
		cleanMessageWindow();
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
    url = 'http://192.168.1.134:8000/uploadFile'
    xhr.open("POST", url, true);

    xhr.onreadystatechange  = function() {
        console.log(xhr.responseText);
        var response = xhr.responseText;
        if(this.readyState === XMLHttpRequest.DONE && this.status === 200) {

            var res = "<a href='" + response + "' target='_blank'>" + "Descargar fichero" + "</a>";

            var message = {
				content : res,
				type : 'CHAT'
			};
			sendMessage(JSON.stringify(message));
        }
        else if(this.readyState === XMLHttpRequest.DONE && this.status != 200) {
        	addCommandToWindow("No se ha podido enviar el fichero")
        }
    }

    xhr.send(formData);
}