package com.opcOpenInterface.type;

import org.jfree.data.time.TimeSeries;

public class Trend {
    private String name;
    private String tag;
    private TimeSeries serie;

    public Trend(String name, String tag) {
        this.name = name;
        this.tag = tag;
    }

    public String getName() {
        return this.name;
    }

    public String toString() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTag() {
        return this.tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public TimeSeries getSerie() {
        return this.serie;
    }

    public void setSerie(TimeSeries serie) {
        this.serie = serie;
    }
}
