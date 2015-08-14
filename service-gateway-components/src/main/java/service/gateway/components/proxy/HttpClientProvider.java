package service.gateway.components.proxy;

import org.eclipse.jetty.client.HttpClient;

public interface HttpClientProvider {
     HttpClient getHttpClient();
}
