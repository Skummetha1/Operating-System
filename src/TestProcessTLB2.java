
public class TestProcessTLB2 extends UserlandProcess{
	
	public void main() {
		
		for(int i = 0; i< 5; i++) {
		System.out.println();
		System.out.println("Test two started.");
		System.out.println("Allocate memory");
        int allocatedMemoryAddress = OS.AllocateMemory(1024); // Allocate memory
        System.out.println("Write to alocated memory");
        Write(allocatedMemoryAddress, (byte) 111); // Use the allocated memory
        OS.sleep(20);
        System.out.println("Free allocated memory.");
        OS.FreeMemory(allocatedMemoryAddress, 1024); // Free the allocated memory
        OS.sleep(5000);
        
        // Attempt to reallocate memory to see if the system properly reuses freed memory
        System.out.println("Reallocating memory to see if the system properly reuses freed memory.");
        int reallocatedMemoryAddress = OS.AllocateMemory(1024);
        
        if (allocatedMemoryAddress == reallocatedMemoryAddress) {
        	System.out.println();
            System.out.println("Memory Re-allocation Test Passed");
        } 
        
        cooperate();
    }
	}

}
