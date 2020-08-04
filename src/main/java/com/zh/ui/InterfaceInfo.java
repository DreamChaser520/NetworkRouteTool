package com.zh.ui;

import com.zh.bean.Interface;
import com.zh.utils.CMDUtil;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InterfaceInfo {
    static String ipconfig = CMDUtil.ExeCMD("ipconfig");

    public static List<Interface> getInterfaces(List<Interface> interfaces) {
        if (!interfaces.isEmpty()) {
            interfaces.clear();
            ipconfig = CMDUtil.ExeCMD("ipconfig");
        }
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                //网卡名称
                String name = networkInterface.getDisplayName();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    //网卡索引
                    int index = networkInterface.getIndex();
                    //网卡ip地址
                    String ip = inetAddress.getHostAddress();
                    if (!inetAddress.isLoopbackAddress() && inetAddress.getHostAddress().indexOf(':') == -1) {
                        String gateway = getParam(ip, "Gateway", "网关");
                        String netmask = getParam(ip, "netmask", "掩码");
                        Interface inf = new Interface(index, name, ip, gateway, netmask);
                        interfaces.add(inf);
                    }
                }
            }
            return interfaces;
        } catch (Exception e) {
            return null;
        }
    }

    private static String getParam(String ip, String EnglishName, String ChineseName) {
        String[] infs = ipconfig.split("\n\n");

        for (String inf : infs) {
            if (inf.indexOf(ip) > 0) {
                String pattern = "((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})(\\.((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})){3}";
                String matcherStr = "";
                if (inf.indexOf(EnglishName) > 0) {
                    matcherStr = inf.substring(inf.indexOf(EnglishName));

                } else if (inf.indexOf(ChineseName) > 0) {
                    matcherStr = inf.substring(inf.indexOf(ChineseName));
                }
                Pattern r = Pattern.compile(pattern);
                Matcher matcher = r.matcher(matcherStr);
                if (matcher.find()) {
                    if (!matcher.group().equals("0.0.0.0"))
                        return matcher.group();
                }
            }
        }
        return "0.0.0.0";
    }
}
