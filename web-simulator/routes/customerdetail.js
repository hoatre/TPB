
/*
 * GET users listing.
 */
/*var MongoClient = require('mongodb').MongoClient;
var express = require('express')
  , routes = require('./routes')
  , user = require('./routes/user')
  //, http = require('http')
  , app = express()
  , path = require('path')
  , server = require('http').createServer(app)
  , io = require('socket.io').listen(server);*/


exports.list = function(req, res){
	res.render('customerdetail', { title: 'Customer Detail', _id:req.params.quang_id });
};

