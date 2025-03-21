package com.formssi.workflow.utils;

import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import org.apache.pdfbox.rendering.ImageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.configurationprocessor.json.JSONException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class DcwsAiUtils {

    private static final Logger log = LoggerFactory.getLogger(DcwsAiUtils.class);

    //简单示例
    public static void simpleMultiModalConversationCall(String str)
            throws ApiException, NoApiKeyException, UploadFileException {
        MultiModalConversation conv = new MultiModalConversation();
        MultiModalMessage systemMessage = MultiModalMessage.builder().role(Role.SYSTEM.getValue())
                .content(Arrays.asList(
                        Collections.singletonMap("text", "You are a helpful assistant."))).build();
        MultiModalMessage userMessage = MultiModalMessage.builder().role(Role.USER.getValue())
                .content(Arrays.asList(
                        Collections.singletonMap("image", "https://img1.baidu.com/it/u=3938002324,3067630694&fm=253&fmt=auto&app=138&f=JPEG?w=1158&h=748"),
                        Collections.singletonMap("text", str))).build();
        MultiModalConversationParam param = MultiModalConversationParam.builder()
                // 若没有配置环境变量，请用百炼API Key将下行替换为：.apiKey("sk-xxx")
                .apiKey("sk-dcc0e4fa316a4eae99e41efac13c0e2c")
                .model("qwen-vl-max-latest")
                .messages(Arrays.asList(systemMessage, userMessage))
                .build();
        MultiModalConversationResult result = conv.call(param);
        System.out.println(result.getOutput().getChoices().get(0).getMessage().getContent().get(0).get("text"));
    }

    //多轮对话（参考历史对话信息）
    public static void MultiRoundConversationCall() throws ApiException, NoApiKeyException, UploadFileException {
        MultiModalConversation conv = new MultiModalConversation();
        MultiModalMessage systemMessage = MultiModalMessage.builder().role(Role.SYSTEM.getValue())
                .content(Arrays.asList(Collections.singletonMap("text", "You are a helpful assistant."))).build();
        MultiModalMessage userMessage = MultiModalMessage.builder().role(Role.USER.getValue())
                .content(Arrays.asList(Collections.singletonMap("image", "https://help-static-aliyun-doc.aliyuncs.com/file-manage-files/zh-CN/20241022/emyrja/dog_and_girl.jpeg"),
                        Collections.singletonMap("text", "图中描绘的是什么景象？"))).build();
        List<MultiModalMessage> messages = new ArrayList<>();
        messages.add(systemMessage);
        messages.add(userMessage);
        MultiModalConversationParam param = MultiModalConversationParam.builder()
                // 若没有配置环境变量，请用百炼API Key将下行替换为：.apiKey("sk-xxx")
                .apiKey("sk-dcc0e4fa316a4eae99e41efac13c0e2c")
                .model("qwen-vl-max-latest")
                .messages(messages)
                .build();
        MultiModalConversationResult result = conv.call(param);
        System.out.println("第一轮输出："+result.getOutput().getChoices().get(0).getMessage().getContent().get(0).get("text"));        // add the result to conversation
        messages.add(result.getOutput().getChoices().get(0).getMessage());
        MultiModalMessage msg = MultiModalMessage.builder().role(Role.USER.getValue())
                .content(Arrays.asList(Collections.singletonMap("text", "做一首诗描述这个场景"))).build();
        messages.add(msg);
        param.setMessages((List)messages);
        result = conv.call(param);
        System.out.println("第二轮输出："+result.getOutput().getChoices().get(0).getMessage().getContent().get(0).get("text"));    }

    //使用本地文件（Base64编码或本地路径）
    public static String callWithLocalFile(String question ,String localPath)
            throws ApiException, NoApiKeyException, UploadFileException, IOException {
        String filePath = "file:///"+localPath;
        //String base64Image = convertImageToBase64("D:\\OA文档\\OA任务1.jpg");

        MultiModalConversation conv = new MultiModalConversation();
        MultiModalMessage systemMessage = MultiModalMessage.builder().role(Role.SYSTEM.getValue())
                .content(Arrays.asList(Collections.singletonMap("text", "You are a helpful assistant."))).build();
        MultiModalMessage userMessage = MultiModalMessage.builder().role(Role.USER.getValue())
                .content(Arrays.asList(new HashMap<String, Object>(){{put("image", filePath);}},
                        new HashMap<String, Object>(){{put("text", question);}})).build();
        MultiModalConversationParam param = MultiModalConversationParam.builder()
                // 若没有配置环境变量，请用百炼API Key将下行替换为：.apiKey("sk-xxx")
                .apiKey("sk-dcc0e4fa316a4eae99e41efac13c0e2c")
                .model("qwen-vl-max-latest")
                .messages(Arrays.asList(systemMessage, userMessage))
                .build();
        MultiModalConversationResult result = conv.call(param);
        return (String) result.getOutput().getChoices().get(0).getMessage().getContent().get(0).get("text");
    }

    /**
     * 将图片文件转换为Base64字符串
     *
     * @param imagePath 图片文件的路径
     * @return Base64编码的字符串
     */
    public static String convertImageToBase64(String imagePath) {
        File imageFile = new File(imagePath);
        String base64String = null;

        // 使用try-with-resources语句确保FileInputStream在使用后被正确关闭
        try (FileInputStream imageInputStream = new FileInputStream(imageFile)) {
            // 读取图片文件为字节数组
            byte[] imageBytes = imageInputStream.readAllBytes();
            // 使用Base64编码器将字节数组编码为Base64字符串
            base64String = Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            // 捕获并处理IO异常
            e.printStackTrace();
        }

        return base64String;
    }

    public static String authentication() {
        try {
            // 创建URL对象
            URL profileurl = new URL("http://10.101.137.3:8001/api/application/authentication");
            // 打开连接
            HttpURLConnection connection = (HttpURLConnection) profileurl.openConnection();
            // 设置请求方法（GET）
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Accept", "application/json");
            // 允许输入流（默认是允许的）
            connection.setDoOutput(true);
            connection.setDoInput(true);
            String str = "{\"access_token\":\"3cf1e9856eb5b264\"}";
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = str.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                log.info(profileurl.toString());
                log.info(response.toString());
            }
            return (String) JSON.parseObject(response.toString()).get("data");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getApplicationId(String token) {
        try {
            // 创建URL对象
            URL profileurl = new URL("http://10.101.137.3:8001/api/application/profile");
            // 打开连接
            HttpURLConnection connection = (HttpURLConnection) profileurl.openConnection();
            // 设置请求方法（GET）
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", token);
            // 允许输入流（默认是允许的）
            connection.setDoInput(true);
            String applicationId = null;
            // 读取响应体
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                // 将JSON字符串转换为JSONObject
                JSONObject jsonObject = JSON.parseObject((response.toString()));
                // 创建一个Map来存储转换后的数据
                Map<String, Object> map = new HashMap<>();
                // 迭代JSONObject的键集合并将其添加到Map中
                Iterator<String> keys = jsonObject.keySet().iterator();
                while (keys.hasNext()) {
                    String key = keys.next();
                    Object value = jsonObject.get(key);
                    map.put(key, value);
                }
                JSONObject j = (JSONObject) map.get("data");
                applicationId = (String) j.get("id");
                log.info(profileurl.toString());
                log.info(response.toString());
            }
            // 关闭连接
            connection.disconnect();
            return applicationId;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getChatId(String token,String applicationId) {
        try {
            // 创建URL对象
            URL profileurl = new URL("http://10.101.137.3:8001/api/application/"+applicationId+"/chat/open");
            // 打开连接
            HttpURLConnection connection = (HttpURLConnection) profileurl.openConnection();
            // 设置请求方法（GET）
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", token);
            // 允许输入流（默认是允许的）
            connection.setDoInput(true);
            String chatId = null;
            // 读取响应体
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                // 将JSON字符串转换为JSONObject
                JSONObject jsonObject =  JSON.parseObject(response.toString());
                // 创建一个Map来存储转换后的数据
                Map<String, Object> map = new HashMap<>();
                // 迭代JSONObject的键集合并将其添加到Map中
                Iterator<String> keys = jsonObject.keySet().iterator();
                while (keys.hasNext()) {
                    String key = keys.next();
                    Object value = jsonObject.get(key);
                    map.put(key, value);
                }
                chatId = (String) map.get("data");
                log.info(profileurl.toString());
                log.info(response.toString());
            }
            // 关闭连接
            connection.disconnect();
            return chatId;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static JSONObject uploadImage(File imageFile,String applicationId,String chatId,String token) throws IOException, JSONException {
        String UPLOAD_URL = "http://10.101.137.3:8001/api/application/"+applicationId+"/chat/"+chatId+"/upload_file";
        String BOUNDARY = "&zwnj;*****&zwnj;";

        URL url = new URL(UPLOAD_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + BOUNDARY);
        connection.setRequestProperty("Authorization", token);

        DataOutputStream request = new DataOutputStream(connection.getOutputStream());

        // 写入文件字段
        request.writeBytes("--" + BOUNDARY + "\r\n");
        request.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\"" + imageFile.getName() + "\"\r\n");
        request.writeBytes("Content-Type: image/jpeg\r\n\r\n");

        // 读取文件并写入到请求中
        FileInputStream inputStream = new FileInputStream(imageFile);
        byte[] buffer = new byte[4096];
        int bytesRead = -1;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            request.write(buffer, 0, bytesRead);
        }
        inputStream.close();

        // 写入请求结束标志
        request.writeBytes("\r\n--" + BOUNDARY + "--\r\n");
        request.flush();
        request.close();

        // 读取服务器响应
        int responseCode = connection.getResponseCode();
        StringBuilder response = new StringBuilder();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                log.info(url.toString());
                log.info(response.toString());
            }
        } else {
            log.error("Failed to upload image. Response code: " + responseCode);
        }
        connection.disconnect();
        return JSON.parseObject((response.toString()));
    }


    public static String chatMessage(String token,String chatid,JSONObject imageJsonObject,String question) {
        try {
            // 创建URL对象
            URL profileurl = new URL("http://10.101.137.3:8001/api/application/chat_message/"+chatid);
            // 打开连接
            HttpURLConnection connection = (HttpURLConnection) profileurl.openConnection();
            // 设置请求方法（GET）
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", token);
            // 允许输入流（默认是允许的）
            connection.setDoOutput(true);
            connection.setDoInput(true);

            String str = "{\"message\":\"\",\"re_chat\":false,\"stream\":false,\"image_list\":[{\"name\":\"\",\"percentage\":0,\"status\":\"ready\",\"size\":99227,\"raw\":{\"uid\":1742007245631},\"uid\":1742007245631,\"url\":\"\",\"file_id\":\"\"}],\"document_list\":[],\"audio_list\":[],\"video_list\":[],\"form_data\":{}}";
            JSONObject j = JSON.parseObject(str);
            j.put("message",question);
            JSONObject imageObject = (JSONObject) j.getJSONArray("image_list").get(0);
            JSONObject imageDetail = (JSONObject) imageJsonObject.getJSONArray("data").get(0);
            imageObject.put("name",(String) imageDetail.get("name"));
            imageObject.put("url",(String) imageDetail.get("url"));
            imageObject.put("file_id",(String) imageDetail.get("file_id"));

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = j.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                log.info(profileurl.toString());
                log.info(response.toString());
            }
            JSONObject jSONObject = (JSONObject) JSON.parseObject(response.toString()).get("data");
            return (String) jSONObject.get("content");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static InputStream convertPdfPageToJpg(InputStream file) throws IOException {
        PDDocument document = PDDocument.load(file);
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(0, 300);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpg", byteArrayOutputStream);
        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }

    public static String invoiceIdentification(InputStream file, String question) throws IOException, JSONException {
        String token = "eyJhcHBsaWNhdGlvbl9pZCI6IjdmYTUxZDllLTAwNzMtMTFmMC04Y2UzLWZhMTYzZWU1MTJlMyIsInVzZXJfaWQiOiIwMzZhZDQ5ZS1mNDJmLTExZWYtOWFmYy1mYTE2M2VlNTEyZTMiLCJhY2Nlc3NfdG9rZW4iOiIzY2YxZTk4NTZlYjViMjY0IiwidHlwZSI6IkFQUExJQ0FUSU9OX0FDQ0VTU19UT0tFTiIsImNsaWVudF9pZCI6Ijk0YmJmYmU4LTAxNzgtMTFmMC04ODQzLWZhMTYzZWU1MTJlMyIsImF1dGhlbnRpY2F0aW9uIjp7fX0:1ttN0L:5sb7C33PadgFRUf_lFLt6Ff8dM2ePdR3wlIC8cr0ZKc";
        String applicationId = "7fa51d9e-0073-11f0-8ce3-fa163ee512e3";
        String chatId = getChatId(token,applicationId);
        // 创建临时文件
        Path tempFilePath = Files.createTempFile("inputStreamTempFile", ".tmp");
        // 将InputStream中的数据写入临时文件
        Files.copy(file, tempFilePath, StandardCopyOption.REPLACE_EXISTING);
        // 返回File对象
        JSONObject imageJsonObject = uploadImage(tempFilePath.toFile(),applicationId,chatId,token);
        return chatMessage(token,chatId,imageJsonObject,question);
    }

    public static void convert(String pdfFilePath, String outputDir) throws IOException {
        // 加载PDF文档
        try (PDDocument document = PDDocument.load(new File(pdfFilePath))) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            // 遍历PDF的每一页
            for (int page = 0; page < document.getNumberOfPages(); ++page) {
                BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);
                // 构造输出文件路径
                String jpgFilePath = outputDir + File.separator + "page_" + (page + 1) + ".jpg";
                // 将BufferedImage写入JPG文件
                ImageIO.write(bim, "jpg", new File(jpgFilePath));
            }
        }
    }


    public static void main(String[] args) throws Exception {
        try {
//            String pdfFilePath = "D:\\dzfp_invoice.pdf"; // 替换为你的PDF文件路径
//            String outputDir = "D:\\"; // 替换为你希望保存JPG文件的目录
//
//            try {
//                Long start = System.currentTimeMillis();
//                convert(pdfFilePath, outputDir);
//                Long end = System.currentTimeMillis();
//                System.out.println("PDF converted to JPG successfully!" + (end - start));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("请输入你的问题：");
            //请识别图中的发票号码，纳税人识别号，价税合计大写，并输出为发票号码重命名为:Invoice,纳税人识别号重命名为:taxnum,价税合计大写重命名为:taxamount的json字符串
            String question = reader.readLine(); // 读取一行文本

            //公司本地部署Qwen2.5-VL-7B-Instruct模型
            Long start = System.currentTimeMillis();
            //String token = authentication();
            String token = "eyJhcHBsaWNhdGlvbl9pZCI6IjdmYTUxZDllLTAwNzMtMTFmMC04Y2UzLWZhMTYzZWU1MTJlMyIsInVzZXJfaWQiOiIwMzZhZDQ5ZS1mNDJmLTExZWYtOWFmYy1mYTE2M2VlNTEyZTMiLCJhY2Nlc3NfdG9rZW4iOiIzY2YxZTk4NTZlYjViMjY0IiwidHlwZSI6IkFQUExJQ0FUSU9OX0FDQ0VTU19UT0tFTiIsImNsaWVudF9pZCI6Ijk0YmJmYmU4LTAxNzgtMTFmMC04ODQzLWZhMTYzZWU1MTJlMyIsImF1dGhlbnRpY2F0aW9uIjp7fX0:1ttN0L:5sb7C33PadgFRUf_lFLt6Ff8dM2ePdR3wlIC8cr0ZKc";
            //String applicationId = getApplicationId(token);
            String applicationId = "7fa51d9e-0073-11f0-8ce3-fa163ee512e3";
            String chatid = getChatId(token,applicationId);

//            InputStream  InputStreamPdf =  new FileInputStream(new File("D:\\dzfp_invoice.pdf"));
//            InputStream  InputStreamJpg = convertPdfPageToJpg(InputStreamPdf);
//            // 创建临时文件
//            Path tempFilePath = Files.createTempFile("inputStreamTempFile", ".tmp");
//            // 将InputStream中的数据写入临时文件
//            Files.copy(InputStreamJpg, tempFilePath, StandardCopyOption.REPLACE_EXISTING);
//
//            JSONObject imageJsonObject = uploadImage(tempFilePath.toFile(),applicationId,chatid,token);
            JSONObject imageJsonObject = uploadImage(new File("D:\\page_1.jpg"),applicationId,chatid,token);
            String answer  = chatMessage(token,chatid,imageJsonObject,question);
            Long end = System.currentTimeMillis();
            System.out.printf(answer+"\n" + "本地部署Qwen2.5-VL-7B-Instruct模型获取图片信息耗时：" + (end - start) +"\n\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
