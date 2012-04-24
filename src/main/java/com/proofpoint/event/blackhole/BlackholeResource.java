/*
 * Copyright 2011 Proofpoint, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.proofpoint.event.blackhole;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
import com.google.common.io.InputSupplier;
import com.proofpoint.event.client.EventClient;
import org.weakref.jmx.Managed;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

@Path("{path: .*}")
public class BlackholeResource
{
    private final EventClient eventClient;
    private final AtomicReference<Double> sampleRate = new AtomicReference<Double>();
    private final Random random = new SecureRandom();

    public BlackholeResource(double sampleRate, EventClient eventClient)
    {
        this.eventClient = eventClient;
        this.sampleRate.set(sampleRate);
    }

    @Managed
    public double getSampleRate()
    {
        return sampleRate.get();
    }

    @Managed
    public void setSampleRate(double sampleRate)
    {
        this.sampleRate.set(sampleRate);
    }

    @Inject
    public BlackholeResource(BlackholeConfig config, EventClient eventClient)
    {
        this.sampleRate.set(config.getSamplingRate().doubleValue());
        this.eventClient = eventClient;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@Context UriInfo uriInfo, InputStream input)
            throws IOException, ExecutionException, InterruptedException
    {
        return processRequest("GET", uriInfo, input);
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response put(@Context UriInfo uriInfo, InputStream input)
            throws IOException, ExecutionException, InterruptedException
    {
        return processRequest("PUT", uriInfo, input);
    }


    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response post(@Context UriInfo uriInfo, InputStream input)
            throws IOException, ExecutionException, InterruptedException
    {
        return processRequest("POST", uriInfo, input);
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@Context UriInfo uriInfo, InputStream input)
            throws IOException, ExecutionException, InterruptedException
    {
        return processRequest("DELETE", uriInfo, input);
    }

    private Response processRequest(String method, UriInfo uriInfo, InputStream input)
            throws IOException
    {
        if (random.nextDouble() < sampleRate.get()) {
            byte[] bytes = ByteStreams.toByteArray(input);
            BlackholeEvent event = new BlackholeEvent(method, uriInfo.getRequestUri(), bytes);
            eventClient.post(event);
            input = new ByteArrayInputStream(bytes);
        }
        long total = ByteStreams.length(new OneTimeUseInputSupplier(input));
        return Response.ok(total).build();
    }

    private static class OneTimeUseInputSupplier
            implements InputSupplier<InputStream>
    {
        private InputStream inputStream;

        private OneTimeUseInputSupplier(InputStream inputStream)
        {
            Preconditions.checkNotNull(inputStream, "inputStream is null");
            this.inputStream = inputStream;
        }

        @Override
        public InputStream getInput()
        {
            if (inputStream == null) {
                throw new IllegalStateException("OneTimeUseInputSupplier has already been used");
            }
            InputStream result = inputStream;
            inputStream = null;
            return result;
        }
    }

}
