var express = require('express');
var router = express.Router();

/* GET home page. */
router.get('/', function(req, res, next) {
  res.render('index', { title: 'Dashboard' });
});

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

module.exports = router;
