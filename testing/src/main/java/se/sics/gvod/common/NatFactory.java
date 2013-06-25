/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.common;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Random;
import java.util.StringTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.net.Nat;

/**
 *
 * @author Salman, Jim
 */
// This class return random nats
public class NatFactory {

    private final Logger logger = LoggerFactory.getLogger(NatFactory.class);

    private class NatWithPercentage {

        private BigDecimal percentage;
        private Nat nat;

        NatWithPercentage(BigDecimal percentage, Nat nat) {
            if (percentage.floatValue() < 0 || percentage.floatValue() > 100) {
                throw new ArithmeticException("Invalid value for percentage. Domain [0,100]");
            }

            // checking for decimal places
            // only one decimal place is allowed
            BigDecimal testInt = new BigDecimal(Integer.toString(percentage.multiply(BigDecimal.TEN).intValue()));
            BigDecimal testFloat = testInt.divide(BigDecimal.TEN);
            if (testFloat.compareTo(percentage) != 0) {
                throw new ArithmeticException("Invalid value for percentage. only one decimal plance is allowed. percentage " + percentage
                        + " testInt " + testInt + " testFloat " + testFloat);
            }

            this.percentage = percentage;
            this.nat = nat;
        }

        public Nat getNat() {
            return nat;
        }

        public BigDecimal getPercentage() {
            return percentage;
        }
    }
    // array to store all the nats with percentages
    private NatWithPercentage natsWithPercentage[] = null;
    // probability array
    private int probArray[] = new int[1000];
    // random
    Random rand;
    int[] ruleExpTime = null;
    float UPnPPercentage = 0;

    public NatFactory(long seed) {
        this("m(EI)_a(PP)_f(PD)_alt(PC)_46.6$"
                + "m(PD)_a(RD)_f(PD)_alt(PC)_12.4$"
                + "m(EI)_a(RD)_f(PD)_alt(PC)_9.9$"
                + "m(EI)_a(PP)_f(EI)_alt(PC)_9.3$"
                + "m(EI)_a(RD)_f(EI)_alt(PC)_7.7$"
                + "m(EI)_a(PP)_f(HD)_alt(PC)_4.6$"
                + "m(EI)_a(PC)_f(EI)_alt(PC)_4.4$"
                + "m(PD)_a(PP)_f(PD)_alt(PC)_2.6$"
                + "m(PD)_a(PP)_f(EI)_alt(PC)_0.7$"
                + "m(PD)_a(RD)_f(EI)_alt(PC)_0.6$"
                + "m(EI)_a(PC)_f(PD)_alt(PC)_0.6$"
                + "m(EI)_a(RD)_f(HD)_alt(PC)_0.4$"
                + "m(HD)_a(RD)_f(PD)_alt(PC)_0.2$",
                0.0f,
                new int[]{
                    Nat.DEFAULT_RULE_EXPIRATION_TIME,
                    Nat.UPPER_RULE_EXPIRATION_TIME
                }, 1, seed);
    }

    public NatFactory(String nats, float UPnPPercentage,
            int[] ruleExpirationTime, int delta, long seed) {
        ruleExpTime = ruleExpirationTime;

        this.UPnPPercentage = UPnPPercentage;

        initializeNats(nats, delta, seed);

        checkVariables();

        String dbgString = "";
        for (int i = 0; i < ruleExpTime.length; i++) {
            dbgString += ruleExpTime[i] + ", ";
        }
        logger.trace("Nat Factory Initialized with rule times " + dbgString);
    }

    private void checkVariables() {
        logger.trace("checking variables");
        BigDecimal totalPercentage = new BigDecimal("0");
        for (int i = 0; i < natsWithPercentage.length; i++) {
            BigDecimal val = natsWithPercentage[i].getPercentage();
            totalPercentage = totalPercentage.add(val);
        }

        if (totalPercentage.compareTo(new BigDecimal("100.0")) != 0) {
            throw new ArithmeticException("Total percentage of all the nats must be 100. Current Total is " + totalPercentage);
        }

        // generating the probability array
        int index = 0;
        for (int i = 0; i < natsWithPercentage.length; i++) {
            int slots = natsWithPercentage[i].getPercentage().multiply(new BigDecimal("10")).intValue();
            for (int j = 0; j < slots; j++) {
                probArray[index++] = i;
            }
        }

        if (index != 1000) {
            throw new UnsupportedOperationException("probArray is not properly filled. index must be 1000");
        }
    }

    private void initializeNats(String nats, int delta, long seed) {
        rand = new Random();
        rand.setSeed(seed);

        HashMap<String, Nat.MappingPolicy> mappingHash = new HashMap<String, Nat.MappingPolicy>();
        HashMap<String, Nat.AllocationPolicy> allocationHash = new HashMap<String, Nat.AllocationPolicy>();
        HashMap<String, Nat.AlternativePortAllocationPolicy> altAllocationHash = new HashMap<String, Nat.AlternativePortAllocationPolicy>();
        HashMap<String, Nat.FilteringPolicy> filteringHash = new HashMap<String, Nat.FilteringPolicy>();

        mappingHash.put("m(EI)", Nat.MappingPolicy.ENDPOINT_INDEPENDENT);
        mappingHash.put("m(HD)", Nat.MappingPolicy.HOST_DEPENDENT);
        mappingHash.put("m(PD)", Nat.MappingPolicy.PORT_DEPENDENT);

        allocationHash.put("a(PP)", Nat.AllocationPolicy.PORT_PRESERVATION);
        allocationHash.put("a(PC)", Nat.AllocationPolicy.PORT_CONTIGUITY);
        allocationHash.put("a(RD)", Nat.AllocationPolicy.RANDOM);

        altAllocationHash.put("alt(PC)", Nat.AlternativePortAllocationPolicy.PORT_CONTIGUITY);
        altAllocationHash.put("alt(RD)", Nat.AlternativePortAllocationPolicy.RANDOM);

        filteringHash.put("f(EI)", Nat.FilteringPolicy.ENDPOINT_INDEPENDENT);
        filteringHash.put("f(HD)", Nat.FilteringPolicy.HOST_DEPENDENT);
        filteringHash.put("f(PD)", Nat.FilteringPolicy.PORT_DEPENDENT);

        StringTokenizer stz = new StringTokenizer(nats, "$");

        int natCount = stz.countTokens();
        NatWithPercentage nwp[] = new NatWithPercentage[natCount];

        for (int i = 0; i < natCount; i++) {
            String natString = stz.nextToken();
            //System.out.println(natString);
            StringTokenizer stz1 = new StringTokenizer(natString, "_");

            String element = (String) stz1.nextElement();
            Nat nat;
            if (element.equals("OPEN")) {
                nat = new Nat(Nat.Type.OPEN);
            } else if (element.equals("UPNP")) {
                //nat = new Nat(Nat.Type.UPNP, null/*public address is not known yet*/);
                throw new UnsupportedOperationException("Specify UPnP percentage as separate config param");
            } else {

                Nat.MappingPolicy mp = (Nat.MappingPolicy) setVariable(mappingHash, element);
                Nat.AllocationPolicy ap = (Nat.AllocationPolicy) setVariable(allocationHash, (String) stz1.nextElement());
                Nat.FilteringPolicy fp = (Nat.FilteringPolicy) setVariable(filteringHash, (String) stz1.nextElement());
                Nat.AlternativePortAllocationPolicy alt = (Nat.AlternativePortAllocationPolicy) setVariable(altAllocationHash, (String) stz1.nextElement());

                nat = new Nat(Nat.Type.NAT, mp, ap, fp, delta, Nat.DEFAULT_RULE_EXPIRATION_TIME);
            }
            String percent = (String) stz1.nextElement();

            nwp[i] = new NatWithPercentage(new BigDecimal(percent), nat);
        }

        natsWithPercentage = nwp;
    }

    private Object setVariable(HashMap map, String hashKey) {
        Object obj = map.get(hashKey);
        if (obj == null) {
            throw new UnsupportedOperationException("Incorrect File Name Format. hash key " + hashKey);
        }

        return obj;
    }

    private void printProbabilityArray() {
        for (int i = 0; i < probArray.length; i++) {
            if (i % 50 == 0) {
                System.out.println();
            }

            System.out.print(probArray[i] + ", ");
        }
    }

    public Nat getRandomNat() {
        int natIndex = rand.nextInt(natsWithPercentage.length);
        Nat nat = natsWithPercentage[natIndex].getNat();
        nat.setBindingTimeout(ruleExpTime[rand.nextInt(ruleExpTime.length)]);


        if (nat.getType() == Nat.Type.NAT) {
            float UPnPProbability = UPnPPercentage / 100f;
            float possibility = rand.nextFloat();
            if (possibility >= 0 && possibility < UPnPProbability) {
                // upnp enabled nat
                return new Nat(Nat.Type.UPNP, null, Nat.MappingPolicy.ENDPOINT_INDEPENDENT,
                        Nat.AllocationPolicy.PORT_PRESERVATION, Nat.FilteringPolicy.ENDPOINT_INDEPENDENT);
            }
        }

        // return nat that does not support UPnP
        return nat;


    }

    public Nat getProbabilisticNat() {
        int probabilityIndex = rand.nextInt(1000);
        int natIndex = probArray[probabilityIndex];
        Nat nat = natsWithPercentage[natIndex].getNat();
        nat.setBindingTimeout(ruleExpTime[rand.nextInt(ruleExpTime.length)]);

        if (nat.getType() == Nat.Type.NAT) {
            float UPnPProbability = UPnPPercentage / 100f;
            float possibility = rand.nextFloat();
            if (possibility >= 0 && possibility < UPnPProbability) {
                // upnp enabled nat
                return new Nat(Nat.Type.UPNP, null, Nat.MappingPolicy.ENDPOINT_INDEPENDENT,
                        Nat.AllocationPolicy.PORT_PRESERVATION, Nat.FilteringPolicy.ENDPOINT_INDEPENDENT);
            }
        }


        // return nat that does not support UPnP
        return nat;

    }

    public Nat getOpenNat() {
        return new Nat(Nat.Type.OPEN);
    }

    public Nat[] getAllNats() {
        Nat[] nats = new Nat[natsWithPercentage.length];
        int i = 0;
        for (NatWithPercentage nwp : natsWithPercentage) {
            nats[i++] = nwp.getNat();
        }
        return nats;
    }

    public Nat getEiPpPd() {
        return new Nat(Nat.Type.NAT,
                Nat.MappingPolicy.ENDPOINT_INDEPENDENT,
                Nat.AllocationPolicy.PORT_PRESERVATION,
                Nat.FilteringPolicy.PORT_DEPENDENT,
                0, Nat.DEFAULT_RULE_EXPIRATION_TIME);
    }
    
    public Nat getEiPpHd() {
        return new Nat(Nat.Type.NAT,
                Nat.MappingPolicy.ENDPOINT_INDEPENDENT,
                Nat.AllocationPolicy.PORT_PRESERVATION,
                Nat.FilteringPolicy.HOST_DEPENDENT,
                0, Nat.DEFAULT_RULE_EXPIRATION_TIME);
    }
    
    public Nat getEiPpEi() {
        return new Nat(Nat.Type.NAT,
                Nat.MappingPolicy.ENDPOINT_INDEPENDENT,
                Nat.AllocationPolicy.PORT_PRESERVATION,
                Nat.FilteringPolicy.ENDPOINT_INDEPENDENT,
                0, Nat.DEFAULT_RULE_EXPIRATION_TIME);
    }
    public Nat getPdPpPd() {
        return new Nat(Nat.Type.NAT,
                Nat.MappingPolicy.PORT_DEPENDENT,
                Nat.AllocationPolicy.PORT_PRESERVATION,
                Nat.FilteringPolicy.PORT_DEPENDENT,
                0, Nat.DEFAULT_RULE_EXPIRATION_TIME);
    }
    
    public Nat getEiPcEi() {
        return new Nat(Nat.Type.NAT,
                Nat.MappingPolicy.ENDPOINT_INDEPENDENT,
                Nat.AllocationPolicy.PORT_CONTIGUITY,
                Nat.FilteringPolicy.ENDPOINT_INDEPENDENT,
                2, Nat.DEFAULT_RULE_EXPIRATION_TIME);
    }
    
    
    public Nat getEiPcHd() {
        return new Nat(Nat.Type.NAT,
                Nat.MappingPolicy.ENDPOINT_INDEPENDENT,
                Nat.AllocationPolicy.PORT_CONTIGUITY,
                Nat.FilteringPolicy.HOST_DEPENDENT,
                1, Nat.DEFAULT_RULE_EXPIRATION_TIME);
    }

    public Nat getEiRdPd() {
        return new Nat(Nat.Type.NAT,
                Nat.MappingPolicy.ENDPOINT_INDEPENDENT,
                Nat.AllocationPolicy.RANDOM,
                Nat.FilteringPolicy.PORT_DEPENDENT,
                0, Nat.DEFAULT_RULE_EXPIRATION_TIME);
    }

    public Nat getEiPcPd() {
        return new Nat(Nat.Type.NAT,
                Nat.MappingPolicy.ENDPOINT_INDEPENDENT,
                Nat.AllocationPolicy.PORT_CONTIGUITY,
                Nat.FilteringPolicy.PORT_DEPENDENT,
                1, Nat.DEFAULT_RULE_EXPIRATION_TIME);
    }
    
    public Nat getPdPcPd() {
        return new Nat(Nat.Type.NAT,
                Nat.MappingPolicy.PORT_DEPENDENT,
                Nat.AllocationPolicy.PORT_CONTIGUITY,
                Nat.FilteringPolicy.PORT_DEPENDENT,
                1, Nat.DEFAULT_RULE_EXPIRATION_TIME);
    }
    
    public Nat getPdPcHd() {
        return new Nat(Nat.Type.NAT,
                Nat.MappingPolicy.PORT_DEPENDENT,
                Nat.AllocationPolicy.PORT_CONTIGUITY,
                Nat.FilteringPolicy.HOST_DEPENDENT,
                1, Nat.DEFAULT_RULE_EXPIRATION_TIME);
    }

    public Nat getPdRdPd() {
        return new Nat(Nat.Type.NAT,
                Nat.MappingPolicy.PORT_DEPENDENT,
                Nat.AllocationPolicy.RANDOM,
                Nat.FilteringPolicy.PORT_DEPENDENT,
                0, Nat.DEFAULT_RULE_EXPIRATION_TIME);
    }
}
