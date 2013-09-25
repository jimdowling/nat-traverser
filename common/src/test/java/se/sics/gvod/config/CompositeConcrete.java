/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.config;

/**
 *
 * @author jdowling
 */
public class CompositeConcrete extends CompositeConfiguration {

    ConcreteInt configInt;
    ConcreteStr configStr;

    public CompositeConcrete() {
        configInt = new ConcreteInt();
        configStr = new ConcreteStr();
    }

    public ConcreteInt getConfigInt() {
        return configInt;
    }

    public ConcreteStr getConfigStr() {
        return configStr;
    }

    @Override
    public boolean equals(Object obj) {
        return configStr.equals(((CompositeConcrete) obj ).getConfigStr()) &&
        configStr.equals(((CompositeConcrete) obj ).getConfigStr());
                
    }

    
}
