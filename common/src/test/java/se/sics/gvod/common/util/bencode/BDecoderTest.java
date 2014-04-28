/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.common.util.bencode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jdowling
 */
public class BDecoderTest {

    static File f;
    static int maxId = Integer.MAX_VALUE;
    static int minId = Integer.MAX_VALUE;
    static String name = "videoName.mp4";

    public BDecoderTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        try {
            f = File.createTempFile("BeTest", "bin");
            f.delete();

        } catch (IOException ex) {
            Logger.getLogger(BDecoderTest.class.getName()).log(Level.SEVERE, null, ex);
            assert (false);
        }
    }

    @AfterClass
    public static void tearDownClass() {
        f.deleteOnExit();
    }

    @Before
    public void setUp() {
        FileOutputStream fos = null;
        try {
            BEncoder be = new BEncoder();
            Map<String, Object> m = new HashMap<String, Object>();
            m.put("minId", minId);
            m.put("maxId", maxId);
            m.put("name", name);
            byte[] result = BEncoder.bencode(m);
            fos = new FileOutputStream(f);
            fos.write(result);
            fos.close();
        } catch (IOException ex) {
            Logger.getLogger(BDecoderTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fos.close();
            } catch (IOException ex) {
                Logger.getLogger(BDecoderTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of get_special_map_digest method, of class BDecoder.
     */
    @Test
    public void testDecode() {
        try {
            FileInputStream in = null;
            in = new FileInputStream(f);
            Map m = new BDecoder(in).bdecode().getMap();

            BEValue val = (BEValue) m.get("minId");
            if (val == null) {
                throw new InvalidBEncodingException("minId");
            }
            assertEquals(minId, val.getNumber().intValue());

            val = (BEValue) m.get("maxId");
            if (val == null) {
                throw new InvalidBEncodingException("maxId");
            }
            assertEquals(maxId, val.getNumber().intValue());
            
            val = (BEValue) m.get("name");
            if (val == null) {
                throw new InvalidBEncodingException("name");
            }
            assertEquals(name, val.getString());
        } catch (InvalidBEncodingException ex) {
            Logger.getLogger(BDecoderTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(BDecoderTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(BDecoderTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


}
