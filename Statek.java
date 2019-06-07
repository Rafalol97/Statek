import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class Statek extends JPanel {
    volatile Semaphore mostek;
    volatile Semaphore obecni;
    volatile Semaphore plynie = new Semaphore(1);
    volatile Semaphore stoi = new Semaphore(1);
    volatile Semaphore kolejka  = new Semaphore(16);
    volatile Semaphore mutex = new Semaphore(1);

    volatile LinkedList<Integer> mojeMiejsce;
    volatile int miejsceWKolejce = 1;

    volatile LinkedList<Integer> miejsceNaStatku;
    volatile int następneMiejsce = 0;

    volatile int liczbaMiejscNaStatku;
    volatile int liczbaMiejscNaMostku;

    volatile int liczbaJuzObecnych;
    volatile static int ostatnieMiejsceWKolejce=0;
    volatile int licznikBezpeczenstwa;
    volatile int licznik = 0;

    LinkedList<Integer> listaPasazerow;
    LinkedList<Integer[]> pozycjeNaMostku;

    volatile static Integer TablicaPozycji[][];
    int [] xTrojkat1 = new int[3];
    int [] xTrojkat2 = new int[3];

    int [] yTrojkat1 = new int[3];
    int [] yTrojkat2 = new int[3];
    int xStatek= 700; int yStatek= 200;
    int statekWidth = 150,statekHeight=300;
    int yStartowe = yStatek;

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
        try {
            plynie.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    public void paint(Graphics g) {
        super.paint(g);

        g.setColor(Color.BLUE);
        g.fillRect(600,0,getWidth(),getHeight());

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
        g.setColor(Color.BLACK);
        g.fillRect(xStatek,yStatek,statekWidth,statekHeight);

        //Pasazery
        for(Integer[] x: TablicaPozycji){
               // if(x[2]!=-1) {
                    g.fillRect(x[0], x[1], 30, 30);
            //    }
        }

    }

    public void wsiadanie(int nr) {
        try {
            kolejka.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        TablicaPozycji[ostatnieMiejsceWKolejce][2] = nr;
        ostatnieMiejsceWKolejce++;
        this.repaint();
        try {
            obecni.acquire();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(obecni.availablePermits()==liczbaMiejscNaStatku-2){
            try {
                stoi.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            mostek.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        kolejka.release();



        try {
            mutex.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int numerTemp = TablicaPozycji[0][2];
        przesunKolejke();
        TablicaPozycji[ostatnieMiejsceWKolejce-1][2]=-1;
        ostatnieMiejsceWKolejce--;

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
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
        for(int y = yStatek;y+statekHeight*2>0;y-=10){
            this.repaint();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            yStatek=y;
        }
        //tutaj moze byc petla zwalniajace obecnych jesli chcesz zeby wchodzili jak juz nowy przyplywa



        if(licznik == licznikBezpeczenstwa){
            //brak nowego statku
        }
        else{
            //animacja przyplywu nowego statku
            for(int y = 750;y>yStartowe;y-=10){
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                yStatek=y;
                this.repaint();
            }

        }
    }

    public void kapitanStop() {

        liczbaJuzObecnych = 0;
        następneMiejsce = 0;

        for (int i = 0; i < liczbaMiejscNaStatku; i++) {
            obecni.release();
            miejsceNaStatku.set(i, -1);
        }
        stoi.release();


    }
    public void initTablicaPozycji(){
        int xStart =500-40,yStart = 460;
        int xOffset=0,yOffset=0;
        for(int i=0;i<8;i++){
            TablicaPozycji[i][0]=xStart-xOffset;
            TablicaPozycji[i][1]=yStart;
            TablicaPozycji[i][2]=-1;
            xOffset+=40;
        }

        for(int i=8;i<16;i++){
            TablicaPozycji[i][0]=xStart-xOffset;
            TablicaPozycji[i][1]=yStart-yOffset;
            TablicaPozycji[i][2]=-1;
            yOffset+=40;
        }
    }
    public void przesunKolejke(){
        int xStop=TablicaPozycji[0][0],yStop=TablicaPozycji[0][1];
        for(int i=1;i<TablicaPozycji.length;i++){

            int temp1=TablicaPozycji[i][0],temp2=TablicaPozycji[i][1];
            while (!TablicaPozycji[i][0].equals(xStop)|| !TablicaPozycji[i][1].equals(yStop) ) {
                if (TablicaPozycji[i][1] >yStop) {
                 //   TablicaPozycji[i][1]--;

                }
                else if (TablicaPozycji[i][1] <yStop) {
                    TablicaPozycji[i][1]++;

                }
                if (TablicaPozycji[i][0] > xStop) {
                 //   TablicaPozycji[i][0]--;
                } else if (TablicaPozycji[i][0] < xStop) {
                    TablicaPozycji[i][0]++;
                }

                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                this.repaint();
                System.out.println("rysuje");
            }
            xStop=temp1;yStop=temp2;


        }
        resetTablica();


    }
    public void resetTablica()
    {
        int xStart =500-40,yStart = 460;
        int xOffset=0,yOffset=0;
        for(int i=0;i<8;i++){
            TablicaPozycji[i][0]=xStart-xOffset;
            TablicaPozycji[i][1]=yStart;

            xOffset+=40;
        }

        for(int i=8;i<16;i++){
            TablicaPozycji[i][0]=xStart-xOffset;
            TablicaPozycji[i][1]=yStart-yOffset;
            yOffset+=40;
        }
    }
}

