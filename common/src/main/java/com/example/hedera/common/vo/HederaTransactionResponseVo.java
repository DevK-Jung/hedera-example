package com.example.hedera.common.vo;

import com.hedera.hashgraph.sdk.Status;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HederaTransactionResponseVo<T> {
    private final Status status;
    private final String transactionId;
    private final T result;

    public static <T> HederaTransactionResponseVo<T> of(TransactionReceipt receipt, T result) {
        Objects.requireNonNull(receipt);

        return new HederaTransactionResponseVo<>(
                Objects.requireNonNull(receipt.status, "Status cannot be null"),
                receipt.transactionId != null ? receipt.transactionId.toString() : "UNKNOWN_TRANSACTION_ID",
                result
        );
    }
}
