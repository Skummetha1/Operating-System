
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.time.Clock;

public class Scheduler {

	// private LinkedList<UserlandProcess> processes = new LinkedList<>();
	private PCB currentlyRunning;
	// private UserlandProcess currentlyRunning;
	// private Timer timer;
	private Clock clock = Clock.systemDefaultZone();
	private LinkedList<PCB> realTimeQueue = new LinkedList<>();
	private LinkedList<PCB> interactiveQueue = new LinkedList<>();
	private LinkedList<PCB> backgroundQueue = new LinkedList<>();
	private LinkedList<PCB> sleepingQueue = new LinkedList<>();
	List<PCB> allProcesses = new LinkedList<>();
	private Random random = new Random();
	private Kernel kernel;
	private int nextFreeBlock = 0; 

	public Scheduler(Kernel kernel) {
		this.kernel = kernel;

	}

	public Scheduler() {

	}

	

	public int findFreePhysicalPage() {
		for (int i = 0; i < Kernel.getFreelist().length; i++) {
			if (Kernel.getFreelist()[i]) { 
				Kernel.getFreelist()[i] = false; //page used
				return i; //index of free page
			}
		}
		return -1; 
	}

	public int handlePageSwap(int virtualPage) {
	    PCB victimProcess = getRandomProcess();
	    if (victimProcess == null) {
	        throw new RuntimeException("No available process for swapping.");
	    }
	    int victimPageIndex = findVictimPage(victimProcess);
	    if (victimPageIndex == -1) {
	        throw new RuntimeException("No swappable page found in the selected process.");
	    }

	    VirtualToPhysicalMapping victimMapping = PCB.getPagetable()[victimPageIndex];
	    if (victimMapping.onDiskPageNumber == -1) {
	        victimMapping.onDiskPageNumber = allocateNewDiskBlock();  // allocate a new block on the disk
	    }

	    writePageToDisk(victimMapping.physicalPageNumber, victimMapping.onDiskPageNumber);  // swap out the victim page to disk
	    int freedPhysicalPage = victimMapping.physicalPageNumber;
	    victimMapping.physicalPageNumber = -1;  // free physical page

	    return freedPhysicalPage;  // return index
	}
	
	public int findVictimPage(PCB victimProcess) {
	    VirtualToPhysicalMapping[] pageTable = PCB.getPagetable();
	    List<Integer> Pages = new ArrayList<>();
	    for (int i = 0; i < pageTable.length; i++) {
	        if (pageTable[i] != null && pageTable[i].physicalPageNumber != -1) {
	            Pages.add(i);
	        }
	    }
	    if (Pages.isEmpty()) {
	        return -1;
	    }
	    Random random = new Random();
	    return Pages.get(random.nextInt(Pages.size()));
	}
	
	public int swapOut(PCB victimProcess, int victimPageIndex) {
	    VirtualToPhysicalMapping mapping = PCB.getPagetable()[victimPageIndex];
	    if (mapping.onDiskPageNumber == -1) {
	        mapping.onDiskPageNumber = allocateNewDiskBlock();  
	    }
	    if (!writePageToDisk(mapping.physicalPageNumber, mapping.onDiskPageNumber)) {
	        throw new RuntimeException("Failed to write page to disk during swap.");
	    }
	    int freedPhysicalPage = mapping.physicalPageNumber;
	    mapping.physicalPageNumber = -1;  // free the physical memory
	    return freedPhysicalPage;
	}
	
	public PCB getRandomProcess() {
	    List<PCB> allProcesses = getAllProcesses();  
	    Random random = new Random();
	    List<PCB> candidates = allProcesses.stream()
	        .filter(p -> Arrays.stream(PCB.getPagetable()).anyMatch(m -> m != null && m.physicalPageNumber != -1))
	        .collect(Collectors.toList());

	    if (candidates.isEmpty()) {
	        return null;  // no process 
	    }
	    return candidates.get(random.nextInt(candidates.size()));
	}
	
	private int allocateNewDiskBlock() {
	    int newDiskBlock = nextFreeBlock++;
	    System.out.println("Allocating new disk block: " + newDiskBlock);
	    return newDiskBlock;
	}
	
	
	
	public boolean writePageToDisk(int physicalPage, int diskPageNumber) {
	    try {
	        byte[] data = Kernel.getPhysicalMemory(physicalPage);  // Get data from physical memory
			VFS.Write(diskPageNumber, data);  // Write data to the specified block
	        return true;
	    } catch (Exception e) {
	        System.err.println("Disk write failed: " + e.getMessage());
	        return false;
	    }
	    }

	

	// returns the current process’ pid
	public int getPid() {
		return currentlyRunning.getPid();
	}

	// returns the pid of a process with that name
	public int GetPidByName(String name) {

		for (PCB pcb : this.getAllProcesses()) { // Assuming `allProcesses` is a collection of all PCBs
			if (pcb.getName().equals(name)) {
				return pcb.getPid();
			}
		}
		return -1; // Return -1 if no process with the given name was found
	}

	public List<PCB> getAllProcesses() {
		return allProcesses;
	}

	// sleep and then switch process
	public void sleep(int milliseconds) {
		currentlyRunning.setWakeUpTime(clock.millis() + milliseconds);
		sleepingQueue.add(currentlyRunning);
		switchProcess();

	}

	// add process to appropiate queue
	public void createProcess(PCB up) {
		allProcesses.add(up);
		switch (up.getPriority()) {
		case REAL_TIME:
			realTimeQueue.add(up);
			break;
		case INTERACTIVE:
			interactiveQueue.add(up);
			break;
		case BACKGROUND:
			backgroundQueue.add(up);
			break;
		default:
			throw new IllegalArgumentException("Unknown priority: " + up.getPriority());

		}

		if (currentlyRunning == null) {
			switchProcess();
		}

	}

	// method to run process called by switch process
	private void runProcess(PCB pcb) {
		if (pcb != null) {

			currentlyRunning = pcb;
			pcb.run();

		}
	}

	public void switchProcess() {

		// close all devices for the process
		if (currentlyRunning != null && currentlyRunning.isDone()) {
			kernel.closeAllDevicesForProcess(currentlyRunning);

		}

		// TLB cleared when process switched
		if (currentlyRunning != null) {
			int[][] TLB = UserlandProcess.getTlb();
			for (int[] entry : TLB) {
				Arrays.fill(entry, -1); // Set both virtual and physical page entries to -1
			}
		}

		// Wake up sleeping processes that are due
		long currentTime = clock.millis();
		Iterator<PCB> iterator = sleepingQueue.iterator();
		while (iterator.hasNext()) {
			PCB pcb = iterator.next();
			if (pcb.getWakeUpTime() <= currentTime) {
				iterator.remove();
				pcb.setPriority(determinePriority(pcb));
				createProcess(pcb); // Requeue the process with its current priority
			}
		}

		// Select next process based on priority and the probabilistic model
		if (!realTimeQueue.isEmpty()) {
			if (random.nextInt(10) < 6) {
				runProcess(realTimeQueue.poll());
			} else if (!interactiveQueue.isEmpty() && random.nextInt(10) < 9) {
				runProcess(interactiveQueue.poll());
			} else {
				runProcess(backgroundQueue.poll());
			}
		}

		else if (!interactiveQueue.isEmpty()) {
			if (random.nextInt(4) < 3) {
				runProcess(interactiveQueue.poll());
			} else {
				runProcess(backgroundQueue.poll());
			}
		}
		// If neither real-time nor interactive tasks are selected,
		// process a task from the background queue
		else if (!backgroundQueue.isEmpty()) {
			runProcess(backgroundQueue.poll());
		}

	}

	private PCB.Priority determinePriority(PCB pcb) {
		final int MAX_EXCEEDANCES = 5;

		// Check if the process has exceeded its time limit too frequently and demote if
		// necessary
		if (pcb.getTimeLimit() > MAX_EXCEEDANCES) {
			pcb.demotePriority(); // also resets the exceedance counter
		}

		return pcb.getPriority();
	}

	public PCB getCurrentlyRunning() {
		return currentlyRunning;
	}

	public void ResoreToRunnableQueue(PCB targetProcess) {
		// Determine the correct queue based on the process's priority and add it back
		switch (targetProcess.getPriority()) {
		case REAL_TIME:
			realTimeQueue.add(targetProcess);
			break;
		case INTERACTIVE:
			interactiveQueue.add(targetProcess);
			break;
		case BACKGROUND:
			backgroundQueue.add(targetProcess);
			break;
		default:
			throw new IllegalArgumentException("Unknown priority: " + targetProcess.getPriority());
		}

	}

}