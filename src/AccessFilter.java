import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class AccessFilter extends Filter {
    private final byte[] expected;

    public AccessFilter(String password) {
        expected = ("potentialman:" + password).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        String header = exchange.getRequestHeaders().getFirst("Authorization");
        if (header != null && header.startsWith("Basic ")) {
            try {
                byte[] supplied = Base64.getDecoder().decode(header.substring(6));
                if (MessageDigest.isEqual(expected, supplied)) {
                    chain.doFilter(exchange);
                    return;
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
        exchange.getResponseHeaders().set("WWW-Authenticate", "Basic realm=\"PotentialMan\"");
        exchange.sendResponseHeaders(HttpURLConnection.HTTP_UNAUTHORIZED, -1);
        exchange.close();
    }

    @Override
    public String description() {
        return "Password for private testing";
    }
}
