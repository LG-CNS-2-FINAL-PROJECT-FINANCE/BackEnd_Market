package com.ddiring.backend_market.trade.service;

import com.ddiring.backend_market.api.asset.AssetClient;
import com.ddiring.backend_market.api.blockchain.dto.trade.Eip712DataDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Sign;
import org.web3j.crypto.StructuredDataEncoder;
import org.web3j.utils.Numeric;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SignatureService {

    private final AssetClient assetClient;
    private final ObjectMapper objectMapper;

    public Sign.SignatureData signPermit(String userSeq, Eip712DataDto eip712Data) {
        String privateKey = null;
        try {
            // 1. Asset 서비스에서 개인키를 가져옵니다.
            privateKey = assetClient.getDecryptedPrivateKey(userSeq).getData();
            ECKeyPair keyPair = ECKeyPair.create(Numeric.toBigInt(privateKey));

            // 2. 받아온 데이터를 서명을 위해 JSON 문자열로 변환합니다.
            String jsonData = objectMapper.writeValueAsString(Map.of(
                    "domain", eip712Data.getDomain(),
                    "types", eip712Data.getTypes(),
                    "message", eip712Data.getMessage(),
                    "primaryType", "Permit"
            ));

            // 3. EIP-712 데이터를 해싱하고, 그 해시를 서명합니다.
            StructuredDataEncoder dataEncoder = new StructuredDataEncoder(jsonData);
            byte[] messageHash = dataEncoder.hashStructuredData();
            return Sign.signMessage(messageHash, keyPair, false);

        } catch (Exception e) {
            throw new RuntimeException("EIP-712 서명 생성에 실패했습니다.", e);
        } finally {
            if (privateKey != null) privateKey = null; // 메모리에서 개인키 참조 제거
        }
    }
}