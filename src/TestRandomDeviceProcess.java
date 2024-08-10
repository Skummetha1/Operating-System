import java.util.Arrays;

public class TestRandomDeviceProcess extends UserlandProcess {

	public void main() throws Exception {
		for (int j = 0; j < 4; j++) {
			int deviceId = OS.open("random");
			if (deviceId == -1) {
				System.out.println("TestRandomDeviceProcess: Failed to open random device.");
				return;
			}

			else {
				System.out.println();
				System.out.println("TestRandomDeviceProcess: Open Random Sucessful");
			}

			// Assuming the process does multiple reads or is part of a loop
			// for (int i = 0; i < 5; i++) {
			byte[] data = OS.read(deviceId, 10);
			System.out.println("TestRandomDeviceProcess: Read Random Data: " + Arrays.toString(data));
			OS.seek(deviceId, 4);
			System.out.println("TestRandomDeviceProcess: Seek to 4th position");
			byte[] dataAfterSeek = OS.read(deviceId, 4);
			System.out
					.println("TestRandomDeviceProcess: Read After Seek Random Data: " + Arrays.toString(dataAfterSeek));

			OS.sleep(100);
			// Yield control back to the OS after each read operation
			cooperate();
			// OS.sleep(100);
			// }

			OS.close(deviceId);
			System.out.println("TestRandomDeviceProcess: Random device closed successfully.");
			// OS.sleep(60);
			// It's also a good practice to call cooperate() after closing the device,
			// in case the process continues to perform other operations.

			cooperate();

		}
	}
}