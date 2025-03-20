package com.formssi.system.util;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

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

    /**
     * 将PDF的MultipartFile转换为JPG的MultipartFile
     *
     * @param pdfMultipartFile PDF格式的MultipartFile
     * @return JPG格式的MultipartFile
     * @throws IOException 如果转换过程中发生IO异常
     */
    public static MultipartFile convertPdfToJpg(MultipartFile pdfMultipartFile) throws IOException {
        // 1. 从MultipartFile中获取PDF文件内容并加载为PDDocument对象
        PDDocument document = PDDocument.load(pdfMultipartFile.getInputStream());

        // 2. 使用PDFRenderer将PDF文件渲染为BufferedImage对象
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        BufferedImage image = pdfRenderer.renderImageWithDPI(0, 300); // 这里以300 DPI的分辨率渲染第一页

        // 3. 将BufferedImage对象转换为字节数组
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        byte[] jpgBytes = baos.toByteArray();

        // 4. 创建并返回JPG格式的MultipartFile
        String jpgFileName = "converted_" + System.currentTimeMillis() + ".jpg";
        MultipartFile jpgMultipartFile = new MockMultipartFile(
                "file",
                jpgFileName,
                "image/jpeg",
                jpgBytes
        );
        // 关闭PDDocument对象以释放资源
        document.close();
        return jpgMultipartFile;
    }

}
