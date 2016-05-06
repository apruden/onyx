package org.obiba.onyx.marble.core;

import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

public class StageProperties implements EnvironmentAware {

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public String getProperty(String name) {
        return environment.getProperty(name);
    }
}
