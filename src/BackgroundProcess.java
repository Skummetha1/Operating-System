public class BackgroundProcess extends UserlandProcess {
    @Override
    public void main() {
        while (true) {
            
        	System.out.println();
            System.out.println("BackgroundProcess executing");
            System.out.println();
            try {
                Thread.sleep(1000); // background processing
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            OS.sleep(100);
            cooperate();
        }
    }
}
