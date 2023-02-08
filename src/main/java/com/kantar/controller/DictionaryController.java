package com.kantar.controller;

import com.kantar.base.BaseController;
import com.kantar.mapper.DictionaryMapper;
import com.kantar.mapper.UserMapper;
import com.kantar.model.CommonResult;
import com.kantar.service.DictionaryService;
import com.kantar.service.FileService;
import com.kantar.service.ResponseService;
import com.kantar.util.Excel;
import com.kantar.vo.DictionaryDataVO;
import com.kantar.vo.DictionaryVO;
import com.kantar.vo.ProjectVO;
import com.kantar.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.File;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// @RequiredArgsConstructor
@RestController
@RequestMapping("/api/dict")
public class DictionaryController extends BaseController {
    @Autowired
    private ResponseService responseService;
    @Autowired
    private DictionaryMapper dictionaryMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DictionaryService dictionaryService;
    @Autowired
    private FileService fileService;
    @Value("${file.upload-dir}")
    public String filepath;
    @Autowired
    private Excel excel;

    /**
     * 사전 관리 리스트(페이징)
     * @param paramVo
     * @return CommonResult
     * @throws Exception
     */
    @GetMapping("/list_dictionary")
    @Transactional
    public CommonResult getDictionaryList(HttpServletRequest req, DictionaryVO paramVo) throws Exception {
        try{
            UserVO uinfo = getChkUserLogin(req);
            if(uinfo == null){
                return responseService.getFailResult("list_dictionary","로그인이 필요합니다.");
            }
            UserVO userInfo = userMapper.getUserInfo(uinfo);
            if(userInfo == null){
                return responseService.getFailResult("list_dictionary","유저 정보를 찾을 수 없습니다.");
            }
            paramVo.setIdx_user(userInfo.getIdx_user());
            if( paramVo.getRecordCountPerPage() == null || paramVo.getRecordCountPerPage() == 0){
                paramVo.setRecordCountPerPage(10);
            }
            if(paramVo.getCurrentPage() == null || paramVo.getCurrentPage() == 0){
                paramVo.setCurrentPage(0);
            }
            paramVo.setFilter(userInfo.getUser_type());
            paramVo.setFirstIndex(paramVo.getCurrentPage() * paramVo.getRecordCountPerPage());
            Map<String, Object> rs = new HashMap<>();
            List<DictionaryVO> dictList = dictionaryMapper.getDictionaryList(paramVo);
            if(dictList != null){
                rs.put("dictList", dictList);
                rs.put("idx_user", uinfo.getIdx_user());
                rs.put("user_type", uinfo.getRole_type());
                return responseService.getSuccessResult(rs, "list_dictionary", "사전 리스팅 성공");
            }else{
                return responseService.getFailResult("list_dictionary","사전 리스트가 없습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("list_dictionary","오류가 발생하였습니다.");
        }
    }



    @PostMapping("/delete_dictionary")
    @Transactional
    public CommonResult deleteDictionary(HttpServletRequest req, @RequestBody DictionaryVO paramVo) throws Exception {
        try {
            UserVO uinfo = getChkUserLogin(req);
            if(uinfo == null){
                return responseService.getFailResult("delete_dictionary","로그인이 필요합니다.");
            }
            if(paramVo.getIdx_dictionary() == null || paramVo.getIdx_dictionary() == 0){
                return responseService.getFailResult("delete_dictionary","사전 인덱스 값이 없습니다.");
            }
            DictionaryVO dictionaryVO = dictionaryMapper.getDictionary(paramVo);
            if(dictionaryVO != null){
                if(uinfo.getRole_type() == 1){
                    if(dictionaryVO.getIdx_user() != uinfo.getIdx_user() || dictionaryVO.getDic_type() == 0){
                        return responseService.getFailResult("delete_dictionary","삭제 권한이 없습니다.");
                    }
                }
                if(uinfo.getRole_type() == 11){
                    if(dictionaryVO.getDic_type() == 1 && dictionaryVO.getIdx_user() != uinfo.getIdx_user()){
                        return responseService.getFailResult("delete_dictionary","삭제 권한이 없습니다.");
                    }
                }
                dictionaryMapper.deleteDictionary(paramVo);
                dictionaryMapper.deleteDictionaryDataToDictionaryIdx(paramVo);
                return responseService.getSuccessResult("delete_dictionary", "사전 삭제 성공");
            } else {
                return responseService.getFailResult("delete_dictionary","이미 삭제된 사전이거나, 없는 사전입니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("delete_dictionary","오류가 발생하였습니다.");
        }
    }

    @PostMapping("/create")
    @Transactional
    public CommonResult create(MultipartHttpServletRequest req, DictionaryVO paramVo) throws Exception {
        try{
            UserVO uinfo = getChkUserLogin(req);
            if(uinfo == null){
                return responseService.getFailResult("dictionary_create","로그인이 필요합니다.");
            }
            if(StringUtils.isEmpty(paramVo.getTitle())){
                return responseService.getFailResult("dictionary_create","사전 이름을 입력해주세요.");
            }

            List<DictionaryVO> findTitle = dictionaryMapper.getDictionaryByTitle(paramVo.getTitle());
            if(findTitle != null && findTitle.size() != 0){
                return responseService.getFailResult("dictionary_create","이미 존재하는 사전 이름입니다. 확인 후 다시 시도해주세요.");
            }

            UserVO userInfo = userMapper.getUserInfo(uinfo);
            if(userInfo == null){
                return responseService.getFailResult("dictionary_create","유저 정보를 찾을 수 없습니다.");
            }

            paramVo.setIdx_user(userInfo.getIdx_user());

            List<MultipartFile> fileList = req.getFiles("file");

            if(fileList.size()>0) {
                MultipartFile mf = fileList.get(0);
                if (mf.getSize() > 0) {
                    String fname = mf.getOriginalFilename();
                    String ext = FilenameUtils.getExtension(fname);
                    String contentType = mf.getContentType();
                    if (!ext.equals("csv")) {
                        return responseService.getFailResult("dictionary_create", ".csv 포맷 파일이 맞는지 확인 후 다시 업로드를 시도해주세요.");
                    }
                    if (!contentType.equals("text/csv")) {
                        return responseService.getFailResult("dictionary_create", ".csv 포맷 파일이 맞는지 확인 후 다시 업로드를 시도해주세요.");
                    }
                }
                String path = "/dictionary/" + paramVo.getTitle() + "/";
                String fullpath = this.filepath + path;
                File fileDir = new File(fullpath);
                if (!fileDir.exists()) {
                    fileDir.mkdirs();
                }

                String originFileName = mf.getOriginalFilename();
                mf.transferTo(new File(fullpath, originFileName));

                DictionaryVO param = new DictionaryVO();

                param.setTitle(paramVo.getTitle());
                param.setDic_type(userInfo.getUser_type() == 1 ? 1 : 0);
                param.setFilename(originFileName);
                param.setFilepath(path);
                param.setIdx_user(userInfo.getIdx_user());

                dictionaryService.createDictionary(req, param);

                return responseService.getSuccessResult("dictionary_create", "사전 생성 성공");
            } else {
                return responseService.getFailResult("dictionary_create","사전 파일이 없습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("dictionary_create","사전 파일 내 중복되는 키워드가 있습니다. 확인 후 다시 시도해주세요.");
        }
    }

    /**
     * 사전 관리 리스트(페이징)
     * @param paramVo
     * @return CommonResult
     * @throws Exception
     */
    @GetMapping("/dictionary_detail")
    public CommonResult getDictionaryDetail(HttpServletRequest req, DictionaryVO paramVo) throws Exception {
        try {
            DictionaryVO dictionaryVO = dictionaryMapper.getDictionary(paramVo);
            if(dictionaryVO == null){
                return responseService.getFailResult("dictionary_detail","해당 사전을 찾을 수 없습니다.");
            }
            List<DictionaryDataVO> dictDataList = dictionaryMapper.getDictionaryDataList(paramVo);
            if(dictDataList == null){
                return responseService.getFailResult("dictionary_detail","해당 사전의 내용을 찾을 수 없습니다.");
            }
            Map<String, Object> _rdata = new HashMap<String, Object>();
            _rdata.put("title", dictionaryVO.getTitle());
            _rdata.put("dictDataList", dictDataList);
            return responseService.getSuccessResult(_rdata,"dictionary_detail", "사전 수정하기 정보 불러오기 성공");
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("dictionary_detail","오류가 발생하였습니다.");
        }
    }

    @PostMapping("/delete_dictionary_data")
    public CommonResult deleteDictionaryData(HttpServletRequest req, @RequestBody DictionaryDataVO paramVo) throws Exception {
        try {
            UserVO uinfo = getChkUserLogin(req);
            if(uinfo == null){
                return responseService.getFailResult("delete_dictionary_data","유저 정보를 찾을 수 없습니다.");
            }
            if(paramVo.getIdx_dictionary_data() == 0){
                return responseService.getFailResult("delete_dictionary_data","키워드 그룹 인덱스를 받지 못했습니다.");
            }

            DictionaryDataVO dictionaryDataVO = dictionaryMapper.getDictionaryData(paramVo);
            DictionaryVO param = new DictionaryVO();
            param.setIdx_dictionary(dictionaryDataVO.getIdx_dictionary());
            DictionaryVO dictionaryVO = dictionaryMapper.getDictionary(param);

            if(dictionaryVO == null){
                return responseService.getFailResult("delete_dictionary_data","이미 삭제된 사전입니다.");
            }
            if(dictionaryDataVO == null){
                return responseService.getFailResult("delete_dictionary_data","이미 삭제된 키워드 그룹 이거나 없는 키워드 그룹입니다.");
            }
            if(uinfo.getRole_type() == 1){
                if(dictionaryDataVO.getIdx_user() != uinfo.getIdx_user() || dictionaryVO.getDic_type() == 0){
                    return responseService.getFailResult("delete_dictionary_data","삭제 권한이 없습니다.");
                }
            }
            if(uinfo.getRole_type() == 11){
                if(dictionaryVO.getDic_type() != 1 && dictionaryVO.getIdx_user() != uinfo.getIdx_user()){
                    return responseService.getFailResult("delete_dictionary_data","삭제 권한이 없습니다.");
                }
            }
            dictionaryMapper.deleteDictionaryData(paramVo);
            return responseService.getSuccessResult("delete_dictionary_data", "키워드 그룹 삭제 성공");
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("delete_dictionary_data","오류가 발생하였습니다.");
        }
    }

    @PostMapping("/update_dictionary_data")
    public CommonResult updateDictionaryData(HttpServletRequest req, @RequestBody List<DictionaryDataVO> paramVo) throws Exception {
        try {
            UserVO uinfo = getChkUserLogin(req);
            if(uinfo == null){
                return responseService.getFailResult("update_dictionary_data","유저 정보를 찾을 수 없습니다.");
            }
            DictionaryVO paramDict = new DictionaryVO();
            paramDict.setIdx_dictionary(paramVo.get(0).getIdx_dictionary());
            DictionaryVO dictionaryVO = dictionaryMapper.getDictionary(paramDict);

            if(dictionaryVO.getDic_type() == 1){
                if(uinfo.getIdx_user() != dictionaryVO.getIdx_user()){
                    if(uinfo.getRole_type() != 99){
                        return responseService.getFailResult("update_dictionary_data","수정 권한이 없습니다.");
                    }
                }
            } else {
                if(uinfo.getRole_type() == 1){
                    return responseService.getFailResult("update_dictionary_data","수정 권한이 없습니다.");
                }
            }

            for(DictionaryDataVO param: paramVo){
                if(param.getIdx_dictionary_data() == null || param.getIdx_dictionary_data() == 0){
                    if(param.getKeyword() == null){
                        return responseService.getFailResult("update_dictionary_data","대표 키워드는 필수 입력값입니다. 확인 후 다시 저장해 주세요.");
                    }
                    List<DictionaryDataVO> findKeyword = dictionaryMapper.getDictionaryDataByKeyword(param.getKeyword(), param.getIdx_dictionary());
                    if(!findKeyword.isEmpty()){
                        return responseService.getFailResult("update_dictionary_data","이미 존재하는 키워드입니다.");
                    }
                    param.setIdx_user(uinfo.getIdx_user());
                    dictionaryMapper.insertDictionaryData(param);
                } else {
                    if(param.getFilter() != null && param.getFilter() == 1){
                        DictionaryDataVO dictionaryDataVO = dictionaryMapper.getDictionaryData(param);
                        if(dictionaryDataVO == null){
                            return responseService.getFailResult("update_dictionary_data","삭제된 키워드 그룹 이거나 없는 키워드 그룹입니다.");
                        }
                        param.setIdx_user(uinfo.getIdx_user());
                        dictionaryMapper.updateDictionaryData(param);
                    }
                }
            }
            return responseService.getSuccessResult("update_dictionary_data", "키워드 그룹 업데이트 성공");
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("update_dictionary_data","오류가 발생하였습니다.");
        }
    }

    @PostMapping("/get_bulk_dictionary_data")
    public CommonResult getBulkDictionaryData(HttpServletRequest req, @RequestBody List<Integer> paramVo) throws Exception {
        try {
            UserVO uinfo = getChkUserLogin(req);
            if(uinfo == null){
                return responseService.getFailResult("get_bulk_dictionary_data","유저 정보를 찾을 수 없습니다.");
            }
            List<DictionaryDataVO> dictionaryDataVOList = dictionaryMapper.getBulkDictionaryData(paramVo);
            if(dictionaryDataVOList != null && !dictionaryDataVOList.isEmpty()){
                return responseService.getSuccessResult(dictionaryDataVOList, "get_bulk_dictionary_data", "사전 키워드 불러오기 성공");
            } else {
                return responseService.getFailResult("get_bulk_dictionary_data","불러올 수 있는 키워드가 없습니다.");
            }
        } catch (Exception e){
            e.printStackTrace();
            return responseService.getFailResult("get_bulk_dictionary_data","오류가 발생하였습니다.");
        }
    }

    /**
     * 사전 다운로드
     * @param req
     * @param paramVo
     * @return ResponseEntity<Resource>
     * @throws Exception
     */
    @GetMapping("/download")
    public ResponseEntity<Object> getDictDown(HttpServletRequest req, DictionaryVO paramVo) throws Exception {
        try {
            UserVO uinfo = getChkUserLogin(req);
            if(uinfo==null){
                return null;
            }
            paramVo.setIdx_user(uinfo.getIdx_user());

            if(StringUtils.isEmpty(paramVo.getIdx_dictionary()+"")){
                return null;
            }

            String _fpath = this.filepath;

            if(paramVo.getDic_type() != null && paramVo.getDic_type() == 11111){
                _fpath += "/dictionary/sample_kantar.csv";
            } else {
                DictionaryVO rs = dictionaryMapper.getDictDown(paramVo);
                if(rs==null){
                    return null;
                }
                _fpath += rs.getFilepath() + rs.getFilename();
            }

            return fileService.getFileDown(_fpath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
