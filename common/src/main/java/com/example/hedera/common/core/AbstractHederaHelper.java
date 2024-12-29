package com.example.hedera.common.core;

import com.example.hedera.common.utils.HederaResponseUtils;
import com.example.hedera.common.vo.HederaTransactionResponseVo;
import com.hedera.hashgraph.sdk.TransactionReceipt;

public abstract class AbstractHederaHelper {
    protected final <T> HederaTransactionResponseVo<T> makeTransactinoResponse(TransactionReceipt receipt, T result) {
        return HederaResponseUtils.makeResponse(receipt, result);
    }
}
