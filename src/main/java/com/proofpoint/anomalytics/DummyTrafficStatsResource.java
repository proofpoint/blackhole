package com.proofpoint.anomalytics;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.io.InputSupplier;
import com.google.inject.Inject;
import com.proofpoint.anomalytics.bgp.BgpMap;
import com.proofpoint.anomalytics.prs.event.Message;
import com.proofpoint.anomalytics.prs.event.PpsCollectProcessor;
import com.proofpoint.event.client.EventClient;
import com.proofpoint.event.client.EventClient.EventGenerator;
import com.proofpoint.event.client.EventClient.EventPoster;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

@Path("/v1/trafficstats")
public class DummyTrafficStatsResource
{
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.TEXT_PLAIN)
    public Response post(InputStream inputStream)
            throws IOException, ExecutionException, InterruptedException
    {
        long bytes = 0;

        while (inputStream.read() != -1) {
            bytes++;
        }

        return Response.ok(Long.toString(bytes)).build();
    }
}