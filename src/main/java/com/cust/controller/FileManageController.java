package com.cust.controller;

import com.cust.utils.BaiduUtils;
import com.cust.service.UserSearchService;
import com.cust.service.UserService;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/sp/fileCtrl")
public class FileManageController {


    private RedisTemplate<Object, Object> redisTemplate;

    private UserService userService;

    private UserSearchService userSearchService;

    @RequestMapping("/upPicture")
    public Map upPicture(HttpServletRequest request) {
        Map<String, String> respMap = new HashMap<>();
        //设置文件保存路径
        //String checkpath = "C:\\wxpicture\\check\\";//审核路径
        String checkpath = "/home/wxpicture/check/";//审核路径
        File dir = new File(checkpath);
        if (!dir.exists()) {
            //路径不存在侧创建
            dir.mkdir();
        }
        MultipartHttpServletRequest multipartHttpServletRequest = (MultipartHttpServletRequest) request;
        MultipartFile file = multipartHttpServletRequest.getFile("pic");
        if (!file.isEmpty()) {
            try {
                String pictureName = UUID.randomUUID().toString() + ".jpg";//保存的文件名
                String destPath = checkpath + pictureName;
                byte[] bytes = file.getBytes();
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(new File(destPath)));
                bufferedOutputStream.write(bytes);
                bufferedOutputStream.close();
                boolean uploadPermission = BaiduUtils.checkPornograp(destPath);//图片检测//色情、政治检测
                if (uploadPermission) {
                    //图片审核通过
                    if (multipartHttpServletRequest.getParameter("photoId").equals("")) {
                        //相册第一次保存
                        String introduce = multipartHttpServletRequest.getParameter("introduce");
//                        System.out.println(introduce);
                        String userId = (String) redisTemplate.opsForValue().get(multipartHttpServletRequest.getParameter("thirdSessionKey"));
//                        System.out.println(userId);
                        String categories = multipartHttpServletRequest.getParameter("categories");
                        Map<String, String> saveMap = new HashMap<>();
                        saveMap.put("photoId", UUID.randomUUID().toString());
                        //写入硬盘start
                        String savePath = "/home/wxpicture/" + saveMap.get("photoId");
                        //String savePath="C:\\wxpicture\\"+saveMap.get("photoId");
                        File saveFile = new File(savePath);
                        if (!saveFile.exists()) {
                            //路径不存在侧创建
                            saveFile.mkdir();//创建相册文件夹
                        }
                        String filePath = savePath + "/" + pictureName;
                        BufferedOutputStream outTOdesk = new BufferedOutputStream(new FileOutputStream(new File(filePath)));
                        outTOdesk.write(bytes);//写入
                        bufferedOutputStream.close();
                        //压缩
                        File compressFile = new File(savePath + "/compress");
                        if (!compressFile.exists()) {
                            compressFile.mkdir();
                        }
                        System.out.println("图片：" + filePath);
                        Thumbnails.of(filePath)
                                .scale(1f)
                                .outputQuality(0.5f)
                                .toFile(compressFile + "/" + pictureName);
                        //end
                        saveMap.put("ownerId", userId);
                        saveMap.put("instruction", introduce);
                        if (request.getParameter("location") != null) {
                            saveMap.put("location", request.getParameter("location"));
                        }
                        saveMap.put("photoURL", pictureName);//图片url
                        saveMap.put("createTime", (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
                        saveMap.put("categories", categories);
                        boolean isSave = userService.firstSave(saveMap);
                        List picList = userSearchService.getPicList(userId, "0");
                        //处理PicList数据
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
                        //处理结束
                        redisTemplate.opsForList().leftPush("pro" + userId, respList);//更新redis
                        redisTemplate.expire("pro" + userId, 12, TimeUnit.HOURS);
                        if (isSave) {
                            File file2 = new File(destPath);//审核路径
                            if (file2.exists() && file2.isFile())
                                file2.delete();//删除图片
                            respMap.put("photoId", saveMap.get("photoId"));
                            System.out.println(respMap);
                            return respMap;
                        } else {
                            File file2 = new File(destPath);//审核路径
                            if (file2.exists() && file2.isFile())
                                file2.delete();//删除图片
                            respMap.put("status", "-1");
                            return respMap;
                        }
                    } else {
                        //同一相册继续加载
                        String userId = (String) redisTemplate.opsForValue().get(multipartHttpServletRequest.getParameter("thirdSessionKey"));
                        Map<String, String> saveMap = new HashMap<>();
                        System.out.println("返回photoId:" + multipartHttpServletRequest.getParameter("photoId"));
                        saveMap.put("photoId", multipartHttpServletRequest.getParameter("photoId"));
                        saveMap.put("photoURL", ";" + pictureName);//图片url
                        //写入硬盘start
                        String savePath = "/home/wxpicture/" + saveMap.get("photoId");
                        //String savePath="C:\\wxpicture\\"+saveMap.get("photoId");
                        File saveFile = new File(savePath);
                        if (!saveFile.exists()) {
                            //路径不存在侧创建
                            saveFile.mkdir();//创建相册文件夹
                        }
                        String filePath = savePath + "/" + pictureName;
                        BufferedOutputStream outTOdesk = new BufferedOutputStream(new FileOutputStream(new File(filePath)));
                        outTOdesk.write(bytes);//写入
                        bufferedOutputStream.close();
                        //压缩
                        File compressFile = new File(savePath + "/compress");
                        if (!compressFile.exists()) {
                            compressFile.mkdir();
                        }
                        System.out.println("图片：" + filePath);
                        Thumbnails.of(filePath)
                                .scale(1f)
                                .outputQuality(0.5f)
                                .toFile(compressFile + "/" + pictureName);
                        //end


                        //更新相册信息
                        boolean isUpdate = userService.nextSave(saveMap);
                        List picList = userSearchService.getPicList(userId, "0");
                        //处理PicList数据
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
                        //处理结束
                        redisTemplate.opsForList().leftPush("pro" + userId, respList);//更新redis
                        redisTemplate.expire("pro" + userId, 12, TimeUnit.HOURS);
                        if (isUpdate) {
                            File file2 = new File(destPath);//审核路径
                            if (file2.exists() && file2.isFile())
                                file2.delete();//删除图片
                            respMap.put("status", "1");
                            return respMap;
                        } else {
                            File file2 = new File(destPath);//审核路径
                            if (file2.exists() && file2.isFile())
                                file2.delete();//删除图片
                            respMap.put("status", "-1");
                            return respMap;        //保存错误
                        }

                    }
                    //保存结束
                } else {
                    File file2 = new File(destPath);//审核路径
                    if (file2.exists()) {
                        file2.delete();//删除图片
                    }
                    respMap.put("status", "-1");
                    return respMap;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("file is null");
        respMap.put("status", "-1");
        return respMap;
    }

    @Autowired
    public FileManageController(RedisTemplate<Object, Object> redisTemplate,
                                UserService userService,
                                UserSearchService userSearchService) {
        this.redisTemplate = redisTemplate;
        this.userService = userService;
        this.userSearchService = userSearchService;
    }
}
