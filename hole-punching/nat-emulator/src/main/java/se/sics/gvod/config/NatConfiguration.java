/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.config;

import se.sics.gvod.net.Nat;

/**
 *
 * @author Jim
 */
public final class NatConfiguration
        extends AbstractConfiguration<NatConfiguration> {

    /**
     * Fields cannot be private. Package protected, ok.
     */
    String nats;
    double UPnPPercentage;
    int ruleExpirationTime;
    int delta;
//    final int[] ruleExpirationTime;

    /**
     * Default constructor comes first.
     */
    public NatConfiguration() {
        // only called from AbstractConfiguration
        super(VodConfig.getSeed());
        this.delta = 1;
        this.UPnPPercentage = 0.0d;
        StringBuilder sb = new StringBuilder();
        for (int i=0; i< Nat.NAT_COMBINATIONS.length; i++) {
            sb.append(Nat.NAT_COMBINATIONS[i]).append(",");
        }
        this.ruleExpirationTime = Nat.DEFAULT_RULE_EXPIRATION_TIME;
        this.nats = sb.toString();
    }

    /**
     * Full argument constructor comes second.
     */
    public NatConfiguration(int seed, 
            String nats,
            double UPnPPercentage,
            int ruleExpirationTime,
            int delta) {
        super(seed);
        this.ruleExpirationTime = ruleExpirationTime;
        this.delta = delta;
        this.nats = nats;
        this.UPnPPercentage = UPnPPercentage;
    }
    
    public static NatConfiguration build() {
        return new NatConfiguration();
    }

    public double getUPnPPercentage() {
        return UPnPPercentage;
    }

    public String getNats() {
        return nats;
    }

    public int getDelta() {
        return delta;
    }

//    public int[] getRuleExpirationTime()
    public int getRuleExpirationTime() {
        return ruleExpirationTime;
    }

    public NatConfiguration setNats(String nats) {
        this.nats = nats;
        return this;
    }

    public NatConfiguration setRuleExpirationTime(int ruleExpirationTime) {
        this.ruleExpirationTime = ruleExpirationTime;
        return this;
    }

    public NatConfiguration setUPnPPercentage(float UPnPPercentage) {
        this.UPnPPercentage = UPnPPercentage;
        return this;
    }

//    private static String intArrayToString(int[] intArray)
//    {
//        String stringArray = "";
//
//        for(int i = 0; i < intArray.length; i++)
//        {
//            stringArray += intArray[i]+",";
//        }
//        return stringArray;
//    }
//
//    private static int[] stringToIntArray(String stringArray)
//    {
//        StringTokenizer st = new StringTokenizer(stringArray,",");
//        int[] secondIntArray = new int[st.countTokens()];
//
//        for(int i = 0; i < secondIntArray.length; i++)
//        {
//            String token = st.nextToken();
//            secondIntArray[i] = Integer.parseInt(token);
//        }
//
//        return secondIntArray;
//    }
    public void setDelta(int delta) {
        this.delta = delta;
    }
}
