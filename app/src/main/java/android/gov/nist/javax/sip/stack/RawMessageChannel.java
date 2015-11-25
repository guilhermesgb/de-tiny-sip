package android.gov.nist.javax.sip.stack;

import android.gov.nist.javax.sip.message.SIPMessage;

public interface RawMessageChannel {
	
	public abstract void processMessage(SIPMessage sipMessage) throws Exception ;

}
