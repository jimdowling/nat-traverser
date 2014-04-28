package se.sics.gvod.common;

/**
 *
 * @author jdowling
 * @author Niklas Wahl&#233;n <nwahlen@kth.se>
 */
public class UtilityLS implements Utility {

    private int availableBandwidth;

    public UtilityLS() {
        this(0);
    }

    public UtilityLS(int availableBandwidth) {
        this.availableBandwidth = availableBandwidth;
    }

    @Override
    public int getValue() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Utility clone() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Impl getImplType() {
        return Utility.Impl.LsUtility;
    }

    public int getAvailableBandwidth() {
        return availableBandwidth;
    }

    public void setAvailableBandwidth(int availableBandwidth) {
        this.availableBandwidth = availableBandwidth;
    }
}
