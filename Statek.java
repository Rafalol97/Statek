import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class Statek extends JPanel {
    volatile Semaphore mostek;
    volatile Semaphore obecni;
    volatile Semaphore plynie = new Semaphore(1);
    volatile Semaphore mutex = new Semaphore(1);

    volatile LinkedList<Integer> mojeMiejsce;
    volatile int miejsceWKolejce = 1;

    volatile LinkedList<Integer> miejsceNaStatku;
    volatile int następneMiejsce = 0;

    volatile int liczbaMiejscNaStatku;
    volatile int liczbaMiejscNaMostku;

    volatile int liczbaJuzObecnych;
    volatile static int ostatnieMiejsceWKolejce;
    volatile int licznikBezpeczenstwa;
    volatile int licznik = 0;

    LinkedList<Integer[]> listaPasazerow;
    LinkedList<Integer[]> pozycjeNaMostku;

    volatile static Integer TablicaPozycji[][];
    int [] xTrojkat1 = new int[3];
    int [] xTrojkat2 = new int[3];

    int [] yTrojkat1 = new int[3];
    int [] yTrojkat2 = new int[3];
    int xStatek= 700; int yStatek= 200;
    int statekWidth = 150,statekHeight=300;
    public Statek(int iloscPasazerow, int przejscia, int miejscaNaStatku, int miejscaNaMostku) {
        mojeMiejsce = new LinkedList<>();
        this.miejsceNaStatku = new LinkedList<>();
        for (int i = 0; i < iloscPasazerow; i++) {
            this.mojeMiejsce.add(-1);
        }
        for (int i = 0; i < miejscaNaStatku; i++) {
            this.miejsceNaStatku.add(-1);
        }
        this.mostek = new Semaphore(miejscaNaMostku);
        this.obecni = new Semaphore(miejscaNaStatku);
        liczbaMiejscNaStatku = miejscaNaStatku;
        liczbaMiejscNaMostku = miejscaNaMostku;
        licznikBezpeczenstwa = iloscPasazerow * przejscia;
        try {
            plynie.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        pozycjeNaMostku = new LinkedList<>();
        int xStartowy = xStatek-10,yStartowy=460;
        int offsetX=0;
        for(int i=0;i<liczbaMiejscNaMostku;i++){
            Integer temp [] = new Integer[3];
            temp[0]=xStartowy-offsetX;
            temp[1]=yStartowy;
            temp[2]=-1;
            offsetX+=40;
            pozycjeNaMostku.add(temp);
        }
        listaPasazerow= new LinkedList<>();
        TablicaPozycji = new Integer[16][3];
        initTablicaPozycji();

    }

    public void paint(Graphics g) {
        super.paint(g);
        //Most
        g.setColor(Color.decode("0x6C4B36"));
        g.fillRect(500,450,200,50);
        //Statek

        xTrojkat1[0] = xStatek;
        xTrojkat1[1] = xStatek+statekWidth;
        xTrojkat1[2] = xStatek+statekWidth/2;

        xTrojkat2[0] = xStatek;
        xTrojkat2[1] = xStatek+statekWidth;
        xTrojkat2[2] = xStatek+statekWidth/2;

        yTrojkat1[0] = yStatek;
        yTrojkat1[1] = yStatek;
        yTrojkat1[2] = yStatek-60;

        yTrojkat2[0] = yStatek+statekHeight;
        yTrojkat2[1] = yStatek+statekHeight;
        yTrojkat2[2] = yStatek+statekHeight+60;
        g.setColor(Color.black);
        g.fillPolygon(xTrojkat1,yTrojkat1,3);
        g.fillPolygon(xTrojkat2,yTrojkat2,3);
        g.setColor(Color.BLUE);
        g.fillRect(xStatek,yStatek,statekWidth,statekHeight);

        //Pasazery


    }

    public void wsiadanie(int nr) {
        Integer[] temp = new Integer[2];
        temp[0]=nr;
        temp[1]=ostatnieMiejsceWKolejce;
        TablicaPozycji[ostatnieMiejsceWKolejce][2]=nr;

        ostatnieMiejsceWKolejce++;
        listaPasazerow.add(temp);
        try {
            obecni.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            mostek.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            mutex.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mojeMiejsce.set(nr, miejsceWKolejce);
        miejsceWKolejce++;

        mutex.release();

        System.out.println("Wsiadam");
        //animacja wchodzenia na mostek


        try {

            mutex.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //zaklepanie miejsce na statku
        miejsceNaStatku.set(następneMiejsce, nr);
        następneMiejsce++;

        mojeMiejsce.set(nr, -1);
        miejsceWKolejce--;

        mutex.release();

        //animacja wejscia na statek

        mostek.release();

    }

    public void wysiadanie(int nr) {

        //dokonczenie animacji wsiadania

        liczbaJuzObecnych++;

        licznik++;
        if (liczbaJuzObecnych == liczbaMiejscNaStatku || licznik == licznikBezpeczenstwa){
            plynie.release();
        }
    }


    public void kapitanStart() {
            try {
                plynie.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        System.out.println("Odpływam");
        //animacja odplywu statku

        //tutaj moze byc petla zwalniajace obecnych jesli chcesz zeby wchodzili jak juz nowy przyplywa



        if(licznik == licznikBezpeczenstwa){
            //brak nowego statku
        }
        else{
            //animacja przyplywu nowego statku
        }
        plynie.release();
    }

    public void kapitanStop() {

        liczbaJuzObecnych = 0;
        następneMiejsce = 0;

        for (int i = 0; i < liczbaMiejscNaStatku; i++) {
            obecni.release();
            miejsceNaStatku.set(i, -1);
        }
        try {
            plynie.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void initTablicaPozycji(){
        int xStart =500-10,yStart = 450;
        int xOffset=0,yOffset=0;
        for(int i=0;i<8;i++){
            TablicaPozycji[i][0]=xStart-xOffset;
            TablicaPozycji[i][1]=yStart;
            TablicaPozycji[i][2]=-1;
            xOffset+=40;
        }
        for(int i=8;i<16;i++){
            TablicaPozycji[i][0]=xStart;
            TablicaPozycji[i][1]=yStart-yOffset;
            TablicaPozycji[i][2]=-1;
            yOffset+=40;
        }


    }
}

