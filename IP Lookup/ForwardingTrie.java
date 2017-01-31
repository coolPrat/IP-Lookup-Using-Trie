import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Formatter;

/*
This class represents the Forwarding table using trie.
 */
public class ForwardingTrie {

    /**
     * reader used to read the file. This is used to read files to build forwarding table
     * and file having ips to lookup.
     */
    private BufferedReader input;

    /**
     * Writer used to write results in a txt file.
     */
    private BufferedWriter bw;

    /**
     * root of trie.
     */
    private TreeNode root = new TreeNode("root");
    private Formatter formatter;

    /**
     * Constructor for ForwardingTrie. It takes output file as argument
     * @param outputFile    Filepath to write output.
     */
    ForwardingTrie(String outputFile) {
        try {
            this.bw = new BufferedWriter(new FileWriter(outputFile));
            this.bw.write("-lookup address-\t- Result -\t-\tnetwork\t-\n");
            this.formatter = new Formatter();
        } catch (Exception e) {
            System.out.println("Can't create output file");
            System.exit(0);
        }
    }

    /**
     * This method is used to add an entry in trie.
     * Entry is in classless addressing format. (a.b.c.d/s)
     * @param address    the entry to add in trie
     * @throws Exception
     */
    public void addInTrie(String address) throws IOException {
        TreeNode currentNode = this.root;
        String[] parts = address.split("/");
        String addrToAdd = parts[0];
        int subnetBits = Integer.parseInt(parts[1]);
        String addrInBits = MyForwarding.getBitString(addrToAdd, subnetBits);
        for (int i = 0; i < subnetBits; i++) {
            if (addrInBits.charAt(i) == '0') {
                if (currentNode.left == null) {
                    currentNode.left = new TreeNode("0");
                }
                if (i == subnetBits - 1) {
                    currentNode.left.value = addrInBits.substring(0, subnetBits);
                }
                currentNode = currentNode.left;
            } else if (addrInBits.charAt(i) == '1') {
                if (currentNode.right == null) {
                    currentNode.right = new TreeNode("1");
                }
                if (i == subnetBits - 1) {
                    currentNode.right.value = addrInBits.substring(0, subnetBits);
                }
                currentNode = currentNode.right;
            }
        }
    }

    /**
     * This function is used to lookup an address in trie.
     * It will write result of lookup to a file is write is true.
     *
     * @param address    address to lookup
     * @param addrInBits    address in binary format
     * @param write    set to true if we want to write output of the lookup.
     * @throws Exception    if address is in invalid format
     */
    public void searchInTrie(String address, String addrInBits, boolean write) throws IOException {
        String writeString = "";
        TreeNode currentNode = this.root;
        TreeNode ansNode = null;
        for (int i = 0; i < 32; i++) {
            if (addrInBits.charAt(i) == '0') {
                if (currentNode.left != null) {
                    if (currentNode.left.value.length() > 1 && currentNode.left.value.equals(addrInBits.substring(0, i+1))) {
                        ansNode = currentNode.left;
                    }
                    currentNode = currentNode.left;
                } else {
                    break;
                }
            } else if (addrInBits.charAt(i) == '1') {
                if (currentNode.right != null) {
                    if (currentNode.right.value.length() > 1 && currentNode.right.value.equals(addrInBits.substring(0, i+1))) {
                        ansNode = currentNode.right;
                    }
                    currentNode = currentNode.right;
                } else {
                    break;
                }
            }
        }

        if (write) {
            this.bw.write(String.format("%16s\t%10s\t%16s\n", address, (ansNode != null)? "Found":"Not found",
                    (ansNode != null)? MyForwarding.getAddr(ansNode.value) : "0.0.0.0(default)"));
        }
    }


    /**
     * This method will create a trie from a file.
     * @param fileName    file path of the file
     * @throws Exception    if file is not found
     */
    public void buildBinaryTrie(String fileName) throws Exception {
        this.input = new BufferedReader(new FileReader(fileName));
        String line = this.input.readLine();
        while (line !=null) {
            this.addInTrie(line);
            line = this.input.readLine();
        }
    }

    /**
     * This method will be used to lookup all the addresses in a file.
     * it will run lookup times times for evaluating the performance.
     *
     * @param fileName    file path of the file
     * @param times    number of times we need to run lookup.
     * @param write    set if we want to write output of lookup
     *                 to a file
     * @return    long    time required to lookup all the addresses in a file
     * @throws Exception    if File is not found or any address in file is invalid
     */
    public long lookupInTrie(String fileName, int times, boolean write) throws Exception{
        Object lines[] = Files.readAllLines(Paths.get(fileName)).toArray();
        String binLines[] = new String [lines.length];
        for(int i = 0; i < lines.length; i++) {
            binLines[i] = MyForwarding.getBitString((String)lines[i], -1);
        }
        long t1 = System.currentTimeMillis();
        for (int t = 0; t < times; t++) {
            for (int i = 0; i < lines.length; i++) {
                this.searchInTrie((String) lines[i], binLines[i], write);
            }
        }
        long t2 = System.currentTimeMillis() - t1;
        if (write)
            this.bw.close();
        return t2;
    }
}



class TreeNode {
    String value;
    TreeNode right;
    TreeNode left;


    TreeNode() {
        this.right = null;
        this.left = null;
    }

    TreeNode(String value) {
        this.value = value;
    }


    public boolean equals(Object o) {
        if (o instanceof TreeNode) {
            return this.value.equals(((TreeNode)o).value);
        }
        return false;
    }

}