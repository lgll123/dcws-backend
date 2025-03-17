package com.formssi.workflow.service.strategy;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.html2pdf.resolver.font.DefaultFontProvider;
import com.itextpdf.io.font.FontProgram;
import com.itextpdf.io.font.FontProgramFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class CreateApplyFilePDFService {
    @Autowired
    private TemplateEngine templateEngine;

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
        try (InputStream fontStream = new ClassPathResource("/fonts/SourceHanSansSC-Regular-2.otf").getInputStream()) {
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
        // 转换HTML为PDF
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // 初始化 PDF 文档并设置 A4 尺寸
        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(outputStream));
//        pdfDoc.setDefaultPageSize(PageSize.A4);
        HtmlConverter.convertToPdf(
                htmlContent,
                pdfDoc,
                new ConverterProperties().setFontProvider(fontProvider).setBaseUri(new ClassPathResource("templates/").getURI().toString())
        );

        return outputStream.toByteArray();
    }

}
