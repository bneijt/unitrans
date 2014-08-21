package nl.bneijt.unitrans.filter;

import nl.bneijt.unitrans.accesscontrol.AccessControl;
import nl.bneijt.unitrans.accesscontrol.Strangers;
import nl.bneijt.unitrans.accesscontrol.User;
import nl.bneijt.unitrans.resources.ResourceProtection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.*;
import java.io.IOException;

public class CanAccessFilter implements Filter {
    private Logger logger = LoggerFactory.getLogger(CanAccessFilter.class);


    private final ResourceProtection resourceProtection;
    private final AccessControl accessControl;
    private final Strangers strangers;

    @Inject
    public CanAccessFilter(ResourceProtection resourceProtection, AccessControl accessControl, Strangers strangers) {
        this.resourceProtection = resourceProtection;
        this.accessControl = accessControl;
        this.strangers = strangers;
    }


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        User user = resourceProtection.getUser(servletRequest);
        strangers.addStranger(user);

        if (!accessControl.canAccess(user)) {
            logger.debug("Adding stranger {}", user);
            strangers.addStranger(user);
            throw new ServletException("Not authorized");
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
    }
}
