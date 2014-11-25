ersys
=====

This repo contains two android application code and one nodejs server code

The helper and responder are the android applications. Pusher is the back end node.js server which get the information 
from helper application and send notification to the responder application.

Helper :
	Helper application will be provided to the public.
	When a person is in emergency, he has to press the button named "emergency" .
	The person's location (latitude and longitude) is sent to the back end server.
 
Pusher :
	On receiving the information, pusher send notification to all the registered devices.
	It makes use of the push notification provided with the bluemix for this purpose.

Responder :
	The responder will be pin pointing the ambulance location.
	On receiving the notification from the server , the path from its location to the person's (in need of emergency) location    is drawn.
	
References :
  Tutorials given in bluemix documentation pages.
Other Websites referred :
  http://javapapers.com/
  https://developer.ibm.com/
