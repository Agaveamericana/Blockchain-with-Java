package com.zhao;


import java.security.*;
import java.util.ArrayList;
/**
 * @author Agave_americana_L.
 * @Description
 * @date 2024/10/10 下午11:38
 */
public class Transaction {

    public String transactionId; // 这也是交易的哈希值。
    public PublicKey sender; // 发送者的地址/公钥。
    public PublicKey reciepient; // 接收者的地址/公钥。
    public float value;
    public byte[] signature;  // 这是为了防止其他人花我们钱包里的钱。

    public ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
    public ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();

    private static int sequence = 0; // 大致统计已生成的交易数量。

    // 构造函数
    public Transaction(PublicKey from, PublicKey to, float value,  ArrayList<TransactionInput> inputs) {
        this.sender = from;
        this.reciepient = to;
        this.value = value;
        this.inputs = inputs;
    }

    // 计算交易哈希值（将用作其ID）
    private String calulateHash() {
        sequence++; // 增加序列号以避免两个相同的交易具有相同的哈希值
        return StringUtil.applySha256(
                StringUtil.getStringFromKey(sender) +
                        StringUtil.getStringFromKey(reciepient) +
                        Float.toString(value) + sequence
        );
    }

    // 对我们不希望被篡改的所有数据进行签名
    public void generateSignature(PrivateKey privateKey) {
        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(reciepient) + Float.toString(value)	;
        signature = StringUtil.applyECDSASig(privateKey,data);
    }
    // 验证我们签名的数据是否被篡改
    public boolean verifiySignature() {
        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(reciepient) + Float.toString(value)	;
        return StringUtil.verifyECDSASig(sender, data, signature);
    }


    // 如果可以创建新交易，则返回true。
    public boolean processTransaction() {

        if(verifiySignature() == false) {
            System.out.println("#交易签名验证失败");
            return false;
        }

        // 收集交易输入（确保它们未被花费）：
        for(TransactionInput i : inputs) {
            i.UTXO = NoobChain.UTXOs.get(i.transactionOutputId);
        }

        // 检查交易是否有效：
        if(getInputsValue() < NoobChain.minimumTransaction) {
            System.out.println("交易输入太小: " + getInputsValue());
            System.out.println("Please enter the amount greater than " + NoobChain.minimumTransaction);
            return false;
        }

        // 生成交易输出：
        float leftOver = getInputsValue() - value;  // 获取输入的价值，然后计算找零：
        transactionId = calulateHash();
        outputs.add(new TransactionOutput( this.reciepient, value,transactionId)); //send value to recipient
        outputs.add(new TransactionOutput( this.sender, leftOver,transactionId)); //send the left over 'change' back to sender

        // 将输出添加到未花费列表
        for(TransactionOutput o : outputs) {
            NoobChain.UTXOs.put(o.id , o);
        }

        // 从UTXO列表中移除已花费的交易输入：
        for(TransactionInput i : inputs) {
            if(i.UTXO == null) continue; // 如果找不到交易则跳过
            NoobChain.UTXOs.remove(i.UTXO.id);
        }

        return true;
    }

    // 返回输入（UTXOs）价值的总和
    public float getInputsValue() {
        float total = 0;
        for(TransactionInput i : inputs) {
            if(i.UTXO == null) continue;  // 如果找不到交易则跳过
            total += i.UTXO.value;
        }
        return total;
    }

    // 返回输出的总和：
    public float getOutputsValue() {
        float total = 0;
        for(TransactionOutput o : outputs) {
            total += o.value;
        }
        return total;
    }

}
