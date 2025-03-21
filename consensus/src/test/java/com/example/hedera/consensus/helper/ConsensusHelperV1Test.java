package com.example.hedera.consensus.helper;

import com.example.hedera.common.vo.HederaTransactionResponseVo;
import com.example.hedera.consensus.vo.MessageResponseVo;
import com.example.hedera.consensus.vo.TopicResponseVo;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.Status;
import com.hedera.hashgraph.sdk.TopicInfo;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.concurrent.TimeoutException;

@SpringBootTest
@ActiveProfiles("test")
class ConsensusHelperV1Test {
    @Autowired
    ConsensusHelperV1 consensusHelper;

    @Test
    void createTopic() throws ReceiptStatusException, PrecheckStatusException, TimeoutException {
        String topicMemo = "My First Topic";

        HederaTransactionResponseVo<TopicResponseVo> result = consensusHelper.createTopic(topicMemo);

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

    @Test
    void deleteTopic() throws PrecheckStatusException, TimeoutException, ReceiptStatusException {
        String topicId = "0.0.5327337";
        String adminKey = ""; // adminKey

        HederaTransactionResponseVo<TopicResponseVo> result
                = consensusHelper.deleteTopic(topicId, adminKey);

        Assertions.assertThat(result.getStatus()).isEqualTo(Status.SUCCESS);
    }

    @Test
    void submitMessage() throws PrecheckStatusException, TimeoutException, ReceiptStatusException {
        String topicId = "0.0.5328364";
        String message = "hello, i'm kim";

        HederaTransactionResponseVo<MessageResponseVo> result
                = consensusHelper.submitMessage(topicId, message, null, null);

        Assertions.assertThat(result.getStatus()).isEqualTo(Status.SUCCESS);
        Assertions.assertThat(result.getResult().message()).isEqualTo(message);
    }

    @Test
    void updateAdminKey() {

    }

    @Test
    void updateSubmitKey() {
    }

    @Test
    void updateExpirationTime() {
    }

    @Test
    void updateTopicMemo() {
    }

    @Test
    void updateAutoRenewAccount() {
    }

    @Test
    void testUpdateAutoRenewAccount() {
    }

    @Test
    void clearAdminKey() {
    }

    @Test
    void clearSubmitKey() {
    }

    @Test
    void clearTopicMemo() throws ReceiptStatusException, PrecheckStatusException, TimeoutException {
        String topicId = "0.0.5328364";
        String adminKey = "";
        String newTopicMemo = "hello";

        HederaTransactionResponseVo<TopicResponseVo> result = consensusHelper.updateTopicMemo(topicId, adminKey, newTopicMemo);
        TopicInfo topicInfo = consensusHelper.getTopicInfo(topicId);

        Assertions.assertThat(result.getStatus()).isEqualTo(Status.SUCCESS);
        Assertions.assertThat(topicInfo.topicMemo).isEqualTo(newTopicMemo);
    }

    @Test
    void clearAutoRenewAccountId() {
    }

    @Test
    void getTopicMessages() throws ReceiptStatusException, PrecheckStatusException, TimeoutException, InterruptedException {
        String topicId = "0.0.5328364";

        Instant now = Instant.now(); // 현재 시점
        Instant tenMinutesLater = now.plusSeconds(10 * 60); // 현재 시점부터 10분 후

        consensusHelper.getTopicMessages(topicId, now, tenMinutesLater);

        submitMessage(); // 로그 메시지 확인
    }
}