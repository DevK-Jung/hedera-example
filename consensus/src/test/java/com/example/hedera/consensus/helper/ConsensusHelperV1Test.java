//package com.example.hedera.consensus.helper;
//
//import com.example.hedera.common.vo.HederaResponseVo;
//import com.example.hedera.consensus.vo.TopicCreateResponseVo;
//import com.hedera.hashgraph.sdk.PrecheckStatusException;
//import com.hedera.hashgraph.sdk.ReceiptStatusException;
//import com.hedera.hashgraph.sdk.Status;
//import org.assertj.core.api.Assertions;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.TestPropertySource;
//
//import java.util.concurrent.TimeoutException;
//
//@SpringBootTest
//@ActiveProfiles("test")
//class ConsensusHelperV1Test {
//    @Autowired
//    ConsensusHelperV1 consensusHelper;
//
//    @Test
//    void createTopic() throws ReceiptStatusException, PrecheckStatusException, TimeoutException {
//        HederaResponseVo<TopicCreateResponseVo> result = consensusHelper.createTopic();
//
//        Assertions.assertThat(result.getStatus()).isEqualTo(Status.SUCCESS);
//        Assertions.assertThat(result.getResult().topicId()).isNotBlank();
//    }
//}