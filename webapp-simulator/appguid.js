var express = require('express');
app = express(),
server = require('http').createServer(app),
io = require('socket.io').listen(server);

server.listen(3000);

app.get('/',function(req,res){
res.sendFile(__dirname+'/indexguid.html')	
});
console.log('Server running at http://127.0.0.1:3000/');
app.use(express.static(__dirname + '/JQuery'));


var kafka = require('kafka-node'),
    Producer = kafka.Producer,
    Client = kafka.Client,
    client = new Client('localhost:2181');

//var argv = require('optimist').argv;
var topic = 'TransactionTopic';
var p = 0;

var count = 10, rets = 0;
var producer = new Producer(client);
/*
producer.on('ready', function () {
   send('hello');
});
*/
producer.on('error', function (err) {
    console.log('error', err)
})

function send(message) {
    //for (var i = 0; i < count; i++) {
        producer.send([
            {topic: topic, messages: [message + ++rets] , partition: p}
        ], function (err, data) {
            if (err) console.log(arguments);
            //if (rets === count) process.exit();
        });
    //}
}

io.sockets.on('connection',function(socket){
	socket.on('send message',function(data){
	send('hello');
		io.sockets.emit('new message',data);
		console.log('time:'+data.time+' - countmessage:'+data.countmessage+' - chanel:'+data.chanel+' - product:'+data.product+' - transactiontype:'+data.transactiontype);
		console.log('time1:'+data.time1+' - countmessage1:'+data.countmessage1+' - chanel1:'+data.chanel1+' - product1:'+data.product1+' - transactiontype1:'+data.transactiontype1);
		console.log('time2:'+data.time2+' - countmessage2:'+data.countmessage2+' - chanel2:'+data.chanel2+' - product2:'+data.product2+' - transactiontype2:'+data.transactiontype2);
		console.log('time3:'+data.time3+' - countmessage3:'+data.countmessage3+' - chanel3:'+data.chanel3+' - product3:'+data.product3+' - transactiontype3:'+data.transactiontype3);
		
	});
});
