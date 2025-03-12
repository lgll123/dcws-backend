package com.formssi.workflow.externalsystem.assets.service;

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
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
        FontProgram fontProgram = FontProgramFactory.createFont(
                new ClassPathResource("/fonts/SIMHEI.TTF").getFile().getAbsolutePath()
        );
//        PdfFont font = PdfFontFactory.createFont("STSongStd-Light", "UniGB-UCS2-H");
//        FontProgram fontProgram = FontProgramFactory.createFont("STSong-Light" );
        DefaultFontProvider fontProvider = new DefaultFontProvider();
        fontProvider.addFont(fontProgram);
        // 转换HTML为PDF
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // 初始化 PDF 文档并设置 A4 尺寸
        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(outputStream));
        pdfDoc.setDefaultPageSize(PageSize.A4);
        HtmlConverter.convertToPdf(
                htmlContent,
                pdfDoc,
                new ConverterProperties().setFontProvider(fontProvider)
        );

        return outputStream.toByteArray();
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
    public void insertUploadRecord(DcwsSysFileVo dcwsSysFileVo, String originalName) {
        String fileName = dcwsSysFileVo.getFileName();
        DcwsSysFile sysFile = new DcwsSysFile();
        sysFile.setFileName(fileName);
        sysFile.setFileUrl(dcwsSysFileVo.getFileUrl());
        sysFile.setFileStatus(FileStatusEnum.EFFECTIVE.getStatus());
        sysFile.setOriginalName(originalName);
        sysFile.setStorageDocumentServer(0);// TODO
        sysFile.setStorageMinioServer(1);
        sysFile.setTaskNodeDataId(dcwsSysFileVo.getTaskNodeDataId());

        // 获取文件后缀
        String suffix = StringUtils.substring(fileName, fileName.lastIndexOf("."), fileName.length());
        sysFile.setFileSuffix(suffix);

        // 创建者设置为管理员
        sysFile.setCreateBy(1L);// TODO

        // 插入数据
        dcwsSysFileMapper.insert(sysFile);

        dcwsSysFileVo.setId(sysFile.getId());
    }



    private OkHttpClient client = new OkHttpClient();
    public String uploadDocument(byte[] file,String originalFilename,
                                 String title,
                                 String created,
                                 String correspondentId,
                                 String documentTypeId,
                                 String storagePathId,
                                 String[] tags,
                                 String archiveSerialNumber,
                                 String[] customFields) throws IOException {
        // 验证文件是否为空
        if (file == null ) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        // 创建多部分请求体
        MultipartBody.Builder requestBodyBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                // 添加文件部分
                .addFormDataPart(
                        "document",
                        originalFilename,
                        RequestBody.create(
                                file)
                );

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
        try (Response response = client.newCall(request).execute()) {
            taskId =  handleResponse(response);
        }
        return taskId;
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
