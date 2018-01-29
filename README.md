# well-rested-client

Want a nice rest client that just works without too much headache? 
Try this well rested and ready to go client built on top of apache http client

Check out the [Releases](https://github.com/lindar-open/well-rested-client/releases) page for detailed info about each release. 

Examples:

**GET Request:**

``` java
ResponseVO serverResponse = WellRestedRequest.builder().url(url).build().get().submit();
```

The ResponseVO class contains the status code returned by the server and the server response returned as String.


**POST Request:**

**How to post a json string directly:** 

``` java
ResponseVO serverResponse = WellRestedRequest.builder().url(url).build().post().jsonContent(jsonString).submit();
```

**How to post an object as json:**

``` java
ResponseVO serverResponse = WellRestedRequest.builder().url(url).build().post().jsonContent(myObj).submit();
```

**How to post an xml string directly:**

``` java
ResponseVO serverResponse = WellRestedRequest.builder().url(url).build().post().xmlContent(xmlString).submit();
```

More examples to come...

Usage: 

```xml
<dependency>
    <groupId>com.lindar</groupId>
    <artifactId>well-rested-client</artifactId>
    <version>1.4.1</version>
</dependency>
```
