package com.zhao;

import java.security.*;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.GsonBuilder;

public class NoobChain {
    // 存储区块链的ArrayList
    public static ArrayList<Block> blockchain = new ArrayList<Block>();
    // 存储未花费交易输出的HashMap
    public static HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>();
    // 挖矿难度
    public static int difficulty = 5;
    // 最小交易金额
    public static float minimumTransaction = 0.1f;

    public static Wallet walletA;
    public static Wallet walletB;

    // 创世交易
    public static Transaction genesisTransaction;

    public static void main(String[] args) {

        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); // 设置Bouncy Castle作为安全提供者


        walletA = new Wallet();
        walletB = new Wallet();
        //创世钱包？
        Wallet coinbase = new Wallet();

        // 创建创世交易，向钱包A发送100个NoobCoin：
        genesisTransaction = new Transaction(coinbase.publicKey, walletA.publicKey, 100f, null);
        genesisTransaction.generateSignature(coinbase.privateKey);	 // 手动为创世交易签名
        genesisTransaction.transactionId = "0"; // 手动设置交易ID
        genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.reciepient, genesisTransaction.value, genesisTransaction.transactionId)); // 手动添加交易输出
        UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0)); // 将第一个交易存储在UTXOs列表中很重要。

        System.out.println("Creating and Mining Genesis block...\n ");
        // 创建创世区块
        Block genesisblock = new Block("0");
        genesisblock.addTransaction(genesisTransaction);
        addBlock(genesisblock);

        //testing
        Block block1 = new Block(genesisblock.hash);
        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("\nWalletA is Attempting to send funds (40) to WalletB...");
        block1.addTransaction(walletA.sendFunds(walletB.publicKey, 40f));
        block1.addTransaction(walletA.sendFunds(walletB.publicKey, 50f));

        addBlock(block1);
        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("WalletB's balance is: " + walletB.getBalance());

        Block block2 = new Block(block1.hash);
        System.out.println("\nWalletA Attempting to send more funds (1000) than it has...");
        block2.addTransaction(walletA.sendFunds(walletB.publicKey, 1000f));
        addBlock(block2);
        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("WalletB's balance is: " + walletB.getBalance());

        Block block3 = new Block(block2.hash);
        System.out.println("\nWalletB is Attempting to send funds (20) to WalletA...");
        block3.addTransaction(walletB.sendFunds( walletA.publicKey, 20));
        addBlock(block3);
        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("WalletB's balance is: " + walletB.getBalance());

        isChainValid();

    }

    public static Boolean isChainValid() {
        Block currentBlock;
        Block previousBlock;
        String hashTarget = new String(new char[difficulty]).replace('\0', '0');
        // 创建一个由difficulty个"0"组成的字符串，用于检查工作量证明
        HashMap<String,TransactionOutput> tempUTXOs = new HashMap<String,TransactionOutput>();
        // 将创世交易的输出添加到临时UTXO列表中
        tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

        // 遍历区块链以检查哈希值：
        for(int i=1; i < blockchain.size(); i++) {

            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i-1);
            // 比较注册的哈希值和计算出的哈希值：
            if(!currentBlock.hash.equals(currentBlock.calculateHash()) ){
                System.out.println("#当前哈希值不相等");
                return false;
            }
            // 比较前一个区块的哈希值和注册的前一个区块的哈希值
            if(!previousBlock.hash.equals(currentBlock.previousHash) ) {
                System.out.println("#前一个区块的哈希值不相等");
                return false;
            }
            //check if hash is solved
            if(!currentBlock.hash.substring( 0, difficulty).equals(hashTarget)) {
                System.out.println("#这个区块没有被挖矿");
                return false;
            }

            // 遍历区块中的交易：
            TransactionOutput tempOutput;
            for(int t=0; t <currentBlock.transactions.size(); t++) {
                Transaction currentTransaction = currentBlock.transactions.get(t);

                if(!currentTransaction.verifiySignature()) {
                    System.out.println("#交易(" + t + ")的签名无效");
                    return false;
                }
                if(currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
                    System.out.println("#交易(" + t + ")的输入值不等于输出值");
                    return false;
                }

                for(TransactionInput input: currentTransaction.inputs) {
                    tempOutput = tempUTXOs.get(input.transactionOutputId);

                    if(tempOutput == null) {
                        System.out.println("#交易(" + t + ")引用的输入缺失");
                        return false;
                    }

                    if(input.UTXO.value != tempOutput.value) {
                        System.out.println("#交易(" + t + ")引用的输入值无效");
                        return false;
                    }

                    tempUTXOs.remove(input.transactionOutputId);
                }

                for(TransactionOutput output: currentTransaction.outputs) {
                    tempUTXOs.put(output.id, output);
                }

                if( currentTransaction.outputs.get(0).reciepient != currentTransaction.reciepient) {
                    System.out.println("#交易(" + t + ")的输出接收者不正确");
                    return false;
                }
                if( currentTransaction.outputs.get(1).reciepient != currentTransaction.sender) {
                    System.out.println("#交易(" + t + ")的输出'找零'不是发送者");
                    return false;
                }

            }

        }
        System.out.println("区块链是有效的");
        return true;
    }

    public static void addBlock(Block newBlock) {
        newBlock.mineBlock(difficulty);
        blockchain.add(newBlock);
    }
}