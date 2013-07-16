/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.config;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jdowling
 */
public abstract class CompositeConfiguration {

    public CompositeConfiguration() {
    }

    public void store() throws IOException {

        Field[] fields = getClass().getDeclaredFields();
        for (Field f : fields) {
            try {
                Object o = f.get(this);
                if (o instanceof AbstractConfiguration) {
                    AbstractConfiguration configuration = (AbstractConfiguration) o;
                    configuration.store();
                }
            } catch (IllegalArgumentException ex) {
                throw new IOException(ex.getMessage());
            } catch (IllegalAccessException ex) {
                throw new IOException(ex.getMessage());
            }
        }
    }
}
