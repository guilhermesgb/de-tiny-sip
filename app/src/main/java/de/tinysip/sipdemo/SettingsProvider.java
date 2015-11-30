/*
 * This file is part of TinySip. 
 * http://code.google.com/p/de-tiny-sip/
 * 
 * Created 2011 by Sebastian Rosch <flowfire@sebastianroesch.de>
 * 
 * This software is licensed under the Apache License 2.0.
 */

package de.tinysip.sipdemo;

/**
 * The SettingsProvider holds all your SIP details. Change these to suit your SIP provider's details.
 * 
 * @author Sebastian
 * 
 */
public class SettingsProvider {
	/**
	 * The SIP user name.
	 */
	public static String sipUserName = "6001";
	/**
	 * The SIP domain.
	 */
	public static String sipDomain = "192.168.132.80";
	/**
	 * The SIP password.
	 */
	public static String sipPassword = "6001";
	/**
	 * The SIP server port.
	 */
	public static int sipPort = 5060;
	/**
	 * The local users display name.
	 */
	public static String displayName = "SAMIR";

	/**
	 * The SIP contact to call.
	 */
	public static String callContact = "6002";
	/**
	 * The SIP contact's domain.
	 */
	public static String callDomain = "192.168.132.80";
}
