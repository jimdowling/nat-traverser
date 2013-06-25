package se.sics.gvod.simulator.common;

import se.sics.gvod.net.msgs.RewriteableMsg;


public class ReceivedMessage implements Comparable<ReceivedMessage> {
	private final Class<? extends RewriteableMsg> messageType;
	private int totalCount;

//-------------------------------------------------------------------	
	public ReceivedMessage(Class<? extends RewriteableMsg> messageType) {
		this.messageType = messageType;
		this.totalCount = 0;
	}

//-------------------------------------------------------------------	
	public ReceivedMessage(Class<? extends RewriteableMsg> messageType, int totalCount) {
		super();
		this.messageType = messageType;
		this.totalCount = totalCount;
	}

//-------------------------------------------------------------------	
	public Class<? extends RewriteableMsg> getMessageType() {
		return messageType;
	}

//-------------------------------------------------------------------	
	public void incrementCount() {
		totalCount++;
	}
	
//-------------------------------------------------------------------	
	public int getTotalCount() {
		return totalCount;
	}

//-------------------------------------------------------------------	
	@Override
	public int compareTo(ReceivedMessage that) {
		if (this.totalCount < that.totalCount)
			return 1;
		if (this.totalCount > that.totalCount)
			return -1;
		return 0;
	}

//-------------------------------------------------------------------	
	@Override
	public String toString() {
		return totalCount + " " + messageType.getSimpleName();
	}
}
