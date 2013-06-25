/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.common.hp;

/**
 *
 * @author Owner
 */
public class HolePunching {

    private int client_A_ID;
    private int client_B_ID;
    private HPMechanism holePunchingMechanism;
    private HPRole client_A_HPRole;
    private HPRole client_B_HPRole;
    private int client_A_Interleaved_PRP_InterleavedPort;
    private int client_B_Interleaved_PRP_InterleavedPort;
    private int client_A_Interleaved_PRC_PredictedPort;
    private int client_B_Interleaved_PRC_PredictedPort;
    private boolean client_A_FinishedHP;  // whether hp has finished or not
    private boolean client_B_FinishedHP;
    private HpStatus client_A_HPStatus;    // if hp has finished then what was the status. was it successful?
    private HpStatus client_B_HPStatus;
    private long sesssionStartTime;

    public static enum HpStatus {

        FAILURE,
        SUCCESS
    };

    public HolePunching(int client_A_ID, int client_B_ID,
            HPMechanism holePunchingMechanism, HPRole client_A_HPRole,
            HPRole client_B_HPRole) {
        this.client_A_ID = client_A_ID;
        this.client_B_ID = client_B_ID;
        this.client_A_HPRole = client_A_HPRole;     // role of the client i.e. SHP_INITIATOR, SHP_RESPONDER
        this.client_B_HPRole = client_B_HPRole;     // role of the client i.e. SHP_INITIATOR, SHP_RESPONDER
        this.client_A_Interleaved_PRP_InterleavedPort = 0;
        this.client_B_Interleaved_PRP_InterleavedPort = 0;
        this.client_A_Interleaved_PRC_PredictedPort = 0;
        this.client_B_Interleaved_PRC_PredictedPort = 0;
        this.client_A_FinishedHP = false;
        this.client_B_FinishedHP = false;
        this.client_A_HPStatus = null;
        this.client_B_HPStatus = null;
        this.holePunchingMechanism = holePunchingMechanism;   // hole punching mechanism i.e. SHP, PRP, PRP_PRP

        this.sesssionStartTime = -1;
    }

    @Override
    public int hashCode() {
        int val = 37;
        val += this.client_A_ID;
        val += 177;
        val += this.client_B_ID;
        return val;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof HolePunching == false) {
            return false;
        }
        HolePunching that = (HolePunching) obj;
        if ((this.client_A_ID == that.client_A_ID
                && this.client_B_ID == that.client_B_ID)
                || (this.client_B_ID == that.client_A_ID
                && this.client_A_ID == that.client_B_ID)) {
            return true;
        }
        return false;
    }

    public long getSesssionStartTime() {
        return sesssionStartTime;
    }

    public void setSesssionStartTime(long sesssionStartTime) {
        this.sesssionStartTime = sesssionStartTime;
    }

    public void set_Client_Finished_HP(int id, boolean val, HpStatus status) {
        if (client_A_ID == id) {
            this.client_A_FinishedHP = val;
            this.client_A_HPStatus = status;
        } else if (client_B_ID == id) {
            this.client_B_FinishedHP = val;
            this.client_B_HPStatus = status;
        } else {
            throw new WrongClientIdException("Wrong client id");
        }
    }

    public boolean hasBothClientsFinishedHP() {
        return this.client_A_FinishedHP && this.client_B_FinishedHP;
    }

    public boolean bothClientsWereSuccessfulInHP() {
        if (client_A_HPStatus != null
                && client_B_HPStatus != null) {
            if (client_A_HPStatus == HpStatus.SUCCESS
                    && client_B_HPStatus == HpStatus.SUCCESS) {
                return true;
            }
        }

        return false;

    }

    public void set_Interleaved_PRP_Port(int id, int port) {
        if (client_A_ID == id) {
            client_A_Interleaved_PRP_InterleavedPort = port;

        } else if (client_B_ID == id) {
            client_B_Interleaved_PRP_InterleavedPort = port;
        } else {
            throw new WrongClientIdException("Wrong client id");
        }
    }

    public int get_Interleaved_PRP_Port(int id) {
        if (client_A_ID == id) {
            return client_A_Interleaved_PRP_InterleavedPort;
        } else if (client_B_ID == id) {
            return client_B_Interleaved_PRP_InterleavedPort;
        } else {
            throw new WrongClientIdException("Wrong client id");
        }
    }

    public void set_Interleaved_PRC_PredictivePort(int id, int port) {
        if (client_A_ID == id) {
            client_A_Interleaved_PRC_PredictedPort = port;

        } else if (client_B_ID == id) {
            client_B_Interleaved_PRC_PredictedPort = port;
        } else {
            throw new WrongClientIdException("Wrong client id");
        }
    }

    public int get_Interleaved_PRC_PredictedPort(int id) {
        if (client_A_ID == id) {
            return client_A_Interleaved_PRC_PredictedPort;
        } else if (client_B_ID == id) {
            return client_B_Interleaved_PRC_PredictedPort;
        } else {
            throw new WrongClientIdException("Wrong client id");
        }
    }

    public HPRole getHolePunchingRoleOf(int id) {
        if (client_A_ID == id) {
            return client_A_HPRole;
        } else if (client_B_ID == id) {
            return client_B_HPRole;
        } else {
//            logger.error("ERROR: client ID doest not match");
            throw new WrongClientIdException("ERROR: client ID doest not match");
        }
    }

    public int getInitiatorID() {
        // the concept of initiator and responder is only valid for
        // SHP, PRP and PRC

        if (holePunchingMechanism == HPMechanism.SHP
                || holePunchingMechanism == HPMechanism.PRP
                || holePunchingMechanism == HPMechanism.PRC) {
            if (client_A_HPRole == HPRole.SHP_INITIATOR
                    || client_A_HPRole == HPRole.PRP_INITIATOR
                    || client_A_HPRole == HPRole.PRP_INTERLEAVED
                    || client_A_HPRole == HPRole.PRC_INITIATOR) {
                return client_A_ID;
            } else {
                return client_B_ID;
            }
        } else {
            throw new WrongClientIdException("The concept of Initiator/Responder is only valid for SHP, PRP, PRC only. ");
        }
    }

    public int getResponderID() {
        // the concept of initiator and responder is only valid for
        // SHP, PRP and PRC

        if (holePunchingMechanism == HPMechanism.SHP
                || holePunchingMechanism == HPMechanism.PRP
                || holePunchingMechanism == HPMechanism.PRC) {
            if (client_A_HPRole == HPRole.SHP_INITIATOR
                    || client_A_HPRole == HPRole.PRP_INITIATOR
                    || client_A_HPRole == HPRole.PRP_INTERLEAVED
                    || client_A_HPRole == HPRole.PRC_INITIATOR) {
                return client_B_ID;
            } else {
                return client_A_ID;
            }
        } else {
            throw new WrongClientIdException("The concept of Initiator/Responder is only valid for SHP, PRP, PRC only. ");
        }
    }

    public HPRole getClient_A_HPRole() {
        return client_A_HPRole;
    }

    public int getClient_A_ID() {
        return client_A_ID;
    }

    public HPRole getClient_B_HPRole() {
        return client_B_HPRole;
    }

    public int getClient_B_ID() {
        return client_B_ID;
    }

    public HPMechanism getHolePunchingMechanism() {
        return holePunchingMechanism;
    }

    @Override
    public String toString() {
        return "(" + client_A_ID + ", " + client_B_ID + ")" + this.holePunchingMechanism
                + " - " + (System.currentTimeMillis() - this.sesssionStartTime);
    }
}