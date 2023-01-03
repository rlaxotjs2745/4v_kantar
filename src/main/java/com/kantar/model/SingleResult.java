package com.kantar.model;

import lombok.*;

@Getter
@Setter
public class SingleResult<T> extends CommonResult {
    private T list;
}