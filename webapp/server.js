/*
 Node.js server script
 Required node packages: express, redis, socket.io
 */
const PORT = 3000;
const HOST = 'localhost';

//var express = require('express'),
//    http = require('http'),
//    app = express(),
//    server = http.createServer(app);
var express = require('express');
    app = express(),
    server = require('http').createServer(app),
    io = require('socket.io').listen(server);

//server.listen(3000);
const redis = require('redis');
const client1 = redis.createClient();
log('info', 'connected to redis server');

//const io = require('socket.io').listen(server);;
//server.setMaxListeners(1000000);
//function sendHeartbeat(){
    //i++;

//    setTimeout(sendHeartbeat, 1000);
//    client1.on("message", function(data) {
//        client1.lrange('real-time-60s-Branch 1', 0, 59, function(err, items) {
//            if (err) {
//                //console.error("error");
//                log('error', "error");
//            } else {
//                //console.log(items);
//                //log('msg', ar);
//                //buffer.push();
//                socket.emit('listChart-' + 'real-time-60s-Branch 1',items);
//                log('info', "received from channel #" + channel + " : " + items);
//            }
//        });
//    });
//}

//setTimeout(sendHeartbeat, 1000);
//if (!module.parent) {
    server.listen(PORT, HOST);
    const socket  = io.listen(server);
    // At the root of your website, we show the index.html page
    app.get('/', function(req, res) {
        res.sendfile('./client.html')
    });
    //socket.on('connection', function(client){
    //    client1.on("message", function(data) {
    //        client1.lrange('real-time-60s-Branch 1', 0, 59, function(err, items) {
    //            if (err) {
    //                //console.error("error");
    //                log('error', "error");
    //            } else {
    //                //console.log(items);
    //                //log('msg', ar);
    //                //buffer.push();
    //                socket.emit('listChart-' + 'real-time-60s-Branch 1',items);
    //                log('info', "received from channel #" + channel + " : " + items);
    //            }
    //        });
    //    });
    //});


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

        //var result = redis_lrange("listtest", function(redis_items) {
        //
        //});

        //socket.emit('listChart',result);

        //log('msg','aaaa');

        //redis_lrange('real-time-60s-Branch 1');

        //redis_lrange('real-time-60s-Branch 2');
        //client.on("message", function(msg) {
        //    client1.lrange('real-time-60s-Branch 1', 0, 59, function(err, items) {
        //        if (err) {
        //            //console.error("error");
        //            log('error', "error");
        //        } else {
        //            //console.log(items);
        //            //log('msg', ar);
        //            //buffer.push();
        //            socket.emit('listChart-' + 'real-time-60s-Branch 1',items);
        //            log('info', items);
        //            //log('msg', "received from channel #" + channel + " : " + message);
        //        }
        //    });
        //});


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
//}
//socket.on('reloadChart',function(data1){
//    redis_lrange('real-time-60s-Branch 1');
//});
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