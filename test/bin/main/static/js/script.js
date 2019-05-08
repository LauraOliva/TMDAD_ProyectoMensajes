var socket = null

const messageWindow = document.getElementById("messages");
var i_loop = 0         
var m_loop = 0
connect()
function connect(){
	socket = new WebSocket("ws://192.168.1.134:8080/msg");
	socket.binaryType = "arraybuffer";	
	
	var url_string = window.location.href;
	console.log(url_string);
	var url = new URL(url_string);
	var user = url.searchParams.get("user")
	var test = url.searchParams.get("test")
	console.log(user);
	console.log(test)

	socket.onopen = function (event) {
	
	    var message = {
			content : user,
			type : 'VERIFY'
		};
		sendMessage(JSON.stringify(message));
		if(test == '21'){
			if(user == 'user_1'){
				var message = {
					content : 'CREATEROOM room_1',
					type : 'COMMAND'
				};
				sendMessage(JSON.stringify(message));
				for(var i = 1; i <= 100; i++){
					var message = {
						content : 'INVITEROOM room_1 user_' + i ,
						type : 'COMMAND'
					};
					sendMessage(JSON.stringify(message));
				}
			}
		}
		else if(test == '22'){
			var message = {
				content : 'OPENROOM room_1',
				type : 'COMMAND'
			};
			console.log(message)
			sendMessage(JSON.stringify(message));
			var message = {
				content : 'hola',
				type : 'CHAT'
			};
			sendMessage(JSON.stringify(message));  
			m_loop = 5000
			console.log(m_loop)
			myLoop() 
		}
		else if(test == '3'){
			
			var message = {
				content : 'CREATEROOM room_' + user,
				type : 'COMMAND'
			};
			sendMessage(JSON.stringify(message));
			var message = {
				content : 'hola',
				type : 'CHAT'
			};
			sendMessage(JSON.stringify(message));  
			m_loop = 100 - user.split("_")[1]
			console.log(m_loop)
			myLoop() 
			
		}
	
	};
	
	
	socket.onclose = function(event){
		connect();
	}
}

function myLoop () {  
		
	setTimeout(function () {  
		var message = {
			content : 'hola',
			type : 'CHAT'
		};
		sendMessage(JSON.stringify(message));   
		i_loop++;   
		console.log(i_loop)
		console.log(m_loop)                 
		if (i_loop < m_loop) {          
			myLoop();            
		}                        
	}, 2000)
}

function sendMessage(message) {
    socket.send(message);
}

function addMessageToWindow(message) {
    messageWindow.innerHTML += `<div>${message}</div>`;
    messageWindow.scrollTop = messageWindow.scrollHeight;
}