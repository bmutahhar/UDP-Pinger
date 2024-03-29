package udp_pinger;
import java.io.*;
import java.net.*;
import java.util.*;

/*
 * Client to generate a ping requests over UDP.
 * Code has started in the PingServer.java
 */

public class PingClient_Part_A
{
    private static final int MAX_TIMEOUT = 1000;	// milliseconds

    public static void main(String[] args) throws Exception
    {
        // Get command line arguments.
        if (args.length != 2) {
            System.out.println("Required arguments: Server-Address port");
            return;
        }
        // Port number to access
        int port = Integer.parseInt(args[1]);
        // Server to Ping (has to have the PingServer running)
        InetAddress server;
        server = InetAddress.getByName(args[0]);

        // Create a datagram socket for sending and receiving UDP packets
        // through the port specified on the command line.
        DatagramSocket socket = new DatagramSocket();

        int sequence_number = 0;
        // Processing loop.
        while (sequence_number < 10) {
            // Timestamp in ms when we send it
            Date now = new Date();
            long msSend = now.getTime();
            // Create string to send, and transfer i to a Byte Array
            String str = "PING " + sequence_number + " " + msSend + " \n";
            byte[] buffer = new byte[1024];
            buffer = str.getBytes();
            // Create a datagram packet to send as an UDP packet.
            DatagramPacket ping = new DatagramPacket(buffer, buffer.length, server, port);

            // Send the Ping datagram to the specified server
            socket.send(ping);
            // Try to receive the packet - but it can fail (timeout)
            try {
                // Set up the timeout 1000 ms = 1 sec
                socket.setSoTimeout(MAX_TIMEOUT);
                // Set up an UPD packet for recieving
                DatagramPacket response = new DatagramPacket(new byte[1024], 1024);
                // Try to receive the response from the ping
                socket.receive(response);
                // If the response is received, the code will continue here, otherwise it will continue in the catch

                // timestamp for when we received the packet
                now = new Date();
                long msReceived = now.getTime();
                // Print the packet and the delay
                printData(response, msReceived - msSend);
            } catch (IOException e) {
                // Print which packet has timed out
                System.out.println("Timeout for packet " + sequence_number);
            }
            // next packet
            sequence_number ++;
        }
    }

    /*
     * Print ping data to the standard output stream.
     * slightly changed from PingServer
     */
    private static void printData(DatagramPacket request, long RTT) throws Exception
    {
        // Obtain references to the packet's array of bytes.
        byte[] buffer = request.getData();

        // Wrap the bytes in a byte array input stream,
        // so that you can read the data as a stream of bytes.
        ByteArrayInputStream bais = new ByteArrayInputStream(buffer);

        // Wrap the byte array output stream in an input stream reader,
        // so you can read the data as a stream of characters.
        InputStreamReader isr = new InputStreamReader(bais);

        // Wrap the input stream reader in a bufferred reader,
        // so you can read the character data a line at a time.
        // (A line is a sequence of chars terminated by any combination of \r and \n.)
        BufferedReader br = new BufferedReader(isr);

        // The message data is contained in a single line, so read this line.
        String line = br.readLine();

        // Print host address and data received from it.
        System.out.println(
                "Received from " +
                        request.getAddress().getHostAddress() +
                        ": " + (new String(line)).substring(0,6) +" Bytes: "+buffer.length +" RTT: " + RTT +" ms");
    }
}