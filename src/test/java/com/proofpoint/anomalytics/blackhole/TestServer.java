package com.proofpoint.anomalytics.blackhole;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.proofpoint.configuration.ConfigurationFactory;
import com.proofpoint.configuration.ConfigurationModule;
import com.proofpoint.event.client.EventClient;
import com.proofpoint.event.client.InMemoryEventClient;
import com.proofpoint.event.client.InMemoryEventModule;
import com.proofpoint.http.client.ApacheHttpClient;
import com.proofpoint.http.client.BodyGenerator;
import com.proofpoint.http.client.HttpClient;
import com.proofpoint.http.client.Request;
import com.proofpoint.http.client.RequestBuilder;
import com.proofpoint.http.client.ResponseHandler;
import com.proofpoint.http.server.testing.TestingHttpServer;
import com.proofpoint.http.server.testing.TestingHttpServerModule;
import com.proofpoint.jaxrs.JaxrsModule;
import com.proofpoint.json.JsonModule;
import com.proofpoint.node.testing.TestingNodeModule;
import org.apache.commons.codec.binary.Base64;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class TestServer
{
    private static final String DATA = "test data";
    private static final String PATH = "/some/random/url?query=yes";

    private HttpClient client;
    private TestingHttpServer server;
    private ExecutorService executor;
    private InMemoryEventClient eventClient;
    private BlackholeResource blackholeResource;

    @BeforeMethod
    public void setup()
            throws Exception
    {
        Map<String, String> properties = ImmutableMap.<String, String>builder()
                .put("blackhole.announcement", "blackhole")
                .build();

        Injector injector = Guice.createInjector(
                new ConfigurationModule(new ConfigurationFactory(properties)),
                new TestingNodeModule(),
                new InMemoryEventModule(),
                new TestingHttpServerModule(),
                new JsonModule(),
                new JaxrsModule(),
                new MainModule());
        server = injector.getInstance(TestingHttpServer.class);
        eventClient = (InMemoryEventClient) injector.getInstance(EventClient.class);
        blackholeResource = injector.getInstance(BlackholeResource.class);

        server.start();
        executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setDaemon(true).setNameFormat("Test-Server-%s").build());
        client = new ApacheHttpClient();
    }

    @AfterMethod
    public void teardown()
            throws Exception
    {
        if (server != null) {
            server.stop();
        }

        if (executor != null) {
            executor.shutdownNow();
        }
    }

    @Test
    public void testEventSent()
            throws Exception
    {
        assertEquals(eventClient.getEvents().size(), 0);
        blackholeResource.setSampleRate(1);
        testMethod("POST");
        assertEquals(eventClient.getEvents().size(), 1);
        BlackholeEvent blackholeEvent = (BlackholeEvent) eventClient.getEvents().get(0);
        assertEquals(blackholeEvent.getMethod(), "POST");
        assertTrue(blackholeEvent.getUri().endsWith(PATH));
        assertEquals(new String(Base64.decodeBase64(blackholeEvent.getEntity())), DATA);
    }

    @Test
    public void testGet()
            throws Exception
    {
        assertEquals(eventClient.getEvents().size(), 0);
        testMethod("GET");
        assertEquals(eventClient.getEvents().size(), 0);
    }

    @Test
    public void testPut()
            throws Exception
    {
        assertEquals(eventClient.getEvents().size(), 0);
        testMethod("PUT");
        assertEquals(eventClient.getEvents().size(), 0);
    }

    @Test
    public void testPost()
            throws Exception
    {
        assertEquals(eventClient.getEvents().size(), 0);
        testMethod("POST");
        assertEquals(eventClient.getEvents().size(), 0);
    }

    @Test
    public void testDelete()
            throws Exception
    {
        assertEquals(eventClient.getEvents().size(), 0);
        testMethod("DELETE");
        assertEquals(eventClient.getEvents().size(), 0);
    }

    public void testMethod(String method)
            throws Exception
    {
        Request request = new RequestBuilder()
                .setMethod(method)
                .setUri(urlFor(PATH))
                .setBodyGenerator(new BodyGenerator()
                {
                    @Override
                    public void write(OutputStream out)
                            throws Exception
                    {
                        out.write(DATA.getBytes());
                    }
                })
                .build();

        long bytes = client.execute(request, new ResponseHandler<Long, Exception>()
        {
            @Override
            public Exception handleException(Request request, Exception exception)
            {
                return exception;
            }

            @Override
            public Long handle(Request request, com.proofpoint.http.client.Response response)
                    throws Exception
            {
                assertEquals(response.getStatusCode(), Status.OK.getStatusCode());
                assertEquals(response.getHeaders().get("Content-Type"), ImmutableList.of(MediaType.APPLICATION_JSON));
                String entity = CharStreams.toString(new InputStreamReader(response.getInputStream(), Charsets.UTF_8)).trim();
                return Long.parseLong(entity);
            }
        });

        Assert.assertEquals(bytes, DATA.length());
    }

    private URI urlFor(String path)
    {
        return server.getBaseUrl().resolve(path);
    }
}
