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

CREATE TABLE `KT_USER_HISTORY` (
	`idx_history` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`idx_user` INT(11) NOT NULL,
	`remote_ip` VARCHAR(20) NOT NULL COLLATE 'utf8mb4_general_ci',
	`create_dt` DATETIME NULL DEFAULT current_timestamp(),
	`update_dt` DATETIME NULL DEFAULT NULL ON UPDATE current_timestamp(),
	PRIMARY KEY (`idx_history`) USING BTREE
)
COMMENT='멤버 접속 정보'
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB
;


CREATE TABLE `KT_PROJECT` (
	`idx_project` INT(11) NOT NULL AUTO_INCREMENT,
	`idx_project_job_projectid` INT(11) NOT NULL,
	`project_name` VARCHAR(100) NOT NULL COLLATE 'utf8mb4_general_ci',
	`project_type` TINYINT(4) NULL DEFAULT '0' COMMENT '0:임시, 1: 단일, 2:병합',
	`project_status` TINYINT(4) NULL DEFAULT '0' COMMENT '0:임시 저장상태, 1: 파일 파싱이 정상적으로 되어서 정식 프로젝트로 저장됨, 2:병합이 되어버림, 10:병합 임시, 11: 병합완료되어정상',
	`summary0` VARCHAR(400) NULL DEFAULT NULL COLLATE 'utf8mb4_general_ci',
	`idx_user` INT(11) NOT NULL DEFAULT '0',
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
COMMENT='JOB NO _ 서브옵션'
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB
;


CREATE TABLE `KT_PROJECT_JOB_PROJECTID` (
	`idx_project_job_projectid` INT(11) NOT NULL AUTO_INCREMENT,
	`idx_project_job` INT(11) NOT NULL,
	`project_seq` INT(11) UNSIGNED NULL DEFAULT '0',
	`project_id` VARCHAR(10) NULL DEFAULT NULL COLLATE 'utf8mb4_general_ci',
	`filepath` VARCHAR(50) NULL DEFAULT NULL COLLATE 'utf8mb4_general_ci',
	`filename` VARCHAR(100) NULL DEFAULT NULL COLLATE 'utf8mb4_general_ci',
	`idx_user` INT(11) NULL DEFAULT '0',
	PRIMARY KEY (`idx_project_job_projectid`) USING BTREE,
	INDEX `FK_KT_PROJECT_JOB_PROJECTID_KT_PROJECT_JOB` (`idx_project_job`) USING BTREE,
	CONSTRAINT `FK_KT_PROJECT_JOB_PROJECTID_KT_PROJECT` FOREIGN KEY (`idx_project_job_projectid`) REFERENCES `KT_PROJECT` (`idx_project_job_projectid`) ON UPDATE NO ACTION ON DELETE CASCADE,
	CONSTRAINT `FK_KT_PROJECT_JOB_PROJECTID_KT_PROJECT_JOB` FOREIGN KEY (`idx_project_job`) REFERENCES `KT_PROJECT_JOB` (`idx_project_job`) ON UPDATE NO ACTION ON DELETE CASCADE
)
COMMENT='PROJECTID : REPORT\r\nKT_PROJECT와 직접 연계'
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB
;


CREATE TABLE `KT_REPORT` (
	`idx_report` INT(11) NOT NULL AUTO_INCREMENT,
	`idx_project` INT(11) NOT NULL,
	`idx_project_job_projectid` INT(11) NOT NULL,
	`report_id` VARCHAR(10) NULL DEFAULT NULL COLLATE 'utf8mb4_general_ci',
	`filename` VARCHAR(100) NOT NULL COLLATE 'utf8mb4_general_ci',
	`filepath` VARCHAR(50) NOT NULL COLLATE 'utf8mb4_general_ci',
	`idx_user` INT(11) NOT NULL DEFAULT '0',
	`summary0` VARCHAR(4000) NULL DEFAULT NULL COLLATE 'utf8mb4_general_ci',
	`create_dt` DATETIME NULL DEFAULT current_timestamp(),
	`update_dt` DATETIME NULL DEFAULT NULL ON UPDATE current_timestamp(),
	PRIMARY KEY (`idx_report`) USING BTREE,
	INDEX `FK_KT_REPORT_KT_PROJECT` (`idx_project`) USING BTREE,
	INDEX `FK_KT_REPORT_KT_PROJECT_JOB_PROJECTID` (`idx_project_job_projectid`) USING BTREE,
	CONSTRAINT `FK_KT_REPORT_KT_PROJECT` FOREIGN KEY (`idx_project`) REFERENCES `KT_PROJECT` (`idx_project`) ON UPDATE NO ACTION ON DELETE CASCADE,
	CONSTRAINT `FK_KT_REPORT_KT_PROJECT_JOB_PROJECTID` FOREIGN KEY (`idx_project_job_projectid`) REFERENCES `KT_PROJECT_JOB_PROJECTID` (`idx_project_job_projectid`) ON UPDATE NO ACTION ON DELETE CASCADE
)
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB
;


CREATE TABLE `KT_REPORT_DATA` (
	`idx_report_data` INT(11) NOT NULL AUTO_INCREMENT,
	`idx_report` INT(11) NOT NULL DEFAULT '0',
	`title` VARCHAR(2000) NULL DEFAULT NULL COMMENT '요약문 주제' COLLATE 'utf8mb4_general_ci',
	`summary0` VARCHAR(4000) NULL DEFAULT NULL COMMENT '요약문' COLLATE 'utf8mb4_general_ci',
	`idx_user` INT(11) NULL DEFAULT '0',
	`create_dt` DATETIME NULL DEFAULT current_timestamp(),
	`update_dt` DATETIME NULL DEFAULT NULL ON UPDATE current_timestamp(),
	PRIMARY KEY (`idx_report_data`) USING BTREE,
	INDEX `FK_KT_REPORT_DATA_KT_REPORT` (`idx_report`) USING BTREE,
	CONSTRAINT `FK_KT_REPORT_DATA_KT_REPORT` FOREIGN KEY (`idx_report`) REFERENCES `KT_REPORT` (`idx_report`) ON UPDATE NO ACTION ON DELETE CASCADE
)
COMMENT='요약문'
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB
;


CREATE TABLE `KT_REPORT_FILTER` (
	`idx_report_filter` INT(11) NOT NULL AUTO_INCREMENT,
	`filter_title` VARCHAR(50) NOT NULL COLLATE 'utf8mb4_general_ci',
	`create_dt` DATETIME NULL DEFAULT current_timestamp(),
	`update_dt` DATETIME NULL DEFAULT NULL ON UPDATE current_timestamp(),
	PRIMARY KEY (`idx_report_filter`) USING BTREE
)
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB
;


CREATE TABLE `KT_REPORT_KEYWORD` (
	`idx_report_keyword` BIGINT(20) NOT NULL DEFAULT '0',
	`idx_report` BIGINT(20) NOT NULL,
	`keyword` VARCHAR(100) NOT NULL COLLATE 'utf8mb4_general_ci',
	`keytype` VARCHAR(100) NOT NULL COLLATE 'utf8mb4_general_ci',
	`keycount` INT(11) NULL DEFAULT '0',
	`create_dt` DATETIME NULL DEFAULT current_timestamp(),
	`update_dt` DATETIME NULL DEFAULT NULL ON UPDATE current_timestamp(),
	PRIMARY KEY (`idx_report_keyword`) USING BTREE
)
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB
;


CREATE TABLE `KT_DICTIONARY` (
	`idx_dictionary` INT(11) NOT NULL AUTO_INCREMENT,
	`title` VARCHAR(50) NOT NULL COMMENT '사전 이름' COLLATE 'utf8mb4_general_ci',
	`filepath` VARCHAR(50) NOT NULL COMMENT '사전 파일 업로드 위치' COLLATE 'utf8mb4_general_ci',
	`filename` VARCHAR(50) NOT NULL COMMENT '사전 파일명' COLLATE 'utf8mb4_general_ci',
	`dic_type` INT(11) NULL DEFAULT '0' COMMENT '0:기본사전,1:사용자사전',
	`dic_count` INT(11) NULL DEFAULT '0' COMMENT '표제어 수',
	`idx_user` INT(11) NOT NULL DEFAULT '0' COMMENT '등록자',
	`create_dt` DATETIME NULL DEFAULT current_timestamp(),
	`update_dt` DATETIME NULL DEFAULT NULL ON UPDATE current_timestamp(),
	PRIMARY KEY (`idx_dictionary`) USING BTREE
)
COMMENT='사전'
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB
;


CREATE TABLE `KT_DICTIONARY_DATA` (
	`idx_dictionary_data` INT(11) NOT NULL AUTO_INCREMENT,
	`idx_dictionary` INT(11) NOT NULL,
	`keyword` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '대표 키워드' COLLATE 'utf8mb4_general_ci',
	`keyword01` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '키워드01' COLLATE 'utf8mb4_general_ci',
	`keyword02` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '키워드02' COLLATE 'utf8mb4_general_ci',
	`keyword03` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '키워드03' COLLATE 'utf8mb4_general_ci',
	`keyword04` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '키워드04' COLLATE 'utf8mb4_general_ci',
	`keyword05` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '키워드05' COLLATE 'utf8mb4_general_ci',
	`keyword06` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '키워드06' COLLATE 'utf8mb4_general_ci',
	`keyword07` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '키워드07' COLLATE 'utf8mb4_general_ci',
	`keyword08` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '키워드08' COLLATE 'utf8mb4_general_ci',
	`keyword09` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '키워드09' COLLATE 'utf8mb4_general_ci',
	`keyword10` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '키워드10' COLLATE 'utf8mb4_general_ci',
	`idx_user` INT(11) NOT NULL DEFAULT '0' COMMENT '등록자',
	`create_dt` DATETIME NULL DEFAULT current_timestamp(),
	`update_dt` DATETIME NULL DEFAULT NULL ON UPDATE current_timestamp(),
	PRIMARY KEY (`idx_dictionary_data`) USING BTREE,
	INDEX `FK_KT_DICTIONARY_DATA_KT_DICTIONARY` (`idx_dictionary`) USING BTREE,
	CONSTRAINT `FK_KT_DICTIONARY_DATA_KT_DICTIONARY` FOREIGN KEY (`idx_dictionary`) REFERENCES `KT_DICTIONARY` (`idx_dictionary`) ON UPDATE NO ACTION ON DELETE CASCADE
)
COMMENT='키워드 데이터'
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB
;

CREATE TABLE `KT_REPORT_FILTER` (
	`idx_filter` INT(11) NOT NULL AUTO_INCREMENT,
	`idx_project` INT(11) NOT NULL,
	`filter_title` VARCHAR(50) NOT NULL COLLATE 'utf8mb4_general_ci',
	`create_dt` DATETIME NULL DEFAULT current_timestamp(),
	`update_dt` DATETIME NULL DEFAULT NULL ON UPDATE current_timestamp(),
	PRIMARY KEY (`idx_filter`) USING BTREE,
	INDEX `FK_KT_REPORT_FILTER_KT_PROJECT` (`idx_project`) USING BTREE,
	CONSTRAINT `FK_KT_REPORT_FILTER_KT_PROJECT` FOREIGN KEY (`idx_project`) REFERENCES `KT_PROJECT` (`idx_project`) ON UPDATE NO ACTION ON DELETE CASCADE
)
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB
;

CREATE TABLE `KT_REPORT_FILTER_DATA` (
	`idx_filter_data` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
	`idx_filter` INT(11) NOT NULL,
	`filter_type` TINYINT(4) NULL DEFAULT '0' COMMENT '0:없음,1:화자,2:챕터,3:서브챕터,4:질문',
	`filter_data` VARCHAR(4000) NULL DEFAULT NULL COLLATE 'utf8mb4_general_ci',
	PRIMARY KEY (`idx_filter_data`) USING BTREE,
	INDEX `FK_KT_REPORT_FILTER_DATA_KT_REPORT_FILTER` (`idx_filter`) USING BTREE,
	CONSTRAINT `FK_KT_REPORT_FILTER_DATA_KT_REPORT_FILTER` FOREIGN KEY (`idx_filter`) REFERENCES `KT_REPORT_FILTER` (`idx_filter`) ON UPDATE NO ACTION ON DELETE CASCADE
)
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB
;




CREATE TABLE `KT_FILE_STATISTICS` (
    `idx_file_statistics` INT(11) NOT NULL AUTO_INCREMENT,
    `idx_project` INT(11) NOT NULL,
    `file_status` TINYINT(4) NULL DEFAULT '1' COMMENT '1:사용, 99:삭제',
    `file_cnt` INT(11) NOT NULL,
    `file_size` DECIMAL(10,3) NOT NULL,
    `word_length` BIGINT(20) NOT NULL,
    `report_cnt` INT(11) NOT NULL,
    `create_dt` DATETIME NULL DEFAULT current_timestamp(),
    `update_dt` DATETIME NULL DEFAULT NULL ON UPDATE current_timestamp(),
    PRIMARY KEY (`idx_file_statistics`) USING BTREE
)
    COMMENT='시스템 사용 현황'
    COLLATE='utf8mb4_general_ci'
    ENGINE=InnoDB
;


CREATE TABLE `KT_API_STATISTICS` (
     `idx_api_statistics` INT(11) NOT NULL AUTO_INCREMENT,
     `idx_report` INT(11) NOT NULL,
     `report_status` TINYINT(4) NULL DEFAULT '1' COMMENT '1:사용, 99:삭제',
     `summaryUsage` BIGINT(20) NOT NULL,
     `keywordUsage` BIGINT(20) NOT NULL,
     `create_dt` DATETIME NULL DEFAULT current_timestamp(),
     `update_dt` DATETIME NULL DEFAULT NULL ON UPDATE current_timestamp(),
     PRIMARY KEY (`idx_api_statistics`) USING BTREE
)
    COMMENT='API 사용 현황'
    COLLATE='utf8mb4_general_ci'
    ENGINE=InnoDB
;