package uz.pdp.water_delivery.utils;

public class PhoneRepairUtil {

    public static String repair(String phone) {
        phone = phone.replaceAll("[\\s\\-()]", "");
        if (!phone.startsWith("+")) {
            phone = "+" + phone;
        }
        return phone;
    }
}
