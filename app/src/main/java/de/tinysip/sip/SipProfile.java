/*
 * This file is part of TinySip. 
 * http://code.google.com/p/de-tiny-sip/
 * 
 * Created 2011 by Sebastian Rosch <flowfire@sebastianroesch.de>
 * 
 * This software is licensed under the Apache License 2.0.
 */

package de.tinysip.sip;

import android.gov.nist.gnjvx.sip.address.SipUri;
import android.javax.sip.PeerUnavailableException;
import android.javax.sip.address.Address;
import android.javax.sip.address.AddressFactory;
import android.javax.sip.address.SipURI;
import android.javax.sip.header.AuthorizationHeader;
import android.javax.sip.header.FromHeader;
import android.javax.sip.header.HeaderFactory;
import android.javax.sip.header.ProxyAuthorizationHeader;
import android.javax.sip.header.ToHeader;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the local user's profile, containing his user name, the sip domain and so on. Also contains information about user authorization with the sip provider.
 * 
 * @author Sebastian
 * 
 */
public class SipProfile {
	private String username;
	private String sipPassword;
	private String displayName;
    private String localAddress;
	private String sipDomain;
    private boolean isLocalProfile;
    private boolean isSTUN;
	private int sipPort;

    private String stunSipAddress;
    private int stunSipPort;
	private int localSipPort;
	private int localAudioRtpPort;
	private int localAudioRtcpPort;
	private int localVideoRtpPort;
	private int localVideoRtcpPort;
	private List<SipAudioFormat> audioFormats;
	private List<SipVideoFormat> videoFormats;

	private String nonce;
	private String realm;

	private String tag;



	/**
	 * Create a LocalSipProfile for the local user, specifying the user name and his IP address for local use only.
	 *
	 * @param username
	 *            the SIP username of the local user
     * @param sipPassword
     *            the SIP password
	 * @param localAddress
	 *            the local IP Address of the user
     * @param sipDomain
     *            the SIP domain of the local user
     * @param displayName
     *            the SIP domain of the local user
     * @param sipPort
     *            the SIP port of remote server
     * @param localSipPort
     *            the SIP port of the local user
     *
	 */
	public SipProfile(String username, String sipPassword, String localAddress, String sipDomain,
                      String displayName, Integer sipPort, Integer localSipPort,
                      boolean isLocalProfile, boolean isSTUN, String stunSipAddress, Integer stunSipPort) {
		this.username = username;
        this.sipPassword = sipPassword;
        this.localAddress = localAddress;
		this.sipDomain = sipDomain;
        this.displayName = displayName;
		this.sipPort = (sipPort == null ? 5060 : sipPort);
        this.isLocalProfile = isLocalProfile;
        this.isSTUN = isSTUN;
		this.localSipPort = (localSipPort == null ? 5060 : localSipPort);
        this.stunSipAddress = stunSipAddress;
        this.stunSipPort = (stunSipPort == null ? 5060 : stunSipPort);
		this.audioFormats = new ArrayList<SipAudioFormat>();
		this.videoFormats = new ArrayList<SipVideoFormat>();

		String rand = (Math.random() * 10000) + "";
		this.tag = AuthorizationDigest.getHexString(rand.getBytes());
	}

    /**
     *
     * @return the username
     */
    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the sip password
     */
    public String getSipPassword() {
        return sipPassword;
    }

	public void setSipPassword(String sipPassword) {
		this.sipPassword = sipPassword;
	}

    /**
     * @return the local address
     */
    public String getLocalAddress() {
        return localAddress;
    }

    public void setLocalAddress(String localAddress) {
        this.localAddress = localAddress;
    }

    /**
     * @return the sip domain
     */
    public String getSipDomain() {
        return sipDomain;
    }

	public void setSipDomain(String sipDomain) {
		this.sipDomain = sipDomain;
	}

    /**
     * @return the profile's display name
     */
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return the local sip port
     */
    public Integer getSipPort() {
        return sipPort;
    }

    public void setSipPort(Integer sipPort) {
        this.sipPort = (sipPort == null ? 5060 : sipPort);
    }

    /**
     * @return whether this profile is a local profile only
     */
    public boolean isLocalProfile() {
        return isLocalProfile;
    }

    public boolean isSTUN() {
        return this.isSTUN;
    }

    public void setIsSTUN(boolean isSTUN) {
        this.isSTUN = isSTUN;
    }

    /**
     * @return the local sip port
     */
    public int getLocalSipPort() {
        return localSipPort;
    }

    /**
     * Set the local sip port
     *
     * @param localSipPort
     */
    public void setLocalSipPort(Integer localSipPort) {
        this.localSipPort = (localSipPort == null ? 5060 : localSipPort);
    }

    public String getStunSipAddress() {
        return this.stunSipAddress;
    }

    public void setStunSipAddress(String stunSipAddress) {
        this.stunSipAddress = stunSipAddress;
    }

    /**
     * @return the stun sip port
     */
    public int getSTUNSipPort() {
        return stunSipPort;
    }

    /**
     * Set the stun sip port
     *
     * @param stunSipPort
     */
    public void setSTUNSipPort(Integer stunSipPort) {
        this.stunSipPort = (stunSipPort == null ? 5060 : stunSipPort);
    }

    /**
     * Set the supported audio formats as a List of SipAudioFormat
     *
     * @param audioFormats
     *            the supported audio formats as a List of SipAudioFormat
     */
    public void setAudioFormats(List<SipAudioFormat> audioFormats) {
        this.audioFormats = audioFormats;
    }

    /**
     * @return the supported audio formats as a List of SipAudioFormat
     */
    public List<SipAudioFormat> getAudioFormats() {
        return audioFormats;
    }

    /**
     * Set the supported video formats as a List of SipVideoFormat
     *
     * @param videoFormats
     *            the supported audio formats as a List of SipVideoFormat
     */
    public void setVideoFormats(List<SipVideoFormat> videoFormats) {
        this.videoFormats = videoFormats;
    }

    /**
     * @return the supported video formats as a List of SipVideoFormat
     */
    public List<SipVideoFormat> getVideoFormats() {
        return videoFormats;
    }

    /**
     * Set the nonce to use for the authentication
     *
     * @param nonce
     *            the nonce to use for authentication
     */
    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    /**
     * Set the realm to use for the authentication
     *
     * @param realm
     *            the realm to use for authentication
     */
    public void setRealm(String realm) {
        this.realm = realm;
    }











    /**
     * Set the local rtp port for audio.
     *
     * @param localAudioRtpPort
     */
    public void setLocalAudioRtpPort(int localAudioRtpPort) {
        this.localAudioRtpPort = localAudioRtpPort;
    }

    /**
     * @return the local rtp port for audio
     */
    public int getLocalAudioRtpPort() {
        return localAudioRtpPort;
    }

    /**
     * Set the local rtcp port for audio.
     *
     * @param localAudioRtcpPort
     */
    public void setLocalAudioRtcpPort(int localAudioRtcpPort) {
        this.localAudioRtcpPort = localAudioRtcpPort;
    }

    /**
     * @return the local rtcp port for audio
     */
    public int getLocalAudioRtcpPort() {
        return localAudioRtcpPort;
    }

    /**
     * Set the local rtp port for video.
     *
     * @param localVideoRtpPort
     */
    public void setLocalVideoRtpPort(int localVideoRtpPort) {
        this.localVideoRtpPort = localVideoRtpPort;
    }

    /**
     * @return the local rtp port for video
     */
    public int getLocalVideoRtpPort() {
        return localVideoRtpPort;
    }

    /**
     * Set the local rtcp port for video.
     *
     * @param localVideoRtcpPort
     */
    public void setLocalVideoRtcpPort(int localVideoRtcpPort) {
        this.localVideoRtcpPort = localVideoRtcpPort;
    }

    /**
     * @return the local rtcp port for video
     */
    public int getLocalVideoRtcpPort() {
        return localVideoRtcpPort;
    }




	/**
	 * Create a javax.sip.header.FromHeader for this LocalSipProfile
	 * 
	 * @param addressFactory
	 *            an instance of an AddressFactory, created by the SipManager
	 * @param headerFactory
	 *            an instance of a HeaderFactory, created by the SipManager
	 * @return the FromHeader for this LocalSipProfile
	 * @throws PeerUnavailableException
	 * @throws ParseException
	 */
	public FromHeader getFromHeader(AddressFactory addressFactory, HeaderFactory headerFactory) throws PeerUnavailableException, ParseException {
		SipURI fromAddress = addressFactory.createSipURI(this.username, this.sipDomain);
		Address fromNameAddress = addressFactory.createAddress(fromAddress);
		fromNameAddress.setDisplayName(this.displayName);
		return headerFactory.createFromHeader(fromNameAddress, this.tag);
	}

	/**
	 * Create a javax.sip.header.ToHeader for this LocalSipProfile
	 * 
	 * @param addressFactory
	 *            an instance of an AddressFactory, created by the SipManager
	 * @param headerFactory
	 *            an instance of a HeaderFactory, created by the SipManager
	 * @return the ToHeader created for this LocalSipProfile
	 * @throws PeerUnavailableException
	 * @throws ParseException
	 */
	public ToHeader getToHeader(AddressFactory addressFactory, HeaderFactory headerFactory) throws PeerUnavailableException, ParseException {
		SipURI fromAddress = addressFactory.createSipURI(this.username, this.sipDomain);
		Address fromNameAddress = addressFactory.createAddress(fromAddress);
		fromNameAddress.setDisplayName(this.displayName);
		return headerFactory.createToHeader(fromNameAddress, null);
	}

	/**
	 * Create a javax.sip.header.AuthorizationHeader for this LocalSipProfile
	 * 
	 * @param headerFactory
	 *            an instance of a HeaderFactory, created by the SipManager
	 * @return the AuthorizationHeader created for this LocalSipProfile
	 * @throws ParseException
	 */
	public AuthorizationHeader getAuthorizationHeader(HeaderFactory headerFactory) throws ParseException {
		SipUri uri = new SipUri();
		uri.setHost(this.sipDomain);

		String responseDigest = AuthorizationDigest.getDigest(this.username, this.realm, this.sipPassword, "REGISTER", uri.toString(), this.nonce);

		AuthorizationHeader auth = headerFactory.createAuthorizationHeader("Digest");
		auth.setAlgorithm("MD5");
		auth.setNonce(this.nonce);
		auth.setRealm(this.realm);
		auth.setUsername(this.username);
		auth.setURI(uri);
		auth.setResponse(responseDigest);

		return auth;
	}

	/**
	 * Create a javax.sip.header.ProxyAuthorizationHeader for this LocalSipProfile
	 * 
	 * @param headerFactory
	 *            an instance of a HeaderFactory, created by the SipManager
	 * @param contact
	 *            the SipContact to call
	 * @return the ProxyAuthorizationHeader for this LocalSipProfile
	 * @throws ParseException
	 */
	public ProxyAuthorizationHeader getProxyAuthorizationHeader(HeaderFactory headerFactory) throws ParseException {
		SipUri uri = new SipUri();
		uri.setHost(this.sipDomain);

		String responseDigest = AuthorizationDigest.getDigest(this.username, this.realm, this.sipPassword, "INVITE", uri.toString(), this.nonce);

		ProxyAuthorizationHeader auth = headerFactory.createProxyAuthorizationHeader("Digest");
		auth.setAlgorithm("MD5");
		auth.setNonce(this.nonce);
		auth.setRealm(this.realm);
		auth.setUsername(this.username);
		auth.setURI(uri);
		auth.setResponse(responseDigest);

		return auth;
	}


	
	/**
	 * @return the SipUri of the local sip profile
	 */
	public SipUri getSipUri(){
		SipUri uri = new SipUri();
		try {
			uri.setHost(this.sipDomain);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		uri.setUser(this.username);
		if(isLocalProfile())
			uri.setPort(this.localSipPort);
		return uri;
	}


	@Override
	public String toString() {
		if (isLocalProfile())
			return username + "@" + sipDomain + ":" + localSipPort;
		else
			return username + "@" + sipDomain;
	}

}
