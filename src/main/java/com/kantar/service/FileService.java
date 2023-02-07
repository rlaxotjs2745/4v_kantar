package com.kantar.service;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.kantar.vo.ProjectVO;

@Service
public class FileService {
    @Value("${file.upload-dir}")
    public String filepath;

    /**
     * 파일 저장
     * @param req
     * @param _idx
     * @return
     * @throws Exception
     */
    public ProjectVO fileSave(MultipartHttpServletRequest req, String _path, String chkType) throws Exception {
        ProjectVO param = new ProjectVO();
        try {
            List<MultipartFile> fileList = req.getFiles("file");
            if(fileList.size()>0){
                for(MultipartFile mf : fileList) {
                    if(mf.getSize()>0){
                        String fname = mf.getOriginalFilename();
                        String ext = FilenameUtils.getExtension(fname);

                        String contentType = mf.getContentType();

                        if(chkType.equals("xls")){
                            if (!ext.equals("xlsx") && !ext.equals("xls")) {
                                return param;
                            }
                            if(!contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") && !contentType.equals("application/vnd.ms-excel")) {
                                return param;
                            }
                        }

                        if(chkType.equals("csv")){
                            if (!ext.equals("csv")) {
                                return param;
                            }
                            if(!contentType.equals("text/csv")) {
                                return param;
                            }
                        }

                        String path = "/report/" + _path + "/";
                        String fullpath = this.filepath + path;
                        File fileDir = new File(fullpath);
                        if (!fileDir.exists()) {
                            fileDir.mkdirs();
                        }

                        param.setFilename(fname);
                        param.setFilepath(path);
                        try { // 파일생성
                            mf.transferTo(new File(fullpath, fname));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return param;
    }

    /**
     * 파일 다운로드
     * @param fileName
     * @return ResponseEntity<InputStreamResource>
     * @throws Exception
     */
    public ResponseEntity<Object> getFileDown(String fileName) throws Exception {
        try {
            File file = new File(fileName);
            // Path fPath = Paths.get(file.getAbsolutePath());

            if(!file.exists()) {
                return null;
            }

            String _fileName = file.getName();
            // String ext = _fileName.substring(_fileName.lastIndexOf(".") + 1);
            HttpHeaders header = new HttpHeaders();

            header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+_fileName);
            header.add("Cache-Control", "no-cache, no-store, must-revalidate");
            header.add("Pragma", "no-cache");
            header.add("Expires", "0");
            
            InputStreamResource resource3 = new InputStreamResource(new FileInputStream(file));
            
            return ResponseEntity.ok()
                    .headers(header)
                    .contentLength(file.length())
                    .contentType(MediaType.parseMediaType("application/octet-stream"))
                    .body(resource3);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
