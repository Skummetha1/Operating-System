
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class Kernel implements Runnable, Device {
	private Thread thread;
	private final Semaphore semaphore = new Semaphore(0);
	private Scheduler scheduler; // Manages process scheduling
	private VFS vfs; // Virtual File System to manage devices
	int[] deviceIds; // Array to track devices opened by the current process
	private Map<Integer, PCB> waitingProcessMap = new HashMap<>();
	private static final boolean[] freeList = new boolean[1024]; // Track if physical pages are in use
	private static final int PAGE_SIZE = 1024;
	private FakeFileSystem fileSystem;
    private int swapFileDescriptor;
    private static final String SWAP_FILENAME = "swapfile.swap";
    

	// Starts the kernel thread releasing the semaphore
	public Kernel() {
		this.thread = new Thread(this);
		this.scheduler = new Scheduler(this);
		this.vfs = new VFS();
		Arrays.fill(freeList, true);
		fileSystem = new FakeFileSystem();
        openSwapFile();
		
	}
	
	public static boolean[] getFreelist() {
		return freeList;
	}

	public void start() {
		semaphore.release();
		if (!thread.isAlive()) {
			thread.start();
		}
	}
	

	// Main loop of the kernel
	public void run() {
		try {
			while (true) {
				semaphore.acquire(); // Wait for a signal to process a system call
				processSystemCall();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	private void openSwapFile() {
        try {
            swapFileDescriptor = fileSystem.Open(SWAP_FILENAME);
            if (swapFileDescriptor == -1) {
                throw new Exception("Failed to open swap file.");
            }
        } catch (Exception e) {
            System.err.println("Error opening swap file: " + e.getMessage());
        }
    }
	
	public static byte[] getPhysicalMemory(int physicalPage) {
        
        if (PCB.getPagetable()[physicalPage] != null && PCB.getPagetable()[physicalPage].data != null) {
            return PCB.getPagetable()[physicalPage].data;  
        } else {
            throw new RuntimeException("No data available for physical page: " + physicalPage);
        }
    }

   
	
    public int AllocateMemory(int size) {
        int numPages = size / PAGE_SIZE;
        for (int i = 0; i <= PCB.getPagetable().length - numPages; i++) {
            if (checkVirtualPagesFree(i, numPages)) {  // check if virtual pages free
                for (int j = 0; j < numPages; j++) {
                    PCB.getPagetable()[i + j] = new VirtualToPhysicalMapping();
                }
                return i * PAGE_SIZE; // virtual address
            }
        }
        return -1; // no virtual memory available
    }
    
	
    private boolean checkVirtualPagesFree(int startIndex, int numPages) {
        for (int i = startIndex; i < startIndex + numPages; i++) {
            if (PCB.getPagetable()[i] != null) {
                return false;  // already in use
            }
        }
        return true;  // virtual page is free
    }

	

	
    public boolean FreeMemory(int pointer, int size) {
        int startVirtualPage = pointer / PAGE_SIZE;
        int numPages = size / PAGE_SIZE;

        for (int i = 0; i < numPages; i++) {
            int pageIndex = startVirtualPage + i;
            VirtualToPhysicalMapping mapping = PCB.getPagetable()[pageIndex];
            if (mapping != null) {
                if (mapping.physicalPageNumber != -1) {
                    freeList[mapping.physicalPageNumber] = true;  // free physical page
                }
                
                PCB.getPagetable()[pageIndex] = null;  
            }
        }
        return true;  
    }
    

	// returns the current process’ pid
	public int getPid() {
		return scheduler.getPid();
	}

	// returns the pid of a process with that name
	public int GetPidByName(String name) {
		return scheduler.GetPidByName(name);
	}

	public void sendMessage(KernelMessage km) {
		// Create a copy of the message to maintain process isolation
		KernelMessage messageCopy = new KernelMessage(km);

		// Set the sender's PID for security
		messageCopy.setSenderPid(getPid());

		// Find the target process using the PID from the message
		PCB targetProcess = scheduler.getAllProcesses().get(messageCopy.getTargetPid());

		if (targetProcess != null) {
			// Add the message to the target's message queue
			targetProcess.getMessageQueue().add(messageCopy);

			// If the target process is waiting for a message, restore it to runnable state
			if (targetProcess.isWaitingForMessage()) {
				RestoreToRunnableQueue(targetProcess);
			}
		} else {
			// Handle the case where the target process doesn't exist
			System.out.println("Target process not found.");
		}

	}

	public void RestoreToRunnableQueue(PCB targetProcess) {
		scheduler.ResoreToRunnableQueue(targetProcess);
	}

	KernelMessage WaitForMessage() {
		// Get the current process
		PCB currentProcess = scheduler.getCurrentlyRunning();

		if (!currentProcess.getMessageQueue().isEmpty()) {
			// If there is already a message, return it and remove from the queue
			return currentProcess.getMessageQueue().remove();
		} else {
			// No message available, de-schedule the current process
			DeScheduleCurrentProcess();

			// Add the process to a structure of processes waiting for messages
			waitingProcessMap.put(currentProcess.getPid(), currentProcess);

			while (currentProcess.isWaitingForMessage()) {
				// Wait until a message is received
				scheduler.switchProcess();
			}

			// Once woken up, return the next message
			return currentProcess.getMessageQueue().remove();
		}
	}

	public void DeScheduleCurrentProcess() {
		// Ensure there is a currently running process
		PCB currentlyRunning = scheduler.getCurrentlyRunning();
		if (currentlyRunning != null) {
			// Mark the current process as waiting for a message
			currentlyRunning.setWaitingForMessage(true);

			// Remove the process from the currently running state
			currentlyRunning = null;

		} else {
			// Handle case where there is no currently running process
			System.out.println("No currently running process to deschedule.");
		}
	}

	// handle currentCall and call appropriate functions
	private void processSystemCall() {
		switch (OS.currentCall) {
		case CREATE_PROCESS:
			// UserlandProcess up = (UserlandProcess) OS.parameters.get(0);
			PCB up = (PCB) (OS.parameters.get(0));
			scheduler.createProcess(up);
			break;
		case SWITCH_PROCESS:
			scheduler.switchProcess();
			break;
		case SLEEP:
			int milliseconds = (Integer) OS.parameters.get(0);
			scheduler.sleep(milliseconds);
			break;
		default:
			throw new IllegalStateException("Unexpected value: " + OS.currentCall);
		}
	}

	@Override
	// Opens a device through VFS and assigns a local ID to it for the current
	// process
	public int Open(String details) throws Exception {
		PCB currentProcess = scheduler.getCurrentlyRunning();
		deviceIds = currentProcess.getDeviceIds();

		for (int i = 0; i < deviceIds.length; i++) {
			if (deviceIds[i] == -1) {

				int vfsId = vfs.Open(details);
				if (vfsId != -1) {
					deviceIds[i] = vfsId; // Assign the VFS ID to the local device ID array
					return i;
				} else {
					return -1; // if VFS open fails
				}
			}
		}
		return -1;
	}

	// Closes a device through VFS and clears its ID from the current process list
	public void Close(int id) {
		PCB currentProcess = scheduler.getCurrentlyRunning();
		int vfsId = currentProcess.getDeviceIds()[id];
		if (vfsId != -1) {
			vfs.Close(vfsId);
			currentProcess.getDeviceIds()[id] = -1; // Mark the device slot as free
		}
	}

	@Override
	public byte[] Read(int id, int size) {
		PCB currentProcess = scheduler.getCurrentlyRunning();
		int vfsId = currentProcess.getDeviceIds()[id]; // Translate to VFS ID
		if (vfsId != -1) {
			return VFS.Read(vfsId, size); // Perform read operation through VFS
		} else {
			return new byte[0]; // handle error by returning empty array
		}
	}

	@Override
	public void Seek(int id, int to) {
		PCB currentProcess = scheduler.getCurrentlyRunning();
		int vfsId = currentProcess.getDeviceIds()[id]; // Translate to VFS ID
		if (vfsId != -1) {
			vfs.Seek(vfsId, to);// Perform seek operation through VFS
		}

	}

	@Override
	public int Write(int id, byte[] data) {
		PCB currentProcess = scheduler.getCurrentlyRunning();
		int vfsId = currentProcess.getDeviceIds()[id]; // Translate to VFS ID
		if (vfsId != -1) {
			return VFS.Write(vfsId, data);
		} else {
			return -1; // return if error
		}
	}

	// Closes all devices for a given process
	public void closeAllDevicesForProcess(PCB process) {
		int[] deviceIds = process.getDeviceIds();
		for (int i = 0; i < deviceIds.length; i++) {
			if (deviceIds[i] != -1) {
				vfs.Close(deviceIds[i]);
				deviceIds[i] = -1; // Reset device ID to indicate it's closed
				System.out.println("Closing all devices for process with PID: " + process.getPid());
			}
		}
	}

}