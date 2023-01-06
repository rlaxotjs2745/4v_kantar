CREATE TABLE `KT_USER` (
	`idx_user` INT(11) NOT NULL AUTO_INCREMENT,
	`user_id` VARCHAR(40) NOT NULL COMMENT '회원 아이디' COLLATE 'utf8mb4_general_ci',
	`user_pw` VARCHAR(100) NOT NULL COMMENT '한글, 숫자, 영어, 특수기호 포함 8~12글자' COLLATE 'utf8mb4_general_ci',
	`user_name` VARCHAR(100) NOT NULL COMMENT '이름' COLLATE 'utf8mb4_general_ci',
	`user_phone` VARCHAR(20) NOT NULL COMMENT '전화번호' COLLATE 'utf8mb4_general_ci',
	`user_type` TINYINT(4) NULL DEFAULT '1' COMMENT '1:일반, 11:일반 관리자, 99:슈퍼 관리자',
	`user_status` TINYINT(4) NULL DEFAULT '0' COMMENT '0:생성, 1:인증완료(정상), 98:직권 정지, 99:탈퇴',
	`create_dt` DATETIME NULL DEFAULT current_timestamp(),
	`update_dt` DATETIME NULL DEFAULT NULL ON UPDATE current_timestamp(),
	PRIMARY KEY (`idx_user`) USING BTREE
)
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB
;

CREATE TABLE `KT_PROJECT` (
	`idx_project` INT(11) NOT NULL AUTO_INCREMENT,
	`idx_project_job_projectid` INT(11) NOT NULL,
	`project_name` VARCHAR(100) NOT NULL DEFAULT '' COLLATE 'utf8mb4_general_ci',
	`project_type` TINYINT(4) NULL DEFAULT '0' COMMENT '0:임시, 1: 단일, 2:병합',
	`project_status` TINYINT(4) NULL DEFAULT '0' COMMENT '0:임시 저장상태, 1: 파일 파싱이 정상적으로 되어서 정식 프로젝트로 저장됨, 2:병합이 되어버림, 10:병합 임시, 11: 병합완료되어정상',
	`idx_user` INT(11) NOT NULL DEFAULT '0',
	`summary0` VARCHAR(400) NULL DEFAULT NULL COLLATE 'utf8mb4_general_ci',
	`create_dt` DATETIME NULL DEFAULT current_timestamp(),
	`update_dt` DATETIME NULL DEFAULT NULL ON UPDATE current_timestamp(),
	PRIMARY KEY (`idx_project`) USING BTREE,
	INDEX `idx_project_job_projectid` (`idx_project_job_projectid`) USING BTREE
)
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB
;

CREATE TABLE `KT_PROJECT_JOB` (
	`idx_project_job` INT(11) NOT NULL AUTO_INCREMENT,
	`job_no` VARCHAR(10) NOT NULL COLLATE 'utf8mb4_general_ci',
	PRIMARY KEY (`idx_project_job`) USING BTREE
)
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB
;

CREATE TABLE `KT_PROJECT_JOB_PROJECTID` (
	`idx_project_job_projectid` INT(11) NOT NULL AUTO_INCREMENT,
	`idx_project_job` INT(11) NOT NULL,
	`project_id` VARCHAR(10) NOT NULL COLLATE 'utf8mb4_general_ci',
	`filepath` VARCHAR(50) NOT NULL COLLATE 'utf8mb4_general_ci',
	`filename` VARCHAR(100) NOT NULL COLLATE 'utf8mb4_general_ci',
	`idx_user` INT(11) NOT NULL DEFAULT '0',
	PRIMARY KEY (`idx_project_job_projectid`) USING BTREE,
	INDEX `FK_KT_PROJECT_JOB_PROJECTID_KT_PROJECT_JOB` (`idx_project_job`) USING BTREE,
	CONSTRAINT `FK_KT_PROJECT_JOB_PROJECTID_KT_PROJECT_JOB` FOREIGN KEY (`idx_project_job`) REFERENCES `KT_PROJECT_JOB` (`idx_project_job`) ON UPDATE NO ACTION ON DELETE CASCADE
)
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB
;

CREATE TABLE `KT_REPORT` (
  `idx_report` int(11) NOT NULL AUTO_INCREMENT,
  `idx_project` int(11) NOT NULL,
  `idx_project_job_projectid` int(11) NOT NULL,
  `report_id` varchar(10) NOT NULL,
  `idx_user` int(11) NOT NULL DEFAULT 0,
  `create_dt` datetime DEFAULT current_timestamp(),
  `update_dt` datetime DEFAULT NULL ON UPDATE current_timestamp(),
  PRIMARY KEY (`idx_report`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `KT_REPORT_DATA` (
  `idx_report_data` int(11) NOT NULL AUTO_INCREMENT,
  `idx_report` int(11) NOT NULL DEFAULT 0,
  `summary1` varchar(400) DEFAULT NULL,
  `summary2` varchar(400) DEFAULT NULL,
  `summary3` varchar(400) DEFAULT NULL,
  `summary4` varchar(400) DEFAULT NULL,
  `idx_user` int(11) NOT NULL DEFAULT 0,
  `create_dt` datetime DEFAULT current_timestamp(),
  `update_dt` datetime DEFAULT NULL ON UPDATE current_timestamp(),
  PRIMARY KEY (`idx_report_data`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `KT_REPORT_KEYWORD` (
  `idx_report_keyword` bigint(20) NOT NULL DEFAULT 0,
  `idx_report` bigint(20) NOT NULL,
  `keyword` varchar(100) NOT NULL,
  `keytype` varchar(100) NOT NULL,
  `keycount` int(11) DEFAULT 0,
  `create_dt` datetime DEFAULT current_timestamp(),
  `update_dt` datetime DEFAULT NULL ON UPDATE current_timestamp(),
  PRIMARY KEY (`idx_report_keyword`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

