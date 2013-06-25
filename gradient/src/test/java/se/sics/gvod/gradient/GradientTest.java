/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.gradient;

import se.sics.gvod.config.GradientConfiguration;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import se.sics.gvod.common.VodDescriptor;
import se.sics.gvod.common.VodRetryComponentTestCase;
import se.sics.gvod.common.Utility;
import se.sics.gvod.common.UtilityVod;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.gradient.events.GradientSetsExchangeCycle;
import se.sics.gvod.gradient.msgs.GradientSetsExchangeMsg;
import se.sics.gvod.net.VodAddress;
import se.sics.kompics.Event;
import se.sics.gvod.timer.SchedulePeriodicTimeout;

/**
 *
 * @author jim
 */
public class GradientTest extends VodRetryComponentTestCase {

    private static final int NUM_NEIGHBOURS = 10;
    Gradient gradient = null;
    int overlayId;
    int nodeId;
    int setsExchangeDelay, setsExchangePeriod, numberOfBestSimilarPeers,
            utilityThreshold, numOfProbes, probeRequestTimeout;
    byte probeTtl;
    VodAddress[] p = new VodAddress[NUM_NEIGHBOURS];
    int[] u = new int[NUM_NEIGHBOURS];
    int selfChunk;
    Utility selfUtility;
    
    public GradientTest() {
        super();
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    @Override
    public void setUp() {
        super.setUp();
        try {
            nodeId = 1;
            overlayId = 10;
            setsExchangePeriod = 2000;
            numberOfBestSimilarPeers = 5;
            utilityThreshold = 30;
            numOfProbes = 2;
            probeRequestTimeout = 2000;
            probeTtl = 2;
            selfChunk = 8;
            selfUtility = new UtilityVod(selfChunk);
            //prepare addresses
            VodConfig.init(new String[]{});
            //Loading GradientView
            ArrayList<VodDescriptor> similarPeers = new ArrayList<VodDescriptor>();
            for (int i=0; i < NUM_NEIGHBOURS; i++) {
                u[i] = 10 + (i * 2);
                p[i] = pubAddrs.get(i);
                similarPeers.add(new VodDescriptor(p[i], new UtilityVod(u[i]), 1, 1));
            }
            
            gradient = new Gradient(this);
            GradientConfiguration config = GradientConfiguration.build()
                    .setSimilarSetSize(10)
                    .setSetsExchangePeriod(setsExchangePeriod)
                    .setSetsExchangeTimeout(setsExchangeDelay)
                    .setSearchRequestTimeout(probeRequestTimeout)
                    .setSearchTtl(probeTtl)
                    .setUtilityThreshold(utilityThreshold)
                    .setNumParallelSearches(numOfProbes)
                    .setTemperature(0.8d)
                    ;
            gradient.handleInit.handle(new GradientInit(this, config));
            gradient.similarSet.addSimilarPeers(similarPeers);
        
        } catch (IOException ex) {
            Logger.getLogger(GradientTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @After
    @Override
    public void tearDown() {
    }

    @Test
    public void testSetsExchangeCycle() {
        GradientSetsExchangeCycle e = new GradientSetsExchangeCycle(new SchedulePeriodicTimeout(0, 0));
        gradient.handleCycle.handle(e);

        Event event = eventList.peekLast();
        assert (event.getClass().equals(GradientSetsExchangeMsg.Request.class));
        GradientSetsExchangeMsg.Request request = (GradientSetsExchangeMsg.Request) event;
        assert (request.getDestination() != null);
        
        boolean destFound = false;
        for (int i=0; i < NUM_NEIGHBOURS; i++) {
            if (request.getDestination().equals(p[i].getPeerAddress())) {
                destFound = true;
                System.out.println("Choosing neighbour " + i);
            }
        }

        Map<Integer,Integer> s = new HashMap<Integer,Integer>();
        for (int j=0; j<10000; j++) {
            gradient.handleCycle.handle(e);
            event = eventList.peekLast();
            request = (GradientSetsExchangeMsg.Request) event;
            for (int i=0; i < NUM_NEIGHBOURS; i++) {
                if (request.getDestination().equals(p[i].getPeerAddress())) {
                    Integer n = s.get(i);
                    if (n ==null) {
                        n = 0;
                    } 
                    n++;
                    s.put(i, n);
                }
            }            
        }
        System.out.println(s);
        
        
        assert (destFound);
    }
}
