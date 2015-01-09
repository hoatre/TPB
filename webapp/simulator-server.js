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
    channel:'',
    channel1:'',
    channel2:'',
    channel3:'',
    product:'',
    product1:'',
    product2:'',
    product3:'',
    transactiontype:'',
    transactiontype1:'',
    transactiontype2:'',
    transactiontype3:'',
    a:5,
    b:5
}

var cnt=-1;
var cnt1=-1;
var cnt2=-1;
var cnt3=-1;
type='';
function recursive()
{
        //console.log('conf1');
        cnt++;
        if(cnt>=obj.countmessage)
        {
            clearTimeout(recursive);
        }
        else
        {
            setTimeout(recursive,obj.time);
            send(GeneratorTransaction(obj.channal, obj.product, obj.transactionType, obj.countmessage));
        }

}
function recursive1()
{
    //console.log('conf2');
    cnt1++;
    if(cnt1>=obj.countmessage1)
    {
        clearTimeout(recursive1);
    }
    else
    {
        setTimeout(recursive1,obj.time1);
        send(GeneratorTransaction(obj.channal1, obj.product1, obj.transactionType1, obj.countmessage1));
    }

}
function recursive2()
{
    //console.log('conf3');
    cnt2++;
    if(cnt2>=obj.countmessage2)
    {
        clearTimeout(recursive2);
    }
    else
    {
        setTimeout(recursive2,obj.time2);
        send(GeneratorTransaction(obj.channal2, obj.product2, obj.transactionType2, obj.countmessage2));
    }

}
function recursive3()
{
    //console.log('conf4');
    cnt3++;
    if(cnt3>=obj.countmessage3)
    {
        clearTimeout(recursive3);
    }
    else
    {
        setTimeout(recursive3,obj.time3);
        send(GeneratorTransaction(obj.channal3, obj.product3, obj.transactionType3, obj.countmessage3));
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
            channel:data.channel,
            channel1:data.channel1,
            channel2:data.channel2,
            channel3:data.channel3,
            product:data.product,
            product1:data.product1,
            product2:data.product2,
            product3:data.product3,
            transactiontype:data.transactiontype,
            transactiontype2:data.transactiontype2,
            transactiontype3:data.transactiontype3,
            a:5,
            b:5}
        cnt=-1;
        cnt1=-1;
        cnt2=-1;
        cnt3=-1;
            recursive();
            recursive1();
            recursive2();
            recursive3();
        //console.log('time:'+data.time+' - countmessage:'+data.countmessage+' - channel:'+data.channel+' - product:'+data.product+' - transactiontype:'+data.transactiontype);

/*
        //Option 2
        for(var i1=0;i1<data.countmessage1;i1++) {
            setInterval(recursive(
                data.time1,//times
                data.countmessage1,//msgs
                data.channel1,//channal
                data.product1,//product,
                data.transactiontype1//transactionType
            ), data.time1);
        }
        console.log('time1:'+data.time1+' - countmessage1:'+data.countmessage1+' - channel1:'+data.channel1+' - product1:'+data.product1+' - transactiontype1:'+data.transactiontype1);


        //Option 3
        for(var i2=0;i2<data.countmessage2;i2++) {
            setInterval(recursive(
                data.time2,//times
                data.countmessage2,//msgs
                data.channel2,//channal
                data.product2,//product,
                data.transactiontype2//transactionType
            ), data.time2);
        }
        console.log('time2:'+data.time2+' - countmessage2:'+data.countmessage2+' - channel2:'+data.channel2+' - product2:'+data.product2+' - transactiontype2:'+data.transactiontype2);


        //Option 4
        for(var i3=0;i3<data.countmessage3;i3++) {
            setInterval(recursive(
                data.time3,//times
                data.countmessage3,//msgs
                data.channel3,//channal
                data.product3,//product,
                data.transactiontype3//transactionType
            ), data.time3);
        }
        console.log('time3:'+data.time3+' - countmessage3:'+data.countmessage3+' - channel3:'+data.channel3+' - product3:'+data.product3+' - transactiontype3:'+data.transactiontype3);
*/
    });
});
