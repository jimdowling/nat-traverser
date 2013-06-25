/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.config;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 *
 * @author jdowling
 */
public abstract class CompositeConfiguration {
    
    int seed;

    public CompositeConfiguration(int seed) {
        this.seed = seed;
    }

    public void store() throws IOException, IllegalAccessException  { 
        System.setProperty("seed", Long.toString(seed));

        Field[] fields = getClass().getDeclaredFields();
        for (Field f : fields) {
            Object o = f.get(this);
            if (o instanceof AbstractConfiguration) {
                AbstractConfiguration configuration = (AbstractConfiguration) o;
                configuration.store(seed);
            }
        }
    }    
}
