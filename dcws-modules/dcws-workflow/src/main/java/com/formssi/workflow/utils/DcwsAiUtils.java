package com.formssi.workflow.utils;

import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class DcwsAiUtils {

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
    public static void callWithLocalFile(String question ,String localPath)
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
        System.out.println(result.getOutput().getChoices().get(0).getMessage().getContent().get(0).get("text"));}

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

    public static void main(String[] args) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            System.out.println("请输入你的问题：");
            //请识别图中的发票号码，纳税人识别号，单价，金额，税额，并输出为发票号码命名Invoice,纳税人识别号命名taxnum,单价命名unitprice,金额命名amount,税额命名taxamount的json字符串
            String question = reader.readLine(); // 读取一行文本
            callWithLocalFile(question,"D:/OA文档/fapiao.jpg");
            //simpleMultiModalConversationCall(str);
        } catch (IOException | NoApiKeyException | UploadFileException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close(); // 关闭读取器
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
