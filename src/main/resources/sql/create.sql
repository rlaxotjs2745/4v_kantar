CREATE TABLE `KT_USER` (
  `idx_user` int(11) NOT NULL AUTO_INCREMENT COMMENT '인덱스',
  `user_id` varchar(40) NOT NULL COMMENT '회원 아이디',
  `user_pw` varchar(100) NOT NULL COMMENT '회원 비밀번호 (한글, 숫자, 영어, 특수기호 포함 8~12글자)',
  `user_name` varchar(100) NOT NULL COMMENT '이름',
  `user_phone` varchar(20) NOT NULL COMMENT '전화번호',
  `user_type` tinyint(4) DEFAULT 1 COMMENT '회원 권한 (1:일반, 11:일반 관리자, 99:슈퍼 관리자)',
  `user_status` tinyint(4) DEFAULT 0 COMMENT '회원 상태 (0:생성, 1:인증완료(정상), 98:직권 정지, 99:탈퇴)',
  `first_code` varchar(100) DEFAULT NULL COMMENT '회원가입 인증 코드',
  `create_dt` datetime DEFAULT current_timestamp() COMMENT '등록 일시정보',
  `update_dt` datetime DEFAULT NULL ON UPDATE current_timestamp() COMMENT '업데이트 일시정보',
  PRIMARY KEY (`idx_user`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='회원 정보';


CREATE TABLE `KT_USER_HISTORY` (
  `idx_history` bigint(20) NOT NULL AUTO_INCREMENT,
  `idx_user` int(11) NOT NULL,
  `remote_ip` varchar(20) NOT NULL,
  `create_dt` datetime DEFAULT current_timestamp(),
  `update_dt` datetime DEFAULT NULL ON UPDATE current_timestamp(),
  PRIMARY KEY (`idx_history`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='멤버 접속 정보';


CREATE TABLE `KT_PROJECT` (
  `idx_project` int(11) NOT NULL AUTO_INCREMENT COMMENT '인덱스',
  `idx_project_job_projectid` int(11) NOT NULL COMMENT 'KT_PROJECT_JOB_PROJECTID 인덱스',
  `project_name` varchar(100) NOT NULL COMMENT '프로젝트명',
  `project_type` tinyint(4) DEFAULT 0 COMMENT '프로젝트 형식 (0:임시, 1: 단일, 2:병합)',
  `project_status` tinyint(4) DEFAULT 0 COMMENT '프로젝트 상태 (0:임시 저장상태, 1: 파일 파싱이 정상적으로 되어서 정식 프로젝트로 저장됨, 2:병합이 되어버림, 10:병합 임시, 11: 병합완료되어정상)',
  `summary0` varchar(400) DEFAULT NULL COMMENT '프로젝트 내용',
  `idx_user` int(11) NOT NULL DEFAULT 0 COMMENT '등록자 인덱스',
  `create_dt` datetime DEFAULT current_timestamp() COMMENT '등록 일시정보',
  `update_dt` datetime DEFAULT NULL ON UPDATE current_timestamp() COMMENT '업데이트 일시정보',
  PRIMARY KEY (`idx_project`) USING BTREE,
  KEY `idx_project_job_projectid` (`idx_project_job_projectid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='프로젝트';


CREATE TABLE `KT_PROJECT_JOB` (
  `idx_project_job` int(11) NOT NULL AUTO_INCREMENT COMMENT '인덱스',
  `job_no` varchar(10) NOT NULL COMMENT 'JOB NO',
  PRIMARY KEY (`idx_project_job`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='프로젝트 JOB 데이터';


CREATE TABLE `KT_PROJECT_JOB_PROJECTID` (
  `idx_project_job_projectid` int(11) NOT NULL AUTO_INCREMENT COMMENT '인덱스',
  `idx_project_job` int(11) NOT NULL COMMENT 'KT_PROJECT_JOB 인덱스',
  `project_seq` int(11) DEFAULT 0 COMMENT '시퀀스',
  `project_id` varchar(10) DEFAULT NULL COMMENT '프로젝트ID 명',
  `filepath` varchar(50) DEFAULT NULL COMMENT '업로드 경로',
  `filename` varchar(100) DEFAULT NULL COMMENT '업로드 파일명',
  `idx_user` int(11) DEFAULT 0 COMMENT '등록자 인덱스',
  PRIMARY KEY (`idx_project_job_projectid`) USING BTREE,
  KEY `FK_KT_PROJECT_JOB_PROJECTID_KT_PROJECT_JOB` (`idx_project_job`) USING BTREE,
  CONSTRAINT `FK_KT_PROJECT_JOB_PROJECTID_KT_PROJECT_JOB` FOREIGN KEY (`idx_project_job`) REFERENCES `KT_PROJECT_JOB` (`idx_project_job`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='프로젝트 데이터 정보\n\nPROJECTID : REPORT\r\nKT_PROJECT와 직접 연계';


CREATE TABLE `KT_REPORT` (
  `idx_report` int(11) NOT NULL AUTO_INCREMENT COMMENT '인덱스',
  `idx_project` int(11) NOT NULL COMMENT 'KT_PROJECT 인덱스',
  `idx_project_job_projectid` int(11) NOT NULL COMMENT 'KT_PROJECT_JOB_PROJECTID 인덱스',
  `report_seq` int(11) DEFAULT 0 COMMENT '시퀀스',
  `report_id` varchar(10) DEFAULT NULL COMMENT '리포트 ID',
  `title` varchar(100) DEFAULT NULL COMMENT '리포트 명/이름',
  `filename` varchar(100) DEFAULT NULL,
  `filepath` varchar(50) DEFAULT NULL,
  `idx_filter` int(11) DEFAULT NULL COMMENT 'KT_REPORT_FILTER 인덱스',
  `summary0` varchar(4000) DEFAULT NULL COMMENT '메모',
  `d_count` int(11) DEFAULT 0 COMMENT '요약 데이터 집계 수',
  `d_count_total` int(11) DEFAULT 0 COMMENT '요약 데이터 집계 총 수',
  `idx_user` int(11) NOT NULL DEFAULT 0 COMMENT '등록자 인덱스',
  `create_dt` datetime DEFAULT current_timestamp() COMMENT '등록 일시정보',
  `update_dt` datetime DEFAULT NULL ON UPDATE current_timestamp() COMMENT '업데이트 일시정보',
  PRIMARY KEY (`idx_report`) USING BTREE,
  KEY `FK_KT_REPORT_KT_PROJECT` (`idx_project`),
  KEY `FK_KT_REPORT_KT_PROJECT_JOB_PROJECTID` (`idx_project_job_projectid`),
  CONSTRAINT `FK_KT_REPORT_KT_PROJECT` FOREIGN KEY (`idx_project`) REFERENCES `KT_PROJECT` (`idx_project`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='리포트 정보';


CREATE TABLE `KT_REPORT_DATA` (
  `idx_report_data` int(11) NOT NULL AUTO_INCREMENT COMMENT '인덱스',
  `idx_report` int(11) NOT NULL DEFAULT 0 COMMENT 'KT_REPORT 인덱스',
  `title` varchar(2000) DEFAULT NULL COMMENT '요약문 주제',
  `summary0` varchar(4000) DEFAULT NULL COMMENT '요약문',
  `idx_user` int(11) DEFAULT 0 COMMENT '등록자 인덱스',
  `create_dt` datetime DEFAULT current_timestamp() COMMENT '등록 일시정보',
  `update_dt` datetime DEFAULT NULL ON UPDATE current_timestamp() COMMENT '업데이트 일시정보',
  PRIMARY KEY (`idx_report_data`) USING BTREE,
  KEY `FK_KT_REPORT_DATA_KT_REPORT` (`idx_report`),
  CONSTRAINT `FK_KT_REPORT_DATA_KT_REPORT` FOREIGN KEY (`idx_report`) REFERENCES `KT_REPORT` (`idx_report`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='요약문';


CREATE TABLE `KT_REPORT_FILTER` (
  `idx_filter` int(11) NOT NULL AUTO_INCREMENT COMMENT '인덱스',
  `idx_project_job_projectid` int(11) NOT NULL COMMENT 'KT_PROJECT_JOB_PROJECTID 인덱스',
  `filter_title` varchar(50) NOT NULL COMMENT '필터명',
  `idx_user` int(11) NOT NULL COMMENT '등록자 인덱스',
  `create_dt` datetime DEFAULT current_timestamp() COMMENT '등록 일시정보',
  `update_dt` datetime DEFAULT NULL ON UPDATE current_timestamp() COMMENT '업데이트 일시정보',
  PRIMARY KEY (`idx_filter`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='리포트용 필터 관리';


CREATE TABLE `KT_REPORT_FILTER_DATA` (
  `idx_filter_data` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '인덱스',
  `idx_filter` int(11) NOT NULL COMMENT 'KT_REPORT_FILTER 인덱스',
  `filter_type` tinyint(4) DEFAULT 0 COMMENT '필터 형식 (0:없음,1:화자,2:챕터,3:서브챕터,4:질문)',
  `filter_data` varchar(4000) DEFAULT NULL COMMENT '필터 데이터',
  PRIMARY KEY (`idx_filter_data`),
  KEY `FK_KT_REPORT_FILTER_DATA_KT_REPORT_FILTER` (`idx_filter`),
  CONSTRAINT `FK_KT_REPORT_FILTER_DATA_KT_REPORT_FILTER` FOREIGN KEY (`idx_filter`) REFERENCES `KT_REPORT_FILTER` (`idx_filter`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='리포트용 필터 데이터';


CREATE TABLE `KT_REPORT_FILTER_METADATA` (
  `idx_report_metadata` int(11) NOT NULL AUTO_INCREMENT COMMENT '인덱스',
  `idx_report` int(11) NOT NULL COMMENT 'KT_REPORT 인덱스',
  `speaker` varchar(4000) DEFAULT NULL COMMENT '화자',
  `chapter` varchar(4000) DEFAULT NULL COMMENT '챕터',
  `word_length` bigint(20) NOT NULL COMMENT '문자 길이',
  `answer_cnt` int(11) NOT NULL COMMENT '답변 수',
  `create_dt` datetime DEFAULT current_timestamp() COMMENT '등록 일시정보',
  `update_dt` datetime DEFAULT NULL ON UPDATE current_timestamp() COMMENT '업데이트 일시정보',
  PRIMARY KEY (`idx_report_metadata`) USING BTREE,
  KEY `FK_KT_REPORT_METADATA_KT_REPORT` (`idx_report`) USING BTREE,
  CONSTRAINT `FK_KT_REPORT_METADATA_KT_REPORT` FOREIGN KEY (`idx_report`) REFERENCES `KT_REPORT` (`idx_report`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='리포트 필터적용 메타데이터';


CREATE TABLE `KT_REPORT_KEYWORD` (
  `idx_report_keyword` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '인덱스',
  `idx_report` int(11) NOT NULL DEFAULT 0 COMMENT 'KT_REPORT 인덱스',
  `keyword` varchar(100) NOT NULL COMMENT '키워드',
  `keytype` varchar(100) NOT NULL COMMENT '키워드 형식 (1:명사,2:형용사)',
  `keycount` int(11) DEFAULT 0 COMMENT '키워드 빈도수',
  `create_dt` datetime DEFAULT current_timestamp() COMMENT '등록 일시정보',
  `update_dt` datetime DEFAULT NULL ON UPDATE current_timestamp() COMMENT '업데이트 일시정보',
  PRIMARY KEY (`idx_report_keyword`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='키워드 데이터';


CREATE TABLE `KT_DICTIONARY` (
  `idx_dictionary` int(11) NOT NULL AUTO_INCREMENT COMMENT '인덱스',
  `title` varchar(50) NOT NULL COMMENT '사전 이름',
  `filepath` varchar(50) NOT NULL COMMENT '사전 파일 업로드 위치',
  `filename` varchar(50) NOT NULL COMMENT '사전 파일명',
  `dic_type` int(11) DEFAULT 0 COMMENT '사전형식 (0:기본사전,1:사용자사전)',
  `dic_count` int(11) DEFAULT 0 COMMENT '표제어 수',
  `idx_user` int(11) NOT NULL DEFAULT 0 COMMENT '등록자 인덱스',
  `create_dt` datetime DEFAULT current_timestamp() COMMENT '등록 일시정보',
  `update_dt` datetime DEFAULT NULL ON UPDATE current_timestamp() COMMENT '업데이트 일시정보',
  PRIMARY KEY (`idx_dictionary`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='사전';


CREATE TABLE `KT_DICTIONARY_DATA` (
  `idx_dictionary_data` int(11) NOT NULL AUTO_INCREMENT COMMENT '인덱스',
  `idx_dictionary` int(11) NOT NULL COMMENT 'KT_DICTIONARY 인덱스',
  `keyword` varchar(50) NOT NULL DEFAULT '' COMMENT '대표 키워드',
  `keyword01` varchar(50) DEFAULT NULL COMMENT '키워드01',
  `keyword02` varchar(50) DEFAULT NULL COMMENT '키워드02',
  `keyword03` varchar(50) DEFAULT NULL COMMENT '키워드03',
  `keyword04` varchar(50) DEFAULT NULL COMMENT '키워드04',
  `keyword05` varchar(50) DEFAULT NULL COMMENT '키워드05',
  `keyword06` varchar(50) DEFAULT NULL COMMENT '키워드06',
  `keyword07` varchar(50) DEFAULT NULL COMMENT '키워드07',
  `keyword08` varchar(50) DEFAULT NULL COMMENT '키워드08',
  `keyword09` varchar(50) DEFAULT NULL COMMENT '키워드09',
  `keyword10` varchar(50) DEFAULT NULL COMMENT '키워드10',
  `idx_user` int(11) NOT NULL DEFAULT 0 COMMENT '등록자 인덱스',
  `create_dt` datetime DEFAULT current_timestamp() COMMENT '등록 일시정보',
  `update_dt` datetime DEFAULT NULL ON UPDATE current_timestamp() COMMENT '업데이트 일시정보',
  PRIMARY KEY (`idx_dictionary_data`),
  KEY `FK_KT_DICTIONARY_DATA_KT_DICTIONARY` (`idx_dictionary`),
  CONSTRAINT `FK_KT_DICTIONARY_DATA_KT_DICTIONARY` FOREIGN KEY (`idx_dictionary`) REFERENCES `KT_DICTIONARY` (`idx_dictionary`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='키워드 데이터';


CREATE TABLE `KT_API_STATISTICS` (
  `idx_api_statistics` int(11) NOT NULL AUTO_INCREMENT COMMENT '인덱스',
  `idx_report` int(11) NOT NULL COMMENT 'KT_REPORT 인덱스',
  `report_status` tinyint(4) DEFAULT 1 COMMENT '파일 통계 데이터 상태 (1:사용, 99:삭제)',
  `summaryUsage` bigint(20) NOT NULL COMMENT '요약 사용량',
  `keywordUsage` bigint(20) NOT NULL COMMENT '키워드 사용량',
  `idx_user` int(11) DEFAULT NULL COMMENT '등록자 인덱스',
  `create_dt` datetime DEFAULT current_timestamp() COMMENT '등록 일시정보',
  `update_dt` datetime DEFAULT NULL ON UPDATE current_timestamp() COMMENT '업데이트 일시정보',
  PRIMARY KEY (`idx_api_statistics`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='API 사용 현황';


CREATE TABLE `KT_FILE_STATISTICS` (
  `idx_file_statistics` int(11) NOT NULL AUTO_INCREMENT COMMENT '인덱스',
  `idx_project` int(11) NOT NULL COMMENT 'KT_PROJECT 인덱스',
  `file_status` tinyint(4) DEFAULT 1 COMMENT '파일 통계 데이터 상태 (1:사용, 99:삭제)',
  `file_cnt` int(11) NOT NULL COMMENT '파일 수',
  `file_size` decimal(10,3) NOT NULL COMMENT '파일 용량',
  `word_length` bigint(20) NOT NULL COMMENT '텍스트 길이',
  `report_cnt` int(11) NOT NULL COMMENT '리포트 수',
  `idx_user` int(11) DEFAULT NULL COMMENT '등록자 인덱스',
  `create_dt` datetime DEFAULT current_timestamp() COMMENT '등록 일시정보',
  `update_dt` datetime DEFAULT NULL ON UPDATE current_timestamp() COMMENT '업데이트 일시정보',
  PRIMARY KEY (`idx_file_statistics`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='시스템 사용 현황';


CREATE TABLE `KT_WORDCLOUD` (
  `idx_wordcloud` int(11) NOT NULL AUTO_INCREMENT COMMENT '인덱스',
  `idx_project_job_projectid` int(11) NOT NULL COMMENT 'KT_PROJECT_JOB_PROJECTID 인덱스',
  `title` varchar(100) NOT NULL COMMENT '워드클라우드명',
  `idx_word_filter` int(11) NOT NULL COMMENT 'KT_WORDCLOUD_FILTER 인덱스',
  `idx_user` int(11) NOT NULL DEFAULT 0 COMMENT '등록자 인덱스',
  `create_dt` datetime DEFAULT current_timestamp() COMMENT '등록 일시정보',
  `update_dt` datetime DEFAULT NULL ON UPDATE current_timestamp() COMMENT '업데이트 일시정보',
  PRIMARY KEY (`idx_wordcloud`) USING BTREE,
  KEY `idx_project_job_projectid` (`idx_project_job_projectid`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='워드 클라우드';


CREATE TABLE `KT_WORDCLOUD_KEYWORD` (
  `idx_wordcloud_keyword` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '인덱스',
  `idx_wordcloud` bigint(20) NOT NULL COMMENT 'KT_WORDCLOUD 인덱스',
  `keyword` varchar(100) NOT NULL COMMENT '키워드',
  `keytype` varchar(100) NOT NULL COMMENT '키워드 형식',
  `keycount` int(11) DEFAULT 0 COMMENT '키워드 빈도수',
  `create_dt` datetime DEFAULT current_timestamp() COMMENT '등록 일시정보',
  `update_dt` datetime DEFAULT NULL ON UPDATE current_timestamp() COMMENT '업데이트 일시정보',
  PRIMARY KEY (`idx_wordcloud_keyword`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='워드 클라우드 키워드 정보';


CREATE TABLE `KT_WORDCLOUD_FILTER` (
  `idx_word_filter` int(11) NOT NULL AUTO_INCREMENT COMMENT '인덱스',
  `idx_project_job_projectid` int(11) NOT NULL COMMENT 'KT_PROJECT_JOB_PROJECTID 인덱스',
  `filter_title` varchar(50) NOT NULL COMMENT '필터명',
  `idx_user` int(11) NOT NULL DEFAULT 0 COMMENT '등록자 인덱스',
  `create_dt` datetime DEFAULT current_timestamp() COMMENT '등록 일시정보',
  `update_dt` datetime DEFAULT NULL ON UPDATE current_timestamp() COMMENT '업데이트 일시정보',
  PRIMARY KEY (`idx_word_filter`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='워드 클라우드 필터 정보';


CREATE TABLE `KT_WORDCLOUD_FILTER_DATA` (
  `idx_word_filter_data` int(11) NOT NULL AUTO_INCREMENT COMMENT '인덱스',
  `idx_word_filter` int(11) NOT NULL COMMENT 'KT_WORDCLOUD_FILTER 인덱스',
  `filter_type` tinyint(4) DEFAULT 0 COMMENT '필터 형식 (0:없음,1:화자,2:챕터,3:서브챕터,4:질문,5:키워드)',
  `filter_data` varchar(4000) DEFAULT NULL COMMENT '필터 데이터',
  PRIMARY KEY (`idx_word_filter_data`) USING BTREE,
  KEY `FK_KT_WORDCLOUD_FILTER_DATA_KT_WORDCLOUD_FILTER` (`idx_word_filter`) USING BTREE,
  CONSTRAINT `FK_KT_WORDCLOUD_FILTER_DATA_KT_WORDCLOUD_FILTER` FOREIGN KEY (`idx_word_filter`) REFERENCES `KT_WORDCLOUD_FILTER` (`idx_word_filter`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='워드 클라우드 필터 데이터';