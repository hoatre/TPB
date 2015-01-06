/*
    Node.js server script
    Required node packages: express, redis, socket.io
*/
const PORT = 3000;
const HOST = 'localhost';

var express = require('express'),
    http = require('http'), 
    server = http.createServer(app);

var app = express();

const redis = require('redis');
const client = redis.createClient();
log('info', 'connected to redis server');

const io = require('socket.io');

if (!module.parent) {
    server.listen(PORT, HOST);
    const socket  = io.listen(server);

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