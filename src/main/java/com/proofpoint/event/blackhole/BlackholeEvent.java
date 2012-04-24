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
