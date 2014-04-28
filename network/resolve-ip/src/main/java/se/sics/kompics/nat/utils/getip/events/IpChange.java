/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.kompics.nat.utils.getip.events;

import java.net.InetAddress;
import java.util.List;
import se.sics.kompics.Event;
import se.sics.kompics.nat.utils.getip.IpAddrStatus;


/**
 *
 * @author jdowling
 */
public class IpChange extends Event
{
    private final List<IpAddrStatus> addrs;
    private final InetAddress boundAddr;

    public IpChange(List<IpAddrStatus> addrs, InetAddress boundAddr) {
        this.addrs = addrs;
        this.boundAddr = boundAddr;
    }

    public List<IpAddrStatus> getAddrs() {
        return addrs;
    }

    public InetAddress getBoundAddr() {
        return boundAddr;
    }
}
