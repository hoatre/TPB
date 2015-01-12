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

/*
 var recursive = function (times, msgs, channal, product, transactionType) {
 msgs = msgs || 10;
 times = times || 1000;
 send(GeneratorTransaction(channal, product, transactionType, msgs));
 if (--msgs == 0) return;
 setTimeout(recursive(times, msgs, channal, product, transactionType),times);
 }
 */
var obj = {
    time: 0,
    time1: 0,
    time2: 0,
    time3: 0,
    countmessage: 0,
    countmessage1: 0,
    countmessage2: 0,
    countmessage3: 0,
    channel: '',
    channel1: '',
    channel2: '',
    channel3: '',
    product: '',
    product1: '',
    product2: '',
    product3: '',
    transactiontype: '',
    transactiontype1: '',
    transactiontype2: '',
    transactiontype3: '',
    a: 5,
    b: 5
}

var cnt=-1;
var cnt1=-1;
var cnt2=-1;
var cnt3=-1;
var t;
var t1;
var t2;
var t3;
type='';
function recursive()
{

    cnt++;
    if (cnt >= obj.countmessage) {
        clearTimeout(t);
    }
    else {
        console.log('conf1'+obj.channel+':'+obj.countmessage);
        t = setTimeout(recursive, obj.time);
        send(GeneratorTransaction(obj.channel, obj.product, obj.transactiontype, cnt));
    }

}
function recursive1()
{

    cnt1++;
    if (cnt1 >= obj.countmessage1) {
        clearTimeout(t1);
    }
    else {
        console.log('conf2'+obj.channel1+':'+obj.countmessage1);
        t1 = setTimeout(recursive1, obj.time1);
        send(GeneratorTransaction(obj.channel1, obj.product1, obj.transactiontype1, cnt1));
    }
}
function recursive2()
{

    cnt2++;
    if (cnt2 >= obj.countmessage2) {
        clearTimeout(t2);
    }
    else {
        console.log('conf3');
        t2 = setTimeout(recursive2, obj.time2);
        send(GeneratorTransaction(obj.channel2, obj.product2, obj.transactiontype2, cnt2));
    }
}
function recursive3()
{

    cnt3++;
    if(cnt3>=obj.countmessage3)
    {
        clearTimeout(t3);
    }
    else
    {
        console.log('conf4');
        t3 = setTimeout(recursive3,obj.time3);
        send(GeneratorTransaction(obj.channel3, obj.product3, obj.transactiontype3, cnt3));
    }
}

//Ramdom Int
function randomInt (low, high) {
    return Math.floor(Math.random() * (high - low) + low);
}


//Generator transaction
var GeneratorTransaction = function(channal, product, transactionType, msgs)
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
        count: msgs
    };
    var msg = JSON.stringify(trans);
    return msg;
}


io.sockets.on('connection',function(socket){
    socket.on('send message',function(data){

        //Option 1


        obj = {
            time: data.time,
            time1: data.time1,
            time2: data.time2,
            time3: data.time3,
            countmessage: data.countmessage,
            countmessage1: data.countmessage1,
            countmessage2: data.countmessage2,
            countmessage3: data.countmessage3,
            channel: data.channel,
            channel1: data.channel1,
            channel2: data.channel2,
            channel3: data.channel3,
            product: data.product,
            product1: data.product1,
            product2: data.product2,
            product3: data.product3,
            transactiontype: data.transactiontype,
            transactiontype1: data.transactiontype1,
            transactiontype2: data.transactiontype2,
            transactiontype3: data.transactiontype3,
            a: 5,
            b: 5}
        cnt=-1;
        cnt1=-1;
        cnt2=-1;
        cnt3=-1;
        recursive();
        recursive1();
        recursive2();
        recursive3();
        console.log('time:'+obj.time+' - countmessage:'+obj.countmessage+' - channel:'+obj.channel+' - product:'+obj.product+' - transactiontype:'+obj.transactiontype);
    }),
        socket.on('btnConfig1',function(data){
            console.log('btnConfig1:'+data.status);
            if(data.status=='start')
            {
                obj = {
                    time: data.time,
                    time1: data.time1,
                    time2: data.time2,
                    time3: data.time3,
                    countmessage: data.countmessage,
                    countmessage1: data.countmessage1,
                    countmessage2: data.countmessage2,
                    countmessage3: data.countmessage3,
                    channel: data.channel,
                    channel1: data.channel1,
                    channel2: data.channel2,
                    channel3: data.channel3,
                    product: data.product,
                    product1: data.product1,
                    product2: data.product2,
                    product3: data.product3,
                    transactiontype: data.transactiontype,
                    transactiontype1: data.transactiontype1,
                    transactiontype2: data.transactiontype2,
                    transactiontype3: data.transactiontype3,
                    a: 5,
                    b: 5}
                cnt=-1;

                recursive();
            }
            else if(data.status=='stop')
            {
                clearTimeout(t);
            }
        }),
        socket.on('btnConfig2',function(data){
            console.log('btnConfig2:'+data.status1);
            if(data.status1=='start')
            {
                obj = {
                    time: data.time,
                    time1: data.time1,
                    time2: data.time2,
                    time3: data.time3,
                    countmessage: data.countmessage,
                    countmessage1: data.countmessage1,
                    countmessage2: data.countmessage2,
                    countmessage3: data.countmessage3,
                    channel: data.channel,
                    channel1: data.channel1,
                    channel2: data.channel2,
                    channel3: data.channel3,
                    product: data.product,
                    product1: data.product1,
                    product2: data.product2,
                    product3: data.product3,
                    transactiontype: data.transactiontype,
                    transactiontype1: data.transactiontype1,
                    transactiontype2: data.transactiontype2,
                    transactiontype3: data.transactiontype3,
                    a: 5,
                    b: 5}
                cnt1=-1;

                recursive1();
            }
            else if(data.status1=='stop')
            {
                clearTimeout(t1);
            }
        }),
        socket.on('btnConfig3',function(data){
            console.log('btnConfig3:'+data.status2);
            if(data.status2=='start')
            {
                obj = {
                    time: data.time,
                    time1: data.time1,
                    time2: data.time2,
                    time3: data.time3,
                    countmessage: data.countmessage,
                    countmessage1: data.countmessage1,
                    countmessage2: data.countmessage2,
                    countmessage3: data.countmessage3,
                    channel: data.channel,
                    channel1: data.channel1,
                    channel2: data.channel2,
                    channel3: data.channel3,
                    product: data.product,
                    product1: data.product1,
                    product2: data.product2,
                    product3: data.product3,
                    transactiontype: data.transactiontype,
                    transactiontype1: data.transactiontype1,
                    transactiontype2: data.transactiontype2,
                    transactiontype3: data.transactiontype3,
                    a: 5,
                    b: 5}
                cnt2=-1;

                recursive2();
            }
            else if(data.status2=='stop')
            {
                clearTimeout(t2);
            }
        }),
        socket.on('btnConfig4',function(data){
            console.log('btnConfig4:'+data.status3);
            if(data.status3=='start')
            {
                obj = {
                    time: data.time,
                    time1: data.time1,
                    time2: data.time2,
                    time3: data.time3,
                    countmessage: data.countmessage,
                    countmessage1: data.countmessage1,
                    countmessage2: data.countmessage2,
                    countmessage3: data.countmessage3,
                    channel: data.channel,
                    channel1: data.channel1,
                    channel2: data.channel2,
                    channel3: data.channel3,
                    product: data.product,
                    product1: data.product1,
                    product2: data.product2,
                    product3: data.product3,
                    transactiontype: data.transactiontype,
                    transactiontype1: data.transactiontype1,
                    transactiontype2: data.transactiontype2,
                    transactiontype3: data.transactiontype3,
                    a: 5,
                    b: 5}
                cnt3=-1;

                recursive3();
            }
            else if(data.status3=='stop')
            {
                clearTimeout(t3);
            }
        });
});