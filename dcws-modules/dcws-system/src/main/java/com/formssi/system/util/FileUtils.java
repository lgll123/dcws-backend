package com.formssi.system.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * @author lizhangyu
 * @version 1.0
 * @date 2024/12/5 11:52
 */
public class FileUtils {

    /**
     *
     * @param file 文件
     * @param path 文件存放路径
     * @param fileName 源文件名
     * @return
     */
    public static String upload(MultipartFile file, String path, String fileName){

        //使用原文件名
        String realPath = path + "/" + fileName;

        File dest = new File(realPath);

        //判断文件父目录是否存在
        if(!dest.getParentFile().exists()){
            dest.getParentFile().mkdir();
        }

        try {

            //保存文件
            file.transferTo(dest);
            return fileName;
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }

    }

    /**
     * 删除文件
     * @param url 地址
     * @param localPictures 本地图片
     * @return 返回
     */
    public static boolean delFile(String url,String localPictures) {
        File dirFile = new File(localPictures);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        //先获取最后一个  \ 所在的位置
        int index1 = url.lastIndexOf("/");
        //然后获取从最后一个\所在索引+1开始 至 字符串末尾的字符
        String path = localPictures + url.substring(index1+1);

        boolean flag = false;
        File file = new File(path);
        if (!file.exists()) {
            return flag;
        }
        try{
            flag = file.delete();
        }catch (Exception e){
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 删除文件
     * @param url 地址
     * @param localPictures 本地图片
     * @return 返回删除文件结果
     */
    public static boolean deleteFile(String url,String localPictures) {
        boolean flag = false;
        File file = new File(localPictures+url);
        if (!file.exists()) {
            return flag;
        }
        try{
            flag = file.delete();
        }catch (Exception e){
            e.printStackTrace();
        }
        return flag;
    }

}
