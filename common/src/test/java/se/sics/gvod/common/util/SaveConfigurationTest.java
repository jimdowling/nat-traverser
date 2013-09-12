/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.common.util;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import se.sics.gvod.address.Address;
import se.sics.gvod.common.Self;
import se.sics.gvod.common.SelfNoUtility;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.Nat;

/**
 *
 * @author jim
 */
public class SaveConfigurationTest {

    @Test
    public void testSaveConfiguration() {
        int numUnchanged = 0;
        int numStunSinceLastRun = 0;
        boolean isUpnp = false;

        try {
            File f = new File(VodConfig.STARTUP_CONFIG_FILE);
            f.delete();
            
            Address addr = new Address(InetAddress.getByName("192.168.1.12"), 8081, 7);
            Set<Address> parents = new HashSet<Address>();
            parents.add(new Address(InetAddress.getByName("192.168.1.13"), 8888, 10));
            parents.add(new Address(InetAddress.getByName("192.168.1.15"), 9999, 3));
            VodAddress vodAddr = new VodAddress(addr, 
                    VodConfig.SYSTEM_OVERLAY_ID,
                    VodAddress.NatType.NAT, 
                    Nat.MappingPolicy.ENDPOINT_INDEPENDENT, 
                    Nat.AllocationPolicy.PORT_PRESERVATION,
                    Nat.FilteringPolicy.ENDPOINT_INDEPENDENT, 
                    Nat.DEFAULT_RULE_EXPIRATION_TIME, 1,
                    parents);

            VodConfig.removeSavedNatType();
            
            VodConfig.init(new String[]{""});
            
            Self self = new SelfNoUtility(vodAddr);
            for (Address a : parents) {
                self.addParent(a);
            }
            
            if (!VodConfig.saveNatType(self, true, true)) {
                Assert.fail("Saving configuration failed.");
            }            
            
            CachedNatType cachedNt = VodConfig.getSavedNatType();
            NatBean nb = cachedNt.getNatBean();
            Assert.assertEquals(addr, nb.getAddress());
            Assert.assertEquals(vodAddr.getNatAsString(), nb.getNatPolicy());
            Assert.assertEquals(numUnchanged + 1, nb.getNumTimesUnchanged());
            Assert.assertEquals(0, nb.getNumTimesSinceStunLastRun());
            Assert.assertEquals(isUpnp, nb.isUpnpSupported());
            Assert.assertEquals(parents, nb.getParents());

            self.setUpnp(true);
            addr = new Address(InetAddress.getByName("192.168.1.1"), 8081, 7);
            self.setUpnpIp(InetAddress.getByName("192.168.1.1"));
            if (!VodConfig.saveNatType(self, false, true)) {
                Assert.fail("Saving configuration failed.");
            }

            cachedNt = VodConfig.getSavedNatType();
            nb = cachedNt.getNatBean();
            Assert.assertEquals(addr, nb.getAddress());
            Assert.assertEquals(self.getAddress().getNatAsString(), nb.getNatPolicy());
            Assert.assertEquals(numUnchanged + 2, nb.getNumTimesUnchanged());
            Assert.assertEquals(1, nb.getNumTimesSinceStunLastRun());
            Assert.assertEquals(true, nb.isUpnpSupported());
            Assert.assertEquals(parents, nb.getParents());
            
            
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
            Assert.fail(ex.getMessage());
        }
    }
}
