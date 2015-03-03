var express = require('express');
var router = express.Router();

/* GET home page. */
// router.get('/', function(req, res, next) {
//   res.render('index', { title: 'Dashboard' });
// });

router.get('/customers', function(req, res) {
	console.log('customers request');

	var db = req.db;
	var Customers = db.collection('Customers');
    Customers.find({}).skip(1).limit(30).toArray(function(error, customers) {
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

	    var CustomerLogs = db.collection('CustomerLogs');
	    CustomerLogs.find({acc_no:req.params.id}).sort({timestamp: -1}).limit(5).toArray(function(error, customerLogs) {
			if (error) return next(error);
	    	if (!customerLogs) return next(new Error('Get top 5 fail.'));
	    	
	    	res.render('profile', {
			    title: 'Profile',
			    customer: resCustomer || [],
			    customerLogs: customerLogs || []
			});
    	});
	    
  	});
});

router.get('/', function(req, res, next) {
	var resChannels, resProducts, resTransactionTypes;
	var db = req.db;
	var Channels = db.collection('Channels');
    
	Channels.find({}).toArray(function(error, channels) {
	    if (error) return next(error);
	    if (!channels) return next(new Error('Get all channel fail.'));

	    resChannels	= channels;   


	  	var Products = db.collection('Products');
	    Products.find({}).toArray(function(error, products) {
			if (error) return next(error);
	    	if (!products) return next(new Error('Get all product fail.'));

	    	resProducts	= products;  

			var TransactionTypes = db.collection('TransactionTypes');
		    TransactionTypes.find({}).toArray(function(error, transactionTypes) {
				if (error) return next(error);
		    	if (!transactionTypes) return next(new Error('Get all transaction type fail.'));

		    	resTransactionTypes	= transactionTypes;

		    	res.render('simulator', {
				    title: 'Simulator',
				    channels: resChannels || [],
				    products: resProducts || [],
				    transactionTypes: resTransactionTypes || []
				});	
			});
		});
	});

});

module.exports = router;
