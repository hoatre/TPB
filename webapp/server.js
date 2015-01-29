/*
 Node.js server script
 Required node packages: express, redis, socket.io
 */

var t1, t2, t3;
var Deposit = "DE";
var Withdrawal = "WI";
var Transfer_From = "TF";
var Branch1 = "B1";
var Branch2 = "B2";
var Branch3 = "B3";
var Contact_Center = "Contact";

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
function redis_hmget_top(key){
    client1.hmget(key, "Acc", "Amount", function (err, items) {
        if (err) {
            log('error', "error");
        } else {
            socket.emit('Top-' + key, items);
            log('info', "Top-" + key + " : " + items);
        }
    });
}
clearInterval(t2);
clearInterval(t3);
clearInterval(t1);
io.sockets.on('connection',function(socket){
    socket.on('open30s', function (data1) {
        log('warn', "open30s");
        clearInterval(t2);
        clearInterval(t3);
        t1 = setInterval(function() {
            setTime(30000);
        }, 1000);
    }),
    socket.on('open60s', function (data1) {
        log('warn', "open60s");
        clearInterval(t1);
        clearInterval(t3);
        t2 = setInterval(function() {
            setTime(60000);
        }, 1000);
    }),
    socket.on('open10m', function (data1) {
        log('warn', "open10m");
        clearInterval(t2);
        clearInterval(t1);
        t3 = setInterval(function() {
            setTime(600000);
        }, 1000);
    })
});

function setTime(slidingTime){
    redis_get_total('TotalNoTran-' + slidingTime);
    redis_get_total('TotalAmount-' + slidingTime);
    redis_get('real-time-' + Branch1 + '-' + slidingTime);
    redis_get('real-time-' + Branch2 + '-' + slidingTime);
    redis_get('real-time-' + Branch3 + '-' + slidingTime);
    redis_get('real-time-' + Contact_Center + '-' + slidingTime);
    for(z=1; z <= 5; z++) {
        redis_hmget_top('TopTen' + Deposit + '-Top' + z.toString() + "-" + slidingTime);
        redis_hmget_top('TopTen' + Withdrawal + '-Top' + z.toString() + "-" + slidingTime);
        redis_hmget_top('TopTen' + Deposit + '-Bot' + z.toString() + "-" + slidingTime);
        redis_hmget_top('TopTen' + Withdrawal + '-Bot' + z.toString() + "-" + slidingTime);
        redis_hmget_top('TopTen' + Transfer_From + '-Bot' + z.toString() + "-" + slidingTime);
        redis_hmget_top('TopTen' + Transfer_From + '-Top' + z.toString() + "-" + slidingTime);
    }
}