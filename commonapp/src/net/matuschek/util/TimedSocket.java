package net.matuschek.util;

import java.net.*;
import java.io.*;

/**
 * This class offers a timeout feature on socket connections.
 * A maximum length of time allowed for a connection can be 
 * specified, along with a host and port.
 *
 * @author David Reilly (written for JavaWorld)
 * @author Daniel Matuschek
 * @version $Id$
 *
 * imported to the net.matuschek.util source tree by Daniel Matuschek 
 */
public class TimedSocket
{

  /**
   * Attempts to connect to a service at the specified address
   * and port, for a specified maximum amount of time.
   *
   * @param addr  Address of host
   * @param port  Port of service
   * @param delay Delay in milliseconds
   */
  public static Socket getSocket ( InetAddress addr, int port, int delay) 
    throws InterruptedIOException, IOException
  {
    // Create a new socket thread, and start it running
    SocketThread st = new SocketThread( addr, port );
    st.start();
    
    int timer = 0;
    Socket sock = null;
    
    for (;;) {
      // Check to see if a connection is established
      
      if (st.isConnected()) {
	// Yes ...  assign to sock variable, and break out of loop
	sock = st.getSocket();
	break;
      } else {
	// Check to see if an error occurred
	if (st.isError()) {
	  // No connection could be established
	  throw (st.getException());
	}
	
	try {
	  // Sleep for a short period of time
	  Thread.sleep ( POLL_DELAY );
	} catch (InterruptedException ie) {}
	
	// Increment timer
	timer += POLL_DELAY;
	
	// Check to see if time limit exceeded
	if (timer > delay) {
	  // Can't connect to server
	  throw new InterruptedIOException("Could not connect for " + 
					   delay + " milliseconds");
	}
      }
    }
    
    return sock;
  }
  
  /**
   * Attempts to connect to a service at the specified address
   * and port, for a specified maximum amount of time.
   *
   * @param host  Hostname of machine
   * @param port  Port of service
   * @param delay Delay in milliseconds
   */
  public static Socket getSocket ( String host, int port, int delay) 
    throws InterruptedIOException, IOException
  {
    // Convert host into an InetAddress, and call getSocket method
    InetAddress inetAddr = InetAddress.getByName (host);
    
    return getSocket ( inetAddr, port, delay );
  }
  
  
  /**
   * Inner class for establishing a socket thread
   * within another thread, to prevent blocking.
   */
  static class SocketThread extends Thread
  {
    // Socket connection to remote host
    volatile private Socket m_connection = null;
    // Hostname to connect to
    private String m_host       = null;
    // Internet Address to connect to
    private InetAddress m_inet  = null;
    // Port number to connect to
    private int    m_port       = 0;
    // Exception in the event a connection error occurs
    private IOException m_exception = null;
    
    // Connect to the specified host and port number
    public SocketThread ( String host, int port) {
      // Assign to member variables
      m_host = host;
      m_port = port;
    }
    
    // Connect to the specified host IP and port number
    public SocketThread ( InetAddress inetAddr, int port ) {
      // Assign to member variables
      m_inet = inetAddr;
      m_port = port;
    }
    
    public void run() {
      // Socket used for establishing a connection
      Socket sock = null;
      
      try {
	// Was a string or an inet specified
	if (m_host != null) {
	  // Connect to a remote host - BLOCKING I/O
	  sock = new Socket (m_host, m_port);
	} else {
	  // Connect to a remote host - BLOCKING I/O
	  sock = new Socket (m_inet, m_port);
	}
      }
      catch (IOException ioe) {
	// Assign to our exception member variable
	m_exception = ioe;
	return;
      }

      // If socket constructor returned without error,
      // then connection finished
      m_connection = sock;
    }

    // Are we connected?
    public boolean isConnected() {
      if (m_connection == null)
	return false;
      else
	return true;
    }

    // Did an error occur?
    public boolean isError() {
      if (m_exception == null)
	return false;
      else
	return true;
    }

    // Get socket
    public Socket getSocket() {
      return m_connection;
    }
    
    // Get exception
    public IOException getException() {
      return m_exception;
    }
  }
  
  /** Polling delay for socket checks (in milliseconds) */
  private static final int POLL_DELAY = 100;
}
