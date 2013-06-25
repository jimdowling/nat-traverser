package se.sics.gvod.nat.traversal;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import se.sics.gvod.config.NatTraverserConfiguration;
import java.io.IOException;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import se.sics.gvod.address.Address;
import se.sics.gvod.common.Self;
import se.sics.gvod.common.UtilityVod;
import se.sics.gvod.common.VodRetryComponentTestCase;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.common.msgs.ConnectMsg;
import se.sics.gvod.config.HpClientConfiguration;
import se.sics.gvod.config.RendezvousServerConfiguration;
import se.sics.gvod.nat.traversal.events.NatTraverserInit;
import se.sics.gvod.config.ParentMakerConfiguration;
import se.sics.gvod.config.StunClientConfiguration;
import se.sics.gvod.config.StunServerConfiguration;

/**
 *
 * @author jdowling
 */
public class NatTraverserTest extends VodRetryComponentTestCase {
    
    NatTraverser nt;
    public NatTraverserTest() {
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
            Logger.getLogger(NatTraverserTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        nt = new NatTraverser(this);
        NatTraverserInit ntInit = new NatTraverserInit(this,
                new HashSet<Address>(),
                1l,
                NatTraverserConfiguration.build(),
                HpClientConfiguration.build(),
                RendezvousServerConfiguration.build(),
                StunClientConfiguration.build(),
                StunServerConfiguration.build(),
                ParentMakerConfiguration.build(), false
                );
        nt.handleInit.handle(ntInit);
    }
    
    @After
    public void tearDown() {
    }

    @Test
     public void creation() {
        nt.handleUpperMessage.handle(new ConnectMsg.Request(self, pubAddrs.get(0), 
                new UtilityVod(0), true, VodConfig.DEFAULT_MTU));
    
    }
    
    @Override
    public Self clone(int overlayId) {
        return new NatTraverserTest();
    }    
}