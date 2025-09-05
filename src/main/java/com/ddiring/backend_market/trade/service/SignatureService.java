package com.ddiring.backend_market.trade.service;

import com.ddiring.backend_market.api.asset.AssetClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SignatureService {

    private final AssetClient assetClient;
    // 💡 ObjectMapper를 주입받아 JSON 변환에 사용합니다.
    private final ObjectMapper objectMapper;

    // --- application.yaml 또는 쿠버네티스 설정에서 주입받는 값들 ---
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

        Map<String, Object> domain = Map.of(
                "name", contractName,
                "version", contractVersion,
                "chainId", chainId,
                "verifyingContract", verifyingContractAddress
        );
        Map<String, Object> types = Map.of(
                "Permit", List.of(
                        Map.of("name", "owner", "type", "address"),
                        Map.of("name", "spender", "type", "address"),
                        Map.of("name", "value", "type", "uint256"),
                        Map.of("name", "nonce", "type", "uint256"),
                        Map.of("name", "deadline", "type", "uint256")
                )
        );
        Map<String, Object> message = Map.of(
                "owner", ownerAddress,
                "spender", spenderAddress,
                "value", tokenAmount,
                "nonce", nonce,
                "deadline", deadline
        );

        String privateKey = null;
        try {
            // 1. Asset 서비스에서 개인키를 가져옵니다.
            privateKey = assetClient.getDecryptedPrivateKey(userSeq).getData();
            // 2. 개인키로 서명에 사용할 키 쌍을 생성합니다.
            ECKeyPair keyPair = ECKeyPair.create(Numeric.toBigInt(privateKey));

            // 3. EIP-712 데이터를 서명을 위해 JSON 문자열로 변환합니다.
            String jsonData = objectMapper.writeValueAsString(Map.of(
                    "domain", domain,
                    "types", types,
                    "message", message,
                    "primaryType", "Permit" // 💡 서명할 기본 타입을 명시해줍니다.
            ));

            // 4. 서버에서 직접 서명합니다.
            return Sign.signTypedData(jsonData, keyPair);

        } catch (Exception e) {
            throw new RuntimeException("EIP-712 서명 생성에 실패했습니다.", e);
        } finally {
            // ⚠️ 매우 중요: 사용이 끝난 개인키는 즉시 메모리에서 참조를 제거합니다.
            if (privateKey != null) {
                privateKey = null;
            }
        }
    }
}