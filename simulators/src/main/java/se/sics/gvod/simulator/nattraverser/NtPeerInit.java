/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.simulator.nattraverser;

import se.sics.gvod.common.Self;
import se.sics.kompics.Init;

/**
 *
 * @author jdowling
 */
public class NtPeerInit extends Init {
    private final Self self;

    public NtPeerInit(Self self) {
        this.self = self;
    }

    public Self getSelf() {
        return self;
    }

}
