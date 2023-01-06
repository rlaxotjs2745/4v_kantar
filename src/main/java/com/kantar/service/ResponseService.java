package com.kantar.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.kantar.model.CommonResult;
import com.kantar.model.ListResult;
import com.kantar.model.SingleResult;

@Service
public class ResponseService {
    public enum CommonResponse {
        SUCCESS("1", "성공하였습니다.");

        String code;
        String msg;

        CommonResponse(String code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public String getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }

    // 성공 결과만 처리하는 메소드
    public CommonResult getSuccessResult() {
        CommonResult result = new CommonResult();
        setSuccessResult(result);
        return result;
    }
    public CommonResult getSuccessResult(String code, String msg) {
        CommonResult result = new CommonResult();
        result.setSuccess("1");
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }
    public <T> CommonResult getSuccessResult(T data, String code, String msg) {
        SingleResult<T> result = new SingleResult<>();
        result.setSuccess("1");
        result.setData(data);
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }
    public <T> CommonResult getSuccessResult(List<T> data, String code, String msg) {
        ListResult<T> result = new ListResult<>();
        result.setSuccess("1");
        result.setData(data);
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }
    public <T> CommonResult getSuccessResult(List<T> data) {
        ListResult<T> result = new ListResult<>();
        result.setData(data);
        setSuccessResult(result);
        return result;
    }
    public <T> CommonResult getSuccessResult(ArrayList<T> data) {
        ListResult<T> result = new ListResult<>();
        result.setData(data);
        setSuccessResult(result);
        return result;
    }
    public <T> CommonResult getSuccessResult(T data) {
        SingleResult<T> result = new SingleResult<>();
        result.setData(data);
        setSuccessResult(result);
        return result;
    }

    // 실패 결과만 처리하는 메소드
    public CommonResult getFailResult(String code, String msg) {
        CommonResult result = new CommonResult();
        result.setSuccess("0");
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

    // 결과 모델에 api 요청 성공 데이터를 세팅해주는 메소드
    private void setSuccessResult(CommonResult result) {
        result.setSuccess("1");
        result.setCode(CommonResponse.SUCCESS.getCode());
        result.setMsg(CommonResponse.SUCCESS.getMsg());
    }
}
