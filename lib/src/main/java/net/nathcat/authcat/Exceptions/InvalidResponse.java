package net.nathcat.authcat.Exceptions;

public class InvalidResponse extends Exception {
    private final int code;

    public InvalidResponse(int code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "AuthCat replied with response code " + code;
    }
}
