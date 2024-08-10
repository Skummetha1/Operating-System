
public class TestVirtualMemory2 extends UserlandProcess {
	public void main() {
		
	System.out.println();
	
	System.out.println("Test2: Initally allocating memory.");
    int allocatedMemoryAddress = OS.AllocateMemory(2 * 1024);
    System.out.println("Test2: Memory address: " + allocatedMemoryAddress);
    OS.sleep(100);
    int i = 35;
    System.out.println("Test2: Writing a value: " + i);
    Write(allocatedMemoryAddress, (byte) i);
    byte readValue = Read(allocatedMemoryAddress);
    System.out.println("Test2: Attempt to read value. Read value: " + readValue);
    if (readValue == i) {
        System.out.println("Test2: Test Passed");
    } else {
        System.out.println("Test2: Test Failed");
    }
    
    
    OS.FreeMemory(allocatedMemoryAddress, 2 * 1024); // Free the allocated memory
    System.out.println("Test2: Freed allocated memory.");
    cooperate();
    
    
}
}
