/*
 Node.js server script
 Required node packages: express, redis, socket.io
 */
const PORT = 3000;
const HOST = 'localhost';

var express = require('express'),
    http = require('http'),
    app = express(),
    server = http.createServer(app);

const redis = require('redis');
const client = redis.createClient();
log('info', 'connected to redis server');

const io = require('socket.io');

if (!module.parent) {
    server.listen(PORT, HOST);
    const socket  = io.listen(server);
    // At the root of your website, we show the index.html page
    app.get('/', function(req, res) {
        res.sendfile('./client.html')
    });
    socket.on('connection', function(client) {
        const subscribe = redis.createClient()

        subscribe.subscribe('real-time-Branch 1');
        subscribe.subscribe('real-time-Branch 2');
        subscribe.subscribe('real-time-Branch 3');
        subscribe.subscribe('real-time-Contact Center');
        //minutes
        subscribe.subscribe('real-time-minutes-Branch 1');
        subscribe.subscribe('real-time-minutes-Branch 2');
        subscribe.subscribe('real-time-minutes-Branch 3');
        subscribe.subscribe('real-time-minutes-Contact Center');

        //var result = redis_lrange("listtest", function(redis_items) {
        //
        //});

        //socket.emit('listChart',result);

        //log('msg',result);

        redis_lrange('real-time-60s-Branch 1');
        redis_lrange('real-time-60s-Branch 2');

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
}

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




function redis_lrange(key){
    var ar;
    var test = client.lrange(key, 0, 59, function(err, items) {
        if (err) {
            console.error("error");
            log('msg', "2");
            return "error";
        } else {
            //console.log(items);
            //log('msg', ar);
            //buffer.push();
            socket.emit('listChart-' + key,items);

        }
    });
}