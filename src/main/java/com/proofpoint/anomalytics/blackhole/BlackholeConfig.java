package com.proofpoint.anomalytics.blackhole;

import com.proofpoint.configuration.Config;

import javax.validation.constraints.NotNull;

public class BlackholeConfig
{
    private String serviceAnnouncement;

    @NotNull
    public String getServiceAnnouncement()
    {
        return serviceAnnouncement;
    }

    @Config("blackhole.announcement")
    public void setServiceAnnouncement(String serviceAnnouncement)
    {
        this.serviceAnnouncement = serviceAnnouncement;
    }
}
