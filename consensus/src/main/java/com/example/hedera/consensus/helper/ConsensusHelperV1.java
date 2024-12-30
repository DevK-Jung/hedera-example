package com.example.hedera.consensus.helper;

import com.example.hedera.common.core.AbstractHederaHelper;
import com.example.hedera.common.vo.HederaTransactionResponseVo;
import com.example.hedera.consensus.vo.MessageResponseVo;
import com.example.hedera.consensus.vo.TopicResponseVo;
import com.hedera.hashgraph.sdk.*;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
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
     * @param topicMemo 메모
     * @return 생성된 토픽의 ID를 문자열로 반환합니다.
     * @throws PrecheckStatusException 요청이 Hedera 네트워크의 사전 검사 단계에서 실패할 경우 발생합니다.
     * @throws TimeoutException        요청 시간이 초과될 경우 발생합니다.
     * @throws ReceiptStatusException  트랜잭션 영수증 상태가 실패로 반환될 경우 발생합니다.
     */
    @Override
    public HederaTransactionResponseVo<TopicResponseVo> createTopic(String topicMemo) throws PrecheckStatusException, TimeoutException, ReceiptStatusException {

        return createTopic(privateKey, privateKey, topicMemo, accountId, Duration.ofDays(92));
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
    @Override
    public HederaTransactionResponseVo<TopicResponseVo> createTopic(Key adminKey,
                                                                    Key submitKey,
                                                                    String topicMemo,
                                                                    AccountId autoRenewAccountId,
                                                                    Duration autoRenewPeriod)
            throws PrecheckStatusException, TimeoutException, ReceiptStatusException {

        TopicCreateTransaction transaction =
                getTopicCreateTransaction(adminKey, submitKey, topicMemo, autoRenewAccountId, autoRenewPeriod);

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

        return makeTransactionResponse(receipt, new TopicResponseVo(newTopicId.toString()));
    }

    /**
     * update adminKey
     * 토픽 업데이트 및 토픽 삭제 거래를 승인하는 새로운 관리자 키를 설정합니다.
     *
     * @param topicId     topicId
     * @param adminKey    거래 서명할 adminKey
     * @param newAdminKey 수정할 adminKey
     * @return HederaTransactionResponseVo<TopicResponseVo>
     * @throws ReceiptStatusException  ReceiptStatusException
     * @throws PrecheckStatusException PrecheckStatusException
     * @throws TimeoutException        TimeoutException
     */
    @Override
    public HederaTransactionResponseVo<TopicResponseVo> updateAdminKey(@NonNull String topicId,
                                                                       @NonNull String adminKey,
                                                                       @NonNull String newAdminKey)
            throws ReceiptStatusException, PrecheckStatusException, TimeoutException {
        TopicUpdateTransaction transaction = new TopicUpdateTransaction()
                .setTopicId(TopicId.fromString(topicId))
                .setAdminKey(getPrivateKey(newAdminKey));

        return updateTopic(transaction, adminKey);
    }

    /**
     * update submitKey
     * 이 토픽에 메시지를 보내는 것을 허용하는 새로운 submitKey 를 토픽에 설정합니다.
     *
     * @param topicId      topicId
     * @param adminKey     거래 서명할 adminKey
     * @param newSubmitKey 수정할 submitKey
     * @return HederaTransactionResponseVo<TopicResponseVo>
     * @throws ReceiptStatusException  ReceiptStatusException
     * @throws PrecheckStatusException PrecheckStatusException
     * @throws TimeoutException        TimeoutException
     */
    @Override
    public HederaTransactionResponseVo<TopicResponseVo> updateSubmitKey(@NonNull String topicId,
                                                                        @NonNull String adminKey,
                                                                        @NonNull String newSubmitKey) throws ReceiptStatusException, PrecheckStatusException, TimeoutException {
        TopicUpdateTransaction transaction = new TopicUpdateTransaction()
                .setTopicId(TopicId.fromString(topicId))
                .setSubmitKey(getPrivateKey(newSubmitKey));

        return updateTopic(transaction, adminKey);
    }

    /**
     * update expirationTime
     * 만료 기간 수정
     *
     * @param topicId           topicId
     * @param adminKey          거래 서명할 adminKey
     * @param newExpirationTime 수정할 만료 기간
     * @return HederaTransactionResponseVo<TopicResponseVo>
     * @throws ReceiptStatusException  ReceiptStatusException
     * @throws PrecheckStatusException PrecheckStatusException
     * @throws TimeoutException        TimeoutException
     */
    @Override
    public HederaTransactionResponseVo<TopicResponseVo> updateExpirationTime(@NonNull String topicId,
                                                                             @NonNull String adminKey,
                                                                             @NonNull Instant newExpirationTime)
            throws ReceiptStatusException, PrecheckStatusException, TimeoutException {
        TopicUpdateTransaction transaction = new TopicUpdateTransaction()
                .setTopicId(TopicId.fromString(topicId))
                .setExpirationTime(newExpirationTime);

        return updateTopic(transaction, adminKey);
    }

    /**
     * update expirationTime
     * 만료 기간 수정
     *
     * @param topicId      topicId
     * @param adminKey     거래 서명할 adminKey
     * @param newTopicMemo 수정할 토픽 메모
     * @return HederaTransactionResponseVo<TopicResponseVo>
     * @throws ReceiptStatusException  ReceiptStatusException
     * @throws PrecheckStatusException PrecheckStatusException
     * @throws TimeoutException        TimeoutException
     */
    @Override
    public HederaTransactionResponseVo<TopicResponseVo> updateTopicMemo(@NonNull String topicId,
                                                                        @NonNull String adminKey,
                                                                        @NonNull String newTopicMemo)
            throws ReceiptStatusException, PrecheckStatusException, TimeoutException {
        TopicUpdateTransaction transaction = new TopicUpdateTransaction()
                .setTopicId(TopicId.fromString(topicId))
                .setTopicMemo(newTopicMemo);

        return updateTopic(transaction, adminKey);
    }

    /**
     * update AutoRenewAccountId
     * 자동 갱신 account 수정
     *
     * @param topicId               topicId
     * @param adminKey              거래 서명할 adminKey
     * @param newAutoRenewAccountId 수정할 자동 갱신 accountId
     * @return HederaTransactionResponseVo<TopicResponseVo>
     * @throws ReceiptStatusException  ReceiptStatusException
     * @throws PrecheckStatusException PrecheckStatusException
     * @throws TimeoutException        TimeoutException
     */
    @Override
    public HederaTransactionResponseVo<TopicResponseVo> updateAutoRenewAccount(@NonNull String topicId,
                                                                               @NonNull String adminKey,
                                                                               @NonNull String newAutoRenewAccountId)
            throws ReceiptStatusException, PrecheckStatusException, TimeoutException {
        TopicUpdateTransaction transaction = new TopicUpdateTransaction()
                .setTopicId(TopicId.fromString(topicId))
                .setAutoRenewAccountId(AccountId.fromString(newAutoRenewAccountId));

        return updateTopic(transaction, adminKey);
    }


    /**
     * update AutoRenewPeriod
     * 자동 갱신 기간 수정
     *
     * @param topicId            topicId
     * @param adminKey           거래 서명할 adminKey
     * @param newAutoRenewPeriod 수정할 자동 갱신 기간
     * @return HederaTransactionResponseVo<TopicResponseVo>
     * @throws ReceiptStatusException  ReceiptStatusException
     * @throws PrecheckStatusException PrecheckStatusException
     * @throws TimeoutException        TimeoutException
     */
    @Override
    public HederaTransactionResponseVo<TopicResponseVo> updateAutoRenewAccount(@NonNull String topicId,
                                                                               @NonNull String adminKey,
                                                                               @NonNull Duration newAutoRenewPeriod)
            throws ReceiptStatusException, PrecheckStatusException, TimeoutException {
        TopicUpdateTransaction transaction = new TopicUpdateTransaction()
                .setTopicId(TopicId.fromString(topicId))
                .setAutoRenewPeriod(newAutoRenewPeriod);

        return updateTopic(transaction, adminKey);
    }

    /**
     * clear adminKey
     *
     * @param topicId  topicId
     * @param adminKey 거래 서명할 adminKey
     * @return HederaTransactionResponseVo<TopicResponseVo>
     * @throws ReceiptStatusException  ReceiptStatusException
     * @throws PrecheckStatusException PrecheckStatusException
     * @throws TimeoutException        TimeoutException
     */
    @Override
    public HederaTransactionResponseVo<TopicResponseVo> clearAdminKey(@NonNull String topicId,
                                                                      @NonNull String adminKey)
            throws ReceiptStatusException, PrecheckStatusException, TimeoutException {
        TopicUpdateTransaction transaction = new TopicUpdateTransaction()
                .setTopicId(TopicId.fromString(topicId))
                .clearAdminKey();

        return updateTopic(transaction, adminKey);
    }

    /**
     * clearSubmitKey
     *
     * @param topicId  topicId
     * @param adminKey 거래 서명할 adminKey
     * @return HederaTransactionResponseVo<TopicResponseVo>
     * @throws ReceiptStatusException  ReceiptStatusException
     * @throws PrecheckStatusException PrecheckStatusException
     * @throws TimeoutException        TimeoutException
     */
    @Override
    public HederaTransactionResponseVo<TopicResponseVo> clearSubmitKey(@NonNull String topicId,
                                                                       @NonNull String adminKey)
            throws ReceiptStatusException, PrecheckStatusException, TimeoutException {
        TopicUpdateTransaction transaction = new TopicUpdateTransaction()
                .setTopicId(TopicId.fromString(topicId))
                .clearSubmitKey();

        return updateTopic(transaction, adminKey);
    }


    /**
     * clearTopicMemo
     *
     * @param topicId  topicId
     * @param adminKey 거래 서명할 adminKey
     * @return HederaTransactionResponseVo<TopicResponseVo>
     * @throws ReceiptStatusException  ReceiptStatusException
     * @throws PrecheckStatusException PrecheckStatusException
     * @throws TimeoutException        TimeoutException
     */
    @Override
    public HederaTransactionResponseVo<TopicResponseVo> clearTopicMemo(@NonNull String topicId,
                                                                       @NonNull String adminKey)
            throws ReceiptStatusException, PrecheckStatusException, TimeoutException {
        TopicUpdateTransaction transaction = new TopicUpdateTransaction()
                .setTopicId(TopicId.fromString(topicId))
                .clearTopicMemo();

        return updateTopic(transaction, adminKey);
    }

    /**
     * clearAutoRenewAccountId
     *
     * @param topicId  topicId
     * @param adminKey 거래 서명할 adminKey
     * @return HederaTransactionResponseVo<TopicResponseVo>
     * @throws ReceiptStatusException  ReceiptStatusException
     * @throws PrecheckStatusException PrecheckStatusException
     * @throws TimeoutException        TimeoutException
     */
    @Override
    public HederaTransactionResponseVo<TopicResponseVo> clearAutoRenewAccountId(@NonNull String topicId,
                                                                                @NonNull String adminKey)
            throws ReceiptStatusException, PrecheckStatusException, TimeoutException {
        TopicUpdateTransaction transaction = new TopicUpdateTransaction()
                .setTopicId(TopicId.fromString(topicId))
                .clearAutoRenewAccountId();

        return updateTopic(transaction, adminKey);
    }

    private HederaTransactionResponseVo<TopicResponseVo> updateTopic(@NonNull TopicUpdateTransaction transaction,
                                                                     @NonNull String adminKey)
            throws ReceiptStatusException, PrecheckStatusException, TimeoutException {

        //Sign the transaction with the admin key to authorize the update
        TopicUpdateTransaction signTx = transaction
                .freezeWith(client)
                .sign(getPrivateKey(adminKey));

        //Sign the transaction with the client operator, submit to a Hedera network, get the transaction ID
        TransactionResponse txResponse = signTx.execute(client);

        //Request the receipt of the transaction
        TransactionReceipt receipt = txResponse.getReceipt(client);

        //Get the transaction consensus status
        Status transactionStatus = receipt.status;

        log.debug("The transaction consensus status is " + transactionStatus);

        if (!Status.SUCCESS.equals(receipt.status))
            throw new RuntimeException("Failed update Topic");

        return makeTransactionResponse(receipt, new TopicResponseVo(transaction.getTopicMemo()));
    }

    /**
     * delete Topic
     * <p>토픽 삭제, 삭제 후 메시지 수신 불가하며 submitMessage 호출 실패, 토픽이 삭제된 후에도 미러 노드를 통해 이전 메시지 엑세스는 가능</p>
     * <ul>
     *      <li>토픽 생성 시 adminKey가 설정된 경우 토픽을 성공적으로 삭제하려면 adminKey에 서명해야 합니다.</li>
     *      <li>토픽 생성 시 adminKey가 설정되지 않은 경우 토픽을 삭제할 수 없으며 UNAUTHHORIZED 오류가 발생합니다.</li>
     * </ul>
     *
     * @param topicId  topicId
     * @param adminKey 토픽 생성 시 설정한 adminKey
     * @return
     * @throws ReceiptStatusException
     * @throws PrecheckStatusException
     * @throws TimeoutException
     */
    @Override
    public HederaTransactionResponseVo<TopicResponseVo> deleteTopic(@NonNull String topicId,
                                                                    @NonNull String adminKey)
            throws ReceiptStatusException, PrecheckStatusException, TimeoutException {
        //Create the transaction
        TopicDeleteTransaction transaction = new TopicDeleteTransaction()
                .setTopicId(getTopicId(topicId));

        //Sign the transaction with the admin key, sign with the client operator and submit the transaction to a Hedera network, get the transaction ID
        TransactionResponse txResponse = transaction
                .freezeWith(client)
                .sign(PrivateKey.fromString(adminKey))
                .execute(client);

        //Request the receipt of the transaction
        TransactionReceipt receipt = txResponse.getReceipt(client);

        //Get the transaction consensus status
        Status transactionStatus = receipt.status;

        log.debug("The transaction consensus status is " + transactionStatus);

        if (!Status.SUCCESS.equals(receipt.status))
            throw new RuntimeException("Failed delete Topic");

        return makeTransactionResponse(receipt, new TopicResponseVo(topicId));
    }

    /**
     * submit Message
     * <p>
     *
     * @param topicId    topicId
     * @param message    message - 최대 크기 1024byte(1kb)
     * @param chunkSize  메시지에 대한 개별 청크의 최대 크기 - default 1024
     * @param maxChuncks 메시지 분할 할 수 있는 최대 청크 수 - 기본 값 20
     * @return HederaTransactionResponseVo<MessageResponseVo>
     * @throws PrecheckStatusException PrecheckStatusException
     * @throws TimeoutException        TimeoutException
     * @throws ReceiptStatusException  ReceiptStatusException
     */
    @Override
    public HederaTransactionResponseVo<MessageResponseVo> submitMessage(@NonNull String topicId,
                                                                        @NonNull String message,
                                                                        Integer chunkSize,
                                                                        Integer maxChuncks)
            throws PrecheckStatusException, TimeoutException, ReceiptStatusException {

        // Check if the message exceeds 1KB
        if (message.getBytes(StandardCharsets.UTF_8).length > 1024)
            throw new IllegalArgumentException("Message size exceeds 1KB limit");

        //Create the transaction
        TopicMessageSubmitTransaction transaction = new TopicMessageSubmitTransaction()
                .setTopicId(getTopicId(topicId))
                .setMessage(message);

        if (chunkSize != null) transaction.setChunkSize(chunkSize);
        if (maxChuncks != null) transaction.setMaxAttempts(maxChuncks);

        // Sign with the client operator key and submit transaction to a Hedera network, get transaction ID
        TransactionResponse txResponse = transaction.execute(client);

        // Request the receipt of the transaction
        TransactionReceipt receipt = txResponse.getReceipt(client);

        // Get the transaction consensus status
        Status transactionStatus = receipt.status;

        log.info("The transaction consensus status is " + transactionStatus);

        if (!Status.SUCCESS.equals(receipt.status))
            throw new RuntimeException("Failed submit message");

        return makeTransactionResponse(receipt, new MessageResponseVo(topicId, message, receipt.topicSequenceNumber));
    }

    /**
     * topic 정보 조회
     *
     * @param topicId 토픽 아이디
     * @return TopicInfo
     * @throws PrecheckStatusException PrecheckStatusException
     * @throws TimeoutException        TimeoutException
     */
    @Override
    public TopicInfo getTopicInfo(String topicId) throws PrecheckStatusException, TimeoutException {
        //Create the account info query
        TopicInfoQuery query = new TopicInfoQuery()
                .setTopicId(TopicId.fromString(topicId));

        //Submit the query to a Hedera network
        TopicInfo info = query.execute(client);

        //Print the account key to the console
        log.debug("topicInfo: {}", info);

        //v2.0.0
        return info;
    }

    @Override
    public void getTopicMessages(String topicId,
                                 Instant subscribeStartTime,
                                 Instant subscribeEndTime) {
        //Create the query
        TopicMessageQuery topicMessageQuery = new TopicMessageQuery()
                .setTopicId(TopicId.fromString(topicId));

        if (subscribeEndTime != null) topicMessageQuery.setStartTime(subscribeStartTime);
        if (subscribeEndTime != null) topicMessageQuery.setEndTime(subscribeEndTime);

        topicMessageQuery.subscribe(client, topicMessage -> {
            System.out.println("at " + topicMessage.consensusTimestamp + " ( seq = " + topicMessage.sequenceNumber + " ) received topic message of " + topicMessage.contents.length + " bytes");
        });
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

    private TopicId getTopicId(@NonNull String topicId) {
        return TopicId.fromString(topicId);
    }

    private PrivateKey getPrivateKey(@NonNull String privateKey) {
        return PrivateKey.fromString(privateKey);
    }

}
