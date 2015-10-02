# well-rested-client

Want a nice rest client that just works without too much headache? 
Try this well rested and ready to go client built on top of apache http client

You can have a quick look at the [Java Docs](http://htmlpreview.github.io/?https://github.com/spauny/well-rested-client/blob/master/apidocs/index.html) or keep reading :)

How is this rest client different from anything else? 

It has a lot of nice features but the most important thing is that you can easily send and process get calls, post calls, and lots more to be added.

Examples:

**GET Request:**

``` java
AbstractRequestProcessor requestProcessor = new HttpRequestProcessor(urlString);
ResponseVO serverResponse = requestProcessor.processGetRequest();
```

The ResponseVO class contains the status code returned by the server and the server response returned as String.


**POST Request:**

How to post a json string directly: 

``` java
AbstractRequestProcessor requestProcessor = new HttpRequestProcessor(urlString);
ResponseVO serverResponse = requestProcessor.processPostRequest(jsonString);
```

**How to post an object as json:**

``` java
AbstractRequestProcessor requestProcessor = new HttpRequestProcessor(urlString);
MyObject myObj = new MyObject();
ResponseVO serverResponse = requestProcessor.processPostRequest(myObj);
```

**How to post an xml string directly:**

``` java
AbstractRequestProcessor requestProcessor = new HttpRequestProcessor(urlString);
ResponseVO serverResponse = requestProcessor.processPostRequest(xmlString, ContentType.APPLICATION_XML);
```

**How to post a json string directly using HTTPS:**

``` java
AbstractRequestProcessor requestProcessor = new HttpsRequestProcessor(urlString);
ResponseVO serverResponse = requestProcessor.processPostRequest(jsonString);
```

Yes, that's right, exactly the same, you just change from HttpRequestProcessor to HttpsRequestProcessor 

More examples to come...
