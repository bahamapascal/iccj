package org.iotacontrolcenter.ui.util;

import org.iotacontrolcenter.dto.IotaNeighborDto;
import org.iotacontrolcenter.dto.NeighborDto;
import org.iotacontrolcenter.ui.app.Main;

import javax.swing.*;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

public class UiUtil {

    private static Pattern ipV4Pattern;
    private static final String IPV4ADDRESS_PATTERN =
            "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

    public static ImageIcon loadIcon(String imgFileName) {
        try {
            URL imgUrl = Main.mainFrame.getClass().getResource("/" + imgFileName);
            if (imgUrl != null) {
                return new ImageIcon(imgUrl);
            } else {
                System.out.println("for image " + imgFileName + ", generated url is null");
            }
        } catch (Exception e) {
            System.out.println("Exception loading icon from file " + imgFileName +
                    ": " + e.getLocalizedMessage());
        }
        return null;
    }

    /*
    public static Image loadImage(String imgFileName) {
        try {
            URL imgUrl = Main.mainFrame.getClass().getResource("/" + imgFileName);
            if (imgUrl != null){
                return new Image(imgUrl);
            }
            else {
                System.out.println("for image " + imgFileName +", generated url is null");
            }
        }
        catch(Exception e) {
            System.out.println("Exception loading icon from file " + imgFileName +
                    ": " + e.getLocalizedMessage());
        }
        return null;
    }
    */


    public static boolean isPotentialIpv6(String ip) {
        return ip != null && (ip.startsWith("[") && ip.endsWith("]")) ||
                ip.matches("^.*:.*:.*:.*:.*$");
    }

    public static boolean isSameIpv6(String ip1, String ip2) {
        //[2a01:4f8:190:32cc::2]
        // and
        // 2a01:4f8:190:32cc:0:0:0:2
        //System.out.println("isSameIpv6(" + ip1 + ", " + ip2 + ")");

        if (ip1 == null || ip2 == null || ip1.isEmpty() | ip2.isEmpty()) {
            return false;
        }
        ip1 = ip1.toLowerCase();
        ip2 = ip2.toLowerCase();

        ip1 = ip1.replace("[", "");
        ip1 = ip1.replace("]", "");
        ip2 = ip2.replace("[", "");
        ip2 = ip2.replace("]", "");

        String normalIp1 = null;
        String normalIp2 = null;
        try {
            normalIp1 = InetAddress.getByName(ip1).toString().substring(1);
        } catch (UnknownHostException e) {
        }
        try {
            normalIp2 = InetAddress.getByName(ip2).toString().substring(1);
        } catch (UnknownHostException e) {
        }
        //System.out.println("normalIp1: " + normalIp1);
        //System.out.println("normalIp2: " + normalIp2);
        return normalIp1 != null && normalIp2 != null && normalIp1.equals(normalIp2);
    }

    public static boolean isSameNbr(NeighborDto nbr, IotaNeighborDto iotaNbr) {
        // In our model, the nbr uri is something like this:
        //         udp://fred.com:14265
        //  or
        //         udp://10.0.0.0:14265
        //  or
        //         udp://[2a01:4f8:190:32cc::2]:14265

        // The problem: nbr coming from IOTA getNeighbors, the address is something like this:
        //         fred.com/93.188.173.198:14265
        //  or
        //         /10.0.0.0:14265
        //  or iv ipv6:
        //         /2a01:4f8:190:32cc:0:0:0:2:14265
        //  or soon, no embedded "/":
        //         93.188.173.198:14265
        String nbrUri = nbr.getUri();
        int addrSepIdx = nbrUri.indexOf("://");
        int portIdx = nbrUri.lastIndexOf(":");
        if (addrSepIdx < 0) {
            addrSepIdx = 0;
        } else {
            addrSepIdx += 3;
        }
        String nbrHost = nbrUri.substring(addrSepIdx, portIdx);

        boolean nbrIsIpv6 = isPotentialIpv6(nbrHost);

        //System.out.println("nbrHost: isIpv6: " + isIpv6 + ": " + nbrHost);

        String nbrPort = nbrUri.substring(portIdx);

        String iotaUri = iotaNbr.getAddress();
        portIdx = iotaUri.lastIndexOf(":");
        String iotaPort = iotaUri.substring(portIdx);
        addrSepIdx = iotaUri.indexOf("/");

        String iotaAddr1 = addrSepIdx < 0 ? "" : iotaUri.substring(0, addrSepIdx);
        boolean iotaAddr1IsIpv6 = isPotentialIpv6(iotaAddr1);

        int addr2StartIdx = addrSepIdx >= 0 ? addrSepIdx + 1 : 0;
        String iotaAddr2 = iotaUri.substring(addr2StartIdx, portIdx);
        boolean iotaAddr2IsIpv6 = isPotentialIpv6(iotaAddr2);

        boolean same = false;
        if (nbrPort != null) {
            same = nbrPort.equals(iotaPort);
        } else if (iotaPort != null) {
            same = iotaPort.equals(nbrPort);
        } else {
            same = true;
        }

        //System.out.println("nbrPort: " + nbrPort + ", iotaPort: " + iotaPort);
        //System.out.println("nbrHost: " + nbrHost + ", iotaAddr1: " + iotaAddr1 +
        //        ", iotaAddr2: " + iotaAddr2);

        if (!same) {
            System.out.println("not same nbr,  port differs");
            return same;
        }

        // Compare iota addr2 first, addr1 is usually empty
        if (nbrIsIpv6 && (iotaAddr1IsIpv6 || iotaAddr2IsIpv6)) {
            same = isSameIpv6(nbrHost, iotaAddr2) || isSameIpv6(nbrHost, iotaAddr1);
        } else {
            //System.out.println("nbrHost: " + nbrHost);
            //System.out.println("iotaAddr1: " + iotaAddr1);
            //System.out.println("iotaAddr2: " + iotaAddr2);
            same = nbrHost.equalsIgnoreCase(iotaAddr2) || nbrHost.equalsIgnoreCase(iotaAddr1);
        }
        return same;

    }

    public static boolean promptUserYorN(String title, String msg) {
        int reply = JOptionPane.showConfirmDialog(Main.mainFrame, msg, title, JOptionPane.YES_NO_OPTION);
        return reply == JOptionPane.YES_OPTION;
    }

    public static final void showInfoDialog(String title, String msg) {
        JOptionPane.showMessageDialog(Main.mainFrame, msg, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public static final void showErrorDialog(String title, String msg) {
        JOptionPane.showMessageDialog(Main.mainFrame, msg, title, JOptionPane.ERROR_MESSAGE);
    }

    public static boolean disValidIpV4(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        if (ip.equalsIgnoreCase("localhost")) {
            return true;
        }
        if (ipV4Pattern == null) {
            ipV4Pattern = Pattern.compile(IPV4ADDRESS_PATTERN);
        }
        return ipV4Pattern.matcher(ip).matches();
    }

    public static boolean isValidPositiveNumber(String num) {
        try {
            Integer i = Integer.parseInt(num);
            return i > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isValidNumber(String num) {
        try {
            Integer.parseInt(num);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isLocalhostIp(String ip) {
        return ip.equalsIgnoreCase("localhost") ||
                ip.equals("127.0.0.1");
    }

    public static String genServerId(String name) {
        return name.trim().replaceAll("[^A-Za-z0-9 ]", "");
    }
}
