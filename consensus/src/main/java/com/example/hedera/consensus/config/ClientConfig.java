package com.example.hedera.consensus.config;

import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.PrivateKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientConfig {

    @Bean
    public Client client(@Value("${hedera.account-id}") String accountId,
                         @Value("${hedera.private-key}") String privateKey) {

        // Operator account ID and private key from string value
        AccountId MY_ACCOUNT_ID = AccountId.fromString(accountId);
        PrivateKey MY_PRIVATE_KEY = PrivateKey.fromString(privateKey);

        // Pre-configured client for test network (testnet)
        Client client = Client.forTestnet();

        //Set the operator with the account ID and private key
        client.setOperator(MY_ACCOUNT_ID, MY_PRIVATE_KEY);

        return client;
    }
}
