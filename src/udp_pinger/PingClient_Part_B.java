package udp_pinger;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/*
 * Client to generate a ping requests over UDP.
 * Code has started in the PingServer.java
 */

public class PingClient_Part_B {
    private static final int MAX_TIMEOUT = 1000;    // milliseconds
    private static long RTT_Array[]; //Array to stores all RTTs for all Pings

    public static void main(String[] args) throws Exception {
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
        long rtt = 0; //Calculates the round trip time
        RTT_Array = new long[10];
        Timer timer = new Timer(); //Timer object to send each ping after exactly 1 sec
        TimerTask timerTask;
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
            timerTask = new TimerTask() {	//Timer Task object to actually send the ping
                @Override
                public void run() {
                    // Send the Ping datagram to the specified server
                    try {
                        socket.send(ping);
                    } catch (IOException e) {
                        System.out.println(e.toString());;
                    }
                }
            };
            timer.schedule(timerTask,1000);		//Time Scheduler to send ping after 1 second

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
                rtt = msReceived - msSend;
                RTT_Array[sequence_number] = rtt;
                // Print the packet and the delay
                printData(response, rtt);
            } catch (IOException e) {
                // Print which packet has timed out
                System.out.println("Timeout for packet " + sequence_number);
            }
            // next packet
            sequence_number++;
        }
        timer.cancel();
        //Prints the Statistics just like standard ping program
        printStatistics();
    }

    /*
     * Print ping data to the standard output stream.
     * slightly changed from PingServer
     */
    private static void printData(DatagramPacket request, long RTT) throws Exception {
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
                "Reply from " +
                        request.getAddress().getHostAddress() +
                        ": " + (new String(line)).substring(0, 6) + " Bytes: " + buffer.length + " RTT: " + RTT + " ms");
    }

    private static void printStatistics() {
        Arrays.sort(RTT_Array);//Sorts all RTTs in ascending order
        int i = 0;
        int sum = 0;//Counts sum of RTTs for Average
        int count = 0;//Count number of non zero RTTs

        //Gets the index for the first/smallest RTT
        while (RTT_Array[i] == 0) {
            i++;
        }

        //Loop to Calculate Average RTT
        for (int j = i; j < RTT_Array.length; j++) {
            sum += RTT_Array[j];
            count++;
        }

        //Prints Min, Max and Avg RTT
        System.out.println("Minimum = " + RTT_Array[i] + " ms, Maximum =  " + RTT_Array[RTT_Array.length - 1] + " ms, " +
                "Average: " + (sum / count) + " ms");
    }
}