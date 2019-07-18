var express = require("express");
var app = express();
var server = require("http").createServer(app);
var io = require("socket.io").listen(server.listen(process.env.PORT || 5000));

app.get("/", function(req, res){
    	res.send('Hello from 5000');	
	});

	
io.sockets.on('connection' , function(client){

	client.on('join' , function(data){
		client.join(data.id);
		client.broadcast.emit('userjoin' , data);
		console.log('User ' + data.id + ' has connected');
		
	});
	
	client.on('send' , function(data){
		console.log('Make connect to ' + data.id);
		io.sockets.in(data.id).emit('wantconnect' , data);
	});
	
	client.on('acceptconnect' , function(data){
		console.log('createoffer ' + data.id);
		
	});
	
	client.on('newactivity' ,function(data){
		
		console.log('Client has new activity');
		client.broadcast.emit('createoffer' , {});	
				
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


