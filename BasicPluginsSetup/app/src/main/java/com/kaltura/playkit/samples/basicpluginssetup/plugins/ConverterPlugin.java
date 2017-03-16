package com.kaltura.playkit.samples.basicpluginssetup.plugins;

import com.google.gson.JsonObject;

public abstract class ConverterPlugin {

    String pluginName;

    public abstract JsonObject toJson();

    public String getPluginName() {
        return pluginName;
    }

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }
}