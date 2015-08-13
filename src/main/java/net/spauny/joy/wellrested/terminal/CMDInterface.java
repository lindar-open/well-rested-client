package net.spauny.joy.wellrested.terminal;

import net.spauny.joy.wellrested.request.HttpRequestProcessor;
import net.spauny.joy.wellrested.request.HttpsRequestProcessor;
import net.spauny.joy.wellrested.vo.ResponseVO;

/**
 *
 * @author iulian.dafinoiu
 */
public class CMDInterface {
    
    public static void main(String args[]) {
        if (args.length > 0) {
            if (args[0].equals("execute")) {
                if (args.length > 1) {
                    String url = args[1];
                    ResponseVO response;
                    if (url.contains("https")) {
                        boolean trustAll = true;
                        if (args.length > 2) {
                            trustAll = Boolean.parseBoolean(args[2]);
                        }
                        response = new HttpsRequestProcessor(url, trustAll).processGetRequest();
                    } else {
                        response = new HttpRequestProcessor(url).processGetRequest();
                    }
                    System.out.println("Status got: " + response.getStatusCode());
                    System.out.println("Response Message: " + response.getServerResponse());
                } else {
                    System.err.println("You need an url after execute!");
                }
            } else {
                System.err.println("Run directly using: execute <url>");
                return;
            }
           
        } else {
            // implement command line interface
        }
    }
}
