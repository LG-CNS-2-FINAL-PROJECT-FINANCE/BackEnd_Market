package com.ddiring.backend_market.api.client;

import com.ddiring.backend_market.api.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "user")
public interface UserClient {

    @PostMapping("/api/user/list")
    List<UserDTO> getUser(@RequestBody List<Integer> userSeq);
}
