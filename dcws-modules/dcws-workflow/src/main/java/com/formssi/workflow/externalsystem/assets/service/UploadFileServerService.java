package com.formssi.workflow.externalsystem.assets.service;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.formssi.common.core.utils.MapstructUtils;
import com.formssi.common.core.utils.StringUtils;
import com.formssi.common.minio.util.MinioUtil;
import com.formssi.system.domain.SysFile;
import com.formssi.system.domain.vo.SysFileUploadVo;
import com.formssi.system.enums.FileStatusEnum;
import com.formssi.system.enums.FileStorageTypeEnum;
import com.formssi.system.mapper.SysFileMapper;
import com.formssi.workflow.domain.DcwsSysFile;
import com.formssi.workflow.domain.TaskNodeData;
import com.formssi.workflow.domain.vo.DcwsSysFileVo;
import com.formssi.workflow.domain.vo.TaskNodeDataVo;
import com.formssi.workflow.mapper.DcwsSysFileMapper;
import com.formssi.workflow.mapper.TaskNodeDataMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class UploadFileServerService {
    @Autowired
    private SysFileMapper sysFileMapper;
    @Autowired
    private DcwsSysFileMapper dcwsSysFileMapper;
    @Autowired
    private TaskNodeDataMapper taskNodeDataMapper;
    @Autowired
    private IdentifierGenerator identifierGenerator;
    @Autowired
    private MinioUtil minioUtil;

    @Value("${document.api.url}")
    private String apiUrl;

    @Value("${document.api.token}")
    private String token;

    private final WebClient webClient= WebClient.builder()
//            .defaultHeader(HttpHeaders.AUTHORIZATION, token)
//            .baseUrl(apiUrl)
            .build();
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS) // 连接超时 TODO 时长待确认
            .readTimeout(30, TimeUnit.SECONDS)    // 读取超时
            .writeTimeout(30, TimeUnit.SECONDS)   // 写入超时
            .addInterceptor(chain -> {
                Request original = chain.request();
                Request request = original.newBuilder()
                        .header("Authorization", token)
                        .build();
                return chain.proceed(request);
            })
            .build();

    // 上传到minio文件服务器和档案系统服务器
    public void uploadFileMinioAndDocumentServer(byte[] pdfBytes,String fileName,Map<String, Object> documentServerParam,Object dataObject){
        TaskNodeDataVo taskNodeDataVo = (TaskNodeDataVo) dataObject;
        String storageFileStatus = "0";//生成申请单PDF到minio/档案系统服务器状态 1-成功 0-失败 2-minio成功 3-档案系统成功 4-待处理
        // 上传到minio文件服务器
        Map<String, String> fileMinioServerResult = uploadFileMinioServer(new ByteArrayInputStream(pdfBytes), fileName);
                // 插入文件上传服务器记录存储表数据
        DcwsSysFileVo dcwsSysFileVo = new DcwsSysFileVo();
        dcwsSysFileVo.setFileUrl(fileMinioServerResult.get("fileUrl"));
        dcwsSysFileVo.setTaskNodeDataId(taskNodeDataVo.getId());
        dcwsSysFileVo.setFileName(fileName);
        Long recordId = insertUploadRecord(dcwsSysFileVo, fileName, fileMinioServerResult);
        if(!ObjectUtil.isEmpty(fileMinioServerResult) && "succ".equals(fileMinioServerResult.get("code"))){
             storageFileStatus = "2";//2-minio成功
        }
        //上传文件到档案系统
        String documentStatus = uploadFileDocumentServer(pdfBytes, fileName, taskNodeDataVo.getId(), recordId, documentServerParam);
        // 更新文件上传状态
        TaskNodeData convert = MapstructUtils.convert(taskNodeDataVo, TaskNodeData.class);
        if("3".equals(documentStatus) && "2".equals(storageFileStatus)){
            convert.setStorageFileStatus(1);// 成功
        }else if("3".equals(documentStatus)){
            convert.setStorageFileStatus(3);// document成功
        }else if("2".equals(storageFileStatus)){
            convert.setStorageFileStatus(2);// minio成功
        }else {
            convert.setStorageFileStatus(0);// 失败
        }
        taskNodeDataMapper.updateById(convert);

    }


    // 上传到minio文件服务器
   public Map<String, String> uploadFileMinioServer(InputStream inputStream,String fileName){
       String fileUrl;
       Map<String, String> minioResultMap = new HashMap<>();
       minioResultMap.put("code","succ");
       minioResultMap.put("status","上传到minio文件服务器成功");
       try {
           minioUtil.createBucket("dcws-assets");
           minioUtil.uploadFile(inputStream, "dcws-assets", fileName);
           // 获取永久访问URL
           fileUrl = minioUtil.getPermanentTimePreviewUrl("dcws-assets", fileName);
           minioResultMap.put("fileUrl",fileUrl);
           log.info("获取永久访问URL: "+fileUrl);
           // 插入数据
           SysFileUploadVo sysFileUploadVo = new SysFileUploadVo();
           sysFileUploadVo.setUrl(fileUrl);
           sysFileUploadVo.setFileName(fileName);
           insertUploadResult(sysFileUploadVo, fileName);
       } catch (Exception e) {
           log.error("文件上传minio服务器失败："+e.getMessage(),e);
           minioResultMap.put("code","fail");
           minioResultMap.put("result",e.getMessage());
       }
       return minioResultMap;
   }

    //上传文件到档案系统并查询状态更新表数据
   public String uploadFileDocumentServer(byte[]fileBytes,String fileName,String taskNodeDataId,long recordId,Map<String,Object> documentServerParams){
       Map<String, String> resultMap;
       String storageFileStatus = "0";
       try {
           String documentTypeId = (String) documentServerParams.get("documentTypeId");
           String storagePathId = (String) documentServerParams.get("storagePathId");
           String[] tags = (String[]) documentServerParams.get("tags");
           //上传文件到档案系统
           resultMap = uploadDocument(fileBytes, fileName,
                   taskNodeDataId, null, null, documentTypeId, storagePathId, tags,
                   null, null);

           if(!ObjectUtil.isEmpty(resultMap.get("taskId")) && "1".equals(resultMap.get("code"))){
               // 上传文件返回成功，循环查询文件上传到档案系统的结果并更新数据库
               queryFileStatusAndUpdate(resultMap.get("taskId"),recordId);
               storageFileStatus = "3";
           }else{
               // 更新文件上传状态和信息
               DcwsSysFile dcwsSysFile = dcwsSysFileMapper.selectById(recordId);
               dcwsSysFile.setStorageDocumentServer(Convert.toInt(resultMap.get("code")));//失败or待auto处理重新上传
               dcwsSysFile.setDocumentMessage(resultMap.get("result"));
               dcwsSysFile.setDocumentTaskId(resultMap.get("taskId"));
               dcwsSysFileMapper.updateById(dcwsSysFile);
           }
       } catch (Exception e) {
           log.error("文件上传档案系统服务器失败：{}", e.getMessage(), e);
           // 更新数据
           DcwsSysFile dcwsSysFile = dcwsSysFileMapper.selectById(recordId);
           dcwsSysFile.setStorageDocumentServer(0);//失败
           dcwsSysFile.setDocumentMessage(e.getMessage());
           dcwsSysFileMapper.updateById(dcwsSysFile);
           }
       return storageFileStatus;
   }



    public void queryFileStatusAndUpdate(String taskId,Long id){
//        log.info("token-------: {}", token);
//        log.info("apiurl-------: {}", apiUrl);
        webClient.get()
                .uri(apiUrl+"tasks/?task_id=" + taskId)
                .header(HttpHeaders.AUTHORIZATION,token)
                .retrieve()
                .bodyToMono(List.class)
                .doOnSubscribe(sub -> log.info("开始请求任务状态...")) // 订阅日志
                .doOnNext(response -> log.info("收到响应: {}", response)) // 响应日志
                .doOnError(error -> log.error("请求异常: ", error))
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
                        () -> log.info("query file status complete: ")
                );
    }

    private Map<String, String> uploadDocument(byte[] fileBytes,String originalFilename, String title, String created,  String correspondentId,
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
                .url(apiUrl + "documents/post_document/")
                .post(requestBodyBuilder.build())
                .build();
        // 执行请求 上传文件到外部系统
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
        if (!response.isSuccessful()) {//状态码不在 200-299 范围内,如401 Unauthorized
            String errorBody = response.body() != null ?
                    response.body().string() : "No error body";
            throw new IOException("Request failed. Code: " + response.code()
                    + ", Error: " + errorBody);
        }

        try (ResponseBody body = response.body()) {
            if (body != null) {
                return body.string();
            }
        }
        return null;
    }

    /**
     * 插入数据
     * @param sysFileUploadVo 上传文件
     */
    private void insertUploadResult(SysFileUploadVo sysFileUploadVo, String originalName) {
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
        if(!ObjectUtil.isEmpty(minioResultMap)){
            if("succ".equals(minioResultMap.get("code"))){
                sysFile.setStorageMinioServer(1);
                sysFile.setMinioMessage(minioResultMap.get("result"));
            }else {
                sysFile.setStorageMinioServer(0);
                sysFile.setMinioMessage(minioResultMap.get("result")); // 失败原因
            }
        }else {
            log.info("minioResultMap is null");
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
}
