var express = require('express');
var router = express.Router();

/* GET home page. */
router.get('/', function(req, res, next) {

  	console.log('Dashboard request');
	//res.render('index', { title: 'Dashboard' });
	var db = req.db;
	var TransactionTypes = db.collection('TransactionTypes');
    TransactionTypes.find({}).toArray(function(error, transactionTypes) {
	    if (error) return next(error);
	    if (!transactionTypes) return next(new Error('Get list transactionTypes fail.'));

	    res.render('index', {
	      title: 'Dashboard',
	      TransactionTypes: transactionTypes || []
	    });
  	});

});

router.get('/productchart', function(req, res, next) {

  	console.log('Dashboard request');
	//res.render('index', { title: 'Dashboard' });
	var db = req.db;
	var TransactionTypes = db.collection('TransactionTypes');
    TransactionTypes.find({}).toArray(function(error, transactionTypes) {
	    if (error) return next(error);
	    if (!transactionTypes) return next(new Error('Get list transactionTypes fail.'));

	    res.render('productchart', {
	      title: 'Dashboard',
	      TransactionTypes: transactionTypes || []
	    });
  	});

});

router.get('/transactionchart', function(req, res, next) {

  	console.log('Dashboard request');
	//res.render('index', { title: 'Dashboard' });
	var db = req.db;
	var TransactionTypes = db.collection('TransactionTypes');
    TransactionTypes.find({}).toArray(function(error, transactionTypes) {
	    if (error) return next(error);
	    if (!transactionTypes) return next(new Error('Get list transactionTypes fail.'));

	    res.render('transactionchart', {
	      title: 'Dashboard',
	      TransactionTypes: transactionTypes || []
	    });
  	});

});

router.get('/branchtransactionchart', function(req, res, next) {

  	console.log('Dashboard request');
	//res.render('index', { title: 'Dashboard' });
	var db = req.db;
	var TransactionTypes = db.collection('TransactionTypes');
    TransactionTypes.find({}).toArray(function(error, transactionTypes) {
	    if (error) return next(error);
	    if (!transactionTypes) return next(new Error('Get list transactionTypes fail.'));

	    res.render('branchtransactionchart', {
	      title: 'Dashboard',
	      TransactionTypes: transactionTypes || []
	    });
  	});

});

router.get('/stackbar', function(req, res, next) {

  	console.log('Dashboard request');
	//res.render('index', { title: 'Dashboard' });
	var db = req.db;
	var TransactionTypes = db.collection('TransactionTypes');
    TransactionTypes.find({}).toArray(function(error, transactionTypes) {
	    if (error) return next(error);
	    if (!transactionTypes) return next(new Error('Get list transactionTypes fail.'));

	    res.render('stackbar', {
	      title: 'Dashboard',
	      TransactionTypes: transactionTypes || []
	    });
  	});

});

router.get('/customers', function(req, res) {
	console.log('customers request');

	var db = req.db;
	var Customers = db.collection('Customers');
    Customers.find({}).skip(1).limit(300).toArray(function(error, customers) {
	    if (error) return next(error);
	    if (!customers) return next(new Error('Get listcustomers fail.'));

	    res.render('customers', {
	      title: 'Customers',
	      customers: customers || []
	    });
  	});
});

router.get('/profile', function(req, res) {
	console.log('profile request');	
	res.render('profile', { title: 'profile' });
});


router.get('/profile/:id', function(req, res, next) {
	var resCustomer, resCustomerLog;
	var db = req.db;
	var Customers = db.collection('Customers');
    //Customers.find({ACCOUNTNUMBER:req.params.id}).toArray(function(error, customer) {
	Customers.findOne({ACCOUNTNUMBER:req.params.id}, function(error, customer) {
	    if (error) return next(error);
	    if (!customer) return next(new Error('Get detail fail.'));

	    resCustomer	= customer;
	    var INTERACTION = JSON.parse(resCustomer.INTERACTION);

	    var CustomerLogs = db.collection('CustomerLogs');
	    CustomerLogs.find({acc_no:req.params.id}).sort({timestamp: -1}).limit(5).toArray(function(error, customerLogs) {
			if (error) return next(error);
	    	if (!customerLogs) return next(new Error('Get top 5 fail.'));
	    	
	    	res.render('profile', {
			    title: 'Profile',
			    customer: resCustomer || [],
			    INTERACTION: INTERACTION,
			    customerLogs: customerLogs || []
			});
    	});
	    
  	});
});

//chart display
router.get('/transactiontypechart', function(req, res) {
	console.log('chart request');	
	res.render('transactiontypechart', { title: 'transactiontypechart' });
});

//chart display
router.get('/transactionchart', function(req, res) {
	console.log('chart request');
	res.render('transactionchart', { title: 'transactionchart' });
});

//chart display
router.get('/chart', function(req, res) {
	console.log('chart request');
	res.render('chart', { title: 'chart' });
});

//Point chart display
router.get('/PointChart', function(req, res) {
	console.log('PointChart request');
	res.render('PointChart', { title: 'PointChart' });
});

//Point chart display
router.get('/ChannelByTran', function(req, res) {
	console.log('ChannelByTran request');
	res.render('ChannelByTran', { title: 'ChannelByTran' });
});

//smoothie chart display
router.get('/smoothieChart', function(req, res, next) {

	console.log('smoothieChart request');
	//res.render('index', { title: 'Dashboard' });
	var db = req.db;
	var TransactionTypes = db.collection('TransactionTypes');
	TransactionTypes.find({}).toArray(function(error, transactionTypes) {
		if (error) return next(error);
		if (!transactionTypes) return next(new Error('Get list transactionTypes fail.'));

		res.render('smoothieChart', {
			title: 'smoothieChart',
			TransactionTypes: transactionTypes || []
		});
	});

});

module.exports = router;
