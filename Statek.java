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
    volatile Semaphore mutexZaklepanieMiejscaWKolejcePoczatkowej = new Semaphore(1);
    volatile Semaphore mutexPrzesun = new Semaphore(1);
    volatile Semaphore mutexPrzesunPoczatek = new Semaphore(1);
    volatile int mojeMiejsce;
    volatile int miejsceWKolejce = 1;
    volatile int OstatnieMiejsceWKolejceDostepne =0;


    volatile LinkedList<Integer> miejsceNaStatku;
    volatile int następneMiejsce = 0;

    volatile int liczbaMiejscNaStatku;
    volatile int liczbaMiejscNaMostku;

    volatile int liczbaJuzObecnych;
    volatile static int ostatnieMiejsceWKolejce=0;
    volatile static int ostatnieMiejsceNaMostku =0;
    volatile int licznikBezpeczenstwa;
    volatile int licznik = 0;

    LinkedList<Integer> listaPasazerow;

    //Tablice miejsc odpowiednich
    volatile static Integer TablicaPozycji[][];
    volatile static Integer TablicaPozycjiMostek[][];
    volatile static Integer TablicaPozycjiStatek[][];
    //Listy wątków i ich miejsca w kolejce
    volatile static LinkedList<Integer> listaPozycjiWKolejce;
    volatile static LinkedList<Integer> listaPozycjiNaMostku;
    volatile static LinkedList<Integer> listaPozycjiNaStatku;
    int [] xTrojkat1 = new int[3];
    int [] xTrojkat2 = new int[3];

    int [] yTrojkat1 = new int[3];
    int [] yTrojkat2 = new int[3];
    int xStatek= 700; int yStatek= 200;
    int statekWidth = 150,statekHeight=300;
    int yStartowe = yStatek;

    int xKolejkaMostek=460,yKolejkaMostek =460;
    boolean malujPrzejscie,malujNaMostku;

    int xDodanego,yDodanego;
    int xNaMostku,yNaMostku=460;

    public Statek(int iloscPasazerow, int przejscia, int miejscaNaStatku, int miejscaNaMostku) {
        this.mostek = new Semaphore(miejscaNaMostku);
        this.obecni = new Semaphore(miejscaNaStatku);
        liczbaMiejscNaStatku = miejscaNaStatku;
        liczbaMiejscNaMostku = miejscaNaMostku;
        licznikBezpeczenstwa = iloscPasazerow * przejscia;


        listaPozycjiWKolejce = new LinkedList<>();
        listaPozycjiNaStatku = new LinkedList<>();
        listaPozycjiNaMostku = new LinkedList<>();


        TablicaPozycji = new Integer[16][2];
        TablicaPozycjiMostek = new Integer[liczbaMiejscNaMostku][2];
        TablicaPozycjiStatek = new Integer[liczbaMiejscNaStatku][2];
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
        g.fillRect(xStatek,yStatek,statekWidth,statekHeight);
        g.setColor(Color.RED);

        //Pasazery

        for(int i=0;i<listaPozycjiWKolejce.size();i++){
            if(malujNaMostku&&i==0)i++;
            g.fillRect(TablicaPozycji[i][0], TablicaPozycji[i][1], 30, 30);

        }

        for(int i=0;i<listaPozycjiNaMostku.size();i++){

            g.fillRect(TablicaPozycjiMostek[i][0], TablicaPozycjiMostek[i][1], 30, 30);

        }
        for(int i=0;i<listaPozycjiNaStatku.size();i++){

            g.fillRect(TablicaPozycjiStatek[i][0], TablicaPozycjiStatek[i][1], 30, 30);

        }
        if(malujPrzejscie){
            g.fillRect(xDodanego,yDodanego,30,30);
        }
        if(malujNaMostku){
            g.fillRect(xNaMostku,yNaMostku,30,30);
        }


    }

    public void wsiadanie(int nr) throws InterruptedException {

        //Dostanie sie do kolejki
        kolejka.acquire();


        //Zaklepanie miejsce w kolejce


        //przesun klocek tak dlugo az nie bedzie na dobrym miejscu


        mutexPrzesunPoczatek.acquire();

        mojeMiejsce=ostatnieMiejsceWKolejce;
        ostatnieMiejsceWKolejce++;
        przesunPasazeraNaZaklepaneMiejsce(mojeMiejsce,nr);

        mutexPrzesunPoczatek.release();

        //Możliwość wejścia na statek
        obecni.acquire();

        //Statek nie ruszy tak długo jak nie będzie wystarczającej ilości miejsc
        if(obecni.availablePermits()==liczbaMiejscNaStatku-2){
            stoi.acquire();
        }

        //Zaklepanie wejścia na mostek
        mostek.acquire();
       //Zaklepanie miejsca na mostku i przesunięcie kolejki
       // przesunKolejke();

        mutex.acquire();

        listaPozycjiWKolejce.removeFirst();
        ostatnieMiejsceWKolejce--;

        przesunPasazeraNaZaklepaneMiejsceNaMostku(ostatnieMiejsceNaMostku,nr);
        przesunKolejke();
        //TODO przesun pasazera na odpowiednie miejsce na mostku
        ostatnieMiejsceNaMostku++;
        mutex.release();
        //Oddanie miejsce w zewnętrznej kolejce
        kolejka.release();
        Thread.sleep(5000);
        //animacja wchodzenia na mostek
        mutex.acquire();
        System.out.println("Wsiadam");

        listaPozycjiNaStatku.add(nr);
        //TODO przesun pasazera na odpowiednie miejsce na statku
        listaPozycjiNaMostku.removeFirst();
        ostatnieMiejsceNaMostku--;
        //zaklepanie miejsce na statku
        mutex.release();

        mostek.release();

    }

    public void przesunPasazeraNaZaklepaneMiejsce(int miejsce,int nr){


        xDodanego=140;yDodanego=0;
        malujPrzejscie= true;
        while(TablicaPozycji[miejsce][0]!=xDodanego||TablicaPozycji[miejsce][1]!=yDodanego){
            if (TablicaPozycji[miejsce][1]>yDodanego) {
                yDodanego++;

            }
            else if (TablicaPozycji[miejsce][0] > xDodanego) {
                xDodanego++;
            }
            try {
                Thread.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        malujPrzejscie= false;
        listaPozycjiWKolejce.add(nr);

    }
    public void przesunPasazeraNaZaklepaneMiejsceNaMostku(int miejsce,int nr){
        xNaMostku=460;
        malujNaMostku= true;
        while(TablicaPozycjiMostek[miejsce][0]!=xNaMostku){
            if (TablicaPozycjiMostek[miejsce][0] > xNaMostku) {
                xNaMostku++;
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        malujNaMostku= false;
        listaPozycjiNaMostku.add(nr);

    }



    public void wysiadanie(int nr) {

        //dokonczenie animacji wsiadania

        liczbaJuzObecnych++;
        licznik++;
        System.out.println("hehe");
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
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Odpływam");
        //animacja odplywu statku
        for(int y = yStatek;y+statekHeight*2>0;y-=10){

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

            }

        }
    }
    public void kapitanStop() {

        liczbaJuzObecnych = 0;
        następneMiejsce = 0;

        for (int i = 0; i < liczbaMiejscNaStatku; i++) {
            obecni.release();
        }
        listaPozycjiNaStatku.clear();
        stoi.release();


    }

    public void initTablicaPozycji(){
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
        xOffset=0;
        int xStartMostek= xStatek-40;
        int yStartMostek= 460;
        for(int i=0;i<liczbaMiejscNaMostku;i++){
            TablicaPozycjiMostek[i][0]=xStartMostek-xOffset;
            TablicaPozycjiMostek[i][1]=yStartMostek;
            xOffset+=40;
        }

        //TODO dodaj przypiswanie pozycji na statku
        xOffset=0;
        xStartMostek= xStatek+40;
        yStartMostek= 460;
        for(int i=0;i<liczbaMiejscNaStatku;i++){
            TablicaPozycjiStatek[i][0]=xStartMostek-xOffset;
            TablicaPozycjiStatek[i][1]=yStartMostek;
            xOffset+=0;
        }

    }


    public void przesunKolejke(){
        try {
            mutexPrzesun.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int xStop=TablicaPozycji[0][0],yStop=TablicaPozycji[0][1];
        for(int i=1;i<listaPozycjiWKolejce.size();i++){

            int temp1=TablicaPozycji[i][0],temp2=TablicaPozycji[i][1];
            while (!TablicaPozycji[i][0].equals(xStop)|| !TablicaPozycji[i][1].equals(yStop) ) {

                if (TablicaPozycji[i][1] <yStop) {
                    TablicaPozycji[i][1]++;

                }
                 if (TablicaPozycji[i][0] < xStop) {
                     TablicaPozycji[i][0]++;
                 }

                try {
                    if (listaPozycjiWKolejce.size() > 7){
                        Thread.sleep(1);
                    }

                    else{
                        Thread.sleep(3);

                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println("rysuje");
            }
            xStop=temp1;yStop=temp2;


        }
        resetTablica();
        mutexPrzesun.release();
    }

    public void przesunKolejkeMostek() {
        int xStop = TablicaPozycjiMostek[0][0];
        for (int i = 1; i < TablicaPozycjiMostek.length; i++) {

            int temp1 = TablicaPozycjiMostek[i][0];
            while (!TablicaPozycjiMostek[i][0].equals(xStop)) {
                if (TablicaPozycjiMostek[i][0] < xStop) {
                    TablicaPozycjiMostek[i][0]++;
                }
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            xStop = temp1;


        }
        resetTablicaMostek();
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
    public void resetTablicaMostek(){
        int  xOffset=0;
        int xStartMostek= xStatek-40;
        int yStartMostek= 460;
        for(int i=0;i<liczbaMiejscNaMostku;i++){
            TablicaPozycjiMostek[i][0]=xStartMostek-xOffset;
            TablicaPozycjiMostek[i][1]=yStartMostek;

            xOffset+=40;
        }
    }

}

