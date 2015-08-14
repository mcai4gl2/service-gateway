package service.gateway.components.proxy;

import org.eclipse.jetty.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.gateway.domain.ProxyConfig;
import service.gateway.domain.ProxyRoute;

import javax.inject.Inject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.concurrent.CopyOnWriteArrayList;

public class ProxyServlet extends org.eclipse.jetty.proxy.ProxyServlet {
    public static final Logger LOGGER = LoggerFactory.getLogger(ProxyServlet.class);

    private ProxyConfig proxyConfig;
    @Inject
    private HttpClientProvider httpClientProvider;
    private CopyOnWriteArrayList<ProxyRoute> routes;

    public ProxyServlet() {
        super();
        routes = new CopyOnWriteArrayList<>();
    }

    @Override
    protected HttpClient newHttpClient() {
        return httpClientProvider.getHttpClient();
    }

    public void setHttpClientProvider(HttpClientProvider httpClientProvider) {
        this.httpClientProvider = httpClientProvider;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        setupRoutes();
    }

    private void setupRoutes() {
        ServletConfig config = getServletConfig();
        if (config == null) return;
        routes.clear();
        for (ProxyRoute route : proxyConfig.getRoutes()) {
            // Adjust prefix value to account for context path
            ProxyRoute newRoute = new ProxyRoute();
            newRoute.setProxyTo(route.getProxyTo());
            newRoute.setPriority(route.getPriority());
            String contextPath = config.getServletContext().getContextPath();
            newRoute.setPrefix(route.getPrefix() == null ? contextPath : (contextPath + route.getPrefix()));

            if (_log.isDebugEnabled())
                _log.debug(config.getServletName() + " @ " + newRoute.getPrefix() + " to " + newRoute.getProxyTo());

            routes.add(newRoute);
        }
    }

    public void setProxyConfig(ProxyConfig proxyConfig) throws ServletException {
        this.proxyConfig = proxyConfig;
        setupRoutes();
    }

    @Override
    protected URI rewriteURI(HttpServletRequest request) {
        String path = request.getRequestURI();

        for (ProxyRoute proxyRoute : routes) {
            if (path.startsWith(proxyRoute.getPrefix())) {
                StringBuilder uri = new StringBuilder(proxyRoute.getProxyTo());
                if (proxyRoute.getProxyTo().endsWith("/"))
                    uri.setLength(uri.length() - 1);
                String rest = path.substring(proxyRoute.getPrefix().length());
                if (!rest.startsWith("/"))
                    uri.append("/");
                uri.append(rest);
                String query = request.getQueryString();
                if (query != null)
                    uri.append("?").append(query);
                URI rewrittenURI = URI.create(uri.toString()).normalize();

                if (validateDestination(rewrittenURI.getHost(), rewrittenURI.getPort()))
                    return rewrittenURI;
            }
        }

        return null;
    }
}
