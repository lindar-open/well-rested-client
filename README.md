# well-rested-client

Want a nice rest client that just works without too much headache? 
Try this well rested and ready to go client built on top of apache http client

Examples:

**GET Request:**

``` java
ResponseVO serverResponse = WellRestedRequest.build(url).get();
```

The ResponseVO class contains the status code returned by the server and the server response returned as String.


**POST Request:**

**How to post a json string directly:** 

``` java
ResponseVO serverResponse = WellRestedRequest.build(url).post(jsonString);
```

**How to post an object as json:**

``` java
ResponseVO serverResponse = WellRestedRequest.build(url).post(myObj);
```

**How to post an xml string directly:**

``` java
ResponseVO serverResponse = WellRestedRequest.build(url).post(xmlString, ContentType.APPLICATION_XML);
```

More examples to come...
