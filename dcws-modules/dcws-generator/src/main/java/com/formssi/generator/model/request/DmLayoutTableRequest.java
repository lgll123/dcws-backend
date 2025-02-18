package com.formssi.generator.model.request;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DmLayoutTableRequest {
    private String shape;
    private TableData data;
    private Integer width;
    private String id;
    private String label;
    private Position position;
    private Ports ports;
    private Integer height;


    @Data
    public static class TableData {
        private Long tableId;
        private String tableName;
        private String tableComment;
        private String className;
        private String functionAuthor;
        private String remark;
        private List<DmTableIndexRequest> indexes;
    }

    @Data
    static class Position {
        private Integer x;
        private Integer y;
    }

    @Data
    public static class Ports {
        private List<PortItem> items = new ArrayList<>();
    }

    @Data
    public static class PortItem {
        private ColumnData data;
        private String id;
        private String group;
        private DmLayoutRequest.Attrs attrs;
    }

    @Data
    public static class ColumnData {
        private Long DictId;
        private String columnName;
        private String columnComment;
        private String columnType;
        private Boolean isPk;
        private Boolean isRequired;
        private String defaultValue;
    }

}
