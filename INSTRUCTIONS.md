#Developer Instructions

This project is split into two specific parts; reasoner, and server. The reasoner is an Android application, and the other is server infrastructure for logging events from said reasoner.

###Building Reasoner
To build the Android reasoner, you will need to edit the reasoner AndroidManifest.xml, and add the following values:

* openWeatherMap_ApiKey : The API key for getting OpenWeatherMap data. You can get an API from https://openweathermap.org/
* contextService_ApiKey : A String of your choosing to act as an API key, to avoid unauthorised clients making server calls. I'd suggest a large HEX.
* contextService_Host : The host where the server will be running.


###Build Server
To build the Server, you will need to edit the `uk.ac.mdx.cs.ie.contextserver.ContextServer` class file and add the following:

* API_KEY : The same API key as you placed for item "contextService_ApiKey" in the reasoner.

You will also need to edit `uk.ac.mdx.cs.ie.contextserver.MySQLDatabase` class with the relevent database/user details

To build, you will need to use the command: `gradlew installDist`
This command will create a folder named `install` in the `build` folder.

To run the server, the user just needs to go to `build/install/server/bin` and run either `context-service` in Unix/Linux based OSs or `context-service.bat` in Windows based OSs