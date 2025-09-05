package com.ddiring.backend_market.trade.service;

import com.ddiring.backend_market.api.asset.AssetClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Sign;
import org.web3j.crypto.StructuredDataEncoder;
import org.web3j.utils.Numeric;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SignatureService {

    private final AssetClient assetClient;
    private final ObjectMapper objectMapper;

    @Value("${blockchain.contract.address}")
    private String verifyingContractAddress;

    @Value("${blockchain.chain.id}")
    private long chainId;

    @Value("${blockchain.contract.name}")
    private String contractName;

    @Value("${blockchain.contract.version}")
    private String contractVersion;

    @Value("${blockchain.contract.spenderAddress}")
    private String spenderAddress;

    public Sign.SignatureData createAndSignPermit(String userSeq, String tokenAmount, long nonce) {
        String ownerAddress = assetClient.getWalletAddress(userSeq).getData();
        long deadline = Instant.now().getEpochSecond() + 1800; // 30분 후 만료

        Map<String, Object> domain = Map.of("name", contractName, "version", contractVersion, "chainId", chainId, "verifyingContract", verifyingContractAddress);
        Map<String, Object> types = Map.of("Permit", List.of(
                Map.of("name", "owner", "type", "address"),
                Map.of("name", "spender", "type", "address"),
                Map.of("name", "value", "type", "uint256"),
                Map.of("name", "nonce", "type", "uint256"),
                Map.of("name", "deadline", "type", "uint256")
        ));
        Map<String, Object> message = Map.of("owner", ownerAddress, "spender", spenderAddress, "value", tokenAmount, "nonce", nonce, "deadline", deadline);

        String privateKey = null;
        try {
            privateKey = assetClient.getDecryptedPrivateKey(userSeq).getData();
            ECKeyPair keyPair = ECKeyPair.create(Numeric.toBigInt(privateKey));

            String jsonData = objectMapper.writeValueAsString(Map.of("domain", domain, "types", types, "message", message, "primaryType", "Permit"));

            // --- 💡 여기가 수정된 핵심 로직입니다 ---
            // 1. StructuredDataEncoder를 사용하여 EIP-712 데이터를 인코딩하고 해시 값을 얻습니다.
            StructuredDataEncoder dataEncoder = new StructuredDataEncoder(jsonData);
            byte[] messageHash = dataEncoder.hashStructuredData();

            // 2. 일반 메시지 서명 함수(signMessage)를 사용하여 위에서 얻은 해시 값을 서명합니다.
            return Sign.signMessage(messageHash, keyPair, false);
            // ------------------------------------

        } catch (Exception e) {
            // Jackson의 JsonProcessingException을 포함하여 더 구체적인 예외 처리가 가능합니다.
            throw new RuntimeException("EIP-712 서명 생성에 실패했습니다.", e);
        } finally {
            if (privateKey != null) privateKey = null;
        }
    }
}