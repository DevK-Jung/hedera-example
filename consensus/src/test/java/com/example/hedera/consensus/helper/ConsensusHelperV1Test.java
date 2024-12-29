package com.example.hedera.consensus.helper;

import com.example.hedera.common.vo.HederaTransactionResponseVo;
import com.example.hedera.consensus.vo.TopicCreateResponseVo;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.Status;
import com.hedera.hashgraph.sdk.TopicInfo;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.TimeoutException;

@SpringBootTest
@ActiveProfiles("test")
class ConsensusHelperV1Test {
    @Autowired
    ConsensusHelperV1 consensusHelper;

    @Test
    void createTopic() throws ReceiptStatusException, PrecheckStatusException, TimeoutException {
        HederaTransactionResponseVo<TopicCreateResponseVo> result = consensusHelper.createTopic();

        System.out.println("result.getResult().topicId() = " + result.getResult().topicId());

        Assertions.assertThat(result.getStatus()).isEqualTo(Status.SUCCESS);
        Assertions.assertThat(result.getResult().topicId()).isNotBlank();
    }


    @Test
    void getTopicInfo() throws PrecheckStatusException, TimeoutException {
        String topicId = "0.0.5327337";
        TopicInfo topicInfo = consensusHelper.getTopicInfo(topicId);

        Assertions.assertThat(topicInfo.topicId.toString()).isEqualTo(topicId);
    }
}