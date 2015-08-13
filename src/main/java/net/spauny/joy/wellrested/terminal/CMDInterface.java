package net.spauny.joy.wellrested.terminal;

import java.util.Scanner;
import net.spauny.joy.wellrested.request.HttpRequestProcessor;
import net.spauny.joy.wellrested.request.HttpsRequestProcessor;
import net.spauny.joy.wellrested.vo.ResponseVO;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author iulian.dafinoiu
 */
public class CMDInterface {
    
    public static void main(String args[]) {
        if (args.length > 0) {
            if (args[0].equals("execute")) {
                if (args.length > 1) {
                    System.out.println("Would you like to add proxy config? (host and url) (Y/N): ");
                    Scanner scanner = new Scanner(System.in);
                    String userResp = scanner.next();
                    
                    String proxyHost = StringUtils.EMPTY;
                    String proxyPort = StringUtils.EMPTY;
                    if (userResp.equalsIgnoreCase("y") || userResp.equalsIgnoreCase("yes")) {
                        System.out.println("Insert proxy host: ");
                        proxyHost = scanner.next();
                        System.out.println("Insert proxy port: ");
                        proxyPort = scanner.next();
                    }
                    
                    String url = args[1];
                    ResponseVO response;
                    if (url.contains("https")) {
                        boolean trustAll = true;
                        if (args.length > 2) {
                            trustAll = Boolean.parseBoolean(args[2]);
                        }
                        if (StringUtils.isNotBlank(proxyHost) && StringUtils.isNotBlank(proxyPort)) {
                            response = new HttpsRequestProcessor(url, proxyHost, Integer.parseInt(proxyPort)).processGetRequest();
                        } else {
                            response = new HttpsRequestProcessor(url, trustAll).processGetRequest();
                        }
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
