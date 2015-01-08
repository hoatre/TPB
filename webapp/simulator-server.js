var express = require('express');
app = express(),
    server = require('http').createServer(app),
    io = require('socket.io').listen(server);

server.listen(3001);

app.get('/',function(req,res){
    res.sendFile(__dirname+'/simulator-client.html')
});
console.log('Server running at http://127.0.0.1:3001/');
app.use(express.static(__dirname + '/lib'));


var kafka = require('kafka-node'),
    Producer = kafka.Producer,
    Client = kafka.Client,
    client = new Client('localhost:2181');

//Topic
var topic = 'TransactionTopic';

//partition
var p = 0;

//Count message
var  count = 0;


var producer = new Producer(client);


//On Ready
producer.on('ready', function () {
    console.log('Producer ready');
});


//On Error
producer.on('error', function (err) {
    console.log('error', err)
})



//Send message
function send(message) {
    producer.send([
        {topic: topic, messages: [message] , partition: p}
    ], function (err, data) {
        if (err) console.log(arguments);
    });
}


var recursive = function (times, msgs, channal, product, transactionType) {
    msgs = msgs || 10;
    times = times || 1000;
    send(GeneratorTransaction(channal, product, transactionType));
    if (++count == msgs) return;
    setTimeout(recursive(times, msgs, channal, product, transactionType),times);
}


//Ramdom Int
function randomInt (low, high) {
    return Math.floor(Math.random() * (high - low) + low);
}


//Generator transaction
var GeneratorTransaction = function(channal, product, transactionType)
{
    //ID
    var trx_id = randomInt(1,9999999);

    //Generate Transaction Code
    var transactionTypes =  ["Deposit", "Withdrawal", "Transfer From", "Transfer To", "Balance Inquiry"];
    transactionType = transactionType || transactionTypes[randomInt(0,transactionTypes.length)];

    //Generate Channel ID
    var channals = ["Branch 1", "Branch 2", "Branch 3", "Contact Center"];
    channal = channal || channals[randomInt(0,channals.length)];

    //Product
    var products = ["Savings"];
    product = product || products[randomInt(0,products.length)];

    //Generate Account
    var acc_nos = ["100-121-12121212", "200-555-12313123", "100-643-10231323", "400-223-32424234", "500-123-23313443"];
    var acc_no = acc_nos[randomInt(0,acc_nos.length)];

    //Generate Amount
    var amount = randomInt(10000,100000);

    var trans = {
        trx_id: trx_id,
        trx_code: transactionType,
        ch_id: channal,
        acc_no: acc_no,
        prd_id: product,
        amount: amount,
        count: count
    };
    var msg = JSON.stringify(trans);
    return msg;
}


io.sockets.on('connection',function(socket){
    socket.on('send message',function(data){

        //Option 1
        recursive(
            data.time,//times
            data.countmessage,//msgs
            data.channel,//channal
            data.product,//product,
            data.transactiontype//transactionType
        );

        console.log('time:'+data.time+' - countmessage:'+data.countmessage+' - channel:'+data.channel+' - product:'+data.product+' - transactiontype:'+data.transactiontype);


        //Option 2
        recursive(
            data.time1,//times
            data.countmessage1,//msgs
            data.channel1,//channal
            data.product1,//product,
            data.transactiontype1//transactionType
        );

        console.log('time1:'+data.time1+' - countmessage1:'+data.countmessage1+' - channel1:'+data.channel1+' - product1:'+data.product1+' - transactiontype1:'+data.transactiontype1);


        //Option 3
        recursive(
            data.time2,//times
            data.countmessage2,//msgs
            data.channel2,//channal
            data.product2,//product,
            data.transactiontype2//transactionType
        );

        console.log('time2:'+data.time2+' - countmessage2:'+data.countmessage2+' - channel2:'+data.channel2+' - product2:'+data.product2+' - transactiontype2:'+data.transactiontype2);


        //Option 4
        recursive(
            data.time3,//times
            data.countmessage3,//msgs
            data.channel3,//channal
            data.product3,//product,
            data.transactiontype3//transactionType
        );

        console.log('time3:'+data.time3+' - countmessage3:'+data.countmessage3+' - channel3:'+data.channel3+' - product3:'+data.product3+' - transactiontype3:'+data.transactiontype3);

    });
});
