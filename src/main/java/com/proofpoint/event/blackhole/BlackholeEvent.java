package com.proofpoint.event.blackhole;

import com.google.common.base.Preconditions;
import com.proofpoint.event.client.EventField;
import com.proofpoint.event.client.EventType;
import org.apache.commons.codec.binary.Base64;

import java.net.URI;

@EventType("Blackhole")
public class BlackholeEvent
{
    private final String method;
    private final String uri;
    private final String entity;

    public BlackholeEvent(String method, URI uri, byte[] entity)
    {
        Preconditions.checkNotNull(method, "method is null");
        Preconditions.checkNotNull(uri, "uri is null");
        Preconditions.checkNotNull(entity, "entity is null");

        this.method = method;
        this.uri = uri.toString();
        this.entity = Base64.encodeBase64String(entity);
    }

    @EventField
    public String getMethod()
    {
        return method;
    }

    @EventField
    public String getUri()
    {
        return uri;
    }

    @EventField
    public String getEntity()
    {
        return entity;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append("BlackholeEvent");
        sb.append("{method='").append(method).append('\'');
        sb.append(", uri='").append(uri).append('\'');
        sb.append(", entity='").append(entity).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
