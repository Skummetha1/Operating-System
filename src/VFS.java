import java.util.HashMap;
import java.util.Map;

public class VFS {
	// Maps a VFS ID to a device and its corresponding device ID.
	private static Map<Integer, DeviceEntry> deviceMap = new HashMap<>();
	private int nextVfsId = 0; // Counter to generate unique VFS IDs

	// Inner class to represent an entry in the device map.
	// It holds a reference to a device and its associated device-specific ID.
	private class DeviceEntry {
		Device device;
		int deviceId;

		// Constructor to initialize a device entry
		DeviceEntry(Device device, int deviceId) {
			this.device = device;
			this.deviceId = deviceId;
		}
	}

	public int Open(String input) throws Exception {
		// Split the input string into device name and details.
		String[] parts = input.split(" ", 2);
		String deviceName = parts[0];
		String details;
		if (parts.length > 1) {
			details = parts[1];
		} else {
			details = "";
		}

		Device device;
		// Determine the device type
		if ("random".equals(deviceName)) {
			device = new RandomDevice();
		} else if ("file".equals(deviceName)) {
			device = new FakeFileSystem();
		} else {
			throw new IllegalArgumentException("Unsupported device type: " + deviceName);
		}

		// Open the device with the provided details
		int deviceId = device.Open(details); // Open the device and get its specific ID.
		if (deviceId == -1) {
			throw new Exception("Failed to open device: " + deviceName);
		}

		// Create a new VFS ID for the device
		int vfsId = nextVfsId++;
		deviceMap.put(vfsId, new DeviceEntry(device, deviceId));
		return vfsId;
	}

	public void Close(int vfsId) {
		DeviceEntry entry = deviceMap.get(vfsId); // Retrieve the device entry from the map.
		if (entry != null) {
			entry.device.Close(entry.deviceId); // Close the device.
			deviceMap.remove(vfsId); // Remove the entry from the map once the device is closed.
		} else {
			throw new IllegalArgumentException("Invalid VFS ID: " + vfsId);
		}
	}

	public static byte[] Read(int vfsId, int size) {
		DeviceEntry entry = deviceMap.get(vfsId);
		if (entry != null) {
			// Perform the read operation on the device and return the data.
			return entry.device.Read(entry.deviceId, size);
		} else {
			throw new IllegalArgumentException("Invalid VFS ID: " + vfsId);
		}
	}

	public void Seek(int vfsId, int to) {
		DeviceEntry entry = deviceMap.get(vfsId);
		if (entry != null) {
			// Perform the seek operation on the device.
			entry.device.Seek(entry.deviceId, to);
		} else {
			throw new IllegalArgumentException("Invalid VFS ID: " + vfsId);
		}
	}

	// Writes data to a device associated with a given VFS ID.
	public static int Write(int vfsId, byte[] data) {
		DeviceEntry entry = deviceMap.get(vfsId);
		if (entry != null) {
			// Perform the write operation on the device
			return entry.device.Write(entry.deviceId, data);
		} else {
			throw new IllegalArgumentException("Invalid VFS ID: " + vfsId);
		}
	}
}
