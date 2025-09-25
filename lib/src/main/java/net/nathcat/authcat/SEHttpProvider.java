package net.nathcat.authcat;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

/**
 * An implementation of IHttpProvider using the java.net.http package.
 * @author Nathan Baines
 */
public class SEHttpProvider implements IHttpProvider {
    @Override
    public net.nathcat.authcat.HttpResponse post(String url, org.json.simple.JSONObject body, Map<String, String> headers) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest.Builder b = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.ofString(body.toJSONString()));

            for (String key : headers.keySet()) {
                b.setHeader(key, headers.get(key));
            }

            HttpRequest request = b.build();

            HttpResponse<String> r = client.send(request, HttpResponse.BodyHandlers.ofString());

            return new net.nathcat.authcat.HttpResponse(r.statusCode(), r.body());
        }
        catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public net.nathcat.authcat.HttpResponse get(String url, Map<String, String> headers) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest.Builder b = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET();

            for (String key : headers.keySet()) {
                b.setHeader(key, headers.get(key));
            }

            HttpRequest request = b.build();

            HttpResponse<String> r = client.send(request, HttpResponse.BodyHandlers.ofString());

            return new net.nathcat.authcat.HttpResponse(r.statusCode(), r.body());
        }
        catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
