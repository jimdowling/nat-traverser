/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.net;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import se.sics.gvod.address.Address;

/**
 *
 * @author Jim Dowling<jdowling@sics.se>
 */
public class VodAddressTest {
    
    public VodAddressTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
     @Test
     public void natTest() {
         InetAddress ip = null;
        try {
            ip = InetAddress.getByName("192.168.0.1");
        } catch (UnknownHostException ex) {
            Logger.getLogger(VodAddressTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        Address addr = new Address(ip, 10000, 100);
        Nat n = new Nat(Nat.Type.NAT, Nat.MappingPolicy.ENDPOINT_INDEPENDENT,
                Nat.AllocationPolicy.PORT_PRESERVATION, Nat.FilteringPolicy.PORT_DEPENDENT, 1,
                30*1000);
        VodAddress va = new VodAddress(addr, 10, n);
        assert (va.getMappingPolicy() == n.getMappingPolicy());
        assert (va.getAllocationPolicy() == n.getAllocationPolicy());
        assert (va.getFilteringPolicy() == n.getFilteringPolicy());
     }
}
