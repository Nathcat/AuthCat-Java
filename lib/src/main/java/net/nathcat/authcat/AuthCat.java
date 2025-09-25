package net.nathcat.authcat;

import java.io.IOException;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import net.nathcat.authcat.Exceptions.InvalidResponse;

/**
 * <p>An integration for making requests to the AuthCat service.</p>
 * <p>
 *     This class contains methods which will allow you to easily make requests to the AuthCat service for better
 *     integration with your applications.
 * </p>
 * @version 2.0.0
 * @author Nathan Baines
 * @see <a href="https://data.nathcat.net/sso">AuthCat</a>
 */
public class AuthCat {

    private IHttpProvider httpProvider = new SEHttpProvider();

    /**
     * Create a new AuthCat instance with a custom HTTP provider.
     * @param httpProvider
     */
    public AuthCat(IHttpProvider httpProvider) {
        this.httpProvider = httpProvider;
    }

    /**
     * Create a new AuthCat instance using the default HTTP provider (SEHttpProvider).
     */
    public AuthCat() { this.httpProvider = new SEHttpProvider(); }

    /**
     * Call the authentication service from AuthCat.
     * @param authEntry The supplied authentication data, should contain a username and password field
     * @return The AuthResult
     * @throws IOException Thrown if an I/O error occurs when communicating with the service
     * @throws InterruptedException Thrown if the connection with the service is interrupted
     * @throws InvalidResponse Thrown if the service responds with an unexpected or invalid response code
     */
    public AuthResult tryLogin(JSONObject authEntry) throws InvalidResponse {
        Map<String, String> headers = Map.of(
            "Content-Type", "application/json"
        );

        HttpResponse response = httpProvider.post("https://data.nathcat.net/sso/try-login.php", authEntry, headers);

        if (response.statusCode == 200) {
            try {
                JSONObject data = (JSONObject) new JSONParser().parse(response.body);
                if (data.get("status").equals("success")) {
                    return new AuthResult((JSONObject) data.get("user"));
                }
                else {
                    return new AuthResult();
                }
            }
            catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            throw new InvalidResponse(response.statusCode);
        }
    }

    public AuthResult loginWithCookie(String cookie) throws InvalidResponse {
        Map<String, String> headers = Map.of(
            "Cookie", "AuthCat-SSO=" + cookie
        );
        
        HttpResponse response = httpProvider.get("https://data.nathcat.net/sso/get-session.php", headers);

        if (response.statusCode == 200) {
            String body = response.body;

            if (body.contentEquals("[]")) return new AuthResult();

            try {
                return new AuthResult((JSONObject) ((JSONObject) new JSONParser().parse(body)).get("user"));
            }
            catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            throw new InvalidResponse(response.statusCode);
        }
    }

    /**
     * Call the user search service
     * @param searchData The JSON search data, must contain either a username field, or a fullName field, or both
     * @return The JSON response from the server
     * @throws IOException Thrown if an I/O error occurs when communicating with the service
     * @throws InterruptedException Thrown if the connection with the service is interrupted
     * @throws InvalidResponse Thrown if the service responds with an unexpected or invalid response
     */
    public JSONObject userSearch(JSONObject searchData) throws InvalidResponse {
        HttpResponse response = httpProvider.post("https://data.nathcat.net/sso/user-search.php", searchData, Map.of("Content-Type", "application/json"));

        if (response.statusCode == 200) {
            try {
                return (JSONObject) new JSONParser().parse(response.body);
            }
            catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            throw new InvalidResponse(response.statusCode);
        }
    }

    /**
     * Send an authentication request to AuthCat
     * @param user The user data
     * @return The AuthResult returned from the authentication request
     */
    public AuthResult tryAuthenticate(JSONObject user) {
        // Attempt to log in with AuthCat
        AuthResult authCatResponse;

        try {
            if (user.containsKey("cookieAuth")) {
                authCatResponse = loginWithCookie((String) user.get("cookieAuth"));

                if (!authCatResponse.result && user.containsKey("username") && user.containsKey("password")) {
                    // Try normal authentication
                    user.remove("cookieAuth");
                    authCatResponse = tryAuthenticate(user);
                }
            } else {
                authCatResponse = tryLogin(user);
            }
        } catch (InvalidResponse e) {
            throw new RuntimeException(e);
        }

        return authCatResponse;
    }

    /**
     * Use a quick auth token for authentication.
     * @param token The quick auth token to use
     * @return The AuthResult 
     * @throws InterruptedException 
     * @throws IOException 
     * @throws ParseException 
     */
    public AuthResult tokenAuth(String token) throws ParseException {
        JSONObject body = new JSONObject();
        body.put("quick-auth-token", token);
        
        JSONObject result = (JSONObject) new JSONParser().parse(httpProvider.post("https://data.nathcat.net/sso/try-login.php", body, Map.of("Content-Type", "application/json")).toString());
        
        if (result.get("status").equals("success")) {
            return new AuthResult((JSONObject) result.get("user"));
        }
        
        return new AuthResult();
    }

    /**
     * Generate a quick auth token for a user
     * @param user The user to generate the quick auth token for
     * @return The quick auth token
     * @throws InterruptedException 
     * @throws IOException 
     * @throws ParseException 
     */
    public String createAuthToken(JSONObject user) throws ParseException {
        JSONObject result = (JSONObject) new JSONParser().parse(
            httpProvider.post("https://data.nathcat.net/sso/create-quick-auth.php", user, Map.of("Content-Type", "application/json")).body
        );

        if (result.get("status").equals("success")) {
            return (String) result.get("token");
        }

        return null;
    }
}
