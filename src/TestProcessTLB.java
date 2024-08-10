
public class TestProcessTLB extends UserlandProcess{
	
	public void main() {
		
	for (int i = 123; i< 126; i++) {
		
		   System.out.println();
		   System.out.println("Test one started.");
		   System.out.println("Initally allocating memory.");
           int allocatedMemoryAddress = OS.AllocateMemory(2048);
           
           
           // TLB miss on first access
           byte initialValue = Read(allocatedMemoryAddress); 
           System.out.println("Attempt to read without write. Read value: " + initialValue);
           // Write a value
           System.out.println("Writing a value");
           Write(allocatedMemoryAddress, (byte) i);
           OS.sleep(10000);
           
           // TLB hit
           byte readValue = Read(allocatedMemoryAddress);
           System.out.println("Attempt to read value. Read value: " + readValue);
           
           
           
           if (readValue == i) {
               System.out.println("TLB Test Passed");
           } else {
               System.out.println("TLB Test Failed");
           }
           OS.sleep(1000);
           
           cooperate();
        
}
	}
}

   
