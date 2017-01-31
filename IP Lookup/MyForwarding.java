import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;

/*
This class used to run lookup using hashmap and trie and evaluate the result.
 */
public class MyForwarding {

    /**
     * Static method to convert a address in binary format. It will return only
     * valid bits, i.e. first subnetBits bits.
     *
     * if subnetBits is passed as -1 then it returns 32 bit string representing
     * entire address in binary.
     *
     * @param addr    address to convert in binary
     * @param subnetBits    number of bits in subnet
     * @return    String     binary representation of address
     * @throws UnknownHostException    if addr is invalid  IP address
     */
    public static String getBitString(String addr, int subnetBits) throws UnknownHostException {
        String zeroPad = "00000000000000000000000000000000";
        byte[] addrParts = InetAddress.getByName(addr).getAddress();
        StringBuilder addrInBin = new StringBuilder();
        String temp = "";
        for (int i = 0; i < 4; i++) {
            temp = Integer.toBinaryString(addrParts[i] & 255);
            if (temp.length() < 8) {
                temp = zeroPad.substring(0, 8 - temp.length()) + temp;
            }
            addrInBin.append(temp);
        }
        if (subnetBits > 0 && subnetBits < 32) {
            addrInBin.delete(subnetBits, addrInBin.length());
        }
        return addrInBin.toString();
    }

    /**
     * This method is used to convert a binary address String into
     * corresponding decimal number
     * @param addrInBin    address in binary
     * @return    String in decimal format
     */
    public static String getAddr(String addrInBin) {
        String addr = "";
        int len = 0;

        for (int i = 0; i < addrInBin.length(); i = i + 8) {
            len = (i+8 > addrInBin.length()) ? addrInBin.length() : i + 8;
            addr += Integer.parseInt(addrInBin.substring(i, len), 2);
            if (i + 8 < addrInBin.length()) {
                addr += ".";
            }
        }
        int diff = addr.length() - addr.replace(".", "").length();
        if (diff != 3) {
            for(int i = 0; i < 3 - diff; i++) {
                addr += ".0";
            }
        }

        return addr;
    }

    private static boolean checkFile(String file1, String file2) throws IOException {
        Object[] linesInFile1 = Files.readAllLines(Paths.get(file1)).toArray();
        Object[] linesInFile2 = Files.readAllLines(Paths.get(file2)).toArray();
        boolean result = linesInFile1.length == linesInFile2.length;
        for (int i = 0; (i < linesInFile1.length) && result; i ++) {
            result = ((String)linesInFile1[i]).equals((String)linesInFile2[i]);
        }

        return result;
    }

    /**
     * The main program.
     * This will ask for file path for building table and lookup file.
     * @param args    command line arguments (ignored)
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        String dir = System.getProperty("user.dir");

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter filepath to build forwarding table: ");
        String buildFilePath = in.readLine();
        System.out.println("Enter filepath to lookup file: ");
        String lookupFilePath = in.readLine();
        String out = dir + "/result_hashmap.txt";

        ForwardingTable fwdTable = new ForwardingTable(out);


        //"/media/pratik/New Volume/StudyStuff/SEM 3/FCN/Forwarding/src/build_table.txt"
        fwdTable.buildForwardingTable(buildFilePath);

        //"/media/pratik/New Volume/StudyStuff/SEM 3/FCN/Forwarding/src/lookup.txt"

        System.out.println("Running lookup for 100 times using hashmap");
        long t1 = fwdTable.lookupAllInFile(lookupFilePath, 100, false);

        System.out.println("Time required for lookup: " + t1);

        System.out.println("Writing results to file " + out);
        fwdTable.lookupAllInFile(lookupFilePath, 1, true);

        out = dir + "/result_trie.txt";
        ForwardingTrie fwdTrie = new ForwardingTrie(out);

        fwdTrie.buildBinaryTrie(buildFilePath);
        System.out.println("Running lookup for 100 times using trie");
        long t2 = fwdTrie.lookupInTrie(lookupFilePath, 100, false);

        System.out.println("Time required for lookup: " + t2);

        System.out.println("Writing results to file " + out);
        fwdTrie.lookupInTrie(lookupFilePath, 1, true);

        if (checkFile(dir + "/result_hashmap.txt", dir + "/result_trie.txt")) {
            System.out.println("Both the files are same");
        } else {
            System.out.println("Files are not same");
        }

    }



}
