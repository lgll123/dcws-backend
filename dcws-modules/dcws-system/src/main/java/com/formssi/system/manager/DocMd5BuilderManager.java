package com.formssi.system.manager;

import com.formssi.system.builder.DefaultDocMd5Builder;
import com.formssi.system.builder.DocMd5Builder;

/**
 * @author lizhangyu
 */
public class DocMd5BuilderManager {

    private static DocMd5Builder builder = new DefaultDocMd5Builder();

    public static DocMd5Builder getBuilder() {
        return builder;
    }

    public static void setBuilder(DocMd5Builder builder) {
        DocMd5BuilderManager.builder = builder;
    }
}
