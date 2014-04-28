/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.config;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jdowling
 */
public class CompositeConfigurationTest {
    
    
    public CompositeConfigurationTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        try {
            VodConfig.init(new String[0]);
        } catch (IOException ex) {
            Logger.getLogger(CompositeConfigurationTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of store method, of class AbstractConfiguration.
     */
    @Test
    public void testComposite() throws Exception {
        CompositeConcrete cc = new CompositeConcrete();
        cc.store();
        CompositeConcrete loaded = (CompositeConcrete) CompositeConcrete.load(CompositeConcrete.class);
        assert(cc.equals(loaded));
    }

}