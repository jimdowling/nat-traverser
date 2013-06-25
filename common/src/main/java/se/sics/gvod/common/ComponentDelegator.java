/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.common;

import se.sics.kompics.Channel;
import se.sics.kompics.ChannelFilter;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Event;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Port;
import se.sics.kompics.PortType;
import se.sics.kompics.Positive;

/**
 *
 * @author jim
 */
public interface ComponentDelegator {

    public Component doCreate(Class<? extends ComponentDefinition> definition);
    
    public <P extends PortType> void doTrigger(Event event, Port<P> port);

    public <E extends Event, P extends PortType> void doSubscribe(Handler<E> handler, Port<P> port);

    public <P extends PortType> Negative<P> getNegative(Class<P> portType);

    public <P extends PortType> Positive<P> getPositive(Class<P> portType);
    
    public <P extends PortType> Channel<P> doConnect(
			Positive<P> positive, Negative<P> negative);

    public <P extends PortType> Channel<P> doConnect(
			Negative<P> negative, Positive<P> positive);

    public <P extends PortType> Channel<P> doConnect(Positive<P> positive,
			Negative<P> negative, ChannelFilter<?, ?> filter);

    public <P extends PortType> Channel<P> doConnect(Negative<P> negative,
			Positive<P> positive, ChannelFilter<?, ?> filter);

    public <P extends PortType> void doDisconnect(Negative<P> negative,
			Positive<P> positive);

    public <P extends PortType> void doDisconnect(Positive<P> positive,
			Negative<P> negative);
    
    public boolean isUnitTest();
}
