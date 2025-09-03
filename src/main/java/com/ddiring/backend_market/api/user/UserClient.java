package com.ddiring.backend_market.api.user;

import com.ddiring.backend_market.common.dto.ApiResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;

@FeignClient(name = "userClient", url = "${user.base-url}")
public interface UserClient {

    @PostMapping("/api/user/detail")
    List<UserDTO> getUser(@RequestBody List<String> userSeq);

    @PostMapping("/api/user/sell/sign")
    ApiResponseDto<SignDto> sign(@RequestBody SignDto signDto);
}
