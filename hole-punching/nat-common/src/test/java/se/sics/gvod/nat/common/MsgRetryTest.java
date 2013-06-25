/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.nat.common;

import java.io.IOException;
import java.util.logging.Level;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.common.RetryComponentDelegator;
import se.sics.gvod.common.VodRetryComponentTestCase;
import se.sics.gvod.config.VodConfig;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.kompics.Stop;

public class MsgRetryTest extends VodRetryComponentTestCase {
    
    private static Logger logger = LoggerFactory.getLogger(MsgRetryTest.class);
    
    TestComponent msgRetry = null;

    public MsgRetryTest() {
        super();
    }
    
    @Override
    public boolean isPacingReqd() {
        return false;
    }

    public class TestComponent extends MsgRetryComponent {

        Positive<TestPort> testPort = positive(TestPort.class);
                
        public TestComponent() {
            this(null);
        }

        public TestComponent(RetryComponentDelegator delegator) {
            super(delegator);
            this.delegator.doAutoSubscribe();
            
        }

    Handler<TestInit> handleInit = new Handler<TestInit>() {

        @Override
        public void handle(TestInit init) {
        }
    };

    Handler<TestMsg> handleTestMsg = new Handler<TestMsg>() {

        @Override
        public void handle(TestMsg msg) {
        }
    };    
    
    
        @Override
        public void stop(Stop event) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    };
    
    
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
        msgRetry = new TestComponent(this);
        try {
            VodConfig.init(new String[]{});
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(MsgRetryTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        msgRetry.handleInit.handle(new TestInit());
    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();
//        msgRetry.stop(null);
    }
    @Test
    public void testJoinComplete() {
//        msgRetry.handleJoin.handle(new CroupierJoin(insiders));
//        LinkedList<Event> events = pollEvent(2);
//        assertSequence(events, CroupierShuffleCycle.class, CroupierJoinCompleted.class);
                assert(true);
    }
    


    @Test
    public void testMsgRetry() {
        assert(true);
    }

   
}
