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
public class CachedNatType implements Serializable {

    private static final long serialVersionUID = 88881198522692850L;
    private NatBean natBean;

    public CachedNatType() {
        this.natBean = new NatBean();
    }

    public CachedNatType(NatBean natBean) {
        this.natBean = natBean;
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CachedNatType == false) {
            return false;
        }

        CachedNatType that = (CachedNatType) obj;
        if (this.getNatBean().getNatPolicy().compareTo(
                that.getNatBean().getNatPolicy())!=0) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + this.natBean.hashCode();
        return hash;
    }

    /**
     * @return the gvodAddressBean
     */
    public NatBean getNatBean() {
        return natBean;
    }

    /**
     * @param gvodAddressBean the gvodAddressBean to set
     */
    public void setNatBean(NatBean gvodAddressBean) {
        this.natBean = gvodAddressBean;
    }
}
