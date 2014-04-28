/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.config;

/**
 *
 * @author jdowling
 */
public class ConcreteInt extends AbstractConfiguration<ConcreteInt> {

    int val;

    public ConcreteInt() {
        this(100);
    }

    public ConcreteInt(int val) {
        this.val = val;
    }
    
    public int getVal() {
        return val;
    }

    public void setVal(int val) {
        this.val = val;
    }

    @Override
    public boolean equals(Object obj) {
        ConcreteInt c = (ConcreteInt) obj;
        return c.getVal() == this.val;
    }
}
