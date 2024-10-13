package com.zhao;

/**
 * @author Agave_americana_L.
 * @Description
 * @date 2024/10/10 下午11:47
 */
public class TransactionInput {
    public String transactionOutputId; // 引用到TransactionOutputs的transactionId
    public TransactionOutput UTXO; // 包含未花费的交易输出

    public TransactionInput(String transactionOutputId) {
        this.transactionOutputId = transactionOutputId;
    }
}
