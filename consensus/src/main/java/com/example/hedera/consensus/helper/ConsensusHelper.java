package com.example.hedera.consensus.helper;

import com.example.hedera.common.vo.HederaResponseVo;
import com.example.hedera.consensus.vo.TopicCreateResponseVo;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Key;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.ReceiptStatusException;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

public interface ConsensusHelper {
    HederaResponseVo<TopicCreateResponseVo> createTopic() throws PrecheckStatusException, TimeoutException, ReceiptStatusException;

    HederaResponseVo<TopicCreateResponseVo> createTopic(Key adminKey,
                                                        Key submitKey,
                                                        String topicMemo,
                                                        AccountId autoRenewAccountId,
                                                        Duration autoRenewPeriod) throws PrecheckStatusException, TimeoutException, ReceiptStatusException;


}
