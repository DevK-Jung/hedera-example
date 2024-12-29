package com.example.hedera.common.utils;

import com.example.hedera.common.vo.HederaResponseVo;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import lombok.experimental.UtilityClass;

@UtilityClass
public class HederaResponseUtils {
    public <T> HederaResponseVo<T> makeResponse(TransactionReceipt receipt, T result) {
        return HederaResponseVo.of(receipt, result);
    }
}
