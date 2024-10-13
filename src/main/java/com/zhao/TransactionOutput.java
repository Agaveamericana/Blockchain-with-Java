package com.zhao;

import java.security.*;
/**
 * @author Agave_americana_L.
 * @Description
 * @date 2024/10/10 下午11:48
 */
public class TransactionOutput {
    public String id;
    public PublicKey reciepient; // 接收者，也被称为这些币的新所有者
    public float value; // 他们拥有的币的数量
    public String parentTransactionId; // 创建此输出的交易的ID

    // 构造函数
    public TransactionOutput(PublicKey reciepient, float value, String parentTransactionId) {
        this.reciepient = reciepient;
        this.value = value;
        this.parentTransactionId = parentTransactionId;
        this.id = StringUtil.applySha256(StringUtil.getStringFromKey(reciepient)+Float.toString(value)+parentTransactionId);
    }

    // 检查币是否属于你
    public boolean isMine(PublicKey publicKey) {
        return (publicKey == reciepient);
    }
}
