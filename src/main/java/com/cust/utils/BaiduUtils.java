package com.cust.utils;

import com.alibaba.fastjson.JSONArray;
import com.baidu.aip.imagecensor.AipImageCensor;
import org.json.JSONObject;

import static com.baidu.aip.imagecensor.EImgType.FILE;

public class BaiduUtils {
    //设置APPID/AK/SK
    //first APP
//    private static final String APP_ID = "16007168";
//    private static final String API_KEY = "bLrW0YLOM6S0t0iC5uStDId0";
//    private static final String SECRET_KEY = "MYY8rp7Cyr26cxVBcZlWbWCEcNRP7Bqn";

    //seccond App
    private static final String APP_ID = "16037687";
    private static final String API_KEY = "glfXYW2hzTomduQ0MtdYO4mP";
    private static final String SECRET_KEY = "43gZpv2aesykAwkyf968SKrsMSuLqcs5";

    // 初始化一个AipImageCensor
    private static AipImageCensor client = new AipImageCensor(APP_ID, API_KEY, SECRET_KEY);


    public static boolean checkPornograp(String path) {
        boolean allow = false, sq, zz, bk, ex, gs, qt;
        int judge, checkNum = 0;
        JSONObject res = client.imageCensorUserDefined(path, FILE, null);
        if ("合规".equals(res.get("conclusion"))) {
            allow = true;
            System.out.println("合格");
            return allow;
        }
        System.out.println(res.toString(2));
        JSONArray jsonDwa = (JSONArray) JSONArray.parseObject(String.valueOf(res)).get("data");//提出data 转换为数组
        //System.out.println(jsonDwa);
        if (jsonDwa != null) {
            //百度结果为 不合规
            //二次审核
            //降低不合规严格性 只检查 色情信息和政治敏感
            System.out.println("二次审核");
            for (int i = 0; i < jsonDwa.size(); i++) {
                sq = zz = bk = ex = gs = true;

                com.alibaba.fastjson.JSONObject jsonDeal = (com.alibaba.fastjson.JSONObject) jsonDwa.get(i);
                if (jsonDeal.get("msg").toString().contains("色情")) {
                    System.out.println("色情可能性：" + jsonDeal.get("probability"));
                    if (Double.parseDouble(jsonDeal.get("probability").toString()) > (double) 0.66) {
                        sq = false;
                    }
                } else if (jsonDeal.get("msg").toString().contains("政治")) {
                    System.out.println("政治可能性：" + jsonDeal.get("probability"));
                    if (Double.parseDouble(jsonDeal.get("probability").toString()) > (double) 0.88) {
                        zz = false;
                    }
                } else if (jsonDeal.get("msg").toString().contains("暴恐")) {
                    System.out.println("暴恐可能性：" + Long.parseLong((String) jsonDeal.get("probability")));
                    if (Double.parseDouble(jsonDeal.get("probability").toString()) > (double) 0.88) {
                        bk = false;
                    }
                } else if (jsonDeal.get("msg").toString().contains("恶心")) {
                    System.out.println("恶心可能性：" + jsonDeal.get("probability"));
                    if (Double.parseDouble(jsonDeal.get("probability").toString()) > (double) 0.88) {
                        ex = false;
                    }
                } else if (jsonDeal.get("msg").toString().contains("灌水")) {
                    System.out.println("灌水可能性：" + jsonDeal.get("probability"));
                    if (Double.parseDouble(jsonDeal.get("probability").toString()) > (double) 0.55) {
                        gs = false;
                    }
                }

                if (sq && zz && bk && ex && gs) {
                    checkNum++;
                }
            }

            if (checkNum == jsonDwa.size()) {
                allow = true;
            }

        }


        //System.out.println(allow);
        if (allow) System.out.println("合格");
        return allow;
    }


}
