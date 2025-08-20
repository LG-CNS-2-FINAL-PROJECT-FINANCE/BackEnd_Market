package com.ddiring.backend_market.api.user;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;

// TODO: 병합 시 주소 맞춰야함
@FeignClient(name = "user")
public interface UserClient {

    @PostMapping("/api/user/detail")
    List<UserDTO> getUser(@RequestBody List<String> userSeq);
}
