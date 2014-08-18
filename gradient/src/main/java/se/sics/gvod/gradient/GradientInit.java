/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.gradient;

import se.sics.gvod.config.GradientConfiguration;
import se.sics.gvod.common.Self;
import se.sics.kompics.Init;

/**
 *
 * @author jim
 */
public class GradientInit extends Init<Gradient> {

    private Self self;
    private GradientConfiguration config;
    
    public GradientInit(Self self, GradientConfiguration config) {
        this.self = self;
        this.config = config;
    }
    public Self getSelf() {
        return self;
    }

    public GradientConfiguration getConfig() {
        return config;
    }

}
