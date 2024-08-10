
public class KernelMessage {
    private int senderPid;
	private int targetPid;
    private int what;
    private byte[] data;

    // Constructor
    public KernelMessage(int senderPid, int targetPid, int what, byte[] data) {
        this.senderPid = senderPid;
        this.targetPid = targetPid;
        this.what = what;
        this.data = data.clone(); // Clone the data array to ensure data integrity
    }

    // Copy constructor
    public KernelMessage(KernelMessage other) {
        this.senderPid = other.senderPid;
        this.targetPid = other.targetPid;
        this.what = other.what;
        this.data = other.data.clone(); // Clone the data array to ensure data integrity
    }

    // Getter methods
    public int getSenderPid() {
        return senderPid;
    }
    
    public void setSenderPid(int senderPid) {
		this.senderPid = senderPid;
	}

    public int getTargetPid() {
        return targetPid;
    }

    public int getWhat() {
        return what;
    }

    public byte[] getData() {
        return data.clone(); // Return a clone to ensure data integrity when accessed
    }

  
    @Override
    public String toString() {
        String dataString = new String(data); // Convert data to a String
        return "KernelMessage{" +
               "senderPid=" + senderPid +
               ", targetPid=" + targetPid +
               ", messageType=" + what +
               ", data=" + dataString +
               '}';
    }
}
