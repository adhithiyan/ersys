/*
 * Copyright 2014 IBM Corp. All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
var express = require('express'),
    bodyparser = require('body-parser'),
    ibmbluemix = require('ibmbluemix'),
    ibmpush = require('ibmpush');
	
//configuration for application
var appConfig = {
    applicationId: "3fbb36ce-2715-48da-aa57-bc426dc2928b",
	applicationRoute: "pusher.mybluemix.net"
};
// create an express app
var app = express();
app.use(bodyparser.json());
app.use(bodyparser.urlencoded({
  extended: true
}));

//uncomment below code to protect endpoints created afterwards by MAS
//var mas = require('ibmsecurity')();
//app.use(mas);


//initialize mbaas-config module
ibmbluemix.initialize(appConfig);
var logger = ibmbluemix.getLogger();

app.use(function(req, res, next) {
	req.ibmpush = ibmpush.initializeService(req);
	req.logger = logger;
	next();
});

//initialize ibmconfig module
var ibmconfig = ibmbluemix.getConfig();

//get context root to deploy your application
//the context root is '${appHostName}/v1/apps/${applicationId}'
var contextRoot = ibmconfig.getContextRoot();
appContext=express.Router();
app.use(contextRoot, appContext);

console.log("contextRoot: " + contextRoot);

// log all requests
app.all('*', function(req, res, next) {
	console.log("Received request to " + req.url);
	next();
});

// create resource URIs
// endpoint: https://mobile.ng.bluemix.net/${appHostName}/v1/apps/${applicationId}/notifyOtherDevices/
appContext.post('/notifyOtherDevices', function(req,res) {
	console.log("Trying to send push notification via JavaScript Push SDK");
	
	var lat = req.body.lat;
	var lon = req.body.lon;
	var position = lat + ","+ lon ;
	
	var message = { "alert" : "Incoming Alert..!" ,
					"url" : position };
	
	req.ibmpush.sendBroadcastNotification(message,null).then(function (response) {
		console.log("Notification sent successfully to all devices.", response);
		res.send("ERSYS is on the way...");
	}, function(err) {
		console.log("Failed to send notification to all devices.");
		console.log(err);
		res.send(400, {reason: "An error occurred while sending the Push notification.", error: err});
	});
});

// host static files in public folder
// endpoint:  https://mobile.ng.bluemix.net/${appHostName}/v1/apps/${applicationId}/static/
appContext.use('/static', express.static('public'));

//redirect to cloudcode doc page when accessing the root context
app.get('/', function(req, res){
	res.sendfile('public/index.html');
});

app.listen(ibmconfig.getPort());
console.log('Server started at port: '+ibmconfig.getPort());
