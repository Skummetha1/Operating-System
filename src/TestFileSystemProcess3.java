public class TestFileSystemProcess3 extends UserlandProcess {
    

    @Override
    public void main() throws Exception {
    	
    	String file = ("file testfile3.txt");
        int deviceId = OS.open(file);
        if (deviceId == -1) {
        	System.out.println();
            System.out.println("TestFileSystemProcess3: Failed to open file: "+ file);
            return;
        }
        else {
        	System.out.println();
        	System.out.println("TestFileSystemProcess3: Opened file: " + file);
        }



        // Write to the file
        String content = "Shreya Kummetha";
        OS.write(deviceId, content.getBytes());
        System.out.println("TestFileSystemProcess3: Wrote to file: " + content);
        

        
        OS.seek(deviceId, 6);
        System.out.println("TestFileSystemProcess3: Seek to the sixth position");
        
       

        // Read from the file
        byte[] readData = OS.read(deviceId, 9);
        System.out.println("TestFileSystemProcess3: Read from file: " + new String(readData));
        // control back to the OS after reading
        //OS.sleep(50);
        cooperate();

        
        
        // Close the file
        OS.close(deviceId);
        System.out.println("TestFileSystemProcess3: File closed successfully.");

        OS.sleep(100);
        cooperate();
            
    	}
    
}