package com.ddiring.backend_market.api.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

    private String userSeq;
    private String userName;
    private String nickname;
}
