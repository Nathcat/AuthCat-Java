package net.nathcat.authcat;

import java.io.IOException;
import java.util.Map;

import org.json.simple.JSONObject;

/**
 * An interface defining methods for making HTTP requests.
 * This interface may be reimplemented to provide custom methods of making HTTP requests.
 * For example, if the java.net.http package is not available in your environment.
 * @author Nathan Baines
 */
public interface IHttpProvider {
    public HttpResponse post(String url, JSONObject body, Map<String, String> headers);
    public HttpResponse get(String url, Map<String, String> headers);
}