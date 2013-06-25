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
        boolean isUpnp = true;

        try {
            File f = new File(VodConfig.STARTUP_CONFIG_FILE);
            f.delete();
            
            Address addr = new Address(InetAddress.getByName("192.168.1.12"), 8081, 7);
            Set<Address> parents = new HashSet<Address>();
            parents.add(new Address(InetAddress.getByName("192.168.1.13"), 8888, 10));
            parents.add(new Address(InetAddress.getByName("192.168.1.15"), 9999, 3));
            VodAddress selfVodAddress = new VodAddress(addr, 
                    VodConfig.SYSTEM_OVERLAY_ID,
                    VodAddress.NatType.NAT, 
                    Nat.MappingPolicy.ENDPOINT_INDEPENDENT, 
                    Nat.AllocationPolicy.PORT_PRESERVATION,
                    Nat.FilteringPolicy.ENDPOINT_INDEPENDENT, 
                    Nat.DEFAULT_RULE_EXPIRATION_TIME, 1,
                    parents);

            if (!VodConfig.saveConfiguration(new VodAddressBean(selfVodAddress.getPeerAddress(),
                    selfVodAddress.getParents(), selfVodAddress.getNatPolicy()), isUpnp,
                    numUnchanged, numStunSinceLastRun)) {
                Assert.fail("Saving configuration failed.");
            }

            VodConfig.init(new String[]{""});
            
            
            StartupConfig config = VodConfig.getStartupConfig();
            Assert.assertEquals(addr, config.getVodAddressBean().getAddress());
            Assert.assertEquals(selfVodAddress.getNatPolicy(), config.getVodAddressBean().getNatPolicy());
            Assert.assertEquals(numUnchanged + 1, config.getNumTimesSinceStunLastRun());
            Assert.assertEquals(numStunSinceLastRun + 1, config.getNumTimesUnchanged());
            Assert.assertEquals(isUpnp, config.isUpnpSupported());
            Assert.assertEquals(parents, config.getVodAddressBean().getParents());

        } catch (IOException ex) {
            System.err.println(ex.getMessage());
            Assert.fail(ex.getMessage());
        }
    }
}
