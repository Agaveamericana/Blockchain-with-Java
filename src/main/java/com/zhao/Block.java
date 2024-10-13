package com.zhao;

import java.util.ArrayList;
import java.util.Date;

/**
 * @author Agave_americana_L.
 * @Description
 * @date 2024/10/6 下午12:24
 */
public class Block {

    public String hash;// 区块的哈希值
    public String previousHash;// 前一个区块的哈希值
    public String merkleRoot;// 默克尔根，用于校验区块内交易的完整性
    public ArrayList<Transaction> transactions = new ArrayList<Transaction>(); // 区块中包含的交易列表
    public long timeStamp; // 区块的时间戳，以1970年1月1日以来的毫秒数表示
    public int nonce; // 用于工作量证明的随机数


    public Block(String previousHash ) {
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();

        this.hash = calculateHash(); // 在设置其他值之后确保我们计算哈希值。
        System.out.println("首次创建的区块的HASH: " + this.hash);
    }

    // 根据区块内容计算新的哈希值
    public String calculateHash() {
        String calculatedhash = StringUtil.applySha256(
                previousHash +
                        Long.toString(timeStamp) +
                        Integer.toString(nonce) +
                        merkleRoot
        );
        return calculatedhash;
    }

    // 增加nonce值直到达到哈希目标
    public void mineBlock(int difficulty) {
        merkleRoot = StringUtil.getMerkleRoot(transactions);
        String target = StringUtil.getDificultyString(difficulty); // 创建一个由difficulty个"0"组成的字符串
        while(!hash.substring( 0, difficulty).equals(target)) {
            nonce ++;
            hash = calculateHash();
        }
        System.out.println("挖矿后区块更改的HASH: "+ hash);
        System.out.println("Block Mined!!! : " + hash);
    }

    // 将交易添加到这个区块
    public boolean addTransaction(Transaction transaction) {
        // 处理交易并检查其是否有效，除非是创世区块，否则忽略。
        if(transaction == null) return false;
        if((!"0".equals(previousHash))) {
            if((transaction.processTransaction() != true)) {
                System.out.println("Transaction failed to process. Discarded.");
                return false;
            }
        }

        transactions.add(transaction);
        System.out.println("Transaction Successfully added to Block");
        return true;
    }

}