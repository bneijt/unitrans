package nl.bneijt.unitrans.filter;

import com.google.common.io.BaseEncoding;
import nl.bneijt.unitrans.accesscontrol.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;

public class X509ClientAuthenticationFilter implements Filter {
    private Logger logger = LoggerFactory.getLogger(X509ClientAuthenticationFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (!containsRequiredAttributes(servletRequest)) {
            throw new ServletException("certificate missing");
        }
        X509Certificate certs[] =
                (X509Certificate[]) servletRequest.getAttribute("javax.servlet.request.X509Certificate");
        logger.info("Found {} ceritificates in request", certs.length);
        X509Certificate clientCert = certs[0];
        try {
            clientCert.checkValidity();
        } catch (CertificateExpiredException e) {
            throw new IOException("Certificate check failure", e);
        } catch (CertificateNotYetValidException e) {
            throw new IOException("Certificate check failure", e);
        }
        PublicKey publicKey = clientCert.getPublicKey();
        byte[] publicKeyEncoded = publicKey.getEncoded();
        try {
            MessageDigest shaDigest = MessageDigest.getInstance("SHA-512");
            shaDigest.update(publicKeyEncoded);
            byte[] digest = shaDigest.digest();
            servletRequest.setAttribute(User.class.getName(), new User(digest));
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("Failed to calculate digest", e);
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    private boolean containsRequiredAttributes(ServletRequest httpServletRequest) {
        ArrayList<String> attributes = Collections.list(httpServletRequest.getAttributeNames());
        return attributes.contains("javax.servlet.request.X509Certificate")
                && attributes.contains("javax.servlet.request.key_size")
                && attributes.contains("javax.servlet.request.cipher_suite")
                && attributes.contains("javax.servlet.request.ssl_session_id");

    }

    @Override
    public void destroy() {
    }
}
