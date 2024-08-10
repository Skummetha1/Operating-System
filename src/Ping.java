public class Ping extends UserlandProcess {
    @Override
    public void main() throws Exception {
    	//OS.sleep(30);
        int pongPid = OS.GetPidByName("Pong");
        System.out.println("I am PING, pong = " + pongPid);
        
        if (pongPid == -1) {
            System.out.println("Pong process not found.");
            return;
        }
        
        
        for (int i = 0; i < 5; i++) {
            KernelMessage msg = new KernelMessage(OS.getPid(), pongPid, i, new byte[0]);
            OS.sendMessage(msg);
            OS.sleep(20);
            //cooperate();
            // Yield after sending a message
            KernelMessage reply = OS.waitForMessage();
            System.out.println("  PING: from: " + reply.getSenderPid() + " to: " + OS.getPid() + " what: " + reply.getWhat());
            cooperate(); // Yield after processing a reply
        }
        
        
        
    }
}

