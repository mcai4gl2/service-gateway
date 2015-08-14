package service.gateway.components.proxy;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import service.gateway.domain.ProxyConfig;
import service.gateway.domain.ProxyRoute;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class ProxyServletTest {
    private Server server;
    private int port;
    private HandlerCollection handlers;
    private HttpClient httpClient;
    private ProxyServlet proxyServlet;
    private ProxyConfig proxyConfig;

    @Before
    public void before() throws Exception {
        port = 7411;
        server = new Server(port);
        handlers = new HandlerCollection();
        server.setHandler(handlers);

        WebAppContext webAppContext = new WebAppContext();
        webAppContext.setContextPath("/subject");
        String dir = new File(".").getCanonicalPath();
        if (dir.contains("service-gateway-components")) {
            dir = dir.replace("service-gateway-components", "");
        }
        webAppContext.setWar(dir + "/service-gateway-test/build/output/webapp");
        webAppContext.setExtractWAR(true);

        ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        contextHandler.setContextPath("/test");
        proxyServlet = new ProxyServlet();
        proxyServlet.setHttpClientProvider(new DefaultHttpClientProvider());
        proxyConfig = new ProxyConfig();
        ProxyRoute route1 = new ProxyRoute();
        route1.setProxyTo("http://localhost:7411/subject");
        proxyConfig.getRoutes().add(route1);
        proxyServlet.setProxyConfig(proxyConfig);
        ServletHolder holder = new ServletHolder(proxyServlet);
        contextHandler.addServlet(holder, "/*");


        handlers.addHandler(webAppContext);
        handlers.addHandler(contextHandler);

        server.start();

        httpClient = new HttpClient();
        httpClient.start();
    }

    @After
    public void after() throws Exception {
        server.stop();
        httpClient.stop();
    }

    @Test
    public void testAccessPingServlet() throws Exception {
        ContentResponse response = httpClient.GET("http://localhost:7411/subject/ping");
        String result = response.getContentAsString();
        System.out.println(result);
        assertEquals("Pong", result);
    }

    @Test
    public void testAccessPingServletViaProxy() throws Exception {
        ContentResponse response = httpClient.GET("http://localhost:7411/test/ping");
        String result = response.getContentAsString();
        System.out.println(result);
        assertEquals("Pong", result);
    }

    @Test
    public void testAddNewProxyRoute() throws Exception {
        ProxyRoute route = new ProxyRoute();
        route.setProxyTo("http://localhost:7411/nonexist");
        proxyConfig.getRoutes().add(0, route);
        proxyServlet.setProxyConfig(proxyConfig);

        ContentResponse response = httpClient.GET("http://localhost:7411/test/ping");
        String result = response.getContentAsString();
        System.out.println(result);
        assertEquals(404, response.getStatus());
    }
}
