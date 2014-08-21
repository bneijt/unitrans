package nl.bneijt.unitrans.filter;

import nl.bneijt.unitrans.accesscontrol.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import java.io.IOException;

public class AccessLoggingFilter implements Filter {
    private Logger logger = LoggerFactory.getLogger(AccessLoggingFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        logger.info("{} from {} port {}", servletRequest.getAttribute(User.class.getName()), servletRequest.getRemoteAddr(), servletRequest.getRemotePort());
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
    }
}
