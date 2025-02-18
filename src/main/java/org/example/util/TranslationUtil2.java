package org.example.util;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class TranslationUtil2 {
    // 账号信息为 xiaoyuan
    private static final String APP_ID = "20250217002275877";
    private static final String SECRET_KEY = "gPhsrdBAlGzqK4G4TT3b";
    private static final String API_URL = "http://api.fanyi.baidu.com/api/trans/vip/translate";

    public static String translate(String text) {
        try {
            String salt = String.valueOf(System.currentTimeMillis());
            String signStr = APP_ID + text + salt + SECRET_KEY;
            String sign = getMD5(signStr);

            String url = API_URL + "?q=" + URLEncoder.encode(text, "UTF-8") +
                    "&from=en&to=zh&appid=" + APP_ID +
                    "&salt=" + salt +
                    "&sign=" + sign;

            HttpClient httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(url);
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, "UTF-8");

            JSONObject jsonObject = new JSONObject(result);
            System.out.println(jsonObject);
            JSONArray transResult = jsonObject.getJSONArray("trans_result");
            return transResult.getJSONObject(0).getString("dst");
        } catch (Exception e) {
            e.printStackTrace();
            return text;
        }
    }

    private static String getMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xFF & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}