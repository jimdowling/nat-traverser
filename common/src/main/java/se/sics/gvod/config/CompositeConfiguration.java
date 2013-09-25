/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.config;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jdowling
 */
public abstract class CompositeConfiguration {

    private static CompositeConfiguration instance = null;

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

    public static CompositeConfiguration load(Class<? extends CompositeConfiguration> type)
            throws IOException {


        try {
            Constructor<? extends CompositeConfiguration> c = type.getConstructor();
            instance = c.newInstance();
            instance.loadP();
            return instance;
        } catch (InstantiationException ex) {
            Logger.getLogger(AbstractConfiguration.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex.getMessage());
        } catch (IllegalAccessException ex) {
            Logger.getLogger(AbstractConfiguration.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex.getMessage());
        } catch (InvocationTargetException ex) {
            Logger.getLogger(AbstractConfiguration.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex.getMessage());
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(AbstractConfiguration.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex.getMessage());
        } catch (SecurityException ex) {
            Logger.getLogger(AbstractConfiguration.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex.getMessage());
        }
    }

    private void loadP() throws IOException, IllegalAccessException {
        Field[] fields = getClass().getDeclaredFields();
        for (Field f : fields) {
            Class<?> c = f.getType();
            try {
                Class<? extends AbstractConfiguration> ac = c.asSubclass(AbstractConfiguration.class);
                AbstractConfiguration loadedObj =
                        AbstractConfiguration.load(ac);
                f.set(instance, loadedObj);
            } catch (ClassCastException ex) {
                // do nothing
                ex.printStackTrace();
            }
        }
    }
}
