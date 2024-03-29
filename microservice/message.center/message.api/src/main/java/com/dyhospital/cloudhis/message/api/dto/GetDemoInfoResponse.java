package com.dyhospital.cloudhis.message.api.dto;

import com.dyhospital.cloudhis.message.api.bo.DemoBo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author qinsf
 * @date 2019-4-23 16:48:06
 */
@Data
@AllArgsConstructor
@ApiModel(description= "查询demo响应")
public class GetDemoInfoResponse {
    @ApiModelProperty(value = "demo信息")
    private DemoBo demoBo;
}
