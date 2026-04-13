package com.storm.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

public class CommonUser {

    @Data
    @Schema(description = "用户实体")
    public class User {

        @Schema(description = "用户ID")
        private String userId;

        @Schema(description = "用户名")
        private String username;

        @Schema(description = "显示名称")
        private String displayName;

        @Schema(description = "角色")
        private String role;
    }
}
