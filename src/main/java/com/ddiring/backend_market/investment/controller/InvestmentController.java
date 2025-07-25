package com.ddiring.backend_market.investment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/market/invest", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class InvestmentController {
}
