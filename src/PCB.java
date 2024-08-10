import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

public class PCB {
    private static int nextPid = 0;
    private final int pid;
    private final UserlandProcess ulp;
    private long wakeUpTime = Long.MAX_VALUE; // For sleep management
    private Priority priority; // For priority management
    private int timeLimit = 0;
    private int[] deviceIds = new int[10];
    private String name;
    private LinkedList<KernelMessage> messageQueue = new LinkedList<>();
    private boolean waitingForMessage = false;
    private static final int PAGE_SIZE = 1024;
    //private static final int[] pageTable = new int[100]; // Virtual to physical page mapping
    private static final VirtualToPhysicalMapping[] pageTable = new VirtualToPhysicalMapping[100]; // New page table using VirtualToPhysicalMapping
    
    public PCB(UserlandProcess ulp) {
        this.ulp = ulp;
        this.pid = nextPid++;
        Arrays.fill(deviceIds, -1);
        this.name = ulp.getClass().getSimpleName();
        // Initialize the page table with VirtualToPhysicalMapping instances
        for (int i = 0; i < pageTable.length; i++) {
            pageTable[i] = new VirtualToPhysicalMapping();
        }
    }

    public static VirtualToPhysicalMapping[] getPagetable() {
		return pageTable;
	}


    public static VirtualToPhysicalMapping getMapping(int virtualPage, Scheduler scheduler) {
        VirtualToPhysicalMapping mapping = pageTable[virtualPage];

        
        if (mapping.physicalPageNumber == -1) {  
            int physicalPage = scheduler.findFreePhysicalPage();
            if (physicalPage == -1) {  // initiate swap
                physicalPage = scheduler.handlePageSwap(virtualPage);
            }
            mapping.physicalPageNumber = physicalPage;

            if (mapping.onDiskPageNumber != -1) {
                //load it into the newly assigned physical page
                loadDataFromDisk(mapping.onDiskPageNumber, physicalPage);
            } else {
                // If no disk data
                initializePhysicalPage(physicalPage);
            }
        }

        updateTLB(virtualPage, mapping.physicalPageNumber);
        return mapping;
    }
    
    public static void initializePhysicalPage(int physicalPage) {
        VirtualToPhysicalMapping mapping = PCB.getPagetable()[physicalPage];
        if (mapping != null) {
            // Set the initialized flag to true 
            mapping.isInitialized = true;
        } else {
            throw new RuntimeException("Attempted to initialize a non-existent physical page: " + physicalPage);
        }
    }
    
    public static void loadDataFromDisk(int diskPageNumber, int physicalPage) {
        
        int offset = diskPageNumber * PAGE_SIZE;  
        byte[] data;

        try {
            
            data = VFS.Read(offset, PAGE_SIZE);
            if (data.length != PAGE_SIZE) {
                throw new IOException("Disk read error: Insufficient data read.");
            }
           
            System.arraycopy(data, 0, Kernel.getPhysicalMemory(physicalPage), 0, PAGE_SIZE);
        } catch (Exception e) {
            System.err.println("Failed to load data from disk: " + e.getMessage());
        }
    }
    
    private static void updateTLB(int virtualPage, int physicalPage) {
        Random rand = new Random();
        int tlbIndex = rand.nextInt(2);  
        UserlandProcess.getTlb()[tlbIndex][0] = virtualPage;
        UserlandProcess.getTlb()[tlbIndex][1] = physicalPage;
    }
    
    
    
    
    
    
    
    
    public boolean isWaitingForMessage() {
		return waitingForMessage;
	}
    
	
	
	public PCB(UserlandProcess ulp, Priority priority) {
    	this.pid = nextPid++;
		this.ulp = ulp;
        this.priority = priority;
        Arrays.fill(deviceIds, -1);
        this.name = ulp.getClass().getSimpleName();
        
        //for (int i = 0; i < pageTable.length; i++) {
        //    pageTable[i] = new VirtualToPhysicalMapping();
       // }
        
        
    }
	
	
	

   
    public synchronized KernelMessage dequeueMessage() {
        return messageQueue.poll();
    }
    
 // Method to add a message to the queue
    public void addMessage(KernelMessage message) {
        this.messageQueue.add(message);
    }

    public void setWaitingForMessage(boolean waiting) {
        this.waitingForMessage = waiting;
    }
    
 // Getter and setter for the name
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
 // Getter for the message queue
    public LinkedList<KernelMessage> getMessageQueue() {
        return messageQueue;
    }

    
    
    public int[] getDeviceIds() {
        return deviceIds;
    }
    
    
    
    
    public UserlandProcess getUlp() {
		return ulp;
	}

	public enum Priority {
        REAL_TIME, INTERACTIVE, BACKGROUND
    }
	
	
    public void stop() {
        ulp.stop();
        while (!ulp.isStopped()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public boolean isDone() {
        return ulp.isDone();
    }

    public void run() {
        ulp.start();
    }

    public int getPid() {
        return pid;
    }

    public long getWakeUpTime() {
        return wakeUpTime;
    }

    public void setWakeUpTime(long wakeUpTime) {
        this.wakeUpTime = wakeUpTime;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    

    public void incrementTimeLimit() {
        timeLimit++;
    }

    public void resetTimeSliceExceedances() {
        timeLimit = 0;
    }

    

    //demote priority if timeLimit is more than 5 
    public void demotePriority() {
        if (priority == Priority.REAL_TIME) {
            priority = Priority.INTERACTIVE;
        } else if (priority == Priority.INTERACTIVE) {
            priority = Priority.BACKGROUND;
        }
        System.out.println("Process demoted");
        resetTimeSliceExceedances();
    }

    public int getTimeLimit() {
        return timeLimit;
    }

	
		
	    
		
	
}
	
