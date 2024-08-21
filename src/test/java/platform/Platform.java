package platform;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;

public class Platform {

    public static void main(String[] parameters) throws IOException {

        // Retrieve the host name
        final String hostName = InetAddress.getLocalHost().getHostName();
        System.out.println("Hostname: " + hostName);

        InetAddress localhost = InetAddress.getLocalHost();

        // Retrieve the IP address
        final String address = localhost.getHostAddress();
        System.out.println("Address: " + address + (localhost.isLoopbackAddress() ? " (loopback address)" : ""));

        // Retrieve the MAC address (parse hexadecimal hardware address)
        NetworkInterface ni = NetworkInterface.getByInetAddress(localhost);

        if (ni == null) {
            System.out.println("No network interface with as address: " + localhost);
        } else {
            byte[] hardwareAddress = ni.getHardwareAddress();

            StringBuilder macBuilder = new StringBuilder();
            for (int i = 0; i < hardwareAddress.length; i++) {
                macBuilder.append(String.format("%02X%s", hardwareAddress[i], (i < hardwareAddress.length - 1) ? "-" : ""));
            }
            System.out.println("MAC Address: " + macBuilder);
        }
    }

}
