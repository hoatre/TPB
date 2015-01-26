var express = require('express');
var MongoClient = require('mongodb').MongoClient;
app = express(),
server = require('http').createServer(app),
io = require('socket.io').listen(server);

server.listen(3002);

app.get('/',function(req,res){
res.sendFile(__dirname+'/customer-client.html')	
});
console.log('Server running at http://127.0.0.1:3002/');
app.use(express.static(__dirname + '/JQuery'));
//app.use(express.static(__dirname + '/script'));
//app.use(express.static(__dirname + '/gfx'));
//app.use(express.static(__dirname + '/css'));

// Connect to the db
var customer={
	fullname:''
}
var lstcustormer=[];
var arr;
function connMongodb(data)
{
	MongoClient.connect("mongodb://localhost:27017/CIC", function(err, db) {
	  if(err) { return console.dir(err); }

	  var collection = db.collection('Customer');
	  
		/*collection.find(
			{FULLNAME: /^NGUYEN/ }
		).skip(1).limit(10).toArray(function(err, items) {
			if(items!=null&&items.length>0)
			{
				console.log('FULLNAME:'+items[0].FULLNAME);
				io.sockets.emit('ping', items);
			}
		});*/
		if(data.value=='ALL')
		{
			var obj={data:[],count:0}
			collection.find(
				//{FULLNAME: /^NGUYEN/ }
			).skip(data.start).limit(data.end).toArray(function(err, items) {
				if(items!=null&&items.length>0)
				{
					//console.log('FULLNAME:'+items[0].FULLNAME);
					obj.data=items;
					//io.sockets.emit('ping', items);
					collection.count(function(err, count) {
						if(count>0)
						{
							console.log('FULLNAME:'+obj.data[0].FULLNAME);
							obj.count=count;	
							io.sockets.emit('ping', obj);
						}
					});
				}
			});
			
			
		}
		else
		{
			var obj={data:[],count:0}
			collection.find(
				{FULLNAME: new RegExp(data.value) }
			).skip(data.start).limit(data.end).toArray(function(err, items) {
				if(items!=null&&items.length>0)
				{
					//console.log('FULLNAME:'+items[0].FULLNAME);
					obj.data=items;
					//io.sockets.emit('ping', items);
					collection.count({FULLNAME: new RegExp(data.value)} ,function(err, count) {
						if(count>0)
						{
							console.log('FULLNAME:'+obj.data[0].FULLNAME);
							obj.count=count;	
							io.sockets.emit('ping', obj);
						}
					});
				}
			});
		}
	});
}
io.sockets.on('connection',function(socket){
	socket.on('send message',function(data){
		//io.sockets.emit('new message',data);
		connMongodb(data);
		console.log('msg:'+data.value+"--start:"+data.start+"--end:"+data.end);
	});
});
