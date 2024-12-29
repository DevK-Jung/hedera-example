package com.example.hedera.consensus.helper;

import com.hedera.hashgraph.sdk.Client;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConsensusHelperV1 implements ConsensusHelper {

    private final Client client;



}
