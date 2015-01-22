/*
 Node.js server script
 Required node packages: express, redis, socket.io
 */


var express = require('express');
    app = express(),
    server = require('http').createServer(app),
    io = require('socket.io').listen(server);

const redis = require('redis');
const client1 = redis.createClient();
log('info', 'connected to redis server');

server.listen(3000);
const socket  = io.listen(server);

app.get('/',function(req,res){
    res.sendFile(__dirname+'/client.html')
});
console.log('Server running at http://10.20.252.201:3000/');
app.use(express.static(__dirname + '/lib'));

function log(type, msg) {

    var color   = '\u001b[0m',
        reset = '\u001b[0m';

    switch(type) {
        case "info":
            color = '\u001b[36m';
            break;
        case "warn":
            color = '\u001b[33m';
            break;
        case "error":
            color = '\u001b[31m';
            break;
        case "msg":
            color = '\u001b[34m';
            break;
        default:
            color = '\u001b[0m'
    }

    console.log(color + '   ' + type + '  - ' + reset + msg);
}

function redis_get(key){
    client1.get(key, function(err, items) {
        if (err) {
            log('error', "error");
        } else {
            socket.emit('CountChart-' + key,items);
            log('info', "CountChart-" + key + " : " + items);
        }
    });
}
function redis_get_total(key){
    client1.get(key, function(err, items) {
        if (err) {
            log('error', "error");
        } else {
            socket.emit('Total-' + key,items);
            log('info', "Total-" + key + " : " + items);
        }
    });
}
function redis_hmget_top(key, keyMax){
    for(var i = 1;i <= keyMax; i++) {
        client1.hmget(key + i.toString(), "Acc", "Amount", function (err, items) {
            if (err) {
                log('error', "error");
            } else {
                socket.emit('Top-' + key, items);
                log('info', "Top-" + key + " : " + items);
            }
        });
    }
}
setInterval(function() {
    redis_get('real-time-Branch 1');
    redis_get('real-time-Branch 2');
    redis_get('real-time-Branch 3');
    redis_get('real-time-Contact Center');
    redis_get_total('TotalNoTran');
    redis_get_total('TotalAmount');
    redis_hmget_top('TopTenDepsits-Top', 5);
    redis_hmget_top('TopTenWithdrawals-Top', 5);
    redis_hmget_top('TopTenDepsits-Bot', 5);
    redis_hmget_top('TopTenWithdrawals-Bot', 5);
    redis_hmget_top('TopTenTransferFrom-Bot', 5);
    redis_hmget_top('TopTenTransferFrom-Top', 5);
}, 1000);