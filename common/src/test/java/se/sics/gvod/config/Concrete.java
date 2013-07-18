/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.config;

/**
 *
 * @author jdowling
 */
public class Concrete extends AbstractConfiguration<Concrete> {

    int val;

    public Concrete() {
        this(100);
    }

    public Concrete(int val) {
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
        Concrete c = (Concrete) obj;
        return c.getVal() == this.val;
    }
}
