/*
This class is used in ForwardingTable to store network-prefix and number of bits
in subnet mask.
 */
public class TableEntry {
    /**
     * The network prefix
     */
    private String networkPrefix;

    /**
     * number of bits in subnet mask
     */
    private int subnetBits;

    /**
     * Constructor for TableEntry
     * @param networkPrefix    String   number of bits in subnet mask
     */
    TableEntry(String networkPrefix, int subnetBits) {
        this.networkPrefix = networkPrefix;
        this.subnetBits = subnetBits;
    }

    /**
     * Getter for network prefix
     * @return    String     network prefix
     */
    public String getNetworkPrefix() {
        return networkPrefix;
    }

    /**
     * Getter for subnetBits
     * @return    int    bits in network prefix
     */
    public int getSubnetBits() {
        return subnetBits;
    }
}
