/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.config;

/**
 *
 * @author jdowling
 */
public class ConcreteStr extends AbstractConfiguration<ConcreteStr> {

    String val;

    public ConcreteStr() {
        this("Blah");
    }

    public ConcreteStr(String val) {
        this.val = val;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    @Override
    public boolean equals(Object obj) {
        ConcreteStr c = (ConcreteStr) obj;
        return c.getVal().equals(this.val);
    }

    
    
}
