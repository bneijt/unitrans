package nl.bneijt.unitrans.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class AccessLoggingFilter implements Filter {
    private Logger logger = LoggerFactory.getLogger(AccessLoggingFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        logger.info("{}:{} {}", servletRequest.getRemoteAddr(), servletRequest.getRemotePort(), ((HttpServletRequest) servletRequest).getPathInfo());
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
    }
}
