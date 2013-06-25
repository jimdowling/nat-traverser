package se.sics.gvod.simulator.common;
import se.sics.gvod.timer.SchedulePeriodicTimeout;
import se.sics.gvod.timer.ScheduleTimeout;
import se.sics.gvod.timer.Timeout;


public class GenerateReport extends Timeout {

	public GenerateReport(SchedulePeriodicTimeout request) {
		super(request);
	}

//-------------------------------------------------------------------	
	public GenerateReport(ScheduleTimeout request) {
		super(request);
	}
}
