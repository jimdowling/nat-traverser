/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.nat.emu;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Properties;
import java.util.StringTokenizer;
import se.sics.gvod.net.Nat;
import se.sics.gvod.net.Nat.AllocationPolicy;
import se.sics.gvod.net.Nat.AlternativePortAllocationPolicy;
import se.sics.gvod.net.Nat.FilteringPolicy;
import se.sics.gvod.net.Nat.MappingPolicy;
import se.sics.gvod.net.Nat.Type;

/**
 *
 * @author jdowling
 */
public final class NatGatewayConfiguration {

    final int ruleExpirationTime;
    final int delta;
    final long seed;
    final boolean upnp;
    final Nat.MappingPolicy portMappingPolicy;
    final Nat.AllocationPolicy portAllocationPolicy;
    final Nat.AlternativePortAllocationPolicy alternativePortAllocationPolicy;
    final Nat.FilteringPolicy filteringPolicy;
    final Nat.Type natType;
    final int startPortRange;
    final int endPortRange;
    final int natId;
    /**
     * Generated NATs configuration variables
     */
    final boolean generateNat;
    final String nats;
    final float upnpPercentage;
    final int[] ruleExpirationTimes;

    public NatGatewayConfiguration(int ruleExpirationTime, int delta, long seed,
            int natType, int mp, int ap, int fp, int altAp, boolean upnp,
            int startPortRange, int endPortRange, int natIdt) {
        this(ruleExpirationTime, delta, seed, natType, mp, ap, fp, altAp, upnp,
                startPortRange, endPortRange, natIdt, false, "", 0.0f, new int[] {120});
    }

    public NatGatewayConfiguration(int ruleExpirationTime, int delta, long seed,
            int natType, int mp, int ap, int fp, int altAp, boolean upnp,
            int startPortRange, int endPortRange, int natId,
            boolean generateNat, String nats, float upnpPercentage,
            int[] ruleExpirationTimes) {
        this.ruleExpirationTime = ruleExpirationTime;
        this.delta = delta;
        this.seed = seed;
        this.upnp = upnp;

        Nat.Type[] types = Nat.Type.values();
        this.natType = types[natType];

        if (mp > Nat.MappingPolicy.values().length || mp < 0) {
            throw new IllegalArgumentException("Out-of-bounds Mapping Policy selection");
        }
        if (ap > Nat.AllocationPolicy.values().length || ap < 0) {
            throw new IllegalArgumentException("Out-of-bounds Allocation Policy selection");
        }
        if (fp > Nat.FilteringPolicy.values().length || fp < 0) {
            throw new IllegalArgumentException("Out-of-bounds Filtering Policy selection");
        }
        if (altAp > Nat.AlternativePortAllocationPolicy.values().length || altAp < 0) {
            throw new IllegalArgumentException("Out-of-bounds Alternative Allocation Policy selection");
        }

        Nat.MappingPolicy[] mps = Nat.MappingPolicy.values();
        this.portMappingPolicy = mps[mp];

        Nat.AllocationPolicy[] aps = Nat.AllocationPolicy.values();
        this.portAllocationPolicy = aps[ap];

        Nat.FilteringPolicy[] fps = Nat.FilteringPolicy.values();
        this.filteringPolicy = fps[fp];

        Nat.AlternativePortAllocationPolicy[] altAps = Nat.AlternativePortAllocationPolicy.values();
        this.alternativePortAllocationPolicy = altAps[altAp];

        this.startPortRange = startPortRange;
        this.endPortRange = endPortRange;
        this.natId = natId;

        this.generateNat = generateNat;
        this.nats = nats;
        this.upnpPercentage = upnpPercentage;
        this.ruleExpirationTimes = ruleExpirationTimes;
    }

    public int getEndPortRange() {
        return endPortRange;
    }

    public int getStartPortRange() {
        return startPortRange;
    }

    public MappingPolicy getPortMappingPolicy() {
        return portMappingPolicy;
    }

    public AllocationPolicy getPortAllocationPolicy() {
        return portAllocationPolicy;
    }

    public FilteringPolicy getFilteringPolicy() {
        return filteringPolicy;
    }

    public AlternativePortAllocationPolicy getAlternativePortAllocationPolicy() {
        return alternativePortAllocationPolicy;
    }

    public Type getNatType() {
        return natType;
    }

    public int getDelta() {
        return delta;
    }

    public int getRuleExpirationTime() {
        return ruleExpirationTime;
    }

    public long getSeed() {
        return seed;
    }

    public boolean isGenerateNat() {
        return generateNat;
    }

    public String getNats() {
        return nats;
    }

    public float getUpnpPercentage() {
        return upnpPercentage;
    }

    public int getNatId() {
        return natId;
    }

    public int[] getRuleExpirationTimes() {
        return ruleExpirationTimes;
    }



//    public void store(String file) throws IOException {
//        Properties p = new Properties();
//        p.setProperty("nat.ruleExpirationTime", "" + this.ruleExpirationTime);
//        p.setProperty("nat.delta", "" + this.delta);
//        p.setProperty("nat.seed", "" + this.seed);
//        p.setProperty("nat.type", "" + this.natType.ordinal());
//        p.setProperty("nat.mp", "" + portMappingPolicy.ordinal());
//        p.setProperty("nat.ap", "" + portAllocationPolicy.ordinal());
//        p.setProperty("nat.fp", "" + filteringPolicy.ordinal());
//        p.setProperty("nat.alt.ap", "" + alternativePortAllocationPolicy.ordinal());
//        p.setProperty("upnp", "" + this.upnp);
//        p.setProperty("nat.startPort", "" + this.startPortRange);
//        p.setProperty("nat.endPort", "" + this.endPortRange);
//        p.setProperty("nat.id", "" + this.natId);
//        p.setProperty("nat.generate", "" + this.isGenerateNat());
//        p.setProperty("nat.strings", "" + this.nats);
//        p.setProperty("nat.upnpPercent", "" + this.upnpPercentage);
//        p.setProperty("nat.ruleExpirationTimes", "" + intArrayToString(this.ruleExpirationTimes));
//
//        Writer writer = new FileWriter(file);
//        p.store(writer, "se.sics.usurp.natemu");
//    }
//
//    public static NatGatewayConfiguration load(String file) throws IOException {
//        Properties p = new Properties();
//        Reader reader = new FileReader(file);
//        p.load(reader);
//
//        int ruleExpTime = Integer.parseInt(p.getProperty("nat.ruleExpirationTime"));
//        int natDelta = Integer.parseInt(p.getProperty("nat.delta"));
//        long natSeed = Long.parseLong(p.getProperty("nat.seed"));
//        int natType = Integer.parseInt(p.getProperty("nat.type"));
//        int mp = Integer.parseInt(p.getProperty("nat.mp"));
//        int ap = Integer.parseInt(p.getProperty("nat.ap"));
//        int fp = Integer.parseInt(p.getProperty("nat.fp"));
//        int altAp = Integer.parseInt(p.getProperty("nat.alt.ap"));
//        int startPort = Integer.parseInt(p.getProperty("nat.startPort"));
//        int endPort = Integer.parseInt(p.getProperty("nat.endPort"));
//        int natId = Integer.parseInt(p.getProperty("nat.id"));
//
//        boolean upnp = Boolean.parseBoolean(p.getProperty("upnp"));
//
//        boolean generateNat = Boolean.parseBoolean(p.getProperty("nat.generate"));
//        String natStrings = p.getProperty("nat.strings");
//        float upnpPercent = Float.parseFloat(p.getProperty("nat.upnpPercent"));
//        int[] ruleExpTimes = stringToIntArray(p.getProperty("nat.ruleExpirationTimes"));
//
//        return new NatGatewayConfiguration(ruleExpTime, natDelta, natSeed, natType, mp, fp, ap, altAp, upnp,
//                startPort, endPort, natId, generateNat, natStrings, upnpPercent, ruleExpTimes);
//    }
//
//    private static String intArrayToString(int[] intArray)
//    {
//        StringBuilder stringArray = new StringBuilder();
//
//        for(int i = 0; i < intArray.length; i++)
//        {
//            stringArray.append(intArray[i]).append(",");
//        }
//        return stringArray.toString();
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

}
