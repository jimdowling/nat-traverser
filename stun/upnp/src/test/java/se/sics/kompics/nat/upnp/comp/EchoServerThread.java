/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.kompics.nat.upnp.comp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author jdowling
 */
public class EchoServerThread extends Thread {
    private Socket socket = null;

    public EchoServerThread(Socket socket) {
	super("ControlServerThread");
	this.socket = socket;
    }

    @Override
    public void run() {

	try {
	    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
	    BufferedReader in = new BufferedReader(
				    new InputStreamReader(
				    socket.getInputStream()));

	    String inputLine, outputLine;

	    while ((inputLine = in.readLine()) != null) {
		outputLine = inputLine;
                if (outputLine != null) {
                    out.print(outputLine + "\r\n");
                    out.flush();
                }
	    }
	    out.close();
	    in.close();
	    socket.close();

	} catch (IOException e) {
	    e.printStackTrace();
	}
    }
}