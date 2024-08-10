public class Pong extends UserlandProcess {
    @Override
    public void main() throws InterruptedException {
        while (true) {
            //OS.sleep(50);
        	System.out.println("I am PONG");
            KernelMessage message = OS.waitForMessage();
            
            if (message.getWhat() == 0) {
                System.out.println("I am PONG, ping = " + message.getSenderPid());
            }

            System.out.println("  PONG: from: " + message.getSenderPid() + " to: " + OS.getPid() + " what: " + message.getWhat());
            
            // Respond back to Ping with a reply
            KernelMessage reply = new KernelMessage(OS.getPid(), message.getSenderPid(), message.getWhat(), new byte[0]);
            OS.sendMessage(reply);
            cooperate(); // Yield after sending a reply
        }
    }
}