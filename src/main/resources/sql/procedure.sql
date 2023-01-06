CREATE DEFINER=`kantar`@`%` PROCEDURE `prc_savProjectInfo`(
	IN `_idx_user` INT,
	IN `_job_no` VARCHAR(10),
	IN `_project_name` VARCHAR(100),
	IN `_summary` VARCHAR(400),
	IN `_fpath` VARCHAR(50),
	IN `_fname` VARCHAR(100)
)
LANGUAGE SQL
NOT DETERMINISTIC
CONTAINS SQL
SQL SECURITY DEFINER
COMMENT ''
BEGIN
	DECLARE M_ERR INT DEFAULT 0;
	DECLARE CONTINUE HANDLER FOR SQLEXCEPTION SET M_ERR = -1;
	SET @IDX_PROJECT_JOB = 0;
	SET @IDX_PROJECT_JOB_PROJECTID = 0;
	SET @IDX_PROJECT = 0;
	
	START TRANSACTION;
		IF EXISTS(SELECT job_no FROM KT_PROJECT_JOB WHERE job_no=_job_no) THEN
			INSERT INTO KT_PROJECT_JOB (job_no) VALUES (_job_no);
			SELECT LAST_INSERT_ID() INTO @IDX_PROJECT_JOB;
		ELSE
			SELECT job_no INTO @IDX_PROJECT_JOB FROM KT_PROJECT_JOB WHERE job_no=_job_no;
		END IF;
		
		INSERT INTO KT_PROJECT_JOB_PROJECTID (idx_project_job, project_id, idx_user) VALUES (@IDX_PROJECT_JOB, _project_id, _idx_user);
		SELECT LAST_INSERT_ID() INTO @IDX_PROJECT_JOB_PROJECTID;
		
		INSERT INTO KT_PROJECT (idx_project_job_projectid, project_name, summary0, idx_user) VALUES (@IDX_PROJECT_JOB_PROJECTID, _project_name, _summary, _idx_user);
		SELECT LAST_INSERT_ID() INTO @IDX_PROJECT;
	
		-- INSERT INTO KT_REPORT (idx_project, idx_project_job_projectid, filepath, `filename`, idx_user) VALUES (@IDX_PROJECT, @IDX_PROJECT_JOB_PROJECTID, _fpath, _fname, _idx_user);

   IF M_ERR > 0 THEN
		ROLLBACK;
	ELSE
		COMMIT;
	END IF;


	SELECT @IDX_PROJECT AS idx_project, @IDX_PROJECT_JOB_PROJECTID AS idx_project_job_projectid;

END