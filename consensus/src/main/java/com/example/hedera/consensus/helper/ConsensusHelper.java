package com.example.hedera.consensus.helper;

import com.example.hedera.common.vo.HederaTransactionResponseVo;
import com.example.hedera.consensus.vo.TopicCreateResponseVo;
import com.hedera.hashgraph.sdk.*;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

public interface ConsensusHelper {
    HederaTransactionResponseVo<TopicCreateResponseVo> createTopic() throws PrecheckStatusException, TimeoutException, ReceiptStatusException;

    HederaTransactionResponseVo<TopicCreateResponseVo> createTopic(Key adminKey,
                                                                   Key submitKey,
                                                                   String topicMemo,
                                                                   AccountId autoRenewAccountId,
                                                                   Duration autoRenewPeriod) throws PrecheckStatusException, TimeoutException, ReceiptStatusException;

    TopicInfo getTopicInfo(String topicId) throws PrecheckStatusException, TimeoutException ;

}
