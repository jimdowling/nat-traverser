/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.config;

import java.io.File;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jdowling
 */
public class AbstractConfigurationTest {
    
    
    public AbstractConfigurationTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of store method, of class AbstractConfiguration.
     */
    @Test
    public void testStore() throws Exception {
        Concrete instance = new Concrete(10, 3);
        File f = instance.store(1);
        Concrete loaded = (Concrete) Concrete.load(Concrete.class);
        assert(instance.equals(loaded));
    }

}