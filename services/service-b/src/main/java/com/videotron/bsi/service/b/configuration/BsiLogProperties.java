package com.videotron.bsi.service.b.configuration;

import java.util.Set;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "bsi.log")
public interface BsiLogProperties {

    @WithDefault("false")
    boolean logPayload();

    Set<String> httpHeaders();
}
