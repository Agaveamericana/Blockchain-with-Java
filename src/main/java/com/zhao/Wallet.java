package com.zhao;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Agave_americana_L.
 * @Description
 * @date 2024/10/6 下午3:14
 */
public class Wallet {
    public PrivateKey privateKey;
    public PublicKey publicKey;

    public Map<String, TransactionOutput> UTXOs = new HashMap<String, TransactionOutput>();

    public Wallet(){
        generateKeyPair();
    }

    // 生成密钥对PK&SK
    public void generateKeyPair() {
        try {
            // 使用BC提供者生成ECDSA密钥对
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA","BC");
            // 生成安全的随机数
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            // 使用P-192椭圆曲线
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");

            // 生成密钥对
            keyGen.initialize(ecSpec, random);
            KeyPair keyPair = keyGen.generateKeyPair();

            // 将生成的密钥对赋值给成员变量
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();
        }catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 返回余额，并将此钱包拥有的UTXO存储在this.UTXOs中
    public float getBalance() {
        float total = 0;
        for (Map.Entry<String, TransactionOutput> item: NoobChain.UTXOs.entrySet()){
            TransactionOutput UTXO = item.getValue();
            if(UTXO.isMine(publicKey)) { // 如果输出属于我（如果币属于我）
                UTXOs.put(UTXO.id,UTXO); // 将其添加到我们的未花费交易列表中
                total += UTXO.value ;
            }
        }
        return total;
    }
    // 生成并返回从这个钱包发送的新交易
    public Transaction sendFunds(PublicKey _recipient,float value ) {
        if(getBalance() < value) { // 收集余额并检查资金
            System.out.println("#资金不足，无法发送交易。交易已丢弃。");
            return null;
        }

        // 创建输入的数组列表
        ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();

        float total = 0;
        for (Map.Entry<String, TransactionOutput> item: UTXOs.entrySet()){
            TransactionOutput UTXO = item.getValue();
            total += UTXO.value;
            inputs.add(new TransactionInput(UTXO.id));
            if(total > value) break;
        }

        Transaction newTransaction = new Transaction(publicKey, _recipient , value, inputs);
        newTransaction.generateSignature(privateKey);

        for(TransactionInput input: inputs){
            UTXOs.remove(input.transactionOutputId);
        }
        return newTransaction;
    }
}

