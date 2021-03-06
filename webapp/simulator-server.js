var MongoClient = require('mongodb').MongoClient;
var express = require('express');
app = express(),
    server = require('http').createServer(app),
    io = require('socket.io').listen(server);

server.listen(3003);

app.get('/',function(req,res){
    res.sendFile(__dirname+'/simulator-client.html')
});
console.log('Server running at http://127.0.0.1:3003/');
app.use(express.static(__dirname + '/lib'));

//---------------variable--------------------------
var ADD_KAFKA='localhost:2181';
var ADD_MONGODB_CIC="mongodb://10.20.252.202:27017/CIC";
var ADD_MONGODB_CLOUBBANK="mongodb://10.20.252.202:27017/CloudBank";
//---------------variable--------------------------

var kafka = require('kafka-node'),
    Producer = kafka.Producer,
    Client = kafka.Client,
    //client = new Client('10.20.252.201:2181');
    client = new Client(ADD_KAFKA);

//Topic
var topic = 'TransactionTopic';

//partition
var p = 0;

//Count message
var  count = 0;

var acc_nos = [];


var producer = new Producer(client);


//On Ready
producer.on('ready', function () {
    console.log('Producer ready');
});


//On Error
producer.on('error', function (err) {
    console.log('error', err)
})



//SIMULATOR_LIST_SEND_MESSAGE
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
    amountto: 0,
    amountto1: 0,
    amountto2: 0,
    amountto3: 0,
    amountfrom: 0,
    amountfrom1: 0,
    amountfrom2: 0,
    amountfrom3: 0,
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
    console.log('conf1 status:'+obj.status);
    if (obj.countmessage!=''&&cnt >= obj.countmessage||obj.status=='stop') {
        console.log('stop conf1');
        clearTimeout(t);
    }
    else {
        console.log('conf1'+obj.channel+':'+obj.countmessage+'-'+obj.amountto+':'+obj.amountfrom);
        t = setTimeout(recursive, obj.time);
        send(GeneratorTransaction(obj.amountto,obj.amountfrom,obj.channel, obj.product, obj.transactiontype, cnt));
    }

}
function recursive1()
{

    cnt1++;
    if (obj.countmessage1!=''&&cnt1 >= obj.countmessage1||obj.status1=='stop') {
        console.log('stop conf2');
        clearTimeout(t1);
    }
    else {
        console.log('conf2'+obj.channel1+':'+obj.countmessage1+'-'+obj.amountto1+':'+obj.amountfrom1);
        t1 = setTimeout(recursive1, obj.time1);
        send(GeneratorTransaction(obj.amountto1,obj.amountfrom1,obj.channel1, obj.product1, obj.transactiontype1, cnt1));
    }
}
function recursive2()
{

    cnt2++;
    if (obj.countmessage2!=''&&cnt2 >= obj.countmessage2||obj.status2=='stop') {
        console.log('stop conf3');
        clearTimeout(t2);
    }
    else {
        console.log('conf3'+obj.channel2+':'+obj.countmessage2+'-'+obj.amountto2+':'+obj.amountfrom2);
        t2 = setTimeout(recursive2, obj.time2);
        send(GeneratorTransaction(obj.amountto2,obj.amountfrom2,obj.channel2, obj.product2, obj.transactiontype2, cnt2));
    }
}
function recursive3()
{

    cnt3++;
    if(obj.countmessage3!=''&&cnt3>=obj.countmessage3||obj.status3=='stop')
    {
        console.log('stop conf4');
        clearTimeout(t3);
    }
    else
    {
        console.log('conf4'+obj.channel3+':'+obj.countmessage3+'-'+obj.amountto3+':'+obj.amountfrom3);
        t3 = setTimeout(recursive3,obj.time3);
        send(GeneratorTransaction(obj.amountto3,obj.amountfrom3,obj.channel3, obj.product3, obj.transactiontype3, cnt3));
    }
}

//Ramdom Int
function randomInt (low, high) {
    return Math.floor(Math.random() * (parseInt(high) - parseInt(low)) + parseInt(low));
}


//Generator transaction
var GeneratorTransaction = function(amountto,amountfrom,channal, product, transactionType, msgs)
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
    //var acc_nos = ["100-121-12121212", "200-555-12313123", "100-643-10231323", "400-223-32424234", "500-123-23313443"
    //    ,"100-121-12121212", "200-555-12313121", "100-643-10231321", "400-223-32424231", "500-123-23313441"
    //    ,"100-121-12121213", "200-555-12313122", "100-643-10231322", "400-223-32424232", "500-123-23313442"
    //    ,"100-121-12121214", "200-555-12313124", "100-643-10231324", "400-223-32424233", "500-123-23313444"
    //    ,"100-121-12121215", "200-555-12313125", "100-643-10231325", "400-223-32424235", "500-123-23313445"
    //    ,"100-121-12121216", "200-555-12313126", "100-643-10231326", "400-223-32424236", "500-123-23313446"
    //    ,"100-121-12121217", "200-555-12313127", "100-643-10231327", "400-223-32424237", "500-123-23313447"
    //    ,"100-121-12121218", "200-555-12313128", "100-643-10231328", "400-223-32424238", "500-123-23313448"
    //    ,"100-121-12121219", "200-555-12313129", "100-643-10231329", "400-223-32424239", "500-123-23313449"];
    var acc_no = acc_nos[randomInt(0,acc_nos.length)];

    //console.log('amountfrom: '+ amountfrom);
    //console.log('amountto: '+ amountto);
    //Generate Amount
    var amount = randomInt(amountto, amountfrom);

    var timestamp = new Date().getTime();

    var trans = {
        trx_id: trx_id,
        trx_code: transactionType,
        ch_id: channal,
        amount: amount,
        acc_no: acc_no,
        prd_id: product,
        timestamp: timestamp,
        count: msgs
    };
    var msg = JSON.stringify(trans);
    return msg;
}


io.sockets.on('connection',function(socket){
		socket.on('SIMULATOR_LIST_SEND_MESSAGE',function(data){

        //Option 1
		console.log('SIMULATOR_LIST_SEND_MESSAGE:'+data);

        obj = {
            time: data.time==''?1000:data.time,
            time1: data.time1==''?1000:data.time1,
            time2: data.time2==''?1000:data.time2,
            time3: data.time3==''?1000:data.time3,
            countmessage: data.countmessage,
            countmessage1: data.countmessage1,
            countmessage2: data.countmessage2,
            countmessage3: data.countmessage3,
            amountto: data.amountto==''?10:data.amountto,
            amountto1: data.amountto1==''?10:data.amountto1,
            amountto2: data.amountto2==''?10:data.amountto2,
            amountto3: data.amountto3==''?10:data.amountto3,
            amountfrom: data.amountfrom==''?100:data.amountfrom,
            amountfrom1: data.amountfrom1==''?100:data.amountfrom1,
            amountfrom2: data.amountfrom2==''?100:data.amountfrom2,
            amountfrom3: data.amountfrom3==''?100:data.amountfrom3,
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
            status:data.status,
            status1:data.status1,
            status2:data.status2,
            status3:data.status3,
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
        console.log('time:'+obj.time+' - countmessage:'+obj.countmessage+' - channel:'+obj.channel+' - product:'+obj.product+' - transactiontype:'+obj.transactiontype+' - amountto:'+obj.amountto+' - amountfrom:'+obj.amountfrom);
    }),
        socket.on('btnConfig1',function(data){
            console.log('btnConfig1:'+data.status);
            if(data.status=='start')
            {
                obj = {
                    time: data.time==''?1000:data.time,
                    time1: data.time1==''?1000:data.time1,
                    time2: data.time2==''?1000:data.time2,
                    time3: data.time3==''?1000:data.time3,
                    countmessage: data.countmessage,
                    countmessage1: data.countmessage1,
                    countmessage2: data.countmessage2,
                    countmessage3: data.countmessage3,
                    amountto: data.amountto==''?10:data.amountto,
                    amountto1: data.amountto1==''?10:data.amountto1,
                    amountto2: data.amountto2==''?10:data.amountto2,
                    amountto3: data.amountto3==''?10:data.amountto3,
                    amountfrom: data.amountfrom==''?100:data.amountfrom,
                    amountfrom1: data.amountfrom1==''?100:data.amountfrom1,
                    amountfrom2: data.amountfrom2==''?100:data.amountfrom2,
                    amountfrom3: data.amountfrom3==''?100:data.amountfrom3,
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
                    time: data.time==''?1000:data.time,
                    time1: data.time1==''?1000:data.time1,
                    time2: data.time2==''?1000:data.time2,
                    time3: data.time3==''?1000:data.time3,
                    countmessage: data.countmessage,
                    countmessage1: data.countmessage1,
                    countmessage2: data.countmessage2,
                    countmessage3: data.countmessage3,
                    amountto: data.amountto==''?10:data.amountto,
                    amountto1: data.amountto1==''?10:data.amountto1,
                    amountto2: data.amountto2==''?10:data.amountto2,
                    amountto3: data.amountto3==''?10:data.amountto3,
                    amountfrom: data.amountfrom==''?100:data.amountfrom,
                    amountfrom1: data.amountfrom1==''?100:data.amountfrom1,
                    amountfrom2: data.amountfrom2==''?100:data.amountfrom2,
                    amountfrom3: data.amountfrom3==''?100:data.amountfrom3,
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
                    time: data.time==''?1000:data.time,
                    time1: data.time1==''?1000:data.time1,
                    time2: data.time2==''?1000:data.time2,
                    time3: data.time3==''?1000:data.time3,
                    countmessage: data.countmessage,
                    countmessage1: data.countmessage1,
                    countmessage2: data.countmessage2,
                    countmessage3: data.countmessage3,
                    amountto: data.amountto==''?10:data.amountto,
                    amountto1: data.amountto1==''?10:data.amountto1,
                    amountto2: data.amountto2==''?10:data.amountto2,
                    amountto3: data.amountto3==''?10:data.amountto3,
                    amountfrom: data.amountfrom==''?100:data.amountfrom,
                    amountfrom1: data.amountfrom1==''?100:data.amountfrom1,
                    amountfrom2: data.amountfrom2==''?100:data.amountfrom2,
                    amountfrom3: data.amountfrom3==''?100:data.amountfrom3,
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
                    time: data.time==''?1000:data.time,
                    time1: data.time1==''?1000:data.time1,
                    time2: data.time2==''?1000:data.time2,
                    time3: data.time3==''?1000:data.time3,
                    countmessage: data.countmessage,
                    countmessage1: data.countmessage1,
                    countmessage2: data.countmessage2,
                    countmessage3: data.countmessage3,
                    amountto: data.amountto==''?10:data.amountto,
                    amountto1: data.amountto1==''?10:data.amountto1,
                    amountto2: data.amountto2==''?10:data.amountto2,
                    amountto3: data.amountto3==''?10:data.amountto3,
                    amountfrom: data.amountfrom==''?100:data.amountfrom,
                    amountfrom1: data.amountfrom1==''?100:data.amountfrom1,
                    amountfrom2: data.amountfrom2==''?100:data.amountfrom2,
                    amountfrom3: data.amountfrom3==''?100:data.amountfrom3,
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
		socket.on('SIMULATOR-GET-PARAMETER',function(data){
		//io.sockets.emit('new message',data);
		console.log('SIMULATOR-GET-PARAMETER:'+data);
		SimulatorConfig('Channels','SIMULATOR-PARAMETER-CONFIG-CHANNELS');
		SimulatorConfig('Products','SIMULATOR-PARAMETER-CONFIG-PRODUCT');
		SimulatorConfig('TransactionTypes','SIMULATOR-PARAMETER-CONFIG-TRANSACTIONTYPES');
		//console.log('msg:'+data);
	});
});

//-----------------connect mongodb------------------
function SimulatorConfig(tablename,parameterconfig)
{
	//console.log('connMongodb');
	MongoClient.connect(ADD_MONGODB_CLOUBBANK, function(err, db) {
	  if(err) { return console.dir(err); }
	  
	  var collection = db.collection(tablename);
			console.log(tablename);
			collection.find(
				//{FULLNAME: /^NGUYEN/ }
			).toArray(function(err, items) {
				if(items!=null&&items.length>0)
				{
					console.log('cnt:'+items.length);
					io.sockets.emit(parameterconfig, items);
				}
			});
			
			
	});
}

//HieuLD add method GetAccountNumberList
function GetAccountNumberList()
{
    MongoClient.connect(ADD_MONGODB_CLOUBBANK, function(err, db) {
        if(err) { return console.dir(err); }

        var collection = db.collection("AccNumbers");
        console.log("AccNumbers");
        collection.find({}, {ACCOUNTNUMBER:1, _id:0}
        ).toArray(function(err, items) {
                if(items!=null&&items.length>0)
                {
                    for (i=0; i<items.length; i++)
                        acc_nos.push(items[i].ACCOUNTNUMBER);
                }
            });


    });
}
GetAccountNumberList();

//--------------------------------------------------
