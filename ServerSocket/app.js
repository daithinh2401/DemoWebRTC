var express = require("express");
var app = express();
var server = require("http").createServer(app);
// var io = require("socket.io").listen(server.listen(process.env.PORT || 5000));

var io = require("socket.io").listen(server.listen(3000 , '', () => {
	console.log(`Server is running `);
}));

// Join message
let SOCKET_MESSAGE_JOIN 					= 'join';
let SOCKET_MESSAGE_JOIN_SUCCESS 			= 'join_success';

// Update list
let SOCKET_MESSAGE_LEAVE					= 'leave';
let SOCKET_MESSAGE_GET_USER					= 'get_users';
let SOCKET_MESSAGE_UPDATE_USER_LIST			= 'update_user_list';

// Call
let SOCKET_MESSAGE_OFFER					= 'offer';
let SOCKET_MESSAGE_ANSWER					= 'answer';
let SOCKET_MESSAGE_REJECT					= 'reject';
let SOCKET_MESSAGE_CANDIDATE				= 'candidate';

var users = [];

app.get("/", function(req, res){
    	res.send('Hello from 5000');	
});
	
io.sockets.on('connection' , function(client){

	client.on(SOCKET_MESSAGE_JOIN , function(data){
		if(!users.includes(data.to)){
			users.push(data.id);
		} 
		client.join(data.id);
		io.sockets.in(data.id).emit(SOCKET_MESSAGE_JOIN_SUCCESS);
		console.log('User ' + data.id + ' has connected');

		client.broadcast.emit(SOCKET_MESSAGE_UPDATE_USER_LIST , users);
	});

	client.on(SOCKET_MESSAGE_LEAVE, function(data){
		console.log('User ' + data.id + ' has left');
		client.leave(data.id);
		users.splice( users.indexOf(data.id), 1);

		console.log('List users: ' + users);

		client.broadcast.emit(SOCKET_MESSAGE_UPDATE_USER_LIST , users);
	});

	client.on(SOCKET_MESSAGE_GET_USER, function(data){
		console.log('Get list user from ' + data.id);

		io.sockets.in(data.id).emit(SOCKET_MESSAGE_UPDATE_USER_LIST, users);
	});




	client.on(SOCKET_MESSAGE_OFFER, function (data) {
		io.sockets.in(data.to).emit(SOCKET_MESSAGE_OFFER, data);
		console.log('Send offer to: ' + data.to);
	});

	client.on(SOCKET_MESSAGE_ANSWER, function (data) {
		io.sockets.in(data.to).emit(SOCKET_MESSAGE_ANSWER, data);
		console.log('Send answer to: ' + data.to);
	});

	client.on(SOCKET_MESSAGE_REJECT, function (data) {
		io.sockets.in(data.to).emit(SOCKET_MESSAGE_REJECT);
		console.log('Send reject to: ' + data.to);
	});
		
	client.on(SOCKET_MESSAGE_CANDIDATE, function (data) {
		io.sockets.in(data.to).emit(SOCKET_MESSAGE_CANDIDATE, data);
		console.log('Send candidate to: ' + data.to);
	});
});


