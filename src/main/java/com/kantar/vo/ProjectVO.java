package com.kantar.vo;

import lombok.*;

@Getter
@Setter
public class ProjectVO extends BaseVO {
    private Integer idx_project;
    private Integer idx_project_job_projectid;
    private String job_no;      // VARCHAR(10)
    private String project_id;  // VARCHAR(10)
    private String project_name;
    private Integer project_type;
    private Integer project_status;
    private String summary0;
    private String summary1;
    private String summary2;
    private String summary3;
    private String summary4;
    private String filepath;
    private String filename;
}