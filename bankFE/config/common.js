module.exports = {

	// the server host
	port: 3030,
	//serverKafka : '10.20.252.201',//10.20.252.201
	//serverRedis : '10.20.252.201',
	//serverMongo : 'mongodb://10.20.252.202:27017/CloudBank?auto_reconnect',

	serverKafka : 'localhost',//10.20.252.201
	serverRedis : 'localhost',
	serverMongo : 'mongodb://localhost:27017/CloudBank?auto_reconnect',

	SlidingTimeSecond : 1000,
	SlidingTimeMinute : 60000,
	SlidingTimeHour : 3600000,
	SlidingTimeDay : 86400000,

	SlidingTimeDelayMinute : 1000,
	SlidingTimeDelayHour : 60000,
	SlidingTimeDelayDay : 900000
}