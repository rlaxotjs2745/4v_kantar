package com.kantar.controller;

import com.kantar.base.BaseController;
import com.kantar.mapper.DictionaryMapper;
import com.kantar.mapper.UserMapper;
import com.kantar.model.CommonResult;
import com.kantar.service.DictionaryService;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/dict")
public class DictionaryController extends BaseController {
    @Autowired
    private final ResponseService responseService;
    @Autowired
    private final DictionaryMapper dictionaryMapper;
    @Autowired
    private final UserMapper userMapper;
    @Autowired
    private final DictionaryService dictionaryService;
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
    public CommonResult getDictionaryList(HttpServletRequest req, DictionaryVO paramVo) throws Exception {
        try{
            if(paramVo.getIdx_user() == null){
                return responseService.getFailResult("list_dictionary","사용자 인덱스가 입력되지 않았습니다.");
            }
            UserVO userVO = new UserVO();
            userVO.setIdx_user(paramVo.getIdx_user());
            UserVO userInfo = userMapper.getUserInfo(userVO);
            if(paramVo.getRecordCountPerPage() == 0){
                paramVo.setRecordCountPerPage(10);
            }
            if(paramVo.getCurrentPage() == 0){
                paramVo.setCurrentPage(1);
            }
            paramVo.setFilter(userInfo.getUser_type());
            paramVo.setFirstIndex(paramVo.getCurrentPage() * paramVo.getRecordCountPerPage() - (paramVo.getRecordCountPerPage() - 1));
            List<DictionaryVO> rs = dictionaryMapper.getDictionaryList(paramVo);
            if(rs != null){
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
    public CommonResult deleteDictionary(HttpServletRequest req, DictionaryVO paramVo) throws Exception {
        try {
            if(paramVo.getIdx_dictionary() == 0){
                return responseService.getFailResult("delete_dictionary","사전 인덱스 값이 없습니다.");
            }
            if(dictionaryMapper.getDictionary(paramVo) != null){
                dictionaryMapper.deleteDictionary(paramVo);
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
    public CommonResult create(MultipartHttpServletRequest req, DictionaryVO paramVo) throws Exception {

        try{
            if(StringUtils.isEmpty(paramVo.getTitle())){
                return responseService.getFailResult("dictionary_create","사전 이름을 입력해주세요.");
            }
            UserVO userVO = new UserVO();
            userVO.setIdx_user(paramVo.getIdx_user());
            UserVO userInfo = userMapper.getUserInfo(userVO);

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
                param.setFile_path(path);
                param.setIdx_user(userInfo.getIdx_user());

                dictionaryService.createDictionary(req, param);

                return responseService.getSuccessResult("dictionary_create", "사전 생성 성공");
            } else {
                return responseService.getFailResult("dictionary_create","사전 파일이 없습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("dictionary_create","오류가 발생하였습니다.");
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
    public CommonResult deleteDictionaryData(HttpServletRequest req, DictionaryDataVO paramVo) throws Exception {
        try {
            if(paramVo.getIdx_dictionary_data() == 0){
                return responseService.getFailResult("delete_dictionary_data","표제어 인덱스를 받지 못했습니다.");
            }
            DictionaryDataVO dictionaryDataVO = dictionaryMapper.getDictionaryData(paramVo);
            if(dictionaryDataVO == null){
                return responseService.getFailResult("delete_dictionary_data","이미 삭제된 표제어 이거나 없는 표제어입니다.");
            }
            dictionaryMapper.deleteDictionaryData(paramVo);
            return responseService.getSuccessResult("delete_dictionary_data", "표제어 삭제 성공");
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("delete_dictionary_data","오류가 발생하였습니다.");
        }
    }

    @PostMapping("/update_dictionary_data")
    public CommonResult updateDictionaryData(HttpServletRequest req, DictionaryDataVO paramVo) throws Exception {
        try {
            if(paramVo.getIdx_dictionary_data() == 0){
                return responseService.getFailResult("update_dictionary_data","표제어 인덱스를 받지 못했습니다.");
            }
            DictionaryDataVO dictionaryDataVO = dictionaryMapper.getDictionaryData(paramVo);
            if(dictionaryDataVO == null){
                return responseService.getFailResult("update_dictionary_data","삭제된 표제어 이거나 없는 표제어입니다.");
            }
            dictionaryMapper.updateDictionaryData(paramVo);
            return responseService.getSuccessResult("update_dictionary_data", "표제어 삭제 성공");
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.getFailResult("update_dictionary_data","오류가 발생하였습니다.");
        }
    }


}
