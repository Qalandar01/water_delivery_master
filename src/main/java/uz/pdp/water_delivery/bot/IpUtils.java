package uz.pdp.water_delivery.bot;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

public class IpUtils {

    public static boolean isInRange(InetAddress address, String cidr) {
        String[] parts = cidr.split("/");
        try {
            InetAddress network = InetAddress.getByName(parts[0]);
            int prefixLength = Integer.parseInt(parts[1]);

            byte[] networkBytes = network.getAddress();
            byte[] addressBytes = address.getAddress();

            int fullBytes = prefixLength / 8;
            int remainingBits = prefixLength % 8;

            for (int i = 0; i < fullBytes; i++) {
                if (networkBytes[i] != addressBytes[i]) {
                    return false;
                }
            }

            if (remainingBits > 0) {
                int mask = (0xFF << (8 - remainingBits)) & 0xFF;
                if ((networkBytes[fullBytes] & mask) != (addressBytes[fullBytes] & mask)) {
                    return false;
                }
            }

            return true;
        } catch (UnknownHostException e) {
            return false;
        }
    }
}
