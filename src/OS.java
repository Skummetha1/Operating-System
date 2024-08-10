import java.util.ArrayList;

public class OS {
	private static Kernel kernel;
	private static Scheduler scheduler;
	public static CallType currentCall;
	public static ArrayList<Object> parameters = new ArrayList<>();
	public static Object returnValue;

	public enum CallType {
		CREATE_PROCESS, SWITCH_PROCESS, SLEEP
	}

	static {

		scheduler = new Scheduler();
	}
	
	public static int AllocateMemory(int size) {
	if(size % 1024 == 0) {
	    return kernel.AllocateMemory(size);
	}
	else return -1;
	
	
    }
	
	
	public static boolean FreeMemory(int pointer, int size) {
		if (pointer % 1024 != 0 || size % 1024 != 0) {
	        return false;
	    }
		
		return kernel.FreeMemory(pointer, size);
	}
	
	public static void getMapping(int virtualPage) { 
        PCB.getMapping(virtualPage, scheduler);
    }
	
	// returns the current process’ pid
	public static int getPid() {
		return kernel.getPid();
	}
	
	// returns the pid of a process with that name
	public static int GetPidByName(String name){
		return kernel.GetPidByName(name);
	}

	public static void sendMessage(KernelMessage message) {
		kernel.sendMessage(message);
	}
	
	public static KernelMessage waitForMessage() {
        // Assuming the current process calls Kernel to wait for a message
        return kernel.WaitForMessage();
    }
	
	public static void sleep(int milliseconds) {
		parameters.clear();
		parameters.add(milliseconds);
		currentCall = CallType.SLEEP;
		switchToKernel();

	}

	public static int createProcess(UserlandProcess up) {
		return createProcess(up, PCB.Priority.INTERACTIVE); // Default priority
	}

	public static int createProcess(UserlandProcess up, PCB.Priority priority) {
		parameters.clear();
		PCB process = new PCB(up, priority);
		parameters.add(process);
		currentCall = CallType.CREATE_PROCESS;

		switchToKernel();

		System.out.println("New process was created with PID: " + process.getPid());
		return process.getPid();
	}

	public static void startup(UserlandProcess up, PCB.Priority priority, FakeFileSystem file) {
		kernel = new Kernel();
		createProcess(up, priority);
		// createProcess(new IdleProcess());
		
	}

	public static void startup(UserlandProcess up) {
		kernel = new Kernel();
		createProcess(up, PCB.Priority.INTERACTIVE);
		// createProcess(new IdleProcess());
	}

	private static void switchToKernel() {
		// start kernal thread
		kernel.start();

		if (scheduler.getCurrentlyRunning() != null) {
			scheduler.getCurrentlyRunning().stop();
		}
		// If no process is running then wait
		if (scheduler.getCurrentlyRunning() == null) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	public static void switchProcess() {
		currentCall = CallType.SWITCH_PROCESS;
		switchToKernel();
	}

	// Method for userland processes to open a device
	public static int open(String deviceName) throws Exception {
		// The kernel handles the actual opening of the device and returns a device id
		return kernel.Open(deviceName);
	}

	// Method for userland processes to read from a device
	public static byte[] read(int deviceId, int size) {
		// read operation to the kernel
		return kernel.Read(deviceId, size);
	}

	// Method for userland processes to write to a device
	public static int write(int deviceId, byte[] data) {
		// write operation to the kernel
		return kernel.Write(deviceId, data);
	}

	// Method for userland processes to seek within a device
	public static void seek(int deviceId, int position) {
		// seek operation to the kernel
		kernel.Seek(deviceId, position);
	}

	// Method for userland processes to close a device
	public static void close(int deviceId) {
		// close operation to the kernel
		kernel.Close(deviceId);
	}



	

}