public class RealTimeProcess extends UserlandProcess {
    @Override
    public void main() {
        while (true) {
            // Real-time logic here
            System.out.println("RealTimeProcess executing");
            try {
                Thread.sleep(50); // real-time processing work
               
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            OS.sleep(100);
            cooperate();
            
            
        }
    }
}