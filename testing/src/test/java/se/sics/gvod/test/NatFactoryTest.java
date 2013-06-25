package se.sics.gvod.test;



import se.sics.gvod.common.NatFactory;
import java.util.Random;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class NatFactoryTest
        extends TestCase
{

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public NatFactoryTest(String testName)
    {
        super(testName);
        


    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {

        NatFactory natFactory = new NatFactory(
                "m(EI)_a(PP)_f(PD)_alt(PC)_25.2$"
                + "OPEN_21.4$"
                + "m(PD)_a(RD)_f(PD)_alt(PC)_12.4$"
                + "m(EI)_a(RD)_f(PD)_alt(PC)_9.9$"
                + "m(EI)_a(PP)_f(EI)_alt(PC)_9.3$"
                + "m(EI)_a(RD)_f(EI)_alt(PC)_7.7$"
                + "m(EI)_a(PP)_f(HD)_alt(PC)_4.6$"
                + "m(EI)_a(PC)_f(EI)_alt(PC)_4.4$"
                + "m(PD)_a(PP)_f(PD)_alt(PC)_2.6$"
                + "m(PD)_a(PP)_f(EI)_alt(PC)_0.7$"
                + "m(PD)_a(RD)_f(EI)_alt(PC)_0.6$"
                + "m(EI)_a(PC)_f(PD)_alt(PC)_0.6$"
                + "m(EI)_a(RD)_f(HD)_alt(PC)_0.4$"
                + "m(HD)_a(RD)_f(PD)_alt(PC)_0.2$",
                0f,
                new int[]
                {
                    1
                }, 1, System.currentTimeMillis());

        
        float sum = 0;

        for (int j = 0; j < 100; j++)
        {
            Random rand = new Random();
            rand.setSeed(j);

            float UPnPPercentage = 5;
            int count = 0;
            float UPnPProbability = UPnPPercentage / 100f;
            for (int i = 0; i < 100; i++)
            {


                float possibility = rand.nextFloat();
                //System.out.println("possibility is "+possibility);
                if (possibility >= 0 && possibility < UPnPProbability)
                {
                    // upnp enabled nat
                    //System.out.println("UPnP");
                    count++;
                }
                else
                {
                    // return nat that does not support UPnP
                }
            }
            System.out.println("Total Upnp is " + count);
            sum+=count;

        }


        System.out.println("Avg is "+sum/100);
        return new TestSuite(NatFactoryTest.class);

    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue(true);
    }
}
