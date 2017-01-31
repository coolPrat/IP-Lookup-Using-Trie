import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Formatter;
import java.util.HashMap;

/*
This class represents the Forwarding table using hashmap.
 */
public class ForwardingTable {

    /**
     * forwarding table as hashmap
     */
    private HashMap<String, TableEntry> forwardingTable = new HashMap<>();

    /**
     * reader used to read the file. This is used to read files to build forwarding table
     * and file having ips to lookup.
     */
    private BufferedReader input;

    /**
     * Writer used to write results in a txt file.
     */
    private BufferedWriter bw;
    private Formatter formatter;


    /**
     * Constructor for ForwardingTable. It takes output file as argument
     * @param outputFile    Filepath to write output.
     */
    ForwardingTable(String outputFile) {
        try {
            this.bw = new BufferedWriter(new FileWriter(outputFile));
            this.formatter = new Formatter();
            this.bw.write("-lookup address-\t- Result -\t-\tnetwork\t-\n");
        } catch (Exception e) {
            System.out.println("Can't create output file");
            System.exit(0);
        }
    }

    /**
     * This method is used to add an entry in forwarding table.
     * Entry is in classless addressing format. (a.b.c.d/s)
     * @param entry    the entry to add in forwarding table
     * @throws Exception
     */
    public void addInTable(String entry) throws Exception {
        String[] parts = entry.split("/");
        String addr = parts[0];
        int subnetBits = Integer.parseInt(parts[1]);

        String addressInBits = MyForwarding.getBitString(addr, subnetBits);
        forwardingTable.put(addressInBits, new TableEntry(MyForwarding.getAddr(addressInBits), subnetBits));
    }

    /**
     * This function is used to lookup an address in forwarding table.
     * It will write result of lookup to a file is write is true.
     *
     * @param address    address to lookup
     * @param addrInBits    address in binary format
     * @param write    set to true if we want to write output of the lookup.
     * @throws Exception    if address is in invalid format
     */
    public void lookup(String address, String addrInBits, boolean write) throws Exception {
        String temp = "", writeString;
        boolean resFound = false;
        for (int i = 0; i < 32; i++) {
            temp = addrInBits.substring(0, 32 - i);
            if (this.forwardingTable.containsKey(temp)) {
                if ((32 - i) == this.forwardingTable.get(temp).getSubnetBits()) {
                    resFound = true;
                    break;
                }
            }
        }

        if (write) {
            this.bw.write(String.format("%16s\t%10s\t%16s\n", address, resFound? "Found":"Not found",
                    resFound? this.forwardingTable.get(temp).getNetworkPrefix(): "0.0.0.0(default)"));
        }
    }

    /**
     * This method will create forwarding table from a file.
     * @param fileName    file path of the file
     * @throws Exception    if file is not found
     */
    public void buildForwardingTable(String fileName) throws Exception {
        System.out.println("Building forwarding table");
        this.input = new BufferedReader(new FileReader(fileName));
        String line = this.input.readLine();
        while (line !=null) {
            this.addInTable(line);
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
    public long lookupAllInFile(String fileName, int times, boolean write) throws Exception {
        Object lines[] = Files.readAllLines(Paths.get(fileName)).toArray();
        String binLines[] = new String [lines.length];
        for(int i = 0; i < lines.length; i++) {
            binLines[i] = MyForwarding.getBitString((String)lines[i], -1);
        }
        long t1 = System.currentTimeMillis();
        for (int t = 0; t < times; t++) {
            for (int i = 0; i < lines.length; i++) {
                this.lookup((String) lines[i], binLines[i], write);
            }
        }
        long t2 = System.currentTimeMillis() - t1;
        if (write)
            this.bw.close();
        return t2;
    }
}
