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
var totalNoTran = 0;
var totalAmount = 0;
var time1 = 60000;
var time2 = 3600000;
var time3 = 86400000;
var lengthRange = 1000000000;
var port=4000;
var express = require('express');
var    app = express();
    //server = require('http').createServer(app),
var io = require('socket.io').listen(app.listen(port));
//var socket = io.socket;

const redis = require('redis');
//const client1 = redis.createClient(6379, '10.20.252.201', {});
const client1 = redis.createClient();
log('info', 'connected to redis server');


//const socket  = io.listen(server);

app.get('/',function(req,res){
    res.sendFile(__dirname+'/client.html')
});
console.log('Server running at http://10.20.252.201:4000/');
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

//t1 = setInterval(function() {
//    setTime(time1);
//}, 1000);
//t2 = setInterval(function() {
//    setTime(time2);
//}, 1000);
//t3 = setInterval(function() {
//    setTime(time3);
//}, 1000);

clearInterval(t2);
clearInterval(t3);
clearInterval(t1);

io.sockets.on('connection',function(socket){
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

    function redis_hmget_top(key){

        client1.hmget(key, "Acc", "Amount", function (err, items) {
            if (err) {
                log('error', "error");
            } else {
                str = items;
                socket.emit('Top-' + key, items);
                log('info', "Top-" + key + " : " + items);
            }
        });

    }

    function redis_lrange(key){
        client1.lrange(key, 0, 1000000, function(err, items) {
            if (err) {
                log('error', "error");
            } else {
               socket.emit('listChart-' + key,items);
                //log('info', items);
            }
        });
    }
    //setInterval(function() {
    //
    //
    //
    //}, 1000);
    function setTime(slidingTime){
        redis_get('real-time-count-' + Branch1 + '-' + slidingTime);
        redis_get('real-time-count-' + Branch2 + '-' + slidingTime);
        redis_get('real-time-count-' + Branch3 + '-' + slidingTime);
        redis_get('real-time-count-' + Contact_Center + '-' + slidingTime);
        redis_get('real-time-sum-' + Branch1 + '-' + slidingTime);
        redis_get('real-time-sum-' + Branch2 + '-' + slidingTime);
        redis_get('real-time-sum-' + Branch3 + '-' + slidingTime);
        redis_get('real-time-sum-' + Contact_Center + '-' + slidingTime);
        for (z = 1; z <= 5; z++) {
            redis_hmget_top('TopTen' + Deposit + '-Top' + z.toString() + "-" + slidingTime);
            redis_hmget_top('TopTen' + Withdrawal + '-Top' + z.toString() + "-" + slidingTime);
            redis_hmget_top('TopTen' + Deposit + '-Bot' + z.toString() + "-" + slidingTime);
            redis_hmget_top('TopTen' + Withdrawal + '-Bot' + z.toString() + "-" + slidingTime);
            redis_hmget_top('TopTen' + Transfer_From + '-Bot' + z.toString() + "-" + slidingTime);
            redis_hmget_top('TopTen' + Transfer_From + '-Top' + z.toString() + "-" + slidingTime);
        }
    }
    socket.on('open 1 Minute', function (data1) {
        log('warn', "open 1 Minute");
        clearInterval(t2);
        clearInterval(t3);
        t1 = setInterval(function() {
            setTime(time1);
            redis_lrange('real-time-count-chart-' + time1);
        }, 1000);
    }),
    socket.on('open 1 hour', function (data1) {
        log('warn', "open 1 hour");
        clearInterval(t1);
        clearInterval(t3);
        t2 = setInterval(function() {
            setTime(time2);
            redis_lrange('real-time-count-chart-' + time2);
        }, 1000);
    }),
    socket.on('open 1 day', function (data1) {
        log('warn', "open 1 day");
        clearInterval(t2);
        clearInterval(t1);
        t3 = setInterval(function() {
            setTime(time3);
            redis_lrange('real-time-count-chart-' + time3);
        }, 1000);
    })
});


//--------------------------------fake kafka ----------------------------------------------------------------------

var ADD_KAFKA='localhost:2181';
var kafka = require('kafka-node'),
    Producer = kafka.Producer,
    Client = kafka.Client,
//client = new Client('10.20.252.201:2181');
    client = new Client(ADD_KAFKA);
var topic = 'TransactionTopic';
var producer = new Producer(client);
//On Ready
producer.on('ready', function () {
    console.log('Producer ready');
});


//On Error
producer.on('error', function (err) {
    console.log('error', err)
})
//partition
var p = 0;
//SIMULATOR_LIST_SEND_MESSAGE
function send(message) {
    producer.send([
        {topic: topic, messages: [message] , partition: p}
    ], function (err, data) {
        if (err) console.log(arguments);
    });
}

var obj = {
    time: 1000,
    countmessage: 0,
    amountto: 0,
    amountfrom: 0,
    channel: '',
    product: '',
    transactiontype: '',
    a: 5,
    b: 5
}
var cnt=-1;
var trans = {
    trx_id: 0,
    trx_code: "TranTypeFake",
    ch_id: "ChannelFake",
    amount: 0,
    acc_no: "000-000-00000000",
    prd_id: "ProductFake",
    timestamp: new Date().getTime(),
    count: 0
};
var msg = JSON.stringify(trans);

recursive();
function recursive()
{

    cnt++;
    console.log('conf1 status:'+obj.status);
    if (obj.countmessage!=''&&cnt >= obj.countmessage||obj.status=='stop') {
        console.log('stop conf1');
        clearTimeout(t);
    }
    else {
        console.log('conf1'+obj.channel+':'+obj.countmessage+'-'+obj.amountto+':'+obj.amountfrom);
        t = setTimeout(recursive, obj.time);
        send(msg);
    }


}

//Generator transaction



