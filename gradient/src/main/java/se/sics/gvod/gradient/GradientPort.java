/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.gradient;

import se.sics.gvod.gradient.events.FingersRequest;
import se.sics.gvod.gradient.events.FingersResponse;
import se.sics.gvod.gradient.events.GradientSample;
import se.sics.gvod.gradient.events.UtilityChanged;
import se.sics.kompics.PortType;

/**
 *
 * @author jim
 */
public class GradientPort extends PortType {

    {
        negative(FingersRequest.class);
        negative(UtilityChanged.class);
        negative(GradientSample.class);
        positive(GradientSample.class);
        positive(FingersResponse.class);
    }
}
