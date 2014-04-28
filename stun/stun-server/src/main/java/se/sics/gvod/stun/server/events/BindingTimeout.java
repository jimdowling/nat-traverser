package se.sics.gvod.stun.server.events;

import se.sics.gvod.timer.Timeout;
import se.sics.gvod.timer.ScheduleTimeout;

public final class BindingTimeout extends Timeout {

	private final int attribute;

	public BindingTimeout(ScheduleTimeout request, int attribute) {
		super(request);
		this.attribute = attribute;
	}
	
	public int getAttribute() {
		return attribute;
	}
}
