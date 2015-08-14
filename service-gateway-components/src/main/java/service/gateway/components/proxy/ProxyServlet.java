package service.gateway.components.proxy;

import org.eclipse.jetty.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class ProxyServlet extends org.eclipse.jetty.proxy.ProxyServlet.Transparent {
    public static final Logger LOGGER = LoggerFactory.getLogger(ProxyServlet.class);

    @Inject
    private HttpClientProvider httpClientProvider;

    @Override
    protected HttpClient newHttpClient() {
        return httpClientProvider.getHttpClient();
    }

    public void setHttpClientProvider(HttpClientProvider httpClientProvider) {
        this.httpClientProvider = httpClientProvider;
    }
}
