package net.nathcat.authcat;

public class HttpResponse {
    public final int statusCode;
    public final String body;

    public HttpResponse(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
    }
}
