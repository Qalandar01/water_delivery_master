package uz.pdp.water_delivery.bot;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.InetAddress;

@Profile("prod")
@Component
public class WebhookFilter extends OncePerRequestFilter {
    private final String[] allowedCidrs = {
            "149.154.160.0/20",
            "91.108.4.0/22"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (!path.equals("/api/webhook")) {
            // skip IP check, continue normally
            filterChain.doFilter(request, response);
            return;
        }

        String remoteAddr = request.getRemoteAddr();
        InetAddress address = InetAddress.getByName(remoteAddr);

        boolean allowed = false;
        for (String cidr : allowedCidrs) {
            if (IpUtils.isInRange(address, cidr)) {
                allowed = true;
                break;
            }
        }

        if (!allowed) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
            return;
        }
        filterChain.doFilter(request, response);
    }

}
