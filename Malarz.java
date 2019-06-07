public class Malarz extends Thread {
    Statek statek;
    private long start;
    private boolean running = true;
    public Malarz(Statek statek) {
        this.statek = statek;
    }
    public void run(){
        while(running) {
            start = System.nanoTime();
            statek.repaint();
            System.out.println(System.nanoTime() - start);
        }
        statek.setVisible(false);
    }
}
