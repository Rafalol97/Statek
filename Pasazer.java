import java.util.Random;

public class Pasazer extends Thread {
    Statek statek;
    int numer;
    int przejscia;
    Random rng;

    public Pasazer(Statek statek, int numer, int przejscia, Random rng) {
        super(String.valueOf(numer));
        this.numer = numer;
        this.statek = statek;
        this.przejscia = przejscia;
        this.rng = rng;
    }

    public void run() {
        for (int i = 0; i<przejscia; i++) {
            try {
                sleep(rng.nextInt(5000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            statek.wsiadanie(numer);

            statek.wysiadanie(numer);
        }
    }
}
