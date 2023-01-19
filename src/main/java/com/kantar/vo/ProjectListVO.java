package com.kantar.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.*;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectListVO {
    private Integer idx_project;
    private Integer idx_project_job;
    private Integer idx_project_job_projectid;
    private Integer idx_report;
    private String job_no;      // VARCHAR(10)
    private String project_id;  // VARCHAR(10)
    private String project_name;
    private Integer project_type;
    private Integer project_status;
    private String project_type_str;
    private String title;
    private String filename;
    private String user_name;
    private String create_dt;
    private String doc_version;
    List<ReportListVO> reportList;
}