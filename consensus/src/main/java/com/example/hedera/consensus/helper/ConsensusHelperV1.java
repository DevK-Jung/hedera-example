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

    @Override
    public HederaTransactionResponseVo<TopicResponseVo> createTopic(String topicMemo) throws PrecheckStatusException, TimeoutException, ReceiptStatusException {

        return createTopic(privateKey, privateKey, topicMemo, accountId, Duration.ofDays(92));
    }


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


    @Override
    public HederaTransactionResponseVo<TopicResponseVo> updateSubmitKey(@NonNull String topicId,
                                                                        @NonNull String adminKey,
                                                                        @NonNull String newSubmitKey) throws ReceiptStatusException, PrecheckStatusException, TimeoutException {
        TopicUpdateTransaction transaction = new TopicUpdateTransaction()
                .setTopicId(TopicId.fromString(topicId))
                .setSubmitKey(getPrivateKey(newSubmitKey));

        return updateTopic(transaction, adminKey);
    }


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


    @Override
    public HederaTransactionResponseVo<TopicResponseVo> clearAdminKey(@NonNull String topicId,
                                                                      @NonNull String adminKey)
            throws ReceiptStatusException, PrecheckStatusException, TimeoutException {
        TopicUpdateTransaction transaction = new TopicUpdateTransaction()
                .setTopicId(TopicId.fromString(topicId))
                .clearAdminKey();

        return updateTopic(transaction, adminKey);
    }


    @Override
    public HederaTransactionResponseVo<TopicResponseVo> clearSubmitKey(@NonNull String topicId,
                                                                       @NonNull String adminKey)
            throws ReceiptStatusException, PrecheckStatusException, TimeoutException {
        TopicUpdateTransaction transaction = new TopicUpdateTransaction()
                .setTopicId(TopicId.fromString(topicId))
                .clearSubmitKey();

        return updateTopic(transaction, adminKey);
    }


    @Override
    public HederaTransactionResponseVo<TopicResponseVo> clearTopicMemo(@NonNull String topicId,
                                                                       @NonNull String adminKey)
            throws ReceiptStatusException, PrecheckStatusException, TimeoutException {
        TopicUpdateTransaction transaction = new TopicUpdateTransaction()
                .setTopicId(TopicId.fromString(topicId))
                .clearTopicMemo();

        return updateTopic(transaction, adminKey);
    }


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
