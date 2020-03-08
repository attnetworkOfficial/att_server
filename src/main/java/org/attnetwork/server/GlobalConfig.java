package org.attnetwork.server;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource(value = "classpath:config.json")
@ConfigurationProperties
public class GlobalConfig {
}
