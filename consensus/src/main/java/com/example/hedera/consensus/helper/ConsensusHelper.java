package com.example.hedera.consensus.helper;

import com.example.hedera.common.vo.HederaTransactionResponseVo;
import com.example.hedera.consensus.vo.MessageResponseVo;
import com.example.hedera.consensus.vo.TopicResponseVo;
import com.hedera.hashgraph.sdk.*;
import lombok.NonNull;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

public interface ConsensusHelper {
    HederaTransactionResponseVo<TopicResponseVo> createTopic() throws PrecheckStatusException, TimeoutException, ReceiptStatusException;

    HederaTransactionResponseVo<TopicResponseVo> createTopic(Key adminKey,
                                                             Key submitKey,
                                                             String topicMemo,
                                                             AccountId autoRenewAccountId,
                                                             Duration autoRenewPeriod)
            throws PrecheckStatusException, TimeoutException, ReceiptStatusException;

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
