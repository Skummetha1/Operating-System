
public class TestFileSystemProcess extends UserlandProcess {

	@Override
	public void main() throws Exception {

		String file = ("file testfile.txt");
		int deviceId = OS.open(file);
		if (deviceId == -1) {
			System.out.println();
			System.out.println("TestFileSystemProcess: Failed to open file: " + file);
			return;
		} else {
			System.out.println();
			System.out.println("TestFileSystemProcess: Opened file: " + file);
		}

		// Write to the file
		String content = "Hello, File System!";
		OS.write(deviceId, content.getBytes());
		System.out.println("TestFileSystemProcess: Wrote to file: " + content);

		// Seek to the beginning of the file
		OS.seek(deviceId, 0);
		System.out.println("TestFileSystemProcess: Seek to the beginning of the file");

		// Read from the file
		byte[] readData = OS.read(deviceId, content.length());
		System.out.println("TestFileSystemProcess: Read from file: " + new String(readData));
		// control back to the OS after reading
		cooperate();

		OS.seek(deviceId, 7); // Seek to position 7, which is the start of File
		System.out.println("TestFileSystemProcess: Seek to position 7");
		byte[] data = OS.read(deviceId, 12);
		System.out.println("TestFileSystemProcess: Read from file: " + new String(data));

		// Close the file
		OS.close(deviceId);
		System.out.println("TestFileSystemProcess: File closed successfully.");

		OS.sleep(200);
		cooperate();

	}

}