package oreo.com.bittext;
import java.util.ArrayList;

public class Block {
    private String currentHash;
    private String previousHash;
    private String message;
    private String recipient;
    private String sender;
    private long timeStamp;
    private int nonce;

    public Block(String previousHash, String message, String recipient, String sender) {
        this.previousHash = previousHash;
        this.message = message;
        this.timeStamp = System.currentTimeMillis();
        this.currentHash = generateHash();
        this.recipient = recipient;
        this.sender = sender;

    }

    public String getCurrentHash() {
        return currentHash;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public String generateHash() {
        String hash = Hasher.applySha256(previousHash + Long.toString(timeStamp) + Integer.toString(nonce)+message);
        return hash;
    }

    public String mineHash(int nonce){
        String hash = Hasher.applySha256(previousHash + Long.toString(timeStamp) + Integer.toString(nonce)+ message);
        return hash;
    }

    public void mineBlock(int difficulty, int nonce) {
        String target = new String(new char[difficulty]).replace('\0', '0');
        while (!currentHash.substring(0, difficulty).equals(target)) {
            nonce++;
            currentHash = mineHash(nonce);
        }
        this.nonce = nonce;
        System.out.println("Block Mined!!! : " + currentHash);
    }

}
