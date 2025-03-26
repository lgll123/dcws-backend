package com.formssi.workflow.externalsystem.assets.service;

import cn.hutool.core.convert.Convert;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.formssi.common.core.utils.StringUtils;
import com.formssi.system.domain.SysFile;
import com.formssi.system.domain.vo.SysFileUploadVo;
import com.formssi.system.enums.FileStatusEnum;
import com.formssi.system.enums.FileStorageTypeEnum;
import com.formssi.system.mapper.SysFileMapper;
import com.formssi.workflow.domain.DcwsSysFile;
import com.formssi.workflow.domain.vo.DcwsSysFileVo;
import com.formssi.workflow.mapper.DcwsSysFileMapper;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.html2pdf.resolver.font.DefaultFontProvider;
import com.itextpdf.io.font.FontProgram;
import com.itextpdf.io.font.FontProgramFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class PdfGeneratorService {
    private final SysFileMapper sysFileMapper;
    private final DcwsSysFileMapper dcwsSysFileMapper;
    @Autowired
    private TemplateEngine templateEngine;
    @Autowired
    private IdentifierGenerator identifierGenerator;

    public byte[] generatePdf(String templateName, Map<String, Object> data) throws Exception {
        // 渲染HTML模板
        Context context = new Context();
        context.setVariable("data",data);
        String htmlContent = templateEngine.process(templateName, context);

        // 配置中文字体
        /*FontProgram fontProgram = FontProgramFactory.createFont(
                new ClassPathResource("fonts/SIMHEI.TTF").getFile().getAbsolutePath()
        );*/
        FontProgram fontProgram = null;
        try (InputStream fontStream = new ClassPathResource("/fonts/SIMHEI.TTF").getInputStream()) {
            byte[] fontData = IOUtils.toByteArray(fontStream);  // 将字体转换为字节数组
            fontProgram = FontProgramFactory.createFont(fontData);  // 使用字节数组方式加载
        } catch (Exception e) {
            throw new RuntimeException("字体加载失败", e);
        }
        // 2. 资源基准路径（本地图片必须配置）
//        props.setBaseUri("file:/absolute/path/to/static/");
        // 或从 classpath 加载
        // props.setBaseUri(new ClassPathResource("static/").getURI().toString());

//        PdfFont font = PdfFontFactory.createFont("STSongStd-Light", "UniGB-UCS2-H");
//        FontProgram fontProgram = FontProgramFactory.createFont("STSong-Light" );
        DefaultFontProvider fontProvider = new DefaultFontProvider();
        fontProvider.addFont(fontProgram);

        try (
            // 转换HTML为PDF
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            // 初始化 PDF 文档并设置 A4 尺寸
            PdfDocument pdfDoc = new PdfDocument(new PdfWriter(outputStream))) {
//          pdfDoc.setDefaultPageSize(PageSize.A4);
            HtmlConverter.convertToPdf(
                    htmlContent,
                    pdfDoc,
                    new ConverterProperties().setFontProvider(fontProvider).setBaseUri(new ClassPathResource("templates/").getURI().toString())
            );

            return outputStream.toByteArray();
        } catch (Exception e){
            throw new RuntimeException("转换HTML为PDF失败", e);
        }
    }

    /**
     * 插入数据
     * @param sysFileUploadVo 上传文件
     */
    public void insertUploadResult(SysFileUploadVo sysFileUploadVo, String originalName) {
        String fileName = sysFileUploadVo.getFileName();
        SysFile sysFile = new SysFile();
        sysFile.setFileName(fileName);
        sysFile.setFileUrl(sysFileUploadVo.getUrl());
        sysFile.setFileStatus(FileStatusEnum.EFFECTIVE.getStatus());
        sysFile.setOriginalName(originalName);
        sysFile.setStorageType(FileStorageTypeEnum.SERVER.getType());

        // 获取文件后缀
        String suffix = StringUtils.substring(fileName, fileName.lastIndexOf("."), fileName.length());
        sysFile.setFileSuffix(suffix);

        // 设置md5唯一标识
        String identifier = identifierGenerator.nextId(null).toString();
        sysFile.setIdentifier(identifier);
        // 创建者设置为管理员
        sysFile.setCreateBy(1L);

        // 插入数据
        sysFileMapper.insert(sysFile);

        sysFileUploadVo.setFileId(sysFile.getFileId().toString());
    }

    /**
     * 插入文件上传服务器记录存储表数据
     */
    public Long insertUploadRecord(DcwsSysFileVo dcwsSysFileVo, String originalName,Map<String, String> minioResultMap) {
        String fileName = dcwsSysFileVo.getFileName();
        DcwsSysFile sysFile = new DcwsSysFile();
        sysFile.setFileName(fileName);
        sysFile.setFileUrl(dcwsSysFileVo.getFileUrl());
        sysFile.setFileStatus(FileStatusEnum.EFFECTIVE.getStatus());
        sysFile.setOriginalName(originalName);
        sysFile.setStorageDocumentServer(0);
        sysFile.setTaskNodeDataId(dcwsSysFileVo.getTaskNodeDataId());
        if("succ".equals(minioResultMap.get("code"))){
            sysFile.setStorageMinioServer(1);
            sysFile.setMinioMessage(minioResultMap.get("result"));
        }else {
            sysFile.setStorageMinioServer(0);
            sysFile.setMinioMessage(minioResultMap.get("result")); // 失败原因
        }
        // 获取文件后缀
        String suffix = StringUtils.substring(fileName, fileName.lastIndexOf("."), fileName.length());
        sysFile.setFileSuffix(suffix);
        // 创建者设置为管理员
        sysFile.setCreateBy(1L);// TODO
        // 插入数据
        dcwsSysFileMapper.insert(sysFile);
        dcwsSysFileVo.setId(sysFile.getId());
        return sysFile.getId();
    }



    private OkHttpClient client = new OkHttpClient();
    public Map<String, String> uploadDocument(byte[] fileBytes,String originalFilename, String title, String created,  String correspondentId,
                                              String documentTypeId,
                                              String storagePathId,
                                              String[] tags,
                                              String archiveSerialNumber,
                                              String[] customFields) {
        // 验证文件是否为空
        if (fileBytes == null ) {
            throw new RuntimeException("File cannot be empty");
        }
        // 创建多部分请求体
        MultipartBody.Builder requestBodyBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                // 添加文件部分
                .addFormDataPart(
                        "document",
                        originalFilename,
                        RequestBody.create(fileBytes));
        // 添加可选字段（与原实现相同）
        addOptionalFields(requestBodyBuilder, title, created, correspondentId,
                documentTypeId, storagePathId, tags,
                archiveSerialNumber, customFields);
        // 构建请求
        Request request = new Request.Builder()
                .url("http://10.100.216.113:8000/api/documents/post_document/")
                .post(requestBodyBuilder.build())
                .build();
        // 执行请求 上传文件到外部系统
        setAuthToken("Token bb04390c75d903d1baf536ddac2ce2868b5d31ef");
        String taskId = null;
        String code;
        String result = null;
        int maxRetries = 5;
        int retryCount = 0;
        while (true) {
            try (Response response = client.newCall(request).execute()) {
                taskId = handleResponse(response);
                code ="1";//成功
                break; // 成功则退出循环
            } catch (IOException e) { // 捕获网络异常
                result = e.getMessage();
                log.error("网络错误: {}", e.getMessage(), e);
                retryCount++;
                if (retryCount >= maxRetries) {
                    log.error("重试 {} 次后仍失败", maxRetries);
                    code ="2";//失败后待auto后续处理
                    break;
                }
                log.info("第 {} 次重试...", retryCount);
                try {
                    Thread.sleep(2000); // 重试间隔 2 秒
                } catch (InterruptedException ex) {
                    code ="2";//失败后待auto后续处理
                    log.error("重试间隔错误：{}",ex.getMessage(),ex);
                }
            } catch (Exception e) { // 其他非IO异常（如 JSON 解析错误、业务逻辑异常）
                code ="0";//失败
                result = e.getMessage();
                log.error("非网络异常: {}", e.getMessage(), e);
            }
        }
        Map<String, String> retResult = new HashMap<>();
        if(StringUtils.isEmpty(taskId)){
            log.info("上传文件到档案系统返回的taskId为空");
        }else {
            taskId = taskId.replaceAll("\"","");
        }
        retResult.put("taskId",taskId);
        retResult.put("code",code);
        retResult.put("result",result);
        return retResult;
    }

    private static final String AUTH_TOKEN = "Token bb04390c75d903d1baf536ddac2ce2868b5d31ef";
    private final WebClient webClient= WebClient.builder()
            .defaultHeader(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
            .build();
    public void queryFileStatusAndUpdate(String taskId,Long id){
        webClient.get()
                .uri("http://10.100.216.113:8000/api/tasks/?task_id=" + taskId)
                .retrieve()
                .bodyToMono(List.class)
                .flatMap(response -> {
                    Map<String, Object> firstItem = (Map<String, Object>) response.get(0);
                    String status = (String) firstItem.get("status");
                    if ("SUCCESS".equals(status) || "FAILURE".equals(status)) {
                        // 状态已终止，返回结果
                        return Mono.just(response);
                    } else {
                        // 状态未就绪，抛出错误触发重试
                        return Mono.error(new RuntimeException("Status pending"));
                    }
                })
                // 捕获网络异常（如超时、连接拒绝）
                .onErrorResume(error -> {
                    // 处理网络异常
                    if (error instanceof WebClientRequestException) {
                        return Mono.error(new RuntimeException("网络异常: " + error.getMessage()));
                    }
                    // 其他错误（如状态未就绪）直接传递
                    return Mono.error(error);
                })
                .retryWhen(Retry
                        .backoff(10, Duration.ofSeconds(2)) // 最多重试10次（总请求次数=初始1次+重试10次）
                        .filter(error ->  // 状态未就绪 or 网络错误
                                error.getMessage().contains("Status pending") || error.getMessage().contains("网络异常")
                        )
                        .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) ->
                                new RuntimeException("重试耗尽，最终失败"))
                )
                .subscribe(
                        resultList -> {
                            Map<String, Object> map = (Map<String, Object>) resultList.get(0);
                            // 更新数据
                            DcwsSysFile dcwsSysFile = dcwsSysFileMapper.selectById(id);
                            int status = 0;//查询失败
                            String message = null;
                            String documentId = null;
                            if("SUCCESS".equals(map.get("status"))){
                                status = 1;//查询成功，文件上传成功
                                documentId = Convert.toStr(map.get("related_document"));
                                message = Convert.toStr(map.get("result"));
                            }
                            if("FAILURE".equals(map.get("status"))){
                                status = 0;//查询成功文件上传失败
                                documentId = Convert.toStr(map.get("related_document"));
                                message = Convert.toStr(map.get("result"));
                            }
                            if("STARTED".equals(map.get("status"))){
                                documentId = Convert.toStr(map.get("related_document"));
                                status = 3;//文件上传中待auto处理
                                message = "文件上传中";
                            }
                            dcwsSysFile.setDocumentId(documentId);
                            dcwsSysFile.setStorageDocumentServer(status);
                            dcwsSysFile.setDocumentMessage(message);
                            dcwsSysFile.setDocumentTaskId(taskId);
                            dcwsSysFileMapper.updateById(dcwsSysFile);
                            log.info("update file status success: ");
                        },
                        error -> {
                            // 更新数据
                            DcwsSysFile dcwsSysFile = dcwsSysFileMapper.selectById(id);
                            int status = 0;//查询失败
                            String message;
                            // 分类处理错误
                            if (error.getMessage().contains("Unauthorized")) {
                                log.error("权限问题: {}", error.getMessage());
                                status = 4;//查询失败待auto处理
                                message = "权限问题: " + error.getMessage();
                            } else if (error.getMessage().contains("网络异常")) {
                                log.error("网络问题: {}", error.getMessage());
                                message = "网络问题: " + error.getMessage();
                                status = 4;//查询失败待auto处理
                            } else if (error.getMessage().contains("重试耗尽")) {
                                log.error("轮询超时未获取最终状态");
                                message = "重试耗尽: " + error.getMessage();
                                status = 4;//查询失败待auto处理
                            } else {
                                status = 5;//查询失败
                                message = "其他错误: " + error.getMessage();
                                log.error("其他错误: {}", error.getMessage());
                            }
                            dcwsSysFile.setStorageDocumentServer(status);
                            dcwsSysFile.setDocumentMessage(message);
                            dcwsSysFile.setDocumentTaskId(taskId);
                            dcwsSysFileMapper.updateById(dcwsSysFile);
                            log.info("query file status fail: {}", error.getMessage(), error);
                        },
                        () -> {
                            log.info("query file status complete: ");
                        }
                );
    }

    private void pollTaskStatus(String taskId) {
        log.info("taskId--------"+taskId);
        // 构建请求
        Request request = new Request.Builder()
                .url("http://10.100.216.113:8000/api/tasks/?task_id="+taskId)
                .header("Authorization", "Token bb04390c75d903d1baf536ddac2ce2868b5d31ef")
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            String s = handleResponse(response);
        } catch (Exception e){
            log.error("文件上传结果查询异常：",e);
        }

    }

    // 专用方法处理可选字段
    private void addOptionalFields(MultipartBody.Builder builder,
                                   String title,
                                   String created,
                                   String correspondentId,
                                   String documentTypeId,
                                   String storagePathId,
                                   String[] tags,
                                   String archiveSerialNumber,
                                   String[] customFields) {
        if (title != null) builder.addFormDataPart("title", title);
        if (created != null) builder.addFormDataPart("created", created);
        if (correspondentId != null) builder.addFormDataPart("correspondent", correspondentId);
        if (documentTypeId != null) builder.addFormDataPart("document_type", documentTypeId);
        if (storagePathId != null) builder.addFormDataPart("storage_path", storagePathId);
        if (archiveSerialNumber != null) builder.addFormDataPart("archive_serial_number", archiveSerialNumber);

        if (tags != null) {
            for (String tag : tags) {
                builder.addFormDataPart("tags", tag);
            }
        }

        if (customFields != null) {
            for (String field : customFields) {
                builder.addFormDataPart("custom_fields", field);
            }
        }
    }

    // 处理响应
    private String handleResponse(Response response) throws IOException {
        if (!response.isSuccessful()) {
            String errorBody = response.body() != null ?
                    response.body().string() : "No error body";
            throw new IOException("Request failed. Code: " + response.code()
                    + ", Error: " + errorBody);
        }

        try (ResponseBody body = response.body()) {
            if (body != null) {
//                log.info("Response: " + body.string());
                return body.string();
            }
        }
        return null;
    }

    // 添加认证头（可选）
    public void setAuthToken(String token) {
        client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request request = original.newBuilder()
                            .header("Authorization", token)
                            .build();
                    return chain.proceed(request);
                })
                .build();
    }
}
