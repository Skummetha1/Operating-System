import java.util.Random;

public class RandomDevice implements Device {

	// Array to hold Random instances, so multiple devices can be opened
	private Random[] random = new Random[10];

	@Override

	public int Open(String seed) {
		for (int i = 0; i < random.length; i++) {
			if (random[i] == null) {
				// Check if seed is provided
				if (seed != null && !seed.isEmpty()) {
					try {
						// Attempt to parse the seed as an integer and use it to create a new Random
						// instance
						int parsedSeed = Integer.parseInt(seed);
						random[i] = new Random(parsedSeed);
					} catch (NumberFormatException e) {
						// If seed is not a valid integer
						random[i] = new Random();
					}
				} else {
					// If no seed is provided, use the default Random constructor
					random[i] = new Random();
				}
				return i; // Return the index of the newly created Random instance
			}
		}
		return -1; // if there is an error
	}

	@Override
	public void Close(int id) {
		// Validate that the ID is within bounds and close the device if valid
		if (id >= 0 && id < random.length) {
			random[id] = null;
		}
	}

	@Override
	public byte[] Read(int id, int size) {
		// Check if the device ID is valid and the Random instance is initialized
		if (id >= 0 && id < random.length && random[id] != null) {
			byte[] buffer = new byte[size];
			random[id].nextBytes(buffer); // Fill the buffer with random bytes
			return buffer;
		}
		return null;
	}

	public void Seek(int id, int to) {
		// Check if the device ID is valid and the Random instance is initialized
		if (id >= 0 && id < random.length && random[id] != null) {
			byte[] buffer = new byte[to];
			random[id].nextBytes(buffer);
		}
	}

	@Override
	public int Write(int id, byte[] data) {

		return 0;
	}

}