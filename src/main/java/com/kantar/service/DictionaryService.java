package com.kantar.service;

import com.kantar.mapper.DictionaryMapper;
import com.kantar.util.Excel;
import com.kantar.vo.DictionaryDataVO;
import com.kantar.vo.DictionaryVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class DictionaryService {

    @Autowired
    private DictionaryMapper dictionaryMapper;

    @Autowired
    private Excel excel;

    @Value("${file.upload-dir}")
    public String filepath;

    @Value("${spring.smr.token}")
    public String smrtoken;


    /**
     * 비동기 사전 만들기
     * @param paramVo
     * @throws Exception
     */
    @Async
    @Transactional
    public void createDictionary(HttpServletRequest req, DictionaryVO paramVo) throws Exception{
        try{
            String _fpath = this.filepath + paramVo.getFilepath() + paramVo.getFilename();

            List<String[]> ers = excel.getCsvListData(_fpath);
            paramVo.setDic_count(ers.size());
            dictionaryMapper.insertDictionary(paramVo);

            Integer dictIdx = dictionaryMapper.getDictionaryByTitle(paramVo.getTitle()).get(0).getIdx_dictionary();
            // int i = 0;
            // for(int i = 1; i < ers.size(); i++){
            for(String[] _erss : ers){
                // if(i>0){
                    // String[] _erss = ers.get(i);
                    DictionaryDataVO dictionaryDataVO = new DictionaryDataVO();
                    dictionaryDataVO.setIdx_dictionary(dictIdx);
                    dictionaryDataVO.setIdx_user(paramVo.getIdx_user());
                    int keywordNum = 0;
                    for(String _rss: _erss){
                        if(keywordNum == 0){
                            List<DictionaryDataVO> findKeyword = dictionaryMapper.getDictionaryDataByKeyword(_rss, dictIdx);
                            if(!findKeyword.isEmpty()){
                                throw new Exception();
                            }
                        }
                        switch (keywordNum){
                            case 0 : dictionaryDataVO.setKeyword(_rss); break;
                            case 1 : dictionaryDataVO.setKeyword01(_rss); break;
                            case 2 : dictionaryDataVO.setKeyword02(_rss); break;
                            case 3 : dictionaryDataVO.setKeyword03(_rss); break;
                            case 4 : dictionaryDataVO.setKeyword04(_rss); break;
                            case 5 : dictionaryDataVO.setKeyword05(_rss); break;
                            case 6 : dictionaryDataVO.setKeyword06(_rss); break;
                            case 7 : dictionaryDataVO.setKeyword07(_rss); break;
                            case 8 : dictionaryDataVO.setKeyword08(_rss); break;
                            case 9 : dictionaryDataVO.setKeyword09(_rss); break;
                            case 10 : dictionaryDataVO.setKeyword10(_rss); break;
                            default: break;
                        }
                        keywordNum++;
                    }
                    dictionaryMapper.insertDictionaryData(dictionaryDataVO);
                // }
                // i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
