package service.gateway.components.proxy;

import org.eclipse.jetty.client.HttpClient;

public class DefaultHttpClientProvider implements HttpClientProvider {
    @Override
    public HttpClient getHttpClient() {
        return new HttpClient();
    }
}
