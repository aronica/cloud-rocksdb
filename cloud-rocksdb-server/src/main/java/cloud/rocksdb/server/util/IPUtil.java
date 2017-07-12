package cloud.rocksdb.server.util;

import com.alibaba.fastjson.JSON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by lishuaiwei on 16/8/29.
 */
public class IPUtil {

    public static String getRegionByIp(String ip) {
        String fullAddress = getFullAddressByIP(ip);
        String region = getRegion(fullAddress);
        return transfer(region);
    }

    public static String getFullAddressByIP(String ip) {
        try {
            URL url = new URL("http://ip.taobao.com/service/getIpInfo.php?ip=" + ip);
            URLConnection conn = url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "GBK"));
            String line = null;
            StringBuffer result = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            reader.close();
            return result.toString();
        } catch (IOException e) {
            return null;
        }
    }

    public static String getRegion(String fullAddress) {
        String region = null;
        if(fullAddress != null && fullAddress.length() > 0) {
            region = JSON.parseObject(fullAddress).getJSONObject("data").getString("region");
        }
        return region;
    }

    public static String getCity(String fullAddress) {
        String region = null;
        if(fullAddress != null && fullAddress.length() > 0) {
            region = JSON.parseObject(fullAddress).getJSONObject("data").getString("city");
        }
        return region;
    }

    private static String transfer(String region) {
        if(region != null && region.length() > 0) {
            region = region.substring(0, region.length()-1);
            //行政
            if(region.contains("香港")) {
                region = "香港";
            }
            if(region.contains("澳门")) {
                region = "澳门";
            }

            //自治区
            if(region.contains("新疆")) {
                region = "新疆";
            }
            if(region.contains("内蒙古")) {
                region = "内蒙古";
            }
            if(region.contains("广西")) {
                region = "广西";
            }
            if(region.contains("宁夏")) {
                region = "宁夏";
            }
            if(region.contains("西藏")) {
                region = "西藏";
            }
        }
        return region;
    }

    public static void main(String[] args) {
        String fullAddress = getFullAddressByIP("125.46.28.141");
        System.out.println(getRegion(fullAddress));
        System.out.println(getCity(fullAddress));
    }
}
