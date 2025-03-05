package com.formssi.workflow.externalsystem.assets.service.impl;

import com.formssi.common.core.exception.ServiceException;
import com.formssi.common.core.utils.SpringUtils;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.workflow.domain.bo.AssetsSystemBo;
import com.formssi.workflow.domain.bo.CategoryBo;
import com.formssi.workflow.externalsystem.assets.service.IAssetsSystemService;
import com.formssi.workflow.externalsystem.assets.strategy.AssetProcessor;
import com.formssi.workflow.externalsystem.assets.strategy.IExternalSystemAPIStrategy;
import com.formssi.workflow.utils.TypeSafeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.formssi.workflow.externalsystem.assets.constant.AssetsConstant.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetsSystemServiceImpl implements IAssetsSystemService {
    private static final String beanName = "assets" + IExternalSystemAPIStrategy.BASE_NAME;
    @Autowired
    private final Map<String, AssetProcessor> processors;

    @Override
    public AssetsSystemBo queryQtyById(AssetsSystemBo bo, String pathUrl) {
        String fullUrl = String.format("%s/%s", pathUrl, bo.getId());
        if (!SpringUtils.containsBean(beanName)) {
            throw new ServiceException("外系统类型不正确!");
        }
        IExternalSystemAPIStrategy instance = SpringUtils.getBean(beanName);
        Map<String, Object> responseMap = instance.process(null,fullUrl,REQUEST_TYPE_GET);
        AssetsSystemBo result = new AssetsSystemBo();
        result.setId(TypeSafeUtils.safeGetInteger(responseMap, ID));

        Integer remainQty = null;
        Integer qty = null;
        switch (pathUrl){
            case CATEGORIES_ACCESSORIES:
                remainQty=TypeSafeUtils.safeGetInteger(responseMap, REMAINING_QTY);
                qty=TypeSafeUtils.safeGetInteger(responseMap, QTY);
                break;
            case CATEGORIES_COMPONENTS:
                remainQty=TypeSafeUtils.safeGetInteger(responseMap, REMAINING);
                qty=TypeSafeUtils.safeGetInteger(responseMap, QTY);
                break;
            case CATEGORIES_LICENSES:
                remainQty=TypeSafeUtils.safeGetInteger(responseMap, SEATS);
                qty=TypeSafeUtils.safeGetInteger(responseMap, FREE_SEATS_COUNT);
                break;
            case CATEGORIES_CONSUMABLES:
                remainQty=TypeSafeUtils.safeGetInteger(responseMap, REMAINING);
                qty=TypeSafeUtils.safeGetInteger(responseMap, QTY);
                break;
            default:
                break;
        }
        result.setQty(qty);
        result.setRemainQty(remainQty);

        result.setCheckoutsCount(TypeSafeUtils.safeGetInteger(responseMap, CHECKOUTS_COUNT));
        return result;
    }

    @Override
    public TableDataInfo<CategoryBo> queryPageCategories(String categoryType,String applyType, PageQuery pageQuery, String pathUrl) {
        Map<String, String> params = new HashMap<>();
        params.put(CATEGORY_TYPE, categoryType);
        params.put(SEARCH, "");
        params.put(LIMIT, String.valueOf(pageQuery.getPageSize()));
        params.put(OFFSET, String.valueOf(pageQuery.getPageSize() * pageQuery.getPageNum()));
        params.put(ORDER, pageQuery.getIsAsc());

        if (!SpringUtils.containsBean(beanName)) {
            throw new ServiceException("外系统类型不正确!");
        }
        IExternalSystemAPIStrategy instance = SpringUtils.getBean(beanName);
        Map<String, Object> responseMap = instance.process(params, pathUrl,REQUEST_TYPE_GET);
        List<Map<String, Object>> rows = (List<Map<String, Object>>) responseMap.get(ROWS);
        Integer total = TypeSafeUtils.safeGetInteger(responseMap, TOTAL);

        List<CategoryBo> categoryBos = new ArrayList<>();
        rows.forEach(row -> {
            CategoryBo categoryBo = new CategoryBo();
            categoryBo.setId(TypeSafeUtils.safeGetInteger(row, ID));
            categoryBo.setCategoryType(TypeSafeUtils.safeGetString(row, CATEGORY_TYPE));
            categoryBo.setName(TypeSafeUtils.safeGetString(row, NAME));
            categoryBos.add(categoryBo);
        });
        if ("21".equals(applyType)) {//非IT物料申请
            List<CategoryBo> categoryBos1 = categoryBos.stream().filter(t -> t.getName().startsWith("非IT_")).collect(Collectors.toList());
            return new TableDataInfo<>(categoryBos1, total.longValue());
        }else {
            List<CategoryBo> categoryBos1 = categoryBos.stream().filter(t -> !t.getName().startsWith("非IT_")).collect(Collectors.toList());
            return new TableDataInfo<>(categoryBos1, total.longValue());
        }
    }


    @Override
    public TableDataInfo<AssetsSystemBo> queryAccessoriesById(Integer categoryId, PageQuery pageQuery,String categories) {
        Map<String, String> params = new HashMap<>();
        params.put(CATEGORY_ID, String.valueOf(categoryId));
        params.put(SEARCH, "");
        params.put(LIMIT, String.valueOf(pageQuery.getPageSize()));
        params.put(OFFSET, String.valueOf(pageQuery.getPageSize() * pageQuery.getPageNum()));
        params.put(ORDER, pageQuery.getIsAsc());

        if (CATEGORIES_HARDWARE.equals(categories)) {
            params.put(STATUS, STATUS_REQUESTABLE);
        }
        if (!SpringUtils.containsBean(beanName)) {
            throw new ServiceException("外系统类型不正确!");
        }
        IExternalSystemAPIStrategy instance = SpringUtils.getBean(beanName);
        Map<String, Object> responseMap = instance.process(params, categories,REQUEST_TYPE_GET);
        List<Map<String, Object>> rows = (List<Map<String, Object>>) responseMap.get(ROWS);
        Integer total = TypeSafeUtils.safeGetInteger(responseMap, TOTAL);

        List<AssetsSystemBo> assetsBos = new ArrayList<>();
        rows.forEach(row -> {
            AssetsSystemBo bo = new AssetsSystemBo();
            bo.setId(TypeSafeUtils.safeGetInteger(row, ID));
            bo.setName(TypeSafeUtils.safeGetString(row, NAME));
            // 根据urlPath选择处理策略
            AssetProcessor processor = processors.get(categories + "Processor");
            if (processor != null) {
                processor.process(row, bo);
            }
            assetsBos.add(bo);
        });
        return new TableDataInfo<>(assetsBos, total.longValue());
    }


    /**
     * 查询组件可checkout的资产列表
     */
    public Map<String, Object> selectAssetslist(Integer page,String pathUrl) {
        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));
        IExternalSystemAPIStrategy instance = SpringUtils.getBean(beanName);
        return  instance.process(params, pathUrl,REQUEST_TYPE_GET);
    }

    private OkHttpClient client = new OkHttpClient();
    @Override
    public String uploadDocument(MultipartFile file,
                               String title,
                               String created,
                               String correspondentId,
                               String documentTypeId,
                               String storagePathId,
                               String[] tags,
                               String archiveSerialNumber,
                               String[] customFields) throws IOException {
        // 验证文件是否为空
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        // 创建多部分请求体
        MultipartBody.Builder requestBodyBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                // 添加文件部分
                .addFormDataPart(
                        "document",
                        file.getOriginalFilename(),
                        RequestBody.create(
                                file.getBytes(),
                                MediaType.parse(file.getContentType())
                        )
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

        // 执行请求
        setAuthToken("Token bb04390c75d903d1baf536ddac2ce2868b5d31ef");
        try (Response response = client.newCall(request).execute()) {
           return  handleResponse(response);
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





