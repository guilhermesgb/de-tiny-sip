/*
 * This file is part of TinySip. 
 * http://code.google.com/p/de-tiny-sip/
 * 
 * Created 2011 by Sebastian Rosch <flowfire@sebastianroesch.de>
 * 
 * This software is licensed under the Apache License 2.0.
 */

package de.tinysip.sip;

import android.gov.nist.gnjvx.sdp.MediaDescriptionImpl;
import android.gov.nist.gnjvx.sdp.fields.AttributeField;
import android.gov.nist.gnjvx.sdp.fields.ConnectionField;
import android.gov.nist.gnjvx.sdp.fields.MediaField;
import android.gov.nist.gnjvx.sdp.fields.OriginField;
import android.gov.nist.gnjvx.sdp.fields.SDPKeywords;
import android.gov.nist.gnjvx.sdp.fields.SessionNameField;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import android.javax.sdp.Attribute;
import android.javax.sdp.MediaDescription;
import android.javax.sdp.SdpConstants;
import android.javax.sdp.SdpException;
import android.javax.sdp.SdpFactory;
import android.javax.sdp.SessionDescription;
import android.javax.sip.ClientTransaction;
import android.javax.sip.Dialog;
import android.javax.sip.InvalidArgumentException;
import android.javax.sip.ListeningPoint;
import android.javax.sip.ObjectInUseException;
import android.javax.sip.PeerUnavailableException;
import android.javax.sip.ServerTransaction;
import android.javax.sip.SipException;
import android.javax.sip.SipFactory;
import android.javax.sip.SipProvider;
import android.javax.sip.SipStack;
import android.javax.sip.TransportNotSupportedException;
import android.javax.sip.address.Address;
import android.javax.sip.address.AddressFactory;
import android.javax.sip.address.SipURI;
import android.javax.sip.header.CSeqHeader;
import android.javax.sip.header.CallIdHeader;
import android.javax.sip.header.ContactHeader;
import android.javax.sip.header.ContentTypeHeader;
import android.javax.sip.header.ExpiresHeader;
import android.javax.sip.header.FromHeader;
import android.javax.sip.header.HeaderFactory;
import android.javax.sip.header.MaxForwardsHeader;
import android.javax.sip.header.ToHeader;
import android.javax.sip.header.ViaHeader;
import android.javax.sip.message.Message;
import android.javax.sip.message.MessageFactory;
import android.javax.sip.message.Request;
import android.javax.sip.message.Response;
import android.util.Log;

/**
 * SipMessageHandler takes care of sending sip messages and parsing incoming ones.
 * 
 * @author Sebastian
 * 
 */
public class SipMessageHandler {
	private static String TAG = "tSIP";
	private AddressFactory addressFactory = null;
	private MessageFactory messageFactory = null;
	private HeaderFactory headerFactory = null;
	private SipStack sipStack = null;
	private SipProvider sipProvider = null;
	private ListeningPoint listeningPoint = null;

	private SipProfile sipProfile = null;
	private String localIPAddress;
	private int localSipPort;

	private ServerTransaction serverTransaction = null;
	private ClientTransaction clientTransaction = null;
	private CallIdHeader currentCallID = null;
	private Dialog currentDialog = null;

	private long callSequence = 1L;

	private static SipMessageHandler sipMessageHandler = null;

	/**
	 * Create a SipMessageHandler using the LocalSipProfile and a STUN DiscoveryInfo.
	 * 
	 * @param localSipProfile
	 *            the local user's SIP profile to register with the provider
	 * @param discoveryInfo
	 *            the STUN DiscoveryInfo for NAT traversal
	 * @throws PeerUnavailableException
	 * @throws TransportNotSupportedException
	 * @throws InvalidArgumentException
	 * @throws ObjectInUseException
	 */
	private SipMessageHandler(SipProfile sipProfile, String localIPAddress, int localSipPort) throws PeerUnavailableException, TransportNotSupportedException,
			InvalidArgumentException, ObjectInUseException {

		Log.d("SipMessageHandler constructor", "BEGIN");
		this.sipProfile = sipProfile;
		this.localIPAddress = localIPAddress;
		this.localSipPort = localSipPort;
		// Create SipFactory
		SipFactory sipFactory = SipFactory.getInstance();

		// Create unique name properties for SipStack
		Properties properties = new Properties();
		properties.setProperty("android.javax.sip.STACK_NAME", "tinysip");

		// Create SipStack object
		sipStack = sipFactory.createSipStack(properties);
		headerFactory = sipFactory.createHeaderFactory();
		addressFactory = sipFactory.createAddressFactory();
		messageFactory = sipFactory.createMessageFactory();

        Log.d("SipMessageHandler constructor", "SYNC POINT");

		int minPort = 5000, maxPort = 6000;
		boolean successfullyBound = false;
        int tries = 3;
		while (tries > 0 && !successfullyBound) {
			try {
                Log.d("SipMessageHandler", " while tries:" + tries + " localIPAddress:" + localIPAddress + " localSipPort:" + localSipPort);
				listeningPoint = sipStack.createListeningPoint(localIPAddress, localSipPort, ListeningPoint.UDP);
			} catch (InvalidArgumentException ex) {
                ex.printStackTrace();
				// choose another port between MIN and MAX
				localSipPort = (int) ((maxPort - minPort) * Math.random()) + minPort;
                tries--;
				continue;
			}
			successfullyBound = true;
            sipProfile.setLocalSipPort(localSipPort);
			//TODO: needs to check for the public port again if the local one changed
		}

		sipProvider = sipStack.createSipProvider(listeningPoint);
		Log.d("SipMessageHandler constructor", "BEGIN");
	}

	/**
	 * Create an instance of SipMessageHandler.
	 * 
	 * @param sipProfile
	 *            the local user's SIP profile to register with the provider
	 * @param discoveryInfo
	 *            the STUN DiscoveryInfo for NAT traversal
	 * @return the created instance of SipMessageHandler
	 * @throws PeerUnavailableException
	 * @throws TransportNotSupportedException
	 * @throws ObjectInUseException
	 * @throws InvalidArgumentException
	 */
	public static SipMessageHandler createInstance(SipProfile sipProfile, String localIPAddress, int localSipPort) throws PeerUnavailableException, TransportNotSupportedException,
			ObjectInUseException, InvalidArgumentException {
		if (sipMessageHandler == null) {
			Log.d("SipMessageHandler", "createInstance - BEGIN");
			sipMessageHandler = new SipMessageHandler(sipProfile, localIPAddress, localSipPort);
			Log.d("SipMessageHandler", "createInstance - END");
		}
		return sipMessageHandler;
	}

	/**
	 * @return the instance of the SipMessageHandler which needs to be created first
	 */
	public static SipMessageHandler getInstance() {
		return sipMessageHandler;
	}

	/**
	 * @return the SipProvider
	 */
	public SipProvider getSipProvider() {
		return sipProvider;
	}

	/**
	 * @return the SipProfile
	 */
	public SipProfile getSipProfile() {
		return sipProfile;
	}

	/**
	 * Register the local profile with the provider.
	 * 
	 * @param state
	 *            the current SipRequestState of the SipManager, used for assembling the message
	 * @throws ParseException
	 * @throws InvalidArgumentException
	 * @throws SipException
	 */
	public void register(SipRequestState state) throws ParseException, InvalidArgumentException, SipException {
		// create To and From headers
		FromHeader fromHeader = sipProfile.getFromHeader(addressFactory, headerFactory);
		ToHeader toHeader = sipProfile.getToHeader(addressFactory, headerFactory);

		// create a new Request URI
		SipURI requestURI = addressFactory.createSipURI(sipProfile.getUsername(), sipProfile.getSipDomain());

		// Create Via headers
		List<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
		ViaHeader viaHeader = headerFactory.createViaHeader(localIPAddress, localSipPort, listeningPoint.getTransport(), null);
		viaHeader.setRPort();
		viaHeaders.add(viaHeader);

		// Create a new CallId header
		CallIdHeader callIdHeader = sipProvider.getNewCallId();

		// Create a new Cseq header
		CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(callSequence, Request.REGISTER);

		// Create a new MaxForwards header
		MaxForwardsHeader maxForwards = headerFactory.createMaxForwardsHeader(70);

		// Create the request
		Request request = messageFactory.createRequest(requestURI, Request.REGISTER, callIdHeader, cSeqHeader, fromHeader, toHeader, viaHeaders, maxForwards);

		if (state.equals(SipRequestState.UNREGISTER) || state.equals(SipRequestState.UNREGISTER_AUTHORIZATION)) {
			// Create a new Expires header
			ExpiresHeader expires = headerFactory.createExpiresHeader(0);
			request.addHeader(expires);

			// Create an empty Contact header
			ContactHeader contactHeader = headerFactory.createContactHeader();
			request.addHeader(contactHeader);
		} else {
			// Create the contact name address
			SipURI contactURI = addressFactory.createSipURI(sipProfile.getUsername(), localIPAddress);
			contactURI.setPort(localSipPort);
			Address contactAddress = addressFactory.createAddress(contactURI);
			contactAddress.setDisplayName(sipProfile.getDisplayName());

			// Create a new Contact header
			ContactHeader contactHeader = headerFactory.createContactHeader(contactAddress);
			contactHeader.setExpires(3600); // 20 minutes
			request.addHeader(contactHeader);
		}

		if (state.equals(SipRequestState.AUTHORIZATION) || state.equals(SipRequestState.UNREGISTER_AUTHORIZATION)) {
			request.addHeader(sipProfile.getAuthorizationHeader(headerFactory));
		}

		// Create the client transaction and send the request
		clientTransaction = sipProvider.getNewClientTransaction(request);
		clientTransaction.sendRequest();
		System.out.println(TAG +": REGISTER sent");
	}

	/**
	 * Creates a SipSession.
	 * 
	 * @param message
	 *            the message to extract the SipSession information
	 * @param sdpSession
	 *            a SessionDescription to parse supported formats
	 * @return a SipSession containing all information about the current session
	 * @throws SdpException
	 * @throws UnknownHostException
	 */
	public static SipSession createSipSession(Message message, SessionDescription sdpSession) throws SdpException, UnknownHostException {
		FromHeader fromHeader = (FromHeader) message.getHeader(FromHeader.NAME);
		ToHeader toHeader = (ToHeader) message.getHeader(ToHeader.NAME);
		ContactHeader contactHeader = (ContactHeader) message.getHeader(ContactHeader.NAME);
		String address = contactHeader.getAddress().getURI().toString().split("@")[1].split(";")[0];

		List<SipAudioFormat> audioFormats = new LinkedList<SipAudioFormat>();
		List<SipVideoFormat> videoFormats = new LinkedList<SipVideoFormat>();
		int audioRtpPort = 0, audioRtcpPort = 0, videoRtpPort = 0, videoRtcpPort = 0;

		@SuppressWarnings("unchecked")
		Vector<MediaDescription> v = sdpSession.getMediaDescriptions(false);
		if (v != null) {
			for (MediaDescription item : v) {
				if (item.getMedia().getMediaType().contains("audio")) {
					audioRtpPort = item.getMedia().getMediaPort();

					@SuppressWarnings("unchecked")
					Vector<Attribute> formats = item.getAttributes(false);
					if (formats != null) {
						for (Attribute attribute : formats) {
							if (attribute.getName() != null && attribute.getName().contains("rtpmap")) {
								try {
									audioFormats.add(SipAudioFormat.parseAudioFormat(attribute.getValue()));
								} catch (Exception e) {
									// not a valid number
								}
							} else if (attribute.getName().contains("rtcp")) {
								try {
									audioRtcpPort = Integer.parseInt(attribute.getValue());
								} catch (Exception e) {
									// not a valid number
								}
							}
						}
					}
				} else if (item.getMedia().getMediaType().contains("video")) {
					videoRtpPort = item.getMedia().getMediaPort();

					@SuppressWarnings("unchecked")
					Vector<Attribute> formats = item.getAttributes(false);
					if (formats != null) {
						for (Attribute attribute : formats) {
							if (attribute.getName() != null && attribute.getName().contains("rtpmap")) {
								try {
									videoFormats.add(SipVideoFormat.parseVideoFormat(attribute.getValue()));
								} catch (Exception e) {
									// not a valid number
								}
							} else if (attribute.getName().contains("rtcp")) {
								try {
									videoRtcpPort = Integer.parseInt(attribute.getValue());
								} catch (Exception e) {
									// not a valid number
								}
							}
						}
					}
				}
			}
		}

		SipSession session;
		if (address.contains(":")) {
			int port = Integer.parseInt(address.split(":")[1]);
			InetAddress inetAddress = InetAddress.getByName(sdpSession.getConnection().getAddress());

			session = new SipSession(fromHeader.getAddress().getURI(), toHeader.getAddress().getURI(), inetAddress, port, audioRtpPort, audioRtcpPort, videoRtpPort, videoRtcpPort);
		} else {
			InetAddress inetAddress = InetAddress.getByName(sdpSession.getConnection().getAddress());

			session = new SipSession(fromHeader.getAddress().getURI(), toHeader.getAddress().getURI(), inetAddress, 5060, audioRtpPort, audioRtcpPort, videoRtpPort, videoRtcpPort);
		}
		session.setAudioFormats(audioFormats);
		session.setVideoFormats(videoFormats);

		System.out.println(TAG +": SipSession created: " + session.toString());
		return session;
	}

	/**
	 * Send a sip ringing reply.
	 * 
	 * @param invite
	 *            the Request to reply to
	 * @throws SipException
	 * @throws InvalidArgumentException
	 * @throws ParseException
	 */
	public void sendRinging(Request invite) throws SipException, InvalidArgumentException, ParseException {
		// Create a new response for the request
		Response response = messageFactory.createResponse(Response.RINGING, invite);

		// Send the created response
		if (serverTransaction == null)
			serverTransaction = sipProvider.getNewServerTransaction(invite);
		serverTransaction.sendResponse(response);
		System.out.println(TAG +": RINGING sent");
	}

	/**
	 * Send a sip ack reply.
	 * 
	 * @throws InvalidArgumentException
	 * @throws SipException
	 */
	public void sendAck() throws InvalidArgumentException, SipException {
		Request request = currentDialog.createAck(callSequence++);
		currentDialog.sendAck(request);
		System.out.println(TAG +": ACK sent");
	}

	/**
	 * Send a sip ok reply.
	 * 
	 * @param request
	 *            the Request to reply to
	 * @param transaction
	 *            the ServerTransaction used for creating the reply
	 * @throws SipException
	 * @throws InvalidArgumentException
	 * @throws ParseException
	 * @throws SdpException
	 */
	public void sendOK(Request request, ServerTransaction transaction) throws SipException, InvalidArgumentException, ParseException, SdpException {
		// Create a new response for the request
		Response response = messageFactory.createResponse(Response.OK, request);

		if (transaction == null) {
			// Create the contact name address
			SipURI contactURI = addressFactory.createSipURI(sipProfile.getUsername(), localIPAddress);
			contactURI.setPort(localSipPort);
			Address contactAddress = addressFactory.createAddress(contactURI);
			contactAddress.setDisplayName(sipProfile.getDisplayName());

			// Create a new Contact header
			ContactHeader contactHeader = headerFactory.createContactHeader(contactAddress);
			contactHeader.setExpires(3600); // 20 minutes
			response.addHeader(contactHeader);

			ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("application", "sdp");
			SessionDescription sdp = createSDP(request);

			response.setContent(sdp.toString(), contentTypeHeader);

			// Send the created response
			if (serverTransaction == null)
				serverTransaction = sipProvider.getNewServerTransaction(request);
			serverTransaction.sendResponse(response);
		} else {
			transaction.sendResponse(response);
		}
		System.out.println(TAG +": OK sent");
	}

	/**
	 * Set the current sip dialog.
	 * 
	 * @param dialog
	 *            the SipManager's current dialog
	 */
	public void setDialog(Dialog dialog) {
		this.currentDialog = dialog;
	}

	/**
	 * Send a sip bye request.
	 * 
	 * @throws ParseException
	 * @throws InvalidArgumentException
	 * @throws SipException
	 */
	public void sendBye() throws ParseException, InvalidArgumentException, SipException {
		// create Request from dialog
		Request request = currentDialog.createRequest(Request.BYE);

		// Create the client transaction and send the request
		ClientTransaction clientTransaction = sipProvider.getNewClientTransaction(request);
		currentDialog.sendRequest(clientTransaction);
		System.out.println(TAG +": BYE sent");
	}

	/**
	 * Send a sip cancel request.
	 * 
	 * @throws ParseException
	 * @throws InvalidArgumentException
	 * @throws SipException
	 */
	public void sendCancel() throws ParseException, InvalidArgumentException, SipException {
		// create Request from dialog
		Request request = clientTransaction.createCancel();

		// Create the client transaction and send the request
		clientTransaction = sipProvider.getNewClientTransaction(request);
		clientTransaction.sendRequest();
		System.out.println(TAG +": CANCEL sent");
	}

	/**
	 * Send a sip invite request.
	 * 
	 * @param contact
	 *            the SipContact to call
	 * @param state
	 *            the SipRequestState of the SipManager
	 * @throws ParseException
	 * @throws InvalidArgumentException
	 * @throws NullPointerException
	 * @throws SipException
	 * @throws SdpException
	 */
	public void sendInvite(SipContact contact, SipRequestState state) throws ParseException, InvalidArgumentException, NullPointerException, SipException, SdpException {
		// create To and From headers
		FromHeader fromHeader = sipProfile.getFromHeader(addressFactory, headerFactory);
		ToHeader toHeader = contact.getToHeader(addressFactory, headerFactory);

		// create a new Request URI
		SipURI requestURI = addressFactory.createSipURI(contact.getSipUserName(), contact.getSipDomain());

		if(contact.isLocalContact())
			requestURI.setPort(contact.getSipPort());
		
		// Create Via headers
		List<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
		ViaHeader viaHeader = headerFactory.createViaHeader(localIPAddress, localSipPort, listeningPoint.getTransport(), null);
		if(!contact.isLocalContact())
			viaHeader.setRPort();
		viaHeaders.add(viaHeader);

		// Create a new CallId header
		if (state.equals(SipRequestState.REGISTER))
			currentCallID = sipProvider.getNewCallId();

		// Create a new Cseq header
		CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(callSequence, Request.INVITE);

		// Create a new MaxForwards header
		MaxForwardsHeader maxForwards = headerFactory.createMaxForwardsHeader(70);

		// Create the request
		Request request = messageFactory.createRequest(requestURI, Request.INVITE, currentCallID, cSeqHeader, fromHeader, toHeader, viaHeaders, maxForwards);

		// Create a new Expires header
		ExpiresHeader expires = headerFactory.createExpiresHeader(120);
		request.addFirst(expires);

		// Create the contact name address
		SipURI contactURI = addressFactory.createSipURI(sipProfile.getUsername(), localIPAddress);
		contactURI.setPort(localSipPort);
		Address contactAddress = addressFactory.createAddress(contactURI);
		contactAddress.setDisplayName(sipProfile.getDisplayName());

		// Create a new Contact header
		ContactHeader contactHeader = headerFactory.createContactHeader(contactAddress);
		request.addHeader(contactHeader);

		if (state.equals(SipRequestState.AUTHORIZATION)) {
			request.addHeader(sipProfile.getProxyAuthorizationHeader(headerFactory));
		}

		ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("application", "sdp");
		SessionDescription sdp = createSDP(null);
		request.setContent(sdp, contentTypeHeader);

		// Create the client transaction and send the request
		clientTransaction = sipProvider.getNewClientTransaction(request);
		clientTransaction.sendRequest();

		currentDialog = clientTransaction.getDialog();
		System.out.println(TAG +": INVITE sent");
	}

	/**
	 * Send a sip decline response.
	 * 
	 * @param invite
	 *            the Request to reply to
	 * @throws ParseException
	 * @throws SipException
	 * @throws InvalidArgumentException
	 */
	public void sendDecline(Request invite) throws ParseException, SipException, InvalidArgumentException {
		// Create a new response for the request
		Response response = messageFactory.createResponse(Response.DECLINE, invite);

		// Send the created response
		if (serverTransaction == null)
			serverTransaction = sipProvider.getNewServerTransaction(invite);
		serverTransaction.sendResponse(response);
		System.out.println(TAG +": DECLINE sent");
	}

	/**
	 * Send a sip busy here response.
	 * 
	 * @param invite
	 *            the Request to reply to
	 * @throws ParseException
	 * @throws SipException
	 * @throws InvalidArgumentException
	 */
	public void sendBusyHere(Request invite) throws ParseException, SipException, InvalidArgumentException {
		// Create a new response for the request
		Response response = messageFactory.createResponse(Response.BUSY_HERE, invite);

		// Send the created response
		if (serverTransaction == null)
			serverTransaction = sipProvider.getNewServerTransaction(invite);
		serverTransaction.sendResponse(response);
		System.out.println(TAG +": BUSY HERE sent");
	}

	/**
	 * Send a sip not found response.
	 * 
	 * @param invite
	 *            the Request to reply to
	 * @throws ParseException
	 * @throws SipException
	 * @throws InvalidArgumentException
	 */
	public void sendNotFound(Request invite) throws ParseException, SipException, InvalidArgumentException {
		// Create a new response for the request
		Response response = messageFactory.createResponse(Response.NOT_FOUND, invite);

		// Send the created response
		if (serverTransaction == null)
			serverTransaction = sipProvider.getNewServerTransaction(invite);
		serverTransaction.sendResponse(response);
		System.out.println(TAG +": NOT FOUND sent");
	}

	/**
	 * Create a session description for the local profile.
	 * 
	 * @param request
	 *            the Request to extract the SessionDescription from
	 * @return the SessionDescription for the local profile
	 * @throws SdpException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private SessionDescription createSDP(Request request) throws SdpException {
		SessionDescription sdp = SdpFactory.getInstance().createSessionDescription();
		long sessionId = 0L, sessionVersion = 0L;
		String sessionName;
		if (request == null) {
			sessionId = (long) Math.random() * 100000000L;
			sessionVersion = sessionId;
			sessionName = "call";
		} else {
			SessionDescription sdpSession = SdpFactory.getInstance().createSessionDescription(new String(request.getRawContent()));
			sessionId = sdpSession.getOrigin().getSessionId();
			sessionVersion = sdpSession.getOrigin().getSessionVersion();
			sessionName = sdpSession.getSessionName().getValue();
		}

		OriginField originField = new OriginField();
		originField.setUsername(sipProfile.getDisplayName());
		originField.setSessionId(sessionId);
		originField.setSessVersion(sessionVersion);
		originField.setNetworkType(SDPKeywords.IN);
		originField.setAddressType(SDPKeywords.IPV4);
		originField.setAddress(localIPAddress);

		SessionNameField sessionNameField = new SessionNameField();
		sessionNameField.setSessionName(sessionName);

		ConnectionField connectionField = new ConnectionField();
		connectionField.setNetworkType(SDPKeywords.IN);
		connectionField.setAddressType(SDPKeywords.IPV4);
		connectionField.setAddress(localIPAddress);

		Vector mediaDescriptions = new Vector();

		// Add audio formats
		if(sipProfile.getAudioFormats().size() > 0){
			MediaField audioField = new MediaField();
			audioField.setMediaType("audio");
			audioField.setPort(sipProfile.getLocalAudioRtpPort());
			audioField.setProtocol(SdpConstants.RTP_AVP);

			Vector<String> audioFormats = new Vector<String>();
			for (SipAudioFormat audioFormat : sipProfile.getAudioFormats()) {
				audioFormats.add(audioFormat.getFormat() + "");
			}
			audioField.setMediaFormats(audioFormats);

			MediaDescriptionImpl audioDescription = new MediaDescriptionImpl();

			for (SipAudioFormat audioFormat : sipProfile.getAudioFormats()) {
				AttributeField attributeField = new AttributeField();
				attributeField.setName(SdpConstants.RTPMAP);
				attributeField.setValue(audioFormat.getSdpField());
				audioDescription.addAttribute(attributeField);
			}

			AttributeField sendReceive = new AttributeField();
			sendReceive.setValue("sendrecv");
			audioDescription.addAttribute(sendReceive);

			AttributeField rtcpAttribute = new AttributeField();
			rtcpAttribute.setName("rtcp");
			rtcpAttribute.setValue(sipProfile.getLocalAudioRtcpPort() + "");
			audioDescription.addAttribute(rtcpAttribute);

			mediaDescriptions.add(audioField);
			mediaDescriptions.add(audioDescription);
		}

		// Add video formats
		if(sipProfile.getVideoFormats().size() > 0){
			MediaField videoField = new MediaField();
			videoField.setMediaType("video");
			videoField.setPort(sipProfile.getLocalVideoRtpPort());
			videoField.setProtocol(SdpConstants.RTP_AVP);

			Vector<String> videoFormats = new Vector<String>();
			for (SipVideoFormat videoFormat : sipProfile.getVideoFormats()) {
				videoFormats.add(videoFormat.getFormat() + "");
			}
			videoField.setMediaFormats(videoFormats);

			MediaDescriptionImpl videoDescription = new MediaDescriptionImpl();

			for (SipVideoFormat videoFormat : sipProfile.getVideoFormats()) {
				AttributeField attributeField = new AttributeField();
				attributeField.setName(SdpConstants.RTPMAP);
				attributeField.setValue(videoFormat.getSdpField());
				videoDescription.addAttribute(attributeField);
			}

			AttributeField sendReceive = new AttributeField();
			sendReceive.setValue("sendrecv");
			videoDescription.addAttribute(sendReceive);

			AttributeField rtcpAttribute = new AttributeField();
			rtcpAttribute.setName("rtcp");
			rtcpAttribute.setValue(sipProfile.getLocalVideoRtcpPort() + "");
			videoDescription.addAttribute(rtcpAttribute);

			mediaDescriptions.add(videoField);
			mediaDescriptions.add(videoDescription);
		}

		sdp.setOrigin(originField);
		sdp.setSessionName(sessionNameField);
		sdp.setConnection(connectionField);
		sdp.setMediaDescriptions(mediaDescriptions);

		return sdp;
	}

	/**
	 * Reset all current data.
	 */
	public void reset() {
		serverTransaction = null;
		clientTransaction = null;
		currentDialog = null;
		currentCallID = null;
		callSequence = 1L;
	}

}
