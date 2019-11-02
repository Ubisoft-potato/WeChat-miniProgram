package com.cust.controller;

import com.cust.entity.User;
import com.cust.utils.Token;
import com.cust.utils.WxUtils;
import com.cust.service.UserSearchService;
import com.cust.service.UserService;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/sp")
public class UserController {


    private UserService userService;

    private RedisTemplate<Object, Object> redisTemplate;

    private UserSearchService userSearchService;


    /**
     * 获取前端code，此方法用于发送请求到auth.code2Session接口获取用户openid以及sessionkey
     * map应包含昵称，国家，城市等微信基本用户信息
     * <p>
     * errcode:
     * -1	系统繁忙，此时请开发者稍候再试
     * 0	请求成功
     * 40029	code 无效
     * 45011	频率限制，每个用户每分钟100次
     *
     * @param request
     * @return repMap
     * status：
     * -1   登陆失败
     * 0    登陆成功
     */
    @RequestMapping("/WeChatlogin")
    public Map wxUserLogin(HttpServletRequest request) throws IOException {
        request.setCharacterEncoding("UTF-8");
        Map<String, String[]> map = request.getParameterMap();
//        Map map = RequestTool.getParameterMap(request);
        System.out.println(map);
        Map<String, String> repMap = new HashMap<>();
        if (!map.containsKey("code")) {
            repMap.put("status", "-1");
            return repMap;
        }
        String userOpenId = WxUtils.oauth2GetOpenid((String) map.get("code")[0]);
        System.out.println(userOpenId);
        try {
            //String转为json
            JSONObject usrOpenIdAndSessionKey = (JSONObject) (new JSONParser().parse(userOpenId));
            if (!usrOpenIdAndSessionKey.containsKey("errcode")) {
                //获取openid成功
                String openid = (String) usrOpenIdAndSessionKey.get("openid");
                System.out.println(openid);
                //产生第三方会话密钥
                String thirdSessionKey = Token.CreateToken();
                //查询此openid是否存在
                String id = userService.selectUserOpenId(openid);
                if (id != null && !id.equals("")) {
                    //此用户为老用户
                    //返回用户数据库中唯一id
                    repMap.put("id", id);
                    repMap.put("thirdSessionKey", thirdSessionKey);
                } else {
                    //此用户为新用户
                    JSONObject wxuser = (JSONObject) (new JSONParser().parse((String) map.get("rawData")[0]));
                    User userInfo = new User();
                    String newId = Token.createNewUserId();
                    userInfo.setAvatarURL((String) wxuser.get("avatarUrl"));
                    userInfo.setId(newId);
                    userInfo.setCity(String.valueOf(wxuser.get("city")));
                    userInfo.setCountry(String.valueOf(wxuser.get("country")));
                    userInfo.setGender(Integer.parseInt(String.valueOf(wxuser.get("gender"))));
                    userInfo.setNickname(String.valueOf(wxuser.get("nickName")));
                    userInfo.setOpenid(openid);
                    userInfo.setProvince(String.valueOf(wxuser.get("province")));
                    if (userService.insertUserInfo(userInfo)) {
                        //插入新用户成功
                        repMap.put("id", newId);
                        repMap.put("thirdSessionKey", thirdSessionKey);
                    } else {
                        //插入新用户失败
                        repMap.put("status", "-1");
                        return repMap;
                    }
                }
                //更新redis
//                String key = (String) redisTemplate.opsForValue().get(openid);
//                if (key != null) {
//                    redisTemplate.delete(redisTemplate.opsForValue().get(key));
//                    redisTemplate.delete(key);
//                }
                //redisTemplate.opsForValue().set(openid, repMap.get("thirdSessionKey"), 7, TimeUnit.DAYS);
                redisTemplate.opsForValue().set(repMap.get("thirdSessionKey"), repMap.get("id"), 7, TimeUnit.DAYS);
                //登陆成功
                repMap.remove("id");
                repMap.put("status", "0");
                return repMap;
            } else {
                //获取用户openid失败
                repMap.put("status", "-1");
                return repMap;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        repMap.put("status", "-1");
        return repMap;
    }

    /**
     * 获取服务器产生的第三方会话密钥，查询redis进行快速登陆（小程序没有cookie机制）
     *
     * @param request
     * @return
     */
    @RequestMapping("/uuidLogin")
    public Map uuidLogin(HttpServletRequest request) {
//        Map map = RequestTool.getParameterMap(request);
        Map<String, String[]> map = request.getParameterMap();
        Map<String, String> reqMap = new HashMap<>();
        String thirdSessionKey = String.valueOf(map.get("thirdSessionKey")[0]);
        //查询redis进行登陆
        String id = (String) redisTemplate.opsForValue().get(thirdSessionKey);
        if (id != null) {
            reqMap.put("status", "0");
            return reqMap;
        }
        //System.out.println("redis not found");
        reqMap.put("status", "-1");
        return reqMap;
    }

    @RequestMapping("/GetPhotoDetail")
    public Map photoDetail(HttpServletRequest request) {
//        Map map = RequestTool.getParameterMap(request);
        Map<String, String[]> map = request.getParameterMap();
        Map<String, String[]> reqMap = new HashMap<>();
        String photoId = (String) map.get("photoId")[0];
        System.out.println(photoId);
        //获取到对应相册的所有图片
        Object pUrl = redisTemplate.opsForValue().get(photoId);
        String photoUrl;
        if (pUrl != null) {
            photoUrl = (String) pUrl;
            String[] photoUrls = photoUrl.split(";");
            reqMap.put("photoUrls", photoUrls);
        } else {
            photoUrl = userSearchService.getPhotoUrl(photoId);
            redisTemplate.opsForValue().set(photoId, photoUrl, 1, TimeUnit.DAYS);
            String[] photoUrls = photoUrl.split(";");
            reqMap.put("photoUrls", photoUrls);
        }
        return reqMap;
    }

    /**
     * 加载更多别人的作品
     */
    @RequestMapping("loadMorePro")
    public List loadMorePro(HttpServletRequest request) {
        String userid = request.getParameter("userid");
        String selectRow = request.getParameter("selectRow");//开始行数
        List picList = userSearchService.getPicList(userid, selectRow);
        List<Map> respList = new ArrayList<>();
        for (int k = 0; k < picList.size(); k++) {
            Map map = (Map) picList.get(k);
            String[] photoURLs = map.get("photoURL").toString().split(";");
            for (int i = 0; i < photoURLs.length; i++) {
                photoURLs[i] = "http://www.xqdiary.top/loadPic/" + map.get("photoId") + "/" + photoURLs[i];
                //photoURLs[i]="http://localhost:8080/loadPic/"+map.get("photoId")+"/"+photoURLs[i];
            }
            Map temp = new HashMap();
            temp.put("photoId", map.get("photoId"));
            temp.put("instruction", map.get("instruction"));
            temp.put("location", map.get("location"));
            temp.put("photoURL", photoURLs);
            temp.put("likeNum", map.get("likeNum"));
            temp.put("createTime", map.get("createTime"));
            temp.put("categories", map.get("categories"));
            temp.put("avatarURL", map.get("avatarURL"));
            temp.put("nickname", map.get("nickname"));
            respList.add(temp);
        }
        return respList;
    }

    /**
     * 查看别人的作品
     */
    @RequestMapping("/lookOtherPro")
    public List lookOtherPro(HttpServletRequest request) {
        String userid = request.getParameter("userid");
        String selectRow = request.getParameter("selectRow");//开始行数
        String prokey = "pro" + userid;
        if (redisTemplate.hasKey(prokey)) {
            List proList = redisTemplate.opsForList().range(prokey, 0, 0);
            List<Map> reList = (List<Map>) proList.get(0);
            return reList;
        } else {
            List picList = userSearchService.getPicList(userid, selectRow);
            List<Map> respList = new ArrayList<>();
            for (int k = 0; k < picList.size(); k++) {
                Map map = (Map) picList.get(k);
                String[] photoURLs = map.get("photoURL").toString().split(";");
                for (int i = 0; i < photoURLs.length; i++) {
                    photoURLs[i] = "http://www.xqdiary.top/loadPic/" + map.get("photoId") + "/" + photoURLs[i];
                    //photoURLs[i]="http://localhost:8080/loadPic/"+map.get("photoId")+"/"+photoURLs[i];
                }
                Map temp = new HashMap();
                temp.put("photoId", map.get("photoId"));
                temp.put("instruction", map.get("instruction"));
                temp.put("location", map.get("location"));
                temp.put("photoURL", photoURLs);
                temp.put("likeNum", map.get("likeNum"));
                temp.put("createTime", map.get("createTime"));
                temp.put("categories", map.get("categories"));
                temp.put("avatarURL", map.get("avatarURL"));
                temp.put("nickname", map.get("nickname"));
                respList.add(temp);
            }
            redisTemplate.opsForList().leftPush(prokey, respList);
            redisTemplate.expire(prokey, 12, TimeUnit.HOURS);
            return respList;
        }

    }


    /**
     * 个人已发布管理
     * return  photoId,instruction,location,photoURL,likeNum,createTime,categories
     */
    @RequestMapping("/manageSend")
    public List manageSend(HttpServletRequest request) {
        String thirdSessionKey = request.getParameter("thirdSessionKey");//个人值
        String selectRow = request.getParameter("selectRow");//开始行数
        String userID = (String) redisTemplate.opsForValue().get(thirdSessionKey);
        if (userID == null) {
            List list = new ArrayList();
            return null;
        }

        List picList = userSearchService.getPicList(userID, selectRow);
        List<Map> respList = new ArrayList<>();
        for (int k = 0; k < picList.size(); k++) {
            Map map = (Map) picList.get(k);
            String[] photoURLs = map.get("photoURL").toString().split(";");
            for (int i = 0; i < photoURLs.length; i++) {
                photoURLs[i] = "http://www.xqdiary.top/loadPic/" + map.get("photoId") + "/" + photoURLs[i];
                //photoURLs[i]="http://localhost:8080/loadPic/"+map.get("photoId")+"/"+photoURLs[i];
            }
            Map temp = new HashMap();
            temp.put("photoId", map.get("photoId"));
            temp.put("instruction", map.get("instruction"));
            temp.put("location", map.get("location"));
            temp.put("photoURL", photoURLs);
            temp.put("likeNum", map.get("likeNum"));
            temp.put("createTime", map.get("createTime"));
            temp.put("categories", map.get("categories"));
            temp.put("avatarURL", map.get("avatarURL"));
            temp.put("nickname", map.get("nickname"));
            respList.add(temp);
        }
        return respList;
    }

    /**
     * 删除已发布的内容
     * 1 成功 -1失败
     */
    @RequestMapping("/delSend")
    public int delSend(HttpServletRequest request) {
        String userid;
        String picID = request.getParameter("photoId");
        String thirdSessionKey = request.getParameter("thirdSessionKey");//个人值
        if (redisTemplate.hasKey(thirdSessionKey)) {
            userid = (String) redisTemplate.opsForValue().get(thirdSessionKey);
        } else {
            return -1;
        }
        boolean isDel = userSearchService.delPic(picID);

        if (isDel) {
            //File file=new File("/home/wxpicture/"+picID);
            //String tempPath="C:\\wxpicture\\"+picID;
            String tempPath = "/home/wxpicture/" + picID;
            File file = new File(tempPath);
            if (file.exists()) {
                System.out.println("删除相册");
                Token.deleteDir(file);
            }
            //刷新redis里个人作品页面
            if (redisTemplate.hasKey("pro" + userid)) {
                List picList = userSearchService.getPicList(userid, "0");
                List<Map> respList = new ArrayList<>();
                for (int k = 0; k < picList.size(); k++) {
                    Map map = (Map) picList.get(k);
                    String[] photoURLs = map.get("photoURL").toString().split(";");
                    for (int i = 0; i < photoURLs.length; i++) {
                        photoURLs[i] = "http://www.xqdiary.top/loadPic/" + map.get("photoId") + "/" + photoURLs[i];
                        //photoURLs[i]="http://localhost:8080/loadPic/"+map.get("photoId")+"/"+photoURLs[i];
                    }
                    Map temp = new HashMap();
                    temp.put("photoId", map.get("photoId"));
                    temp.put("instruction", map.get("instruction"));
                    temp.put("location", map.get("location"));
                    temp.put("photoURL", photoURLs);
                    temp.put("likeNum", map.get("likeNum"));
                    temp.put("createTime", map.get("createTime"));
                    temp.put("categories", map.get("categories"));
                    temp.put("avatarURL", map.get("avatarURL"));
                    temp.put("nickname", map.get("nickname"));
                    respList.add(temp);
                }
                redisTemplate.opsForList().leftPush("pro" + userid, respList);
                redisTemplate.expire("pro" + userid, 12, TimeUnit.HOURS);
            }

            return 1;
        } else {
            return -1;
        }
    }

    /**
     * 修改个人昵称
     * 1 成功 -1失败
     */
    @RequestMapping("/changeNickName")
    public int changeNickName(HttpServletRequest request) {
        Map userChange = new HashMap();
        String thirdSessionKey = request.getParameter("thirdSessionKey");//个人值
        String newnickname = request.getParameter("newnickname");
        String userid = (String) redisTemplate.opsForValue().get(thirdSessionKey);
        userChange.put("nickname", newnickname);
        userChange.put("userid", userid);
        if (userid != null) {
            boolean change = userService.changeNickName(userChange);
            if (change) {
                return 1;
            } else return -1;
        } else
//            System.out.println("redis null");
            return -1;
    }

    /**
     * 退出登陆
     * 1 成功 -1失败
     */
    @RequestMapping("/outLogin")
    public int outLogin(HttpServletRequest request) {
        String thirdSessionKey = request.getParameter("thirdSessionKey");//个人值
        redisTemplate.delete(thirdSessionKey);
        if (!redisTemplate.hasKey(thirdSessionKey)) {
            return 1;//退出成功
        } else {
            return -1;
        }

    }

    @Autowired
    public UserController(UserService userService, RedisTemplate<Object, Object> redisTemplate, UserSearchService userSearchService) {
        this.userService = userService;
        this.redisTemplate = redisTemplate;
        this.userSearchService = userSearchService;
    }
}
