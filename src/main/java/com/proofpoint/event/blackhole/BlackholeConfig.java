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

import com.proofpoint.configuration.Config;
import com.proofpoint.configuration.ConfigDescription;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;

public class BlackholeConfig
{
    private BigDecimal samplingRate = BigDecimal.ZERO;
    private String serviceAnnouncement;

    @Min(0)
    @Max(1)
    @NotNull
    public BigDecimal getSamplingRate()
    {
        return samplingRate;
    }

    @Config("blackhole.sample-rate")
    @ConfigDescription("proportion of requests to send as Blackhole events")
    public BlackholeConfig setSamplingRate(BigDecimal samplingRate)
    {
        this.samplingRate = samplingRate;
        return this;
    }

    @NotNull
    @Pattern(regexp = "[a-z]+[a-z0-9]*", message = "is invalid")
    public String getServiceAnnouncement()
    {
        return serviceAnnouncement;
    }

    @Config("blackhole.announcement")
    @ConfigDescription("name of service to announce")
    public BlackholeConfig setServiceAnnouncement(String serviceAnnouncement)
    {
        this.serviceAnnouncement = serviceAnnouncement;
        return this;
    }
}
