package com.proofpoint.anomalytics;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
import com.google.common.io.InputSupplier;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

@Path("/")
public class DummyResource
{
    @POST
    public Response post(InputStream input)
            throws IOException, ExecutionException, InterruptedException
    {
        long total = ByteStreams.length(new OneTimeUseInputSupplier(input));
        return Response.ok(Long.toString(total)).build();
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