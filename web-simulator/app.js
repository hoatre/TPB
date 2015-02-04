
/**
 * Module dependencies.
 */
var MongoClient = require('mongodb').MongoClient;
var express = require('express')
  , routes = require('./routes')
  , user = require('./routes/user')
  , customerlist = require('./routes/customerlist')
  , customerdetail = require('./routes/customerdetail')
  , simulator = require('./routes/simulator')
  , dashboad = require('./routes/dashboad')
  //, http = require('http')
  , app = express()
  , path = require('path')
  , server = require('http').createServer(app)
  , io = require('socket.io').listen(server);

//---------------variable--------------------------
var ADD_KAFKA='localhost:2181';
//var ADD_KAFKA='10.20.252.201:2181';
var ADD_MONGODB_CIC="mongodb://10.20.252.202:27017/CIC";
var ADD_MONGODB_CLOUBBANK="mongodb://10.20.252.202:27017/CloudBank";
//---------------variable--------------------------

// all environments
//app.set('port', process.env.PORT || 3000);
app.set('views', __dirname + '/views');
app.set('view engine', 'ejs');
app.use(express.favicon());
app.use(express.logger('dev'));
app.use(express.bodyParser());
app.use(express.methodOverride());
app.use(app.router);
app.use(express.static(path.join(__dirname, 'public')));

// development only
if ('development' == app.get('env')) {
  app.use(express.errorHandler());
}

app.get('/', routes.index);
app.get('/users', user.list);
app.get('/customerlist', customerlist.list);
app.get('/customerdetail/:quang_id', customerdetail.list);
app.get('/simulator', simulator.list);
app.get('/dashboad', dashboad.list);


server.listen(3000);
console.log('Server running at http://127.0.0.1:3000/');
app.use(express.static(__dirname + '/lib'));
/*http.createServer(app).listen(app.get('port'), function(){
  console.log('Express server listening on port ' + app.get('port'));
});*/
//--------------------------------------customer---------------------------------------------
var customer={
	fullname:''
}
var lstcustormer=[];
var arr;
function connMongodb(data)
{
	MongoClient.connect(ADD_MONGODB_CLOUBBANK, function(err, db) {
		if(err) { return console.dir(err); }

		var collection = db.collection('Customers');

		/*collection.find(
		 {FULLNAME: /^NGUYEN/ }
		 ).skip(1).limit(10).toArray(function(err, items) {
		 if(items!=null&&items.length>0)
		 {
		 console.log('FULLNAME:'+items[0].FULLNAME);
		 io.sockets.emit('CUSTOMER_LIST_DATA', items);
		 }
		 });*/
		if(data.value=='ALL')
		{
			var obj={data:[],count:0}
			collection.find(
				//{FULLNAME: /^NGUYEN/ }
			).skip(data.start).limit(data.end).toArray(function(err, items) {
					if(items!=null&&items.length>0)
					{
						//console.log('FULLNAME:'+items[0].FULLNAME);
						obj.data=items;
						//io.sockets.emit('CUSTOMER_LIST_DATA', items);
						collection.count(function(err, count) {
							if(count>0)
							{
								console.log('FULLNAME:'+obj.data[0].FULLNAME);
								obj.count=count;
								io.sockets.emit('CUSTOMER_LIST_DATA', obj);
							}
						});
					}
				});


		}
		else
		{
			var obj={data:[],count:0}
			var seachValue = data.value.toUpperCase();
			//var seachValue=data.value.toUpperCase();
			console.log('value:'+seachValue);
			collection.find(
				{FULLNAME: new RegExp(seachValue) }
			).skip(data.start).limit(data.end).toArray(function(err, items) {
					if(items!=null&&items.length>0)
					{
						//console.log('FULLNAME:'+items[0].FULLNAME);
						obj.data=items;
						//io.sockets.emit('CUSTOMER_LIST_DATA', items);
						collection.count({FULLNAME: new RegExp(seachValue)} ,function(err, count) {
							if(count>0)
							{
								console.log('FULLNAME:'+obj.data[0].FULLNAME);
								obj.count=count;
								io.sockets.emit('CUSTOMER_LIST_DATA', obj);
							}
						});
					}
				});
		}
	});
}


var acc_no='';
function GetTop5CustomerLogs()
{
    //console.log('------------------------: '+acc_no);
    setTimeout(GetTop5CustomerLogs,2000);
    if(acc_no!=''&&acc_no!=null)
    {
        MongoClient.connect(ADD_MONGODB_CLOUBBANK, function(err, db) {
                if(err) { return console.dir(err); }

                var collection = db.collection('CustomerLogs');

                //var obj={data:[],count:0}
                //var seachValue = data.value.toUpperCase();
                //var seachValue=data.value.toUpperCase();
                console.log('GetTop5CustomerLogs: '+acc_no);
                collection.find(
                    {acc_no: acc_no }
                ).sort({timestamp: -1}).skip(0).limit(5).toArray(function(err, items) {
                        if(items!=null&&items.length>0)
                        {

                                    io.sockets.emit('CUSTOMERLOGS_TOP5_TRANSACTION_DATA', items);

                        }
                });

        });
    }
}



io.sockets.on('connection',function(socket){
	socket.on('CUSTOMER_LIST_SEND_MESSAGE',function(data){
		//io.sockets.emit('new message',data);
		connMongodb(data);
		console.log('msg:'+data.value+"--start:"+data.start+"--end:"+data.end);
	});
	socket.on('CUSTOMERLOGS_TOP5_TRANSACTION_SEND_MESSAGE',function(data){
		//io.sockets.emit('new message',data);
		//connMongodb(data);
        acc_no=data;
        GetTop5CustomerLogs();
		console.log('CUSTOMERLOGS_TOP5_TRANSACTION_SEND_MESSAGE:'+data);
	});

    socket.on('customerID',function(data){
        getDetail(data);
        //console.log('msg:'+data.value+"--start:"+data.start+"--end:"+data.end);
    });
});

function getDetail(customerID)
{
    console.log('customerID:'+customerID);
    
    MongoClient.connect(ADD_MONGODB_CLOUBBANK, function(err, db) {
      if(err) { return console.dir(err); }
      
      var collection = db.collection('Customers');
            console.log('Customers');
            collection.find(
                {ACCOUNTNUMBER: customerID }
            ).toArray(function(err, items) {
                if(items!=null&&items.length>0)
                {
                    console.log('cnt:'+items.length);
                    io.sockets.emit('_customerDetail', items);
                }
            });
            
            
    });
}
//--------------------------------------customer---------------------------------------------

//--------------------------------------simulator---------------------------------------------

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
    account: '',
    account1: '',
    account2: '',
    account3: '',
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
        send(GeneratorTransaction(obj.account,obj.amountto,obj.amountfrom,obj.channel, obj.product, obj.transactiontype, cnt));
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
        send(GeneratorTransaction(obj.account1,obj.amountto1,obj.amountfrom1,obj.channel1, obj.product1, obj.transactiontype1, cnt1));
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
        send(GeneratorTransaction(obj.account2,obj.amountto2,obj.amountfrom2,obj.channel2, obj.product2, obj.transactiontype2, cnt2));
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
        send(GeneratorTransaction(obj.account3,obj.amountto3,obj.amountfrom3,obj.channel3, obj.product3, obj.transactiontype3, cnt3));
    }
}

//Ramdom Int
function randomInt (low, high) {
    return Math.floor(Math.random() * (parseInt(high) - parseInt(low)) + parseInt(low));
}


//Generator transaction
var GeneratorTransaction = function(account,amountto,amountfrom,channal, product, transactionType, msgs)
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
        
    var acc_no = account==''?acc_nos[randomInt(0,acc_nos.length)]:account;

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
    //-------------------------------------------------
	 /*MongoClient.connect(ADD_MONGODB_CLOUBBANK, function(err, db) {
	 if(err) { return console.dir(err); }
	  
	  var collection = db.collection("AccNumbers");
			console.log("AccNumbers");
			collection.find(
				//{FULLNAME: /^NGUYEN/ }
			).toArray(function(err, items) {
				if(items!=null&&items.length>0)
				{
					//console.log('cnt:'+items.length);
					//io.sockets.emit(parameterconfig, items);
				        //acc_no=items[randomInt(0,acc_nos.length)].ACCOUNTNUMBER;
					trans.acc_no=items[0].ACCOUNTNUMBER;
					//console.log('ACCOUNTNUMBER:'+acc_no);
					//console.log('ACCOUNTNUMBER:'+acc_no);
				}
			});
		
					    	
			
	});*/
	//-------------------------------------------------
	console.log('ACCOUNTNUMBER:'+trans.acc_no);
    
	var msg = JSON.stringify(trans);
	return msg;	
}


io.sockets.on('connection',function(socket){
		socket.on('SIMULATOR_LIST_SEND_MESSAGE',function(data){

        //Option 1
		console.log('SIMULATOR_LIST_SEND_MESSAGE:'+data);

        obj = {
	    account: data.account,
	    account1: data.account1,
	    account2: data.account2,
	    account3: data.account3,
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
		    account: data.account,
		    account1: data.account1,
		    account2: data.account2,
		    account3: data.account3,
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
	            account: data.account,
		    account1: data.account1,
		    account2: data.account2,
		    account3: data.account3,
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
		    account: data.account,
		    account1: data.account1,
		    account2: data.account2,
		    account3: data.account3,
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
		    account: data.account,
		    account1: data.account1,
		    account2: data.account2,
		    account3: data.account3,
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
var acc_nos = [];
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

//--------------------------------------simulator---------------------------------------------

//--------------------------------------dashboad-----------------------------------------------

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


const redis = require('redis');
//const client1 = redis.createClient(6379, '10.20.252.201', {});
const client1 = redis.createClient();
//log('info', 'connected to redis server');

//server.listen(3000);
//const socket  = io.listen(server);

//app.get('/',function(req,res){
//    res.sendFile(__dirname+'/client.html')
//});
//console.log('Server running at http://10.20.252.201:3000/');
//app.use(express.static(__dirname + '/lib'));

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
t1 = setInterval(function() {
    setTime(time1);
}, 1000);
t2 = setInterval(function() {
    setTime(time2);
}, 1000);
t3 = setInterval(function() {
    setTime(time3);
}, 1000);

clearInterval(t2);
clearInterval(t3);
clearInterval(t1);
io.sockets.on('connection',function(socket){
    socket.on('open 1 Minute', function (data1) {
        log('warn', "open 1 Minute");
        clearInterval(t2);
        clearInterval(t3);
        t1 = setInterval(function() {
            setTime(time1);
        }, 1000);
    }),
        socket.on('open 1 hour', function (data1) {
            log('warn', "open 1 hour");
            clearInterval(t1);
            clearInterval(t3);
            t2 = setInterval(function() {
                setTime(time2);
            }, 1000);
        }),
        socket.on('open 1 day', function (data1) {
            log('warn', "open 1 day");
            clearInterval(t2);
            clearInterval(t1);
            t3 = setInterval(function() {
                setTime(time3);
            }, 1000);
        })
});

function setTime(slidingTime){
    redis_get('real-time-count-' + Branch1 + '-' + slidingTime);
    redis_get('real-time-count-' + Branch2 + '-' + slidingTime);
    redis_get('real-time-count-' + Branch3 + '-' + slidingTime);
    redis_get('real-time-count-' + Contact_Center + '-' + slidingTime);
    redis_get('real-time-sum-' + Branch1 + '-' + slidingTime);
    redis_get('real-time-sum-' + Branch2 + '-' + slidingTime);
    redis_get('real-time-sum-' + Branch3 + '-' + slidingTime);
    redis_get('real-time-sum-' + Contact_Center + '-' + slidingTime);
    for(z=1; z <= 5; z++) {
        redis_hmget_top('TopTen' + Deposit + '-Top' + z.toString() + "-" + slidingTime);
        redis_hmget_top('TopTen' + Withdrawal + '-Top' + z.toString() + "-" + slidingTime);
        redis_hmget_top('TopTen' + Deposit + '-Bot' + z.toString() + "-" + slidingTime);
        redis_hmget_top('TopTen' + Withdrawal + '-Bot' + z.toString() + "-" + slidingTime);
        redis_hmget_top('TopTen' + Transfer_From + '-Bot' + z.toString() + "-" + slidingTime);
        redis_hmget_top('TopTen' + Transfer_From + '-Top' + z.toString() + "-" + slidingTime);
    }
}

//--------------------------------------dashboad-----------------------------------------------
