package net.gwerder.java.messagevortex.test.imap;

import net.gwerder.java.messagevortex.ExtendedSecureRandom;
import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.transport.*;
import net.gwerder.java.messagevortex.transport.imap.*;
import org.bouncycastle.util.encoders.Base64;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509KeyManager;
import javax.security.auth.callback.CallbackHandler;
import javax.security.sasl.Sasl;
import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslException;
import javax.security.sasl.SaslServer;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.Security;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

import static net.gwerder.java.messagevortex.transport.SecurityRequirement.PLAIN;
import static net.gwerder.java.messagevortex.transport.SecurityRequirement.SSLTLS;
import static net.gwerder.java.messagevortex.transport.SecurityRequirement.UNTRUSTED_SSLTLS;
import static org.junit.Assert.*;

/**
 * Tests for {@link net.gwerder.java.messagevortex.transport.imap.ImapCommand}.
 *
 * @author martin@gwerder.net (Martin GWERDER)
 */
@RunWith(JUnit4.class)
public class ImapCommandTest {

    private final static boolean  DO_NOT_TEST_ENCRYPTION=false;
    private ExtendedSecureRandom esr = new ExtendedSecureRandom();

    private static final java.util.logging.Logger LOGGER;

    static {
        ImapConnection.setDefaultTimeout(2000);
        ImapClient.setDefaultTimeout(2000);
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MessageVortexLogger.setGlobalLogLevel(Level.ALL);

        // make sure that cache precalc is not interfering
        //AsymmetricKey.setCacheFileName(null);
    }

    private String[] sendCommand(ImapClient c,String command,String reply) {
        try{
            LOGGER.log(Level.INFO,"IMAP C-> "+ ImapLine.commandEncoder(command));
            String[] s=c.sendCommand(command);
            for(String v:s) {
                LOGGER.log(Level.INFO,"IMAP<- C: "+ImapLine.commandEncoder(v));
            }
            assertTrue("command \""+command+"\" has not been answered properly (expected \""+reply+"\" but got \""+s[s.length-1]+"\")",s[s.length-1].startsWith(reply));
            return s;
        } catch(TimeoutException e) {
            LOGGER.log(Level.WARNING,"Unexpected exception",e);
            fail("got timeout while waiting for reply to command "+command);
        }
        return null;
    }

    @Test
    public void checkSetClientTimeout() {
        try{
            LOGGER.log(Level.INFO,"************************************************************************");
            LOGGER.log(Level.INFO,"Check set client timeout");
            LOGGER.log(Level.INFO,"************************************************************************");
            Set<Thread> threadSet = ImapSSLTest.getThreadList();
            ImapServer is=new ImapServer(new InetSocketAddress( "0.0.0.0", 0 ), new SecurityContext(SecurityRequirement.UNTRUSTED_SSLTLS));
            ImapClient ic=new ImapClient( new InetSocketAddress( "localhost", is.getPort() ), new SecurityContext(SecurityRequirement.PLAIN) );
            assertTrue("test default Timeout", ImapClient.getDefaultTimeout() == ImapClient.setDefaultTimeout(123) );
            assertTrue("test default Timeout", ImapClient.getDefaultTimeout() == 123 );
            ImapClient.setDefaultTimeout( 3600*1000 );
            assertTrue("test  Timeout set", ic.getTimeout() == ic.setTimeout(123) );
            assertTrue("test  Timeout get", ic.getTimeout() == 123 );
            ic.setTimeout( 3600*1000 );
            is.shutdown();
            ic.shutdown();
            assertTrue("error searching for hangig threads",ImapSSLTest.verifyHangingThreads(threadSet).size()==0);
        } catch(Exception e) {
            LOGGER.log(Level.WARNING,"Unexpected exception",e);
            fail("Exception thrown ("+e+")");
        }
    }

    @Test
    public void checkServerTimeout() throws IOException {
        ImapServer is=null;
        ImapClient ic=null;
        Set<Thread> threadSet = ImapSSLTest.getThreadList();
        try{
            LOGGER.log(Level.INFO,"************************************************************************");
            LOGGER.log(Level.INFO,"Check server default timeout");
            LOGGER.log(Level.INFO,"************************************************************************");
            ImapConnection.setDefaultTimeout(300);
            is=new ImapServer(new InetSocketAddress( "0.0.0.0", 0 ), new SecurityContext( PLAIN ) );
            ic=new ImapClient( new InetSocketAddress( "localhost", is.getPort() ), new SecurityContext(SecurityRequirement.PLAIN) );
            ic.connect();
            ic.sendCommand( "a0 IWantATimeout", 300 );
            ic.setTimeout( 3600*1000 );
            Thread.sleep( 1000 );
            fail( "Timeout not issued" );
        } catch( TimeoutException toe ) {
            // Exception reached as planed
            assertTrue( "Got expected timeout", true );
        } catch( Exception e ) {
            LOGGER.log( Level.WARNING, "Unexpected exception", e );
            fail( "Exception thrown (" + e + ")" );
        }
        ImapConnection.setDefaultTimeout( 10000 );
        if( is != null ) {
            is.shutdown();
        }
        if( ic != null ) {
            ic.shutdown();
        }
        assertTrue( "error searching for hangig threads", ImapSSLTest.verifyHangingThreads( threadSet ).size() == 0 );
    }

    @Test
    public void checkClientTimeout() throws IOException {
        Set<Thread> threadSet = ImapSSLTest.getThreadList();
        ImapServer is=null;
        ImapClient ic=null;
        try{
            LOGGER.log(Level.INFO,"************************************************************************");
            LOGGER.log(Level.INFO,"Check client timeout");
            LOGGER.log(Level.INFO,"************************************************************************");
            is=new ImapServer(new InetSocketAddress( "0.0.0.0", 0 ), new SecurityContext( PLAIN ) );
            ic=new ImapClient( new InetSocketAddress( "localhost", is.getPort() ), new SecurityContext(SecurityRequirement.PLAIN) );
            ic.connect();
            ic.sendCommand( "a0 IWantATimeout", 300 );
            ic.setTimeout( 300 );
            Thread.sleep(300);
            ic.sendCommand("a0 IWantATimeout",300);
            Thread.sleep(1000);
            fail("Timeout not issued");
        } catch(TimeoutException toe) {
            // Exception reached as planed
            assertTrue("Got expected timeout",true);
        } catch(Exception e) {
            LOGGER.log(Level.WARNING,"Unexpected exception",e);
            fail("Exception thrown ("+e+")");
        }
        if(is!=null) is.shutdown();
        if(ic!=null) ic.shutdown();
        assertTrue("error searching for hangig threads",ImapSSLTest.verifyHangingThreads(threadSet).size()==0);
    }

    @Test
    public void checkClientDefaultTimeout() throws IOException {
        Set<Thread> threadSet = ImapSSLTest.getThreadList();
        ImapClient ic=null;
        ImapServer is=null;
        try{
            LOGGER.log(Level.INFO,"************************************************************************");
            LOGGER.log(Level.INFO,"Check client default timeout");
            LOGGER.log(Level.INFO,"************************************************************************");
            ImapClient.setDefaultTimeout(300);
            is=new ImapServer(new InetSocketAddress( "0.0.0.0", 0 ), new SecurityContext( PLAIN ) );
            ic=new ImapClient( new InetSocketAddress( "localhost", is.getPort() ), new SecurityContext(SecurityRequirement.PLAIN) );
            ic.connect();
            Thread.sleep(300);
            ic.sendCommand("a0 IWantATimeout",300);
            Thread.sleep(1000);
            fail("Timeout not issued");
        } catch(TimeoutException toe) {
            // Exception reached as planed
            assertTrue("Got expected timeout",true);
        } catch(Exception e) {
            LOGGER.log(Level.WARNING,"Unexpected exception",e);
            fail("Exception thrown ("+e+")");
        }
        ImapClient.setDefaultTimeout(10000);
        if(is!=null) is.shutdown();
        if(ic!=null) ic.shutdown();
        assertTrue("error searching for hangig threads",ImapSSLTest.verifyHangingThreads(threadSet).size()==0);
    }

    @Test
    public void checkFullLogout() {
        Set<Thread> threadSet = ImapSSLTest.getThreadList();
        try{
            ImapServer s=new ImapServer(new InetSocketAddress( "0.0.0.0", 0 ), new SecurityContext( PLAIN ) );
            LOGGER.log(Level.INFO,"************************************************************************");
            LOGGER.log(Level.INFO,"Check full Login Logout");
            LOGGER.log(Level.INFO,"************************************************************************");
            ImapClient c=new ImapClient( new InetSocketAddress( "localhost", s.getPort() ), new SecurityContext(SecurityRequirement.PLAIN) );
            c.setTimeout(2000);
            c.connect();
            ImapConnection.setDefaultTimeout(2000);
            String tag=ImapLine.getNextTag();
            assertTrue("command logut failed BYE-check",sendCommand(c,tag+" LOGOUT",tag+" OK")[0].startsWith("* BYE"));
            s.shutdown();
            c.shutdown();
        } catch (Exception toe) {
            LOGGER.log( Level.WARNING, "unexpected exception", toe );
            fail("exception thrown ("+toe.toString()+") while testing");
        }
        assertTrue("error searching for hangig threads",ImapSSLTest.verifyHangingThreads(threadSet).size()==0);
    }

    @Test
    public void checkFullLoginLogout() {
        Set<Thread> threadSet = ImapSSLTest.getThreadList();
        boolean encrypted=false;
        do{
            try{
                final SSLContext context=SSLContext.getInstance("TLS");
                String ks="keystore.jks";
                assertTrue("Keystore check",(new File(ks)).exists());
                context.init(new X509KeyManager[] {new CustomKeyManager(ks,"changeme", "mykey3") }, new TrustManager[] {new AllTrustManager()}, esr.getSecureRandom() );
                ImapServer s=new ImapServer( new InetSocketAddress( "0.0.0.0", 0 ), new SecurityContext( context, encrypted ? SSLTLS : PLAIN ) );
                LOGGER.log(Level.INFO,"************************************************************************");
                LOGGER.log(Level.INFO,"Check full Login Logout ("+encrypted+")");
                LOGGER.log(Level.INFO,"************************************************************************");
                ImapConnection.setDefaultTimeout(200000);
                AuthenticationProxy ap=new AuthenticationProxy();
                ap.addUser("USER","password");

                assertFalse("check for fail if user is null",ap.login(null,"a"));
                assertFalse("check for fail if password is null",ap.login("USER",null));
                assertFalse("check for fail if user is unknown",ap.login("USER1","password"));
                assertFalse("check for fail if password is bad",ap.login("USER","password1"));
                assertTrue("check for success if password is true",ap.login("USER","password"));
                assertTrue("check for success if username casing does not match",ap.login("User","password"));

                s.setAuth(ap);
                ImapClient c=new ImapClient(new InetSocketAddress("localhost",s.getPort()),new SecurityContext( context,encrypted?SSLTLS:PLAIN ));
                c.setTimeout(2000);
                c.connect();
                assertTrue("check encryption ("+encrypted+"/"+c.isTLS()+")", encrypted==c.isTLS());
                String tag=ImapLine.getNextTag();
                String[] ret=sendCommand(c,tag+" NOOP",tag+" OK");
                LOGGER.log( Level.INFO, "reply to NOOP " + Arrays.toString( ret ) );
                tag=ImapLine.getNextTag();
                ret=sendCommand(c,tag+" CAPABILITY",tag+" OK");
                LOGGER.log( Level.INFO, "reply to CAPABILITY " + Arrays.toString( ret ) );
                List<String> l = new Vector<>( Arrays.asList( ret ) );
                // check that PLAIN IS not offered when doing unencrypted auth
                assertTrue( "Capabilities not as expected (left=" + Arrays.toString( ret ) + ")", ( l.get(0).contains( "LOGINDISABLED" ) && ! c.isTLS() ) || ( ! l.get(0).contains( "LOGINDISABLED" ) && c.isTLS() ) );
                tag=ImapLine.getNextTag();
                if(encrypted) {
                    sendCommand(c,tag+" LOGIN user password",tag+" OK");
                } else {
                    sendCommand(c,tag+" LOGIN user password",tag+" BAD");
                }
                tag=ImapLine.getNextTag();
                sendCommand(c,tag+" CAPABILITY",tag+" OK");
                tag=ImapLine.getNextTag();
                sendCommand(c,tag+" NOOP",tag+" OK");
                tag=ImapLine.getNextTag();
                sendCommand(c,tag+" LOGOUT",tag+" OK");
                s.shutdown();
                c.shutdown();
            } catch (Exception toe) {
                LOGGER.log(Level.WARNING,"Unexpected exception",toe);
                fail("exception thrown ("+toe.toString()+") while testing using encryption="+encrypted+" at "+toe.getStackTrace()[0]);
            }
            encrypted=!encrypted;
        } while(encrypted && !DO_NOT_TEST_ENCRYPTION);
        assertTrue("error searching for hangig threads",ImapSSLTest.verifyHangingThreads(threadSet).size()==0);
    }


    @Test
    public void SaslImplementationTests() throws SaslException {
        Security.addProvider( new SaslPlainServer.SecurityProvider() );


        AuthenticationProxy ap=new AuthenticationProxy();
        ap.addCredentials( new Credentials("USER","password", "theRealm" ) );


        for(SaslMechanisms mech : SaslMechanisms.values() ) {
            CallbackHandler clientHandler = new SaslClientCallbackHandler( new Credentials("user","password", "theRealm" ) );
            CallbackHandler serverHandler = new SaslServerCallbackHandler( ap );
            LOGGER.log(Level.INFO, "Testing SASL mechanism " + mech );
            Map<String, String> props = new HashMap<>();

            if( ! SaslMechanisms.PLAIN.equals( mech ) ) {
                props.put(Sasl.POLICY_NOPLAINTEXT, "true");
            }
            // required for server only
            props.put("com.sun.security.sasl.digest.realm", "theRealm");

            LOGGER.log(Level.INFO, "Getting client and server for SASL " + mech );
            SaslClient sc = Sasl.createSaslClient(new String[]{mech.toString()}, "username", "IMAP", "FQHN", props, clientHandler);
            SaslServer ss = Sasl.createSaslServer(mech.toString(), "IMAP", "FQHN", props, serverHandler);
            assertTrue( "No Sasl server found for "+mech,ss!=null );
            assertTrue( "No Sasl client found for "+mech,sc!=null );

            // get Challenge
            LOGGER.log(Level.INFO, "Getting challenge for SASL " + mech );
            byte[] challenge = ss.evaluateResponse(new byte[0]);
            LOGGER.log(Level.INFO, "Challenge is " + new String(Base64.encode(challenge)) + " (" + new String(challenge) + ")");

            // get client reponse
            LOGGER.log(Level.INFO, "Getting client response for SASL " + mech );
            byte[] response = sc.evaluateChallenge(challenge);
            LOGGER.log(Level.INFO, "Response is " + new String(Base64.encode(response)) + " (" + new String(response) + ")");

            // check authentication
            try {
                LOGGER.log(Level.INFO, "evaluating response for SASL " + mech );
                ss.evaluateResponse(response);
                assertTrue( "logon unexpectedly failed when using "+mech, ss.isComplete() );
                if (ss.isComplete()) {
                    LOGGER.log(Level.INFO, "Authentication successful.");
                } else {
                    LOGGER.log(Level.INFO, "Authentication failed.");
                }            } catch (SaslException se) {
                se.printStackTrace();
            }
            try {
                serverHandler = new SaslServerCallbackHandler(ap);
                CallbackHandler badClientHandler = new SaslClientCallbackHandler(new Credentials("user", "password1", "theRealm"));
                sc = Sasl.createSaslClient(new String[]{mech.toString()}, "username", "IMAP", "FQHN", props, badClientHandler);
                ss = Sasl.createSaslServer(mech.toString(), "IMAP", "FQHN", props, serverHandler);
                challenge = ss.evaluateResponse(new byte[0]);
                response = sc.evaluateChallenge(challenge);
                ss.evaluateResponse(response);
                assertTrue("logon unexpectedly succeeded with bad password using mech " + mech, !ss.isComplete());
            } catch(IOException e) {
                LOGGER.log(Level.INFO, "Authentication failed due to exception (this is expected)", e );
            }
        }
    }

    @Test
    public void loginSasl() {
        try{
            final SSLContext context=SSLContext.getInstance("TLS");
            String ks="keystore.jks";
            assertTrue("Keystore check",(new File(ks)).exists());
            context.init(new X509KeyManager[] {new CustomKeyManager(ks,"changeme", "mykey3") }, new TrustManager[] {new AllTrustManager()}, esr.getSecureRandom() );
            ImapServer s=new ImapServer(new InetSocketAddress("localhost",0),new SecurityContext(context,UNTRUSTED_SSLTLS));
            AuthenticationProxy ap=new AuthenticationProxy();
            ap.addUser("USER","password");
            s.setAuth(ap);

            for( SaslMechanisms mech : SaslMechanisms.values() ) {
                LOGGER.log(Level.INFO, "************************************************************************");
                LOGGER.log(Level.INFO, "Check " + mech + " login");
                LOGGER.log(Level.INFO, "************************************************************************");
                ImapConnection.setDefaultTimeout(2000);
                ImapClient c = new ImapClient(new InetSocketAddress("localhost", s.getPort()), new SecurityContext(context, UNTRUSTED_SSLTLS ));
                c.setTimeout(2000);
                c.connect();
                assertTrue("check encryption", c.isTLS());
                String tag = ImapLine.getNextTag();
                tag = ImapLine.getNextTag();
                String[] ret;
                sendCommand(c, tag + " CAPABILITY", tag + " OK");
                assertTrue("authentication unexpectedly failed when using "+mech,c.authenticate(new Credentials("user", "password", "theRealm" ), mech ));
                tag = ImapLine.getNextTag();
                sendCommand(c, tag + " LOGOUT", tag + " OK");
                c.shutdown();
                LOGGER.log(Level.INFO, "************************************************************************");
                LOGGER.log(Level.INFO, "Check " + mech + " login (BAD CREDS)");
                LOGGER.log(Level.INFO, "************************************************************************");
                c = new ImapClient(new InetSocketAddress("localhost", s.getPort()), new SecurityContext(context, UNTRUSTED_SSLTLS ));
                c.setTimeout(2000);
                c.connect();
                assertTrue("check encryption", c.isTLS());
                tag = ImapLine.getNextTag();
                sendCommand(c, tag + " CAPABILITY", tag + " OK");
                assertTrue("authentication unexpectedly succeeded when using "+mech,!c.authenticate(new Credentials("user", "password1", "theRealm" ), mech ));
                tag = ImapLine.getNextTag();
                sendCommand(c, tag + " LOGOUT", tag + " OK");
                c.shutdown();
            }
            s.shutdown();
        } catch (Exception toe) {
            LOGGER.log(Level.WARNING,"Unexpected exception",toe);
            fail("exception thrown ("+toe.toString()+") while testing (at "+toe.getStackTrace()[0]+")");
        }
    }


}
