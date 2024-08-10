import java.util.concurrent.Semaphore;

public abstract class UserlandProcess implements Runnable {
	private Thread thread;
	private final Semaphore semaphore = new Semaphore(0);
	private boolean quantumExpired = false;
	private static int PAGE_SIZE = 1024;
	private static byte[] memory = new byte[1024 * 1024];
	private static int[][] TLB = { { -1, -1 }, { -1, -1 } }; // virtualPage, physicalPage]

	public static int[][] getTlb() {
		return TLB;
	}

	public byte Read(int address) {
		int virtualPage = address / PAGE_SIZE;
		int offset = address % PAGE_SIZE;
		for (int i = 0; i < TLB.length; i++) {
			if (TLB[i][0] == virtualPage) { // TLB hit
				int physicalPage = TLB[i][1];
				return memory[physicalPage * PAGE_SIZE + offset];
			}
		}
		// TLB miss
		OS.getMapping(virtualPage);
		return Read(address);
	}

	public void Write(int address, byte value) {

		int virtualPage = address / PAGE_SIZE;
		int offset = address % PAGE_SIZE;
		for (int i = 0; i < TLB.length; i++) {
			if (TLB[i][0] == virtualPage) { // TLB hit
				int physicalPage = TLB[i][1];
				memory[physicalPage * PAGE_SIZE + offset] = value;
				return;
			}
		}
		
		// TLB miss
		OS.getMapping(virtualPage);
		Write(address, value);
	}

	public UserlandProcess() {
		this.thread = new Thread(this);
	}

	public void requestStop() {
		this.quantumExpired = true;
	}

	public abstract void main() throws Exception;

	public boolean isStopped() {
		return semaphore.availablePermits() == 0;
	}

	public boolean isDone() {
		return !thread.isAlive();
	}

	public void start() {
		semaphore.release();
		if (!thread.isAlive()) {
			thread.start();
		}
	}

	public void stop() {
		try {
			semaphore.acquire();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	@Override
	public void run() {
		try {
			semaphore.acquire();
			main();
		} catch (Exception e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}
	}

	protected void cooperate() {
		if (quantumExpired) {
			quantumExpired = false;
			OS.switchProcess();
		}
	}

}