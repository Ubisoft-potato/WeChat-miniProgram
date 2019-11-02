package com.cust.utils;

public class WxUtils {
    /**
     * 获取当前微信用户的OpenId(唯一标识当前用户)和session_key(会话密钥)
     */
    public static String oauth2GetOpenid(String code) {
        String appid = "wxca5f8060bdd6397b";
        String appsecret = "d9562af91d85d814adf049c06c86ad2c";

        //授权（必填）
        String grant_type = "authorization_code";
        //URL
        String requestUrl = "https://api.weixin.qq.com/sns/jscode2session";
        //请求参数
        String params = "appid=" + appid + "&secret=" + appsecret + "&js_code=" + code + "&grant_type=" + grant_type;
        //发送请求
        String data = HttpUtil.get(requestUrl, params);
        return data;
    }

}
