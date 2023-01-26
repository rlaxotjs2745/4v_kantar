package com.kantar.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.*;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectVO extends BaseVO {
    private Integer idx_project;
    private Integer idx_project_job;
    private Integer idx_project_job_projectid;
    private Integer idx_report;
    private Integer idx_report_data;
    private String job_no;      // VARCHAR(10)
    private String project_id;  // VARCHAR(10)
    private String project_name;
    private Integer project_type;
    private Integer project_status;
    private String project_type_str;
    private String title;
    private String summary0;
    private String summary;
    private String filepath;
    private String filename;
    private String user_name;
    private Integer project_seq;
    private Integer report_seq;
    private String report_id;
    private String filter_tp;
    List<ReportVO> reportList;

    private String project_merge_idx;
}