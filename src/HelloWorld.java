public class HelloWorld extends UserlandProcess {
    @Override
    public void main() {
        while (true) {
            System.out.println("Hello World");
            OS.sleep(100);
            
            try {
                Thread.sleep(50); // To prevent flooding the console too quickly
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            cooperate();
        }
    }
}
