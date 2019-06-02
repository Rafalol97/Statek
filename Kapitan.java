import javax.swing.plaf.nimbus.State;

public class Kapitan extends Thread {
    private Statek statek;
    private int liczbaPrzejsc=0;

    public Kapitan(Statek statek) {
        this.statek = statek;
    }

    @Override
    public void run() {
        while(liczbaPrzejsc!=statek.licznikBezpeczenstwa/statek.liczbaMiejscNaStatku+1){
            statek.kapitanStart();
            statek.kapitanStop();
            liczbaPrzejsc++;
        }
    }
}
