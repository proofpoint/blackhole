package com.proofpoint.anomalytics.blackhole;

import com.proofpoint.configuration.Config;

import javax.validation.constraints.NotNull;

public class BlackholeConfig
{
    private double samplingRate;
    private String serviceAnnouncement;

    public double getSamplingRate()
    {
        return samplingRate;
    }

    @Config("blackhole.sample-percentage")
    public void setSamplingRate(double samplingRate)
    {
        this.samplingRate = samplingRate;
    }

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
