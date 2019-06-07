import javax.swing.*;
import java.util.Random;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Random rng = new Random();
        int liczbaPasazerow =200;
        int liczbaPrzejsc =2;
        int miejscNaStatku = 6;
        int miejscNaMostku =3;
        Statek statek = new Statek(liczbaPasazerow, liczbaPrzejsc,miejscNaStatku,miejscNaMostku);
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

        for(int i = 0; i < liczbaPasazerow; i++){
            pasazerowie[i] = new Pasazer(statek, i, liczbaPrzejsc, rng);
            pasazerowie[i].start();
        }

        for(int i = 0; i < liczbaPasazerow; i++){
            pasazerowie[i].join();
        }
        kapitan.join();
        malarz.join();
    }
}
