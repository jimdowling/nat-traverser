package se.sics.gvod.hp.shared;

import se.sics.gvod.common.hp.HPSessionKey;
import java.util.concurrent.ConcurrentHashMap;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class HPSessionKeyTest
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public HPSessionKeyTest( String testName )
    {
        super( testName );
        HPSessionKey key1 = new HPSessionKey(1,2);
        HPSessionKey key2 = new HPSessionKey(2,1);
        System.out.println("key1 is "+key1+" key2 is  "+key2);
        System.out.println("key1 hash is "+key1.hashCode()+" key2 hash is "+key2.hashCode());
        ConcurrentHashMap<HPSessionKey, Integer> map =
            new ConcurrentHashMap<HPSessionKey, Integer>();

        map.put(key1, Integer.SIZE);
        //map.put(key2, Integer.SIZE);

        for(HPSessionKey key : map.keySet())
        {
            System.out.println("map key is "+ key);
        }

        if(map.size() == 1
                && key1.equals(key2)
                && map.containsKey(key1)
                && map.containsKey(key2)
                )
        {
            assertTrue(true);

        }
        else
        {
            assert(false);
        }
        
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( HPSessionKeyTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }
}
