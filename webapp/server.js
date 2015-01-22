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


socket.on('connection', function(client) {
    const subscribe = redis.createClient();

    subscribe.subscribe('real-time-Branch 1');
    subscribe.subscribe('real-time-Branch 2');
    subscribe.subscribe('real-time-Branch 3');
    subscribe.subscribe('real-time-Contact Center');
    //minutes
    subscribe.subscribe('real-time-minutes-Branch 1');
    subscribe.subscribe('real-time-minutes-Branch 2');
    subscribe.subscribe('real-time-minutes-Branch 3');
    subscribe.subscribe('real-time-minutes-Contact Center');

    subscribe.subscribe('real-time-60s-Branch 1');



    subscribe.on("message", function(channel, message) {
        client.send(channel, message);
        log('msg', "received from channel #" + channel + " : " + message);
    });

    client.on('message', function(msg) {
        log('debug', msg);
    });

    client.on('disconnect', function() {
        log('warn', 'disconnecting from redis');
        subscribe.quit();
    });
});

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
function redis_hmget_top(key){
    client1.hmget(key, "Acc", "Amount", function(err, items) {
        if (err) {
            log('error', "error");
        } else {
            socket.emit('Top-' + key,items);
            log('info', "Top-" + key + " : " + items);
        }
    });
}
setInterval(function() {
    redis_get('real-time-Branch 1');
    redis_get('real-time-Branch 2');
    redis_get('real-time-Branch 3');
    redis_get('real-time-Contact Center');
    redis_get_total('TotalNoTran');
    redis_get_total('TotalAmount');
    redis_hmget_top('TopTenDepsits-Top1');
    redis_hmget_top('TopTenDepsits-Top2');
    redis_hmget_top('TopTenDepsits-Top3');
    redis_hmget_top('TopTenWithdrawals-Top1');
    redis_hmget_top('TopTenWithdrawals-Top2');
    redis_hmget_top('TopTenWithdrawals-Top3');
    redis_hmget_top('TopTenDepsits-Top4');
    redis_hmget_top('TopTenDepsits-Top5');
    redis_hmget_top('TopTenWithdrawals-Top4');
    redis_hmget_top('TopTenWithdrawals-Top5');
    redis_hmget_top('TopTenDepsits-Bot1');
    redis_hmget_top('TopTenDepsits-Bot2');
    redis_hmget_top('TopTenDepsits-Bot3');
    redis_hmget_top('TopTenDepsits-Bot4');
    redis_hmget_top('TopTenDepsits-Bot5');
    redis_hmget_top('TopTenWithdrawals-Bot1');
    redis_hmget_top('TopTenWithdrawals-Bot2');
    redis_hmget_top('TopTenWithdrawals-Bot3');
    redis_hmget_top('TopTenWithdrawals-Bot4');
    redis_hmget_top('TopTenWithdrawals-Bot5');
    redis_hmget_top('TopTenTransferFrom-Bot1');
    redis_hmget_top('TopTenTransferFrom-Bot2');
    redis_hmget_top('TopTenTransferFrom-Bot3');
    redis_hmget_top('TopTenTransferFrom-Bot4');
    redis_hmget_top('TopTenTransferFrom-Bot5');
    redis_hmget_top('TopTenTransferFrom-Top1');
    redis_hmget_top('TopTenTransferFrom-Top2');
    redis_hmget_top('TopTenTransferFrom-Top3');
    redis_hmget_top('TopTenTransferFrom-Top4');
    redis_hmget_top('TopTenTransferFrom-Top5');
}, 1000);