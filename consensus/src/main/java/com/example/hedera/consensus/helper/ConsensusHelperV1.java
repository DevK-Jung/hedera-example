package com.example.hedera.consensus.helper;

import com.example.hedera.common.core.AbstractHederaHelper;
import com.example.hedera.common.vo.HederaResponseVo;
import com.example.hedera.consensus.vo.TopicCreateResponseVo;
import com.hedera.hashgraph.sdk.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
public class ConsensusHelperV1 extends AbstractHederaHelper implements ConsensusHelper {

    private final Client client;
    private final AccountId accountId;
    private final PrivateKey privateKey;

    public ConsensusHelperV1(Client client,
                             @Value("${hedera.account-id}") String accountId,
                             @Value("${hedera.private-key}") String privateKey) {
        this.client = client;
        this.accountId = AccountId.fromString(accountId);
        this.privateKey = PrivateKey.fromString(privateKey);
    }

    /**
     * 기본 설정을 사용해 새로운 토픽을 생성합니다.
     * <p>
     * 기본 설정:
     * - AdminKey와 SubmitKey는 운영자의 개인 키를 사용합니다.
     * - 자동 갱신 기간(Auto-renew period)은 92일로 설정됩니다.
     *
     * @return 생성된 토픽의 ID를 문자열로 반환합니다.
     * @throws PrecheckStatusException 요청이 Hedera 네트워크의 사전 검사 단계에서 실패할 경우 발생합니다.
     * @throws TimeoutException        요청 시간이 초과될 경우 발생합니다.
     * @throws ReceiptStatusException  트랜잭션 영수증 상태가 실패로 반환될 경우 발생합니다.
     */
    public HederaResponseVo<TopicCreateResponseVo> createTopic() throws PrecheckStatusException, TimeoutException, ReceiptStatusException {

        return createTopic(privateKey, privateKey, null, accountId, Duration.ofDays(92));
    }

    /**
     * 사용자 정의 설정을 사용해 새로운 토픽을 생성합니다.
     *
     * @param adminKey           AdminKey는 토픽 업데이트 및 삭제를 제어합니다. (null로 설정하면 업데이트는 가능하지만 삭제는 불가능합니다.)
     * @param submitKey          SubmitKey는 메시지 제출 권한을 제어합니다. (null로 설정하면 모든 사용자가 메시지를 제출할 수 있습니다.)
     * @param topicMemo          토픽 메모(선택 사항, 최대 100바이트).
     * @param autoRenewAccountId 자동 갱신 비용을 부담할 계정 ID(선택 사항).
     * @param autoRenewPeriod    자동 갱신 주기(선택 사항, 30~92일 사이의 값).
     * @return 생성된 토픽의 ID
     * @throws PrecheckStatusException 요청이 Hedera 네트워크의 사전 검사 단계에서 실패할 경우 발생합니다.
     * @throws TimeoutException        요청 시간이 초과될 경우 발생합니다.
     * @throws ReceiptStatusException  트랜잭션 영수증 상태가 실패로 반환될 경우 발생합니다.
     */
    public HederaResponseVo<TopicCreateResponseVo> createTopic(Key adminKey,
                                                               Key submitKey,
                                                               String topicMemo,
                                                               AccountId autoRenewAccountId,
                                                               Duration autoRenewPeriod)
            throws PrecheckStatusException, TimeoutException, ReceiptStatusException {

        TopicCreateTransaction transaction = getTopicCreateTransaction(adminKey, submitKey, topicMemo, autoRenewAccountId, autoRenewPeriod);

        //Sign with the client operator private key and submit the transaction to a Hedera network
        TransactionResponse txResponse = transaction.execute(client);

        //Request the receipt of the transaction
        TransactionReceipt receipt = txResponse.getReceipt(client);

        if (!Status.SUCCESS.equals(receipt.status))
            throw new RuntimeException("Failed create Topic");

        //Get the topic ID
        TopicId newTopicId = receipt.topicId;

        log.debug("The new topic ID is {}", newTopicId);

        if (newTopicId == null)
            throw new IllegalStateException("Topic ID cannot be null");

        return makeResponse(receipt, new TopicCreateResponseVo(newTopicId.toString()));
    }

    private TopicCreateTransaction getTopicCreateTransaction(Key adminKey,
                                                             Key submitKey,
                                                             String topicMemo,
                                                             AccountId autoRenewAccountId,
                                                             Duration autoRenewPeriod) {
        //Create the transaction
        TopicCreateTransaction transaction = new TopicCreateTransaction();

        if (adminKey != null) transaction.setAdminKey(adminKey);
        if (submitKey != null) transaction.setSubmitKey(submitKey);
        if (StringUtils.isNotBlank(topicMemo)) transaction.setTopicMemo(topicMemo);
        if (autoRenewAccountId != null) transaction.setAutoRenewAccountId(autoRenewAccountId);
        if (autoRenewPeriod != null) transaction.setAutoRenewPeriod(autoRenewPeriod);

        return transaction;
    }

}
