
public class InteractiveProcess extends UserlandProcess {

	public void main() {
        while (true) {
            
            System.out.println("Interactive process executing");
            try {
                Thread.sleep(300); 
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            OS.sleep(100);
            cooperate();
        }
    }
}