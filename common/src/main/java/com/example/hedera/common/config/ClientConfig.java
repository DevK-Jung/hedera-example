package com.example.hedera.common.config;

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
        AccountId myAccountId = AccountId.fromString(accountId);
        PrivateKey myPrivateKey = PrivateKey.fromString(privateKey);

        // Pre-configured client for test network (testnet)
        Client client = Client.forTestnet();

        //Set the operator with the account ID and private key
        client.setOperator(myAccountId, myPrivateKey);

        return client;
    }
}
