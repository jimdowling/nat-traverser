/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.net.msgs;

import se.sics.gvod.net.Transport;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.timer.TimeoutId;

/**
 *
 * @author jdowling
 */
public class RelayMsg {

    public static abstract class Base extends NatMsg {

        // src -> relay -> dest
        // nextDest is the 'other' address as part of the relay operation.
        // at 'src', nextDest is the dest. 
        // at 'relay', nextDest is src.
        // at 'dest', nextDest is src.
        protected VodAddress nextDest;
    
        protected final int clientId;
        protected final int remoteId;

        public Base(VodAddress source, VodAddress destination, int clientId, int remoteId) {
            this(source, destination, clientId, remoteId, destination, null);
        }

        public Base(VodAddress source, VodAddress destination, int clientId, int remoteId, 
                TimeoutId timeoutId) {
            this(source, destination, clientId, remoteId,  null, timeoutId);
        }

        public Base(VodAddress source, VodAddress destination, int clientId, int remoteId, 
                VodAddress nextDest) {
            this(source, destination, clientId, remoteId, nextDest, null);
        }

        public Base(VodAddress source, VodAddress destination, int clientId, int remoteId
                , VodAddress nextDest, TimeoutId timeoutId) {
            super(source, destination, Transport.UDP, timeoutId);
            assert(nextDest != null);
            this.nextDest = nextDest;
            this.remoteId = remoteId;
            this.clientId = clientId;
        }

        public VodAddress getNextDest() {
            return nextDest;
        }

        public int getClientId() {
            return clientId;
        }

        public int getRemoteId() {
            return remoteId;
        }

        public boolean atRelay() {
            if (vodDest.equals(nextDest)) {
                return false;
            }
            return true;
        }
        
        
        @Override
        public boolean equals(Object o) {
            // TODO  - not sure what equality should be. src,dest,nextDest is not good enough.
            if ((o instanceof Base) == false) {
                return false;
            }
            Base that = (Base) o;

            if (this.source.equals(that.getSource()) == false
                    || this.destination.equals(that.getDestination()) == false
                    || this.nextDest.equals(that.getNextDest()) == false
                    || this.remoteId != that.remoteId) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int prime = 3;
            return (source.hashCode()  + timeoutId.getId() + remoteId) / prime;
        }

        public void rewriteDestinationAtRelay(VodAddress newSrc, VodAddress destintation) {
            rewriteDestination(destintation.getPeerAddress()); // re-write destination for relay msg
            this.vodDest = destintation;
            // using public nat addr stored in relay
            nextDest = new VodAddress(vodSrc.getPeerAddress(), // source of relay msg
                    vodSrc.getOverlayId(), vodSrc.getNat());   // now stored in nextDest
            vodSrc = newSrc;           // source for this msg is the relay node.
        }

        @Override
        public String toString() {
            return "src: " + vodSrc + "; dest: " + vodDest + "; next: " + nextDest;
        }
        
        
    }


}
