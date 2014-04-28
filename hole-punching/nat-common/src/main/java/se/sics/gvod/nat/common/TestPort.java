/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.nat.common;

import se.sics.kompics.PortType;

/**
 *
 * @author Jim Dowling<jdowling@sics.se>
 */
public class TestPort extends PortType
{
    {
        positive(TestMsg.class);
        negative(TestEvent.class);
        
    }
}
