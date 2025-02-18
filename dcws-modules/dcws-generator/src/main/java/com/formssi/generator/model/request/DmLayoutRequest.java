package com.formssi.generator.model.request;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DmLayoutRequest {
    private String title;
    private String systemCode;
    private String moduleCode;
    private List<DmLayoutTableRequest> tables = new ArrayList<>();
    private List<Layout> layouts = new ArrayList<>();

    @Data
    static class Attrs {
        private PortTypeLabel portTypeLabel;
        private PortNameLabel portNameLabel;
    }

    @Data
    static class PortTypeLabel {
        private String text;
    }

    @Data
    static class PortNameLabel {
        private String text;
    }

    @Data
    static class Layout {
        private String shape;
        private String id;
        private Source source;
        private Target target;
        private List<Label> labels;
        private Attrs attrs;
        private Integer zIndex;
    }

    @Data
    static class Source {
        private String port;
        private String cell;
    }

    @Data
    static class Target {
        private String port;
        private String cell;
    }

    @Data
    static class Label {
        private Attrs attrs;
    }

}


