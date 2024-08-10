import java.io.RandomAccessFile;

public class FakeFileSystem implements Device {

	private RandomAccessFile[] files = new RandomAccessFile[10];

	
	public int Open(String filename) throws Exception {
		if (filename == null || filename.isEmpty()) {
			throw new Exception("Filename cannot be null or empty.");
		}

		for (int i = 0; i < files.length; i++) {
			if (files[i] == null) {
				// create and open file
				files[i] = new RandomAccessFile(filename, "rw");
				return i;
			}
		}
		// fail to open file
		return -1;
	}

	@Override
	public void Close(int id) {
		try {
			// close file with specified id
			if (id >= 0 && id < files.length && files[id] != null) {
				files[id].close();
				files[id] = null;
			}
		} catch (Exception e) {

		}
	}

	@Override
	// read data from file
	public byte[] Read(int id, int size) {
		try {
			if (id >= 0 && id < files.length && files[id] != null) {
				// create buffer
				byte[] buffer = new byte[size];
				// read buffer
				files[id].read(buffer);
				return buffer;
			}
		} catch (Exception e) {

		}
		// else return empty buffer
		return new byte[0];
	}

	@Override
	// go to a specific ndex
	public void Seek(int id, int to) {
		try {
			if (id >= 0 && id < files.length && files[id] != null) {
				files[id].seek(to);
			}
		} catch (Exception e) {

		}
	}

	@Override
	// write specified string to id
	public int Write(int id, byte[] data) {
		try {
			if (id >= 0 && id < files.length && files[id] != null) {
				files[id].write(data);
				return data.length;
			}
		} catch (Exception e) {

		}
		return 0;
	}
}
