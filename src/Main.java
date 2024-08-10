public class Main {
    public static void main(String[] args) {
    	
    	//OS.startup(new TestRandomDeviceProcess());
    	//OS.createProcess(new TestFileSystemProcess());
    	//OS.createProcess(new TestFileSystemProcess2());
    	//OS.createProcess(new TestFileSystemProcess3());
    	
    	//OS.startup(new TestProcessTLB());
    	//OS.startup(new TestVirtualMemory1());
    	OS.startup(new TestVirtualMemory1());
    	OS.createProcess(new TestVirtualMemory2());
    	//OS.createProcess(new Piggy());
    	//OS.startup(new Pong());
    	//OS.createProcess(new TestProcessTLB2());
    	
    	//OS.createProcess(new Ping());
    	
    }
    
}
