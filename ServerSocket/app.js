var express = require("express");
var app = express();
var server = require("http").createServer(app);
var io = require("socket.io").listen(server.listen(process.env.PORT || 5000));

var users = [];

app.get("/", function(req, res){
    	res.send('Hello from 5000');	
});
	
io.sockets.on('connection' , function(client){

	client.on('join' , function(data){
		if(users.includes(data.id)){
			io.sockets.in(data.id).emit('join_faied');
			console.log('User ' + data.id + ' existed');
		} else {
			users.push(data.id);
			client.join(data.id);
			io.sockets.in(data.id).emit('join_success');
			console.log('User ' + data.id + ' has connected');

			client.broadcast.emit('new_user_join' , users);
		}
	});

	client.on('leave', function(data){
		console.log('User ' + data.id + ' has left');
		client.leave(data.id);
		users.pop(data.id);
		client.broadcast.emit('user_has_left' , users);
	});

	client.on('get_users', function(data){
		console.log('Get list user from ' + data.id);
		io.sockets.in(data.id).emit('users_from_server', users);
	});
	
	client.on('send', function(data){
		console.log('Make connect to ' + data.id);
		io.sockets.in(data.id).emit('wantconnect' , data);
	});
	
	client.on('acceptconnect' , function(data){
		console.log('createoffer ' + data.id);
		client.broadcast.emit('createoffer' , {});	
		
	});
	
	client.on('unacceptconnect' , function(data){
		client.broadcast.emit('unacceptconnect' , {});	
	});

	client.on('offer', function (details) {
		client.broadcast.emit('offer', details);
		console.log('offer: ' + JSON.stringify(details));
	});

	client.on('answer', function (details) {
		client.broadcast.emit('answer', details);
		console.log('answer: ' + JSON.stringify(details));
	});
		
	client.on('candidate', function (details) {
		client.broadcast.emit('candidate', details);
		console.log('candidate: ' + JSON.stringify(details));
	});
});


