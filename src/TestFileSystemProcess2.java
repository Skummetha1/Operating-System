public class TestFileSystemProcess2 extends UserlandProcess {
    

    @Override
    public void main() throws Exception {
    	
    	String file = ("file testfile2.txt");
        int deviceId = OS.open(file);
        if (deviceId == -1) {
        	System.out.println();
            System.out.println("TestFileSystemProcess2: Failed to open file: "+ file);
            return;
        }
        else {
        	System.out.println();
        	System.out.println("TestFileSystemProcess2: Opened file: " + file);
        }


        // Write to the file
        String content = "1 2 3 4 5 6 7 8 9 10";
        OS.write(deviceId, content.getBytes());
        System.out.println("TestFileSystemProcess2: Wrote to file: " + content);
       

        // Seek to the beginning of the file
        OS.seek(deviceId, 0);
        System.out.println("TestFileSystemProcess2: Seek to the beginning of the file");
        

        // Read from the file
        byte[] readData = OS.read(deviceId, content.length());
        System.out.println("TestFileSystemProcess2: Read from file: " + new String(readData));
        // Yield control back to the OS after reading
        cooperate();

        
        OS.seek(deviceId, 3); 
        System.out.println("TestFileSystemProcess2: Seek to position 3");
        byte[] data = OS.read(deviceId, 15);
        System.out.println("TestFileSystemProcess2: Read from file: " + new String(data));
        
        
        
        // Close the file
        OS.close(deviceId);
        System.out.println("TestFileSystemProcess2: File closed successfully.");
        // Yield control back to the OS after closing the file
        OS.sleep(100);
        cooperate();
            
    	}
    
}