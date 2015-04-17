package net.spauny.joy.wellrested.request;

import org.apache.http.HttpResponse;

/**
 *
 * @author iulian.dafinoiu
 */
@FunctionalInterface
public interface HttpClientExecutor {
    HttpResponse executeClient() throws Exception;
}
