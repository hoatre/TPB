module.exports = {

	// the server host
	port: 3030,
	//serverKafka : '10.20.252.201',//10.20.252.201
	//serverRedis : '10.20.252.201',
	serverMongo : 'mongodb://localhost:27017/CloudBank?auto_reconnect',

	serverKafka : 'localhost',//10.20.252.201
	serverRedis : 'localhost',

	SlidingTime1 : 60000,
	SlidingTime2 : 3600000,
	SlidingTime3 : 86400000,

	SlidingTimeDelay1 : 1000,
	SlidingTimeDelay2 : 10000,
	SlidingTimeDelay3 : 100000
}