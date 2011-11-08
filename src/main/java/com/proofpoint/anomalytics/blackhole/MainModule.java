package com.proofpoint.anomalytics.blackhole;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.proofpoint.discovery.client.ServiceAnnouncement;
import com.proofpoint.discovery.client.ServiceAnnouncement.ServiceAnnouncementBuilder;
import com.proofpoint.http.server.HttpServerInfo;

import static com.proofpoint.configuration.ConfigurationModule.bindConfig;
import static com.proofpoint.discovery.client.DiscoveryBinder.discoveryBinder;
import static com.proofpoint.discovery.client.ServiceAnnouncement.serviceAnnouncement;

public class MainModule
        implements Module
{
    public void configure(Binder binder)
    {
        binder.requireExplicitBindings();
        binder.disableCircularProxies();

        binder.bind(BlackholeResource.class).in(Scopes.SINGLETON);
        bindConfig(binder).to(BlackholeConfig.class);
        discoveryBinder(binder).bindServiceAnnouncement(JmxHttpRpcAnnouncementProvider.class);
    }

    static class JmxHttpRpcAnnouncementProvider implements Provider<ServiceAnnouncement>
    {
        private HttpServerInfo httpServerInfo;
        private String serviceAnnouncement;

        @Inject
        JmxHttpRpcAnnouncementProvider(BlackholeConfig config, HttpServerInfo httpServerInfo)
        {
            serviceAnnouncement = config.getServiceAnnouncement();
            this.httpServerInfo = httpServerInfo;
        }

        @Override
        public ServiceAnnouncement get()
        {
            ServiceAnnouncementBuilder builder = serviceAnnouncement(serviceAnnouncement);

            if (httpServerInfo.getHttpUri() != null) {
                builder.addProperty("http", httpServerInfo.getHttpUri().toString());
            }
            if (httpServerInfo.getHttpsUri() != null) {
                builder.addProperty("https", httpServerInfo.getHttpsUri().toString());
            }

            return builder.build();
        }
    }
}
