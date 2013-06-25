/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.common.util;

import java.io.Serializable;

/**
 *
 * @author jdowling
 */
public class StartupConfig implements Serializable {

    private static final long serialVersionUID = 88881198522692850L;
    private boolean upnpSupported;
    private int numTimesUnchanged;
    private int numTimesSinceStunLastRun;
    private VodAddressBean vodAddressBean;

    public StartupConfig() {
    }

    public StartupConfig(boolean upnpSupported, VodAddressBean vodAddressBean, int numTimesUnchanged,
            int numTimesSinceStunLastRun) {
        this.upnpSupported = upnpSupported;
        this.vodAddressBean = vodAddressBean;
        this.numTimesUnchanged = numTimesUnchanged;
        this.numTimesSinceStunLastRun = numTimesSinceStunLastRun;
    }

    public int getNumTimesSinceStunLastRun() {
        return numTimesSinceStunLastRun;
    }

    public void setNumTimesSinceStunLastRun(int numTimesSinceStunLastRun) {
        this.numTimesSinceStunLastRun = numTimesSinceStunLastRun;
    }

    public int getNumTimesUnchanged() {
        return numTimesUnchanged;
    }

    public void setNumTimesUnchanged(int numTimesUnchanged) {
        this.numTimesUnchanged = numTimesUnchanged;
    }

    public boolean isUpnpSupported() {
        return upnpSupported;
    }

    public void setUpnpSupported(boolean upnpSupported) {
        this.upnpSupported = upnpSupported;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StartupConfig == false) {
            return false;
        }

        StartupConfig that = (StartupConfig) obj;
        if (this.getVodAddressBean().getNatPolicy() != that.getVodAddressBean().getNatPolicy()) {
            return false;
        }
        if (this.upnpSupported != that.upnpSupported) {
            return false;
        }

        if (this.numTimesSinceStunLastRun != that.numTimesSinceStunLastRun) {
            return false;
        }
        if (this.numTimesUnchanged != that.numTimesUnchanged) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + (this.upnpSupported ? 1 : 0);
        hash = 41 * hash + this.getVodAddressBean().getNatPolicy();
        hash = 41 * hash + this.numTimesUnchanged;
        hash = 41 * hash + this.numTimesSinceStunLastRun;
        return hash;
    }

    /**
     * @return the gvodAddressBean
     */
    public VodAddressBean getVodAddressBean() {
        return vodAddressBean;
    }

    /**
     * @param gvodAddressBean the gvodAddressBean to set
     */
    public void setGvodAddressBean(VodAddressBean gvodAddressBean) {
        this.vodAddressBean = gvodAddressBean;
    }
}
