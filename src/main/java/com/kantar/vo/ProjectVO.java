package com.kantar.vo;

import jakarta.mail.Multipart;
import lombok.*;

@Getter
@Setter
public class ProjectVO extends BaseVO {
    private Integer idx_project;
    private String project_id;
    private Integer project_type;
    private Integer project_status;
    private String summary;
    private Multipart filename;
}