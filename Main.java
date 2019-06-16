import javax.swing.*;
import java.util.Random;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Random rng = new Random();
        int liczbaPasazerow =1000;
        int liczbaPrzejsc =1;
        int miejscNaStatku = 15;
        int miejscNaMostku =4;
        Statek statek = new Statek(liczbaPasazerow, liczbaPrzejsc,miejscNaStatku,miejscNaMostku);
        Statek.liczbaPasazerow = liczbaPasazerow;
        Statek.liczbaPozostalychMiejscNaStatku = miejscNaStatku;
        Statek.liczbaPozostalychMiejscNaMostku = miejscNaMostku;
        Kapitan kapitan = new Kapitan(statek);
        Malarz malarz =new  Malarz(statek);
        kapitan.start();
        JFrame frame = new JFrame("Statek");
        frame.add(statek);
        frame.setVisible(true);
        frame.setSize(1016,639);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        malarz.start();
        Pasazer[] pasazerowie = new Pasazer[liczbaPasazerow];
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for(int i = 0; i < liczbaPasazerow; i++){
            pasazerowie[i] = new Pasazer(statek, i, liczbaPrzejsc, rng);
            pasazerowie[i].start();
        }
        kapitan.join();
        malarz.join();
        for(int i = 0; i < liczbaPasazerow; i++){
            pasazerowie[i].join();
        }

    }
}
