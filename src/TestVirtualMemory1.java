
public class TestVirtualMemory1 extends UserlandProcess {
	
	public void main() {
		
		System.out.println();
		System.out.println("Test1: Initally allocating memory.");
	    int allocatedMemoryAddress = OS.AllocateMemory(2 * 1024 );
	    OS.sleep(10);
	    System.out.println("Test1: Memory address: " + allocatedMemoryAddress);
	    OS.sleep(100);
	    int i = 121;
	    System.out.println("Test1: Writing a value: " + i);
	    Write(allocatedMemoryAddress, (byte) i);
	    OS.sleep(10000);
	    byte readValue = Read(allocatedMemoryAddress);
	    System.out.println("Test1: Attempt to read value. Read value: " + readValue);
	    if (readValue == i) {
	        System.out.println("Test1: Test Passed");
	    } else {
	        System.out.println("Test1: Test Failed");
	    }
	    
	    
	    OS.FreeMemory(allocatedMemoryAddress, 20 * 1024); // Free the allocated memory
	    System.out.println("Test1: Freed allocated memory.");
	    
	    cooperate();
}
}
