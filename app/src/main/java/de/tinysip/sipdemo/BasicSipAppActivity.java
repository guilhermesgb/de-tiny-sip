package de.tinysip.sipdemo;

import android.app.Activity;
import android.os.Bundle;

import java.net.InetAddress;
import java.text.ParseException;
import java.util.ArrayList;

import android.javax.sdp.SdpConstants;
import android.javax.sdp.SdpException;
import android.javax.sip.InvalidArgumentException;
import android.javax.sip.SipException;

import de.javawi.jstun.test.DiscoveryInfo;
import de.tinysip.sip.LocalSipProfile;
import de.tinysip.sip.SipAudioFormat;
import de.tinysip.sip.SipContact;
import de.tinysip.sip.SipManager;
import de.tinysip.sip.SipManagerCallStatusEvent;
import de.tinysip.sip.SipManagerSessionEvent;
import de.tinysip.sip.SipManagerState;
import de.tinysip.sip.SipManagerStatusChangedEvent;
import de.tinysip.sip.SipManagerStatusListener;
import de.tinysip.sip.SipProfile;
import de.tinysip.sip.SipSession;
import de.tinysip.sip.SipVideoFormat;
import de.tinysip.stun.STUNDiscoveryResultEvent;
import de.tinysip.stun.STUNDiscoveryResultListener;
import de.tinysip.stun.STUNDiscoveryTask;
import de.tinysip.stun.STUNInfo;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;


public class BasicSipAppActivity extends Activity implements STUNDiscoveryResultListener, SipManagerStatusListener {


    private Button register;
    private Button unregister;
    private Button invite;
    private EditText sipUsernameEditText;
    private EditText sipUserAddressEditText;
    private EditText sipContactUsernameEditText;
    private EditText sipContactAddressEditText;
    private TextView sipEventsTextView;
    private TextView sipChatTextView;


    private static String TAG = "tSIP";
    //private LocalSipProfile localSipProfile;
    private SipProfile sipProfile;
    private SipContact sipContact;
    private STUNDiscoveryTask sipPortTask;
    private DiscoveryInfo sipDiscoveryInfo;
    private SipManager sipManager;

    private SipManagerState state;

    private Handler handler;


    private void initUI() {

        register = (Button) findViewById(R.id.bt_register);
        unregister = (Button) findViewById(R.id.bt_unregister);
        invite = (Button) findViewById(R.id.bt_invite);

        sipUsernameEditText = (EditText) findViewById(R.id.et_username);
        sipUserAddressEditText = (EditText) findViewById(R.id.et_user_address);
        sipContactUsernameEditText = (EditText) findViewById(R.id.et_contact_username);
        sipContactAddressEditText = (EditText) findViewById(R.id.et_contact_address);

        sipEventsTextView = (TextView) findViewById(R.id.tv_events);
        sipChatTextView = (TextView) findViewById(R.id.tv_chat);

        initUiButtonListeners();
    }

    private void initUiButtonListeners() {
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                handler = new Handler();

                final Runnable r = new Runnable() {
                    public void run() {
                        registerOnJFLSipServer();
                        scrollSipEventsTextView();
                        scrollSipChatTextView();
                    }
                };

                handler.post(r);

            }
        });

        invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //sendSipInvite();
                scrollSipEventsTextView();
                scrollSipChatTextView();
            }
        });

//      unregister;
//      invite;
    }

    private void scrollSipEventsTextView() {
        final ScrollView sipEventsScrollView = ((ScrollView) findViewById(R.id.sv_sip_events));
        sipEventsScrollView.post(new Runnable() {
            @Override
            public void run() {
                sipEventsScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    private void scrollSipChatTextView() {
        final ScrollView sipEventsScrollView = ((ScrollView) findViewById(R.id.sv_sip_events));
        sipEventsScrollView.post(new Runnable() {
            @Override
            public void run() {
                sipEventsScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    private String getSipUsername() {
        Log.d("BasicSipAppActivity", " sipUsernameEditText == NULL? " + (sipUsernameEditText == null ? "YES" : sipUsernameEditText.getText()));
        if (null != sipUsernameEditText && sipUsernameEditText.getText() != null && sipUsernameEditText.getText().length() > 0) {
            Log.d("getSipUsername", "" + sipUsernameEditText.getText().toString());
            return sipUsernameEditText.getText().toString();
        } else {
            Log.d("BasicSipAppActivity", "Interface com bug");
            return "6001";
        }
    }

    private String getSipUserAddress() {
        if (null != sipUserAddressEditText && sipUserAddressEditText.getText() != null && sipUserAddressEditText.getText().length() > 0) {
            return sipUserAddressEditText.getText().toString();
        } else {
            Log.d("BasicSipAppActivity", "Interface com bug");
            return "192.168.132.80";
        }
    }

    private String getSipContactUsername() {
        if (null != sipContactUsernameEditText && sipContactUsernameEditText.getText() != null && sipContactUsernameEditText.getText().length() > 0) {
            return sipContactUsernameEditText.getText().toString();
        } else {
            Log.d("BasicSipAppActivity", "Interface com bug");
            return "6002";
        }
    }

    private String getSipContactAddress() {
        if (null != sipContactAddressEditText && sipContactAddressEditText.getText() != null && sipContactAddressEditText.getText().length() > 0) {
            return sipContactAddressEditText.getText().toString();
        } else {
            Log.d("BasicSipAppActivity", "Interface com bug");
            return "192.168.132.80";
        }
    }


//    private void registerOnSipServer() {
//        // the local user's credentials (change details in SettingsProvider)
//        //localSipProfile = new LocalSipProfile(SettingsProvider.sipUserName, SettingsProvider.sipDomain, SettingsProvider.sipPassword, SettingsProvider.sipPort,
//        //        SettingsProvider.displayName);
//        // the local user's credentials (change details in SettingsProvider)
//
//        InetAddress address = SipManager.getInetAddress();
//        //localSipProfile = new LocalSipProfile(getSipUsername(), address);
//        //localSipProfile.setSipPassword(getSipUsername());
//        //localSipProfile.setSipDomain(SettingsProvider.sipDomain);
//
//        //Log.d("registerOnSipServer", "localSipProfile.toString() - " + localSipProfile.toString());
//
//        Log.d("registerOnSipServer", "localSipProfile.toString() - " + localSipProfile.toString());
//        // create a list of supported audio formats for the local user agent
//        ArrayList<SipAudioFormat> audioFormats = new ArrayList<SipAudioFormat>();
//        audioFormats.add(new SipAudioFormat(SdpConstants.PCMA, "PCMA", 8000));
//        localSipProfile.setAudioFormats(audioFormats);
//
//        // set ports for rtp and rtcp for audio
//        localSipProfile.setLocalAudioRtpPort(5071);
//        localSipProfile.setLocalAudioRtcpPort(5072);
//
//        // create a list of supported video formats for the local user agent
//        ArrayList<SipVideoFormat> videoFormats = new ArrayList<SipVideoFormat>();
//        videoFormats.add(new SipVideoFormat(SdpConstants.JPEG, "JPEG", 90000));
//        localSipProfile.setVideoFormats(videoFormats);
//
//        // set ports for rtp and rtcp for video
//        localSipProfile.setLocalVideoRtpPort(5073);
//        localSipProfile.setLocalVideoRtcpPort(5074);
//
//        // the sip contact to call (change details in SettingsProvider)
//        sipContact = new SipContact(SettingsProvider.callContact, SettingsProvider.callDomain, true);
//        //sipContact = new SipContact(getSipContactUsername(), getSipContactAddress(), 5060);
//
////        if (!localSipProfile.isLocalProfile()) {
////            // the STUN server and port for NAT traversal
////            STUNInfo sipPortInfo = new STUNInfo(STUNInfo.TYPE_SIP, "stun.sipgate.net", 10000);
////            sipPortInfo.setLocalPort(5070); // local port to use for SIP
////            sipPortTask = new STUNDiscoveryTask();
////            sipPortTask.addResultListener(this);
////            sipPortTask.execute(sipPortInfo);
////            sipEventsTextView.append("\n" + getString(R.string.STUNDiscovery));
////        } else {
////            startSipRegistration();
////        }
//
//        startJFLSipRegistration();
//    }


    /**
     * SipProfile(  String username, String sipPassword, String localAddress,
     *              String sipDomain,String displayName, Integer sipPort,
     *              Integer localSipPort, boolean isLocalProfile, boolean isSTUN,
     *              String stunSipAddress, Integer stunSipPort);
     */
    private void registerOnJFLSipServer() {



        sipProfile = new SipProfile( getSipUsername(), getSipUsername(), SipManager.getInetAddress().getHostAddress(),
                SettingsProvider.sipDomain, getSipUsername().toUpperCase(), SettingsProvider.sipPort, SettingsProvider.sipPort,
                true, false, "stun.sipgate.net", 10000);

        // create a list of supported audio formats for the local user agent
        ArrayList<SipAudioFormat> audioFormats = new ArrayList<SipAudioFormat>();
        audioFormats.add(new SipAudioFormat(SdpConstants.PCMA, "PCMA", 8000));
        sipProfile.setAudioFormats(audioFormats);

        // set ports for rtp and rtcp for audio
        sipProfile.setLocalAudioRtpPort(5071);
        sipProfile.setLocalAudioRtcpPort(5072);

        // create a list of supported video formats for the local user agent
        ArrayList<SipVideoFormat> videoFormats = new ArrayList<SipVideoFormat>();
        videoFormats.add(new SipVideoFormat(SdpConstants.JPEG, "JPEG", 90000));
        sipProfile.setVideoFormats(videoFormats);

        // set ports for rtp and rtcp for video
        sipProfile.setLocalVideoRtpPort(5073);
        sipProfile.setLocalVideoRtcpPort(5074);

        // the sip contact to call (change details in SettingsProvider)
        sipContact = new SipContact(SettingsProvider.callContact, SettingsProvider.callDomain, true);


        startJFLSipRegistration();
    }

    private void sendSipInvite() {
//        if (sipManager.getCurrentState() == SipManagerState.READY) {
//            if ((localSipProfile.isLocalProfile() && sipContact.isLocalContact()) || !(localSipProfile.isLocalProfile() && sipContact.isLocalContact())) {
//                sipEventsTextView.append("\n" + getString(R.string.SIPInvite) + " " + sipContact.toString());
//                try {
//                    sipManager.sendInvite(sipContact);
//                } catch (Exception e) {
//                    sipEventsTextView.append("\n" + getString(R.string.SIPRegistering));
//                }
//            }
//        }
        try {
            sipManager.sendInvite(sipContact);
        } catch (Exception e) {
            e.printStackTrace();
            sipEventsTextView.append("\n" + getString(R.string.SIPRegistering));
        }
    }


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_sip_app);
        initUI();
    }

    @Override
    public void SipManagerStatusChanged(SipManagerStatusChangedEvent event) {
        final SipManagerStatusChangedEvent fevent = event;
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                state = fevent.getState();

                switch (state) {
                    case IDLE:
                        Log.d("BasicSipAppActivity - SipManagerStatusChanged", "SIP MANAGER STATE - IDLE");
                        break;
                    case RINGING:
                        Log.d("BasicSipAppActivity - SipManagerStatusChanged", "SIP MANAGER STATE - RINGING");
                        break;
                    case ESTABLISHED:
                        Log.d("BasicSipAppActivity - SipManagerStatusChanged", "SIP MANAGER STATE - ESTABLISHED");
                        break;
                    case INCOMING:
                        Log.d("BasicSipAppActivity - SipManagerStatusChanged", "SIP MANAGER STATE - INCOMING");
                        try {
                            Thread.sleep(3000);
                            sipManager.acceptCall();
                            //Thread.sleep(6000);
                            //sipManager.endCall();
                        } catch (SipException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        } catch (InvalidArgumentException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        } catch (ParseException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        } catch (SdpException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        break;
                    case ERROR:
                        Log.d("BasicSipAppActivity - SipManagerStatusChanged", "SIP MANAGER STATE - ERROR");
                        break;
                    case UNREGISTERING:
                        Log.d("BasicSipAppActivity - SipManagerStatusChanged", "SIP MANAGER STATE - UNREGISTERING");
                        sipEventsTextView.append("\n" + getString(R.string.SIPUnregistering));
                        break;
                    case TIMEOUT:
                        Log.d("BasicSipAppActivity - SipManagerStatusChanged", "SIP MANAGER STATE - TIMEOUT");
                        break;
                    case READY:

                        Log.d("BasicSipAppActivity - SipManagerStatusChanged", "SIP MANAGER STATE - READY");
                        sipProfile = sipManager.getSipProfile();
                        sipEventsTextView.append("\n" + getString(R.string.SIPReady));
                        sipEventsTextView.append("\n" + sipProfile.toString());
                        sendSipInvite();
                        break;
                    case REGISTERING:
                        Log.d("BasicSipAppActivity - SipManagerStatusChanged", "SIP MANAGER STATE - REGISTERING");
                        sipEventsTextView.append("\n" + getString(R.string.SIPRegistering));
                        break;
                    default:
                        Log.d("BasicSipAppActivity - SipManagerStatusChanged", "SIP MANAGER STATE - DEFAULT");
                        break;
                }
            }
        });
    }

    @Override
    public void SipManagerCallStatusChanged(SipManagerCallStatusEvent event) {
        final SipManagerCallStatusEvent fevent = event;
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sipEventsTextView.append("\n" + fevent.getMessage());
            }
        });
    }

    @Override
    public void SipManagerSessionChanged(SipManagerSessionEvent event) {
        final SipManagerSessionEvent fevent = event;
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SipSession session = fevent.getSession();

                if (session != null) {
                    sipEventsTextView.append("\n" + getString(R.string.SIPCallConnected) + " " + session.getCallerNumber());
                }
            }
        });
    }

    @Override
    public void STUNDiscoveryResultChanged(STUNDiscoveryResultEvent event) {
        if (event.getDiscoveryInfo() != null) {
            Log.d(TAG, event.getDiscoveryInfo().toString());

            DiscoveryInfo discoveryInfo = event.getDiscoveryInfo();
            STUNInfo stunInfo = event.getStunInfo();

            switch (stunInfo.getType()) {
                case STUNInfo.TYPE_SIP:
                    // STUN test was completed
                    sipDiscoveryInfo = discoveryInfo;
                    if (sipDiscoveryInfo.isBlockedUDP() || sipDiscoveryInfo.isSymmetric() || sipDiscoveryInfo.isSymmetricUDPFirewall()) {
                        sipEventsTextView.append("\n" + getString(R.string.STUNNotSupported));
                        Log.d(TAG, getString(R.string.STUNNotSupported));
                    } else {
                        // start SIP registration now
                        startSipRegistration();
                    }
                    break;
            }
        } else {
            sipEventsTextView.append("\n" + getString(R.string.STUNError));
        }
    }

    private void startJFLSipRegistration() {
        try {
            Log.d("TinySipDemoActivity", "startJFLSipRegistration - BEGIN");
            sipManager = SipManager.createInstance(sipProfile, sipProfile.getLocalAddress(), sipProfile.getLocalSipPort());
            Log.d("TinySipDemoActivity", "startJFLSipRegistration - END");
        } catch (Exception e) {
            e.printStackTrace();
        }
        sipManager.addStatusListener(this);
        Log.d("BasicSipAppActivity", "startJFLSipRegistration - RegisterProfile - BEGIN");
        try {
            sipManager.registerJFLProfile();
            sipEventsTextView.append("\n" + getString(R.string.SIPReady));
        } catch (Exception e) {
            e.printStackTrace();
            sipEventsTextView.append("\n" + getString(R.string.SIPRegistrationError));
        }
        Log.d("BasicSipAppActivity", "startJFLSipRegistration - RegisterProfile - END");
    }

    private void startSipRegistration() {
        try {
            if (sipProfile.isLocalProfile()) {
                Log.d("TinySipDemoActivity", "startSipRegistration - localSipProfile.isLocalProfile() = true");
                sipManager = SipManager.createInstance(sipProfile, sipProfile.getSipDomain(), sipProfile.getLocalSipPort());
            } else {
                Log.d("TinySipDemoActivity", "startSipRegistration - localSipProfile.isLocalProfile() = false");
                sipManager = SipManager.createInstance(sipProfile, sipDiscoveryInfo.getPublicIP().getHostAddress(), sipDiscoveryInfo.getPublicPort());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        sipManager.addStatusListener(this);

        try {
            sipManager.registerProfile();
            sipEventsTextView.append("\n" + getString(R.string.SIPReady));
        } catch (Exception e) {
            e.printStackTrace();
            sipEventsTextView.append("\n" + getString(R.string.SIPRegistrationError));
        }
    }

}