package com.kantar.service;

import java.io.File;
import java.nio.file.*;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
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
                        // Path savePath = Paths.get(this.filepath + fname);
                        // Files.probeContentType(savePath).startsWith("image");

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
                    }
                }

                if(req.getFiles("file").get(0).getSize() != 0){
                    fileList = req.getFiles("file");

                    
                }
                String path = "/report/" + _path + "/";
                String fullpath = this.filepath + path;
                File fileDir = new File(fullpath);
                if (!fileDir.exists()) {
                    fileDir.mkdirs();
                }

                for(MultipartFile mf : fileList) {
                    String originFileName = mf.getOriginalFilename();   // 원본 파일 명
                    param.setFilename(originFileName);
                    param.setFilepath(path);
                    try { // 파일생성
                        mf.transferTo(new File(fullpath, originFileName));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
        }

        return param;
    }
}
