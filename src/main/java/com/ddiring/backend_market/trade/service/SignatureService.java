package com.ddiring.backend_market.trade.service;

import com.ddiring.backend_market.api.asset.AssetClient;
import com.ddiring.backend_market.api.blockchain.dto.signature.PermitSignatureDto;
import com.ddiring.backend_market.api.blockchain.dto.trade.Eip712DataDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Sign;
import org.web3j.crypto.StructuredDataEncoder;
import org.web3j.utils.Numeric;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignatureService {

    private final AssetClient assetClient;
    private final ObjectMapper objectMapper;

    public Sign.SignatureData signPermit(String userSeq, PermitSignatureDto.Response eip712Data) {
        String privateKey = null;
        try {
            privateKey = assetClient.getDecryptedPrivateKey(userSeq).getData();
            if (privateKey != null) {
                privateKey = privateKey.trim();
            }

            log.info("Asset 서비스로부터 받은 개인키: [{}]", privateKey);
            if (privateKey == null || !privateKey.startsWith("0x")) {
                throw new IllegalArgumentException("개인키 형식이 올바르지 않습니다.");
            }

            ECKeyPair keyPair = ECKeyPair.create(Numeric.toBigInt(privateKey));

            // 1. blockchain 서비스에서 받은 types를 복사하여 수정 가능한 Map을 만듭니다.
            HashMap<String, Object> typesWithDomain = new HashMap<>(eip712Data.getTypes());

            // 2. web3j가 요구하는 EIP712Domain 타입 정의를 추가하여 NullPointerException을 해결합니다.
            typesWithDomain.put("EIP712Domain", List.of(
                    Map.of("name", "name", "type", "string"),
                    Map.of("name", "version", "type", "string"),
                    Map.of("name", "chainId", "type", "uint256"),
                    Map.of("name", "verifyingContract", "type", "address")
            ));

            // 3. 서명을 위해 DTO 객체를 Map으로 변환합니다.
            Map<String, Object> domainMap = objectMapper.convertValue(eip712Data.getDomain(), Map.class);
            Map<String, Object> messageMap = objectMapper.convertValue(eip712Data.getMessage(), Map.class);

            String jsonData = objectMapper.writeValueAsString(Map.of(
                    "domain", domainMap,
                    "types", typesWithDomain,
                    "message", messageMap,
                    "primaryType", "Permit"
            ));

            StructuredDataEncoder dataEncoder = new StructuredDataEncoder(jsonData);
            byte[] messageHash = dataEncoder.hashStructuredData();
            return Sign.signMessage(messageHash, keyPair, false);

        } catch (Exception e) {
            log.error("서명 생성 중 오류 발생. userSeq: {}", userSeq, e);
            throw new RuntimeException("EIP-712 서명 생성에 실패했습니다.", e);
        } finally {
            if (privateKey != null) privateKey = null;
        }
    }
}