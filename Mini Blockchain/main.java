import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class MiniBlockchain {
    public static class Block {
        public String hash;
        public String previousHash;
        private String data;
        private long timeStamp;
        private int nonce;

        public Block(String data, String previousHash) {
            this.data = data;
            this.previousHash = previousHash;
            this.timeStamp = System.currentTimeMillis();
            this.hash = calculateHash();
        }

        public String calculateHash() {
            try {
                String input = previousHash + Long.toString(timeStamp) + Integer.toString(nonce) + data;
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hashBytes = digest.digest(input.getBytes("UTF-8"));
                StringBuffer hexString = new StringBuffer();
                for (byte b : hashBytes) {
                    String hex = Integer.toHexString(0xff & b);
                    if (hex.length() == 1) hexString.append('0');
                    hexString.append(hex);
                }
                return hexString.toString();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public void mineBlock(int difficulty) {
            String target = new String(new char[difficulty]).replace('\0', '0'); // String with difficulty * "0"
            while (!hash.substring(0, difficulty).equals(target)) {
                nonce++;
                hash = calculateHash();
            }
            System.out.println("Block mined: " + hash);
        }
    }

    private List<Block> blockchain = new ArrayList<>();
    private int difficulty;

    public MiniBlockchain(int difficulty) {
        this.difficulty = difficulty;
        // Add genesis block
        blockchain.add(createGenesisBlock());
    }

    private Block createGenesisBlock() {
        System.out.println("Creating genesis block...");
        Block genesis = new Block("Genesis Block", "0");
        genesis.mineBlock(difficulty);
        return genesis;
    }

    public void addBlock(String data) {
        Block previousBlock = blockchain.get(blockchain.size() - 1);
        Block newBlock = new Block(data, previousBlock.hash);
        System.out.println("Mining new block...");
        newBlock.mineBlock(difficulty);
        blockchain.add(newBlock);
    }

    public boolean isChainValid() {
        for (int i = 1; i < blockchain.size(); i++) {
            Block current = blockchain.get(i);
            Block previous = blockchain.get(i - 1);

            if (!current.hash.equals(current.calculateHash())) {
                System.out.println("Current hash is invalid.");
                return false;
            }
            if (!current.previousHash.equals(previous.hash)) {
                System.out.println("Previous hash does not match.");
                return false;
            }
        }
        return true;
    }

    public void printBlockchain() {
        System.out.println("Blockchain:");
        for (int i = 0; i < blockchain.size(); i++) {
            Block block = blockchain.get(i);
            System.out.printf("Block %d:\n", i);
            System.out.println("   Data: " + block.data);
            System.out.println("   Hash: " + block.hash);
            System.out.println("   Previous Hash: " + block.previousHash);
            System.out.println("   Nonce: " + block.nonce);
            System.out.println();
        }
    }

    public static void main(String[] args) {
        MiniBlockchain miniChain = new MiniBlockchain(4);  // Difficulty level

        miniChain.addBlock("First block data");
        miniChain.addBlock("Second block data");
        miniChain.addBlock("Third block data");

        miniChain.printBlockchain();

        System.out.println("Blockchain valid? " + miniChain.isChainValid());
    }
}
