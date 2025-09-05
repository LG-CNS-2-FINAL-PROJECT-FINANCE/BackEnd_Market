package com.ddiring.backend_market.trade.service;

import com.ddiring.backend_market.api.asset.AssetClient;
import com.ddiring.backend_market.api.blockchain.dto.trade.Eip712DataDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Sign;
import org.web3j.crypto.StructuredDataEncoder;
import org.web3j.utils.Numeric;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignatureService {

    private final AssetClient assetClient;
    private final ObjectMapper objectMapper;

    public Sign.SignatureData signPermit(String userSeq, Eip712DataDto eip712Data) {
        String privateKey = null;
        try {
            privateKey = assetClient.getDecryptedPrivateKey(userSeq).getData();

            // --- 💡 디버깅을 위한 로그 추가 ---
            log.info("Asset 서비스로부터 받은 개인키: [{}]", privateKey);
            if (privateKey == null || !privateKey.startsWith("0x")) {
                log.error("개인키 형식이 올바르지 않습니다. '0x'로 시작해야 합니다.");
                throw new IllegalArgumentException("Invalid private key format");
            }
            // ------------------------------------

            ECKeyPair keyPair = ECKeyPair.create(Numeric.toBigInt(privateKey));

            String jsonData = objectMapper.writeValueAsString(Map.of(
                    "domain", eip712Data.getDomain(),
                    "types", eip712Data.getTypes(),
                    "message", eip712Data.getMessage(),
                    "primaryType", "Permit"
            ));

            StructuredDataEncoder dataEncoder = new StructuredDataEncoder(jsonData);
            byte[] messageHash = dataEncoder.hashStructuredData();
            return Sign.signMessage(messageHash, keyPair, false);

        } catch (Exception e) {
            log.error("서명 생성 중 오류 발생. userSeq: {}", userSeq, e); // 💡 상세 로그 추가
            throw new RuntimeException("EIP-712 서명 생성에 실패했습니다.", e);
        } finally {
            if (privateKey != null) privateKey = null;
        }
    }
}