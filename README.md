AppDynamics Data Funnel
==============

Description
--------------

a generalized data extraction / data transformation / data transport application. The basic use case for this application is:

1. Extracting data from a controller (metrics or otherwise)
2. Performing some form of transformation on this data
3. Passing the transformed data on - writing to a file store, or sending on to big data store

Currently, the application can leverage around 90% of the available REST API (including metrics). It is easily configurable via an XML file. Transformation is handled via plugins you can easily write in java. Output is also handled via plugins you can easily write in Java.

Currently, you can setup to run

1. Several data extracts in a batch (in parallel or in sequence)
2. Transformation - simple XML transformation plugin provided out of the box. Also inclues a basic calculation plugin as well.
3. Output - currently will write to system output stream, files, Flume/Hadoop big data stores, or put metrics back into AppD via machine agent

The application takes commands that allow it to be run periodically, so you could easily use this to create a stream of data to the backing store of your choice. A SQL plugin could also easily be written to send data to another database. In the end, any use case you can think of to extract / transform / pass on data from a controller can easily be handled via this app.

I wrote this mainly as an exercise to get to know the REST API, but I think it could have some real world uses.

Building
--------------
This project was built in Eclipse. The required libraries are also checked in. You will need to include the following libraries in order to build the project
- commons-cli-1.2.jar
- commons-codec-1.6.jar
- commons-logging-1.1.3.jar
- httpclient-4.3.1.jar
- httpclient-cache-4.3.1.jar
- httpcore-4.3.jar
- dom4j-1.6.1.jar
- jaxen-1.1.6.jar

Running
--------------
Look at the examples.xml file for some ways in which you can configure the types of data you can extract and the required parameters. This XML file is pretty well documented.  Email chris.stpierre@appdynamics.com with any questions.

