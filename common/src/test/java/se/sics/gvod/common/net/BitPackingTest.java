/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.common.net;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.Nat;

/**
 *
 * @author jdowling
 */
public class BitPackingTest {

    public BitPackingTest() {
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
    public void testBitPacking() {

        int age=36, gender=1, height=112;
        short packed_info = (short) ((((age << 1) | gender) << 7) | height);
// unpacking
        int uheight = packed_info & 0x7f;
        int ugender = (packed_info >>> 7) & 1;
        int uage = (packed_info >>> 8);
        assert(uheight == height);
        assert(ugender == gender);
        assert(uage == age);

    }

    @Test
    public void hello() {

        Nat.MappingPolicy mp = Nat.MappingPolicy.PORT_DEPENDENT;
        Nat.FilteringPolicy fp = Nat.FilteringPolicy.HOST_DEPENDENT;
        Nat.AllocationPolicy ap = Nat.AllocationPolicy.PORT_CONTIGUITY;
        Nat.AlternativePortAllocationPolicy aap =
                Nat.AlternativePortAllocationPolicy.RANDOM;
        VodAddress.NatType nt = VodAddress.NatType.NAT;


        // 2 bits
        int ntV = nt.ordinal();
        // 2 bits
        int mpV = mp.ordinal();
        // 2 bits
        int fpV = fp.ordinal();
        // 2 bits
        int apV = ap.ordinal();
        // 2 bits
        int aapV = aap.ordinal();


//         0xffffffff;
        
        int policy = (ntV << 4) | ((fpV << 2) ) | (mpV & 0x3);

        int mpU = policy & 0x3;
        int fpU = (policy >>> 2) & 0x3;
        int ntU = (policy >>> 4);
        
        Nat.MappingPolicy gmp = Nat.MappingPolicy.values()[mpU];
        VodAddress.NatType gnt = VodAddress.NatType.values()[ntU];
        Nat.FilteringPolicy gfp = Nat.FilteringPolicy.values()[fpU];
        
        assert(gmp == mp);
        assert(gfp == fp);
        assert(gnt == nt);
        
    }
}
