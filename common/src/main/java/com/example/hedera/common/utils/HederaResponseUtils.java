package com.example.hedera.common.utils;

import com.example.hedera.common.vo.HederaTransactionResponseVo;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import lombok.experimental.UtilityClass;

@UtilityClass
public class HederaResponseUtils {
    public <T> HederaTransactionResponseVo<T> makeResponse(TransactionReceipt receipt, T result) {
        return HederaTransactionResponseVo.of(receipt, result);
    }
}
