package com.example.hedera.consensus.helper;

import com.example.hedera.common.vo.HederaTransactionResponseVo;
import com.example.hedera.consensus.vo.MessageResponseVo;
import com.example.hedera.consensus.vo.TopicResponseVo;
import com.hedera.hashgraph.sdk.*;
import lombok.NonNull;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeoutException;

public interface ConsensusHelper {
    HederaTransactionResponseVo<TopicResponseVo> createTopic(String topicMemo) throws PrecheckStatusException, TimeoutException, ReceiptStatusException;

    HederaTransactionResponseVo<TopicResponseVo> createTopic(Key adminKey,
                                                             Key submitKey,
                                                             String topicMemo,
                                                             AccountId autoRenewAccountId,
                                                             Duration autoRenewPeriod)
            throws PrecheckStatusException, TimeoutException, ReceiptStatusException;

    HederaTransactionResponseVo<TopicResponseVo> updateAdminKey(@NonNull String topicId,
                                                                @NonNull String adminKey,
                                                                @NonNull String newAdminKey)
            throws ReceiptStatusException, PrecheckStatusException, TimeoutException;

    HederaTransactionResponseVo<TopicResponseVo> updateSubmitKey(@NonNull String topicId,
                                                                 @NonNull String adminKey,
                                                                 @NonNull String newSubmitKey)
            throws ReceiptStatusException, PrecheckStatusException, TimeoutException;

    HederaTransactionResponseVo<TopicResponseVo> updateExpirationTime(@NonNull String topicId,
                                                                      @NonNull String adminKey,
                                                                      @NonNull Instant newExpirationTime)
            throws ReceiptStatusException, PrecheckStatusException, TimeoutException;

    HederaTransactionResponseVo<TopicResponseVo> updateTopicMemo(@NonNull String topicId,
                                                                 @NonNull String adminKey,
                                                                 @NonNull String newTopicMemo)
            throws ReceiptStatusException, PrecheckStatusException, TimeoutException;

    HederaTransactionResponseVo<TopicResponseVo> updateAutoRenewAccount(@NonNull String topicId,
                                                                        @NonNull String adminKey,
                                                                        @NonNull String newAutoRenewAccountId)
            throws ReceiptStatusException, PrecheckStatusException, TimeoutException;

    HederaTransactionResponseVo<TopicResponseVo> updateAutoRenewAccount(@NonNull String topicId,
                                                                        @NonNull String adminKey,
                                                                        @NonNull Duration newAutoRenewPeriod)
            throws ReceiptStatusException, PrecheckStatusException, TimeoutException;

    HederaTransactionResponseVo<TopicResponseVo> clearAdminKey(@NonNull String topicId,
                                                               @NonNull String adminKey)
            throws ReceiptStatusException, PrecheckStatusException, TimeoutException;

    HederaTransactionResponseVo<TopicResponseVo> clearSubmitKey(@NonNull String topicId,
                                                                @NonNull String adminKey)
            throws ReceiptStatusException, PrecheckStatusException, TimeoutException;

    HederaTransactionResponseVo<TopicResponseVo> clearTopicMemo(@NonNull String topicId,
                                                                @NonNull String adminKey)
            throws ReceiptStatusException, PrecheckStatusException, TimeoutException;

    HederaTransactionResponseVo<TopicResponseVo> clearAutoRenewAccountId(@NonNull String topicId,
                                                                         @NonNull String adminKey)
            throws ReceiptStatusException, PrecheckStatusException, TimeoutException;

    HederaTransactionResponseVo<TopicResponseVo> deleteTopic(@NonNull String topicId,
                                                             @NonNull String adminKey)
            throws ReceiptStatusException, PrecheckStatusException, TimeoutException;

    TopicInfo getTopicInfo(String topicId) throws PrecheckStatusException, TimeoutException;

    HederaTransactionResponseVo<MessageResponseVo> submitMessage(@NonNull String topicId,
                                                                 @NonNull String message,
                                                                 Integer chunkSize,
                                                                 Integer maxChuncks)
            throws PrecheckStatusException, TimeoutException, ReceiptStatusException;

}
