import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.Semaphore;

public class Statek extends JPanel {
    private static Font sanSerifFont = new Font ("TimesRoman", Font.BOLD | Font.ITALIC, 20);

    volatile Semaphore mostek;
    volatile Semaphore obecni;
    volatile Semaphore plynie = new Semaphore(1);
    volatile Semaphore stoi = new Semaphore(1);
    volatile Semaphore kolejka  = new Semaphore(16);
    volatile Semaphore chronZaklepania = new Semaphore(1);
    volatile Semaphore chronRysowanie1 = new Semaphore(1);
    volatile Semaphore chronRysowanie2 = new Semaphore(1);
    volatile Semaphore chronRysowanie3 = new Semaphore(1);
    volatile Semaphore mutexPrzesun = new Semaphore(1);
    volatile LinkedList<Integer> mojeMiejsce;
    volatile static int liczbaPasazerow;
    volatile static int liczbaPozostalychMiejscNaStatku;
    volatile static int liczbaPozostalychMiejscNaMostku;
    volatile LinkedList<Integer> listaKolejnosciRysowania1;
    volatile LinkedList<Integer> listaKolejnosciRysowania2;
    volatile LinkedList<Integer> listaKolejnosciRysowania3;

    volatile int następneMiejsce = 0;

    volatile int liczbaMiejscNaStatku;
    volatile int liczbaMiejscNaMostku;

    volatile int liczbaJuzObecnych;
    volatile int ostatnieMiejsceWKolejce=0;
    volatile int ostatnieMiejsceNaMostku =0;
    volatile  int ostatnieMiejsceNaStatku =0;

    volatile int licznikBezpeczenstwa;
    volatile int licznik = 0;



    //Tablice miejsc odpowiednich
    volatile  Integer TablicaPozycji[][];
    volatile Integer TablicaPozycjiMostek[][];
    volatile Integer TablicaPozycjiStatek[][];
    //Listy wątków i ich miejsca w kolejce
    volatile LinkedList<Integer> listaPozycjiWKolejce;
    volatile LinkedList<Integer> listaPozycjiNaMostku;
    volatile LinkedList<Integer> listaPozycjiNaStatku;
    int [] xTrojkat1 = new int[3];
    int [] xTrojkat2 = new int[3];

    int [] yTrojkat1 = new int[3];
    int [] yTrojkat2 = new int[3];
    int xStatek= 700; int yStatek= 220;
    int statekWidth = 150,statekHeight=300;
    int yStartowe = yStatek;
    int yStatekOffset;
    int xKolejkaMostek=460,yKolejkaMostek =460;
    volatile boolean malujPrzejscie,malujNaMostku,malujNaStatku;
    volatile boolean przesuwamKolejke,przesuwamKolejkeMostek;


    int xDodanego,yDodanego;
    int xNaMostku,yNaMostku=460;
    int xNaStatku=xStatek-40,yNaStatku=460;


    public Statek(int iloscPasazerow, int przejscia, int miejscaNaStatku, int miejscaNaMostku) {

        listaKolejnosciRysowania1 = new LinkedList<>();

        listaKolejnosciRysowania2 = new LinkedList<>();

        listaKolejnosciRysowania3 = new LinkedList<>();
        this.mostek = new Semaphore(miejscaNaMostku);
        this.obecni = new Semaphore(miejscaNaStatku);
        liczbaMiejscNaStatku = miejscaNaStatku;
        liczbaMiejscNaMostku = miejscaNaMostku;
        licznikBezpeczenstwa = iloscPasazerow * przejscia;


        listaPozycjiWKolejce = new LinkedList<>();
        listaPozycjiNaStatku = new LinkedList<>();
        listaPozycjiNaMostku = new LinkedList<>();


        TablicaPozycji = new Integer[16][3];
        TablicaPozycjiMostek = new Integer[liczbaMiejscNaMostku][3];
        TablicaPozycjiStatek = new Integer[liczbaMiejscNaStatku][3];
        initTablicaPozycji();
        try {
            plynie.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        mojeMiejsce = new LinkedList<>();
        for(int i=0;i<iloscPasazerow;i++){
            mojeMiejsce.add(-1);
        }
    }

    public void paint(Graphics g) {
        super.paint(g);

        g.setColor(Color.decode("0x78A077"));
        g.fillRect(0,0,getWidth(),getHeight());
        g.setColor(Color.decode("0x1B3B6F"));
        g.fillRect(600,0,getWidth(),getHeight());

        g.setColor(Color.decode("0x1F271B"));
        g.fillRect(130,0,50,500);
        g.fillRect(130,450,500,50);


        //Most
        g.setColor(Color.decode("0x6C4B36"));
        g.fillRect(500,450,200,50);
        //Statek
        g.setColor(Color.decode("0x6C1124"));
        g.fillRect(495,445,10,60);

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
        g.setColor(Color.decode("0xAEA8A0"));
        g.fillPolygon(xTrojkat1,yTrojkat1,3);
        g.fillPolygon(xTrojkat2,yTrojkat2,3);
        g.fillRect(xStatek,yStatek,statekWidth,statekHeight);
        g.setColor(Color.decode("0x4F2113"));
        g.fillRect(xStatek+15,yStatek+15,statekWidth-30,statekHeight-30);

        //Napisy
        g.setColor(Color.black  );
        g.setFont(sanSerifFont);
        if(liczbaPasazerow==1){
            g.drawString("Został " + liczbaPasazerow + " pasażer do obsłużenia", 200, 20);

        }
        else {
            g.drawString("Zostało " + liczbaPasazerow + " pasażerów do obsłużenia", 200, 20);
        }
        g.drawString("Liczba miejsc na mostku: "+ liczbaMiejscNaMostku,200,50);
        g.drawString("Liczba miejsc na statku: "+ liczbaMiejscNaStatku,200,80);
        g.drawString("Pozostała liczba miejsc na mostku: "+ liczbaPozostalychMiejscNaMostku,200,110);
        g.drawString("Pozostałą liczba miejsc na statku: "+ liczbaPozostalychMiejscNaStatku,200,140);

        //Pasazery
        g.setColor(Color.RED);
        int k=0;
        for(int i=0;i<listaPozycjiWKolejce.size();i++){
            if((malujNaMostku&&i==0&&TablicaPozycji[0][2]==-1)||przesuwamKolejke&&i==0)k++;
            g.fillRect(TablicaPozycji[i+k][0], TablicaPozycji[i+k][1], 30, 30);

        }
        k=0;
        for(int i=0;i<listaPozycjiNaMostku.size();i++){
           if((malujNaStatku&&i==0&&TablicaPozycjiMostek[0][2]==-1)||przesuwamKolejkeMostek&&i==0)k++;
            g.fillRect(TablicaPozycjiMostek[i+k][0], TablicaPozycjiMostek[i+k][1], 30, 30);

        }
        for(int i=0;i<listaPozycjiNaStatku.size();i++){

            g.fillRect(TablicaPozycjiStatek[i][0], TablicaPozycjiStatek[i][1]+yStatekOffset, 30, 30);

        }
        if(malujPrzejscie){
            g.fillRect(xDodanego,yDodanego,30,30);
        }
        if(malujNaMostku){
            g.fillRect(xNaMostku,yNaMostku,30,30);
        }
        if(malujNaStatku){
            g.fillRect(xNaStatku,yNaStatku,30,30);
        }


    }

    public void wsiadanie(int nr) throws InterruptedException {

        //Dostanie sie do kolejki w której jest 16 miejsc
        kolejka.acquire();

        //Zaklepanie miejsce w kolejce

        chronZaklepania.acquire();

        listaKolejnosciRysowania1.add(nr);
        mojeMiejsce.set(nr,ostatnieMiejsceWKolejce);

        ostatnieMiejsceWKolejce++;

        chronZaklepania.release();

        //Przesun klocek na odpowiednie miejsce w kolejce
        //Przesuwanie do kolejki, z kolejki na mostek i z mostku do statku jest opatrywane jednym semaforem
        //Organizacja kolejki czekania procesów
        while(!(listaKolejnosciRysowania1.size()!=0&&(listaKolejnosciRysowania1.getFirst()==nr))){Thread.sleep(1);}
        while(!(chronRysowanie1.tryAcquire())){
            Thread.sleep(1);
            chronRysowanie1.release();
        }

        mutexPrzesun.acquire();
        //Przesun tam gdzie ma byc
        przesunPasazeraNaZaklepaneMiejsce(mojeMiejsce.get(nr),nr);
        mutexPrzesun.release();
        listaKolejnosciRysowania1.removeFirst();
        chronRysowanie1.release();

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

        chronZaklepania.acquire();

        mojeMiejsce.set(nr,ostatnieMiejsceNaMostku) ;
        ostatnieMiejsceNaMostku++;


        listaKolejnosciRysowania2.add(nr);
        chronZaklepania.release();

        while(!(listaKolejnosciRysowania2.size()!=0&&(listaKolejnosciRysowania2.getFirst()==nr))){Thread.sleep(1);}
        while(!(chronRysowanie2.tryAcquire())){
            chronRysowanie2.release();
            Thread.sleep(1);
        }



        mutexPrzesun.acquire();

        przesunPasazeraNaZaklepaneMiejsceNaMostku(mojeMiejsce.get(nr),nr);
        przesunKolejke();
        mutexPrzesun.release();
        liczbaPozostalychMiejscNaMostku--;
        this.repaint();

        listaKolejnosciRysowania2.removeFirst();
        chronRysowanie2.release();





        kolejka.release();


        chronZaklepania.acquire();

        mojeMiejsce.set(nr,ostatnieMiejsceNaStatku);
        ostatnieMiejsceNaStatku++;

        listaKolejnosciRysowania3.add(nr);
        chronZaklepania.release();

        Thread.sleep(mostek.availablePermits()*3000+((kolejka.availablePermits())/4)*3000);

        while(!(listaKolejnosciRysowania3.size()!=0&&(listaKolejnosciRysowania3.getFirst()==nr))){Thread.sleep(1);}
        while(!(chronRysowanie3.tryAcquire())){
            chronRysowanie3.release();
            Thread.sleep(1);
        }

        mutexPrzesun.acquire();

        liczbaPozostalychMiejscNaMostku++;
        this.repaint();
        przesunPasazeraNaZaklepaneMiejsceNaStatku(mojeMiejsce.get(nr),nr);
        przesunKolejkeMostek();
        mutexPrzesun.release();
        listaKolejnosciRysowania3.removeFirst();

        chronRysowanie3.release();



        mostek.release();

    }

    public void przesunPasazeraNaZaklepaneMiejsce(int miejsce,int nr){


        xDodanego=140;yDodanego=0;
        malujPrzejscie= true;

        int i=16-1;
        while(!TablicaPozycji[16-1][1].equals(yDodanego)){
            if (TablicaPozycji[16-1][1]>yDodanego) {
                yDodanego++;

                this.repaint();

            }
            try {
                Thread.sleep(0,4);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        while(i>=1&&TablicaPozycji[i-1][2]==-1){
            for(int j=0;j<40;j++) {

                 if (TablicaPozycji[i - 1][1] > yDodanego) {
                    yDodanego++;
                     this.repaint();

                }else if (TablicaPozycji[i - 1][0] > xDodanego) {
                    xDodanego++;
                     this.repaint();

                }
                try {
                    Thread.sleep(0,2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            i--;

        }
        TablicaPozycji[i][2]=0;
      //  wypiszTablice();
        listaPozycjiWKolejce.add(nr);
        malujPrzejscie= false;

    }
    public void wypiszTablice(){
        for(int i=15;i>=0;i--){
            System.out.printf("%d ",TablicaPozycji[i][2]);
        }
    }
    public  void przesunPasazeraNaZaklepaneMiejsceNaMostku(int miejsce,int nr){ //TODO DO SPRAWDZENIA
        xNaMostku=460;
        malujNaMostku= true;
        TablicaPozycji[0][2]=-1;
        listaPozycjiWKolejce.removeFirst();
        ostatnieMiejsceWKolejce--;
        int i=liczbaMiejscNaMostku-1;

        while(!TablicaPozycjiMostek[liczbaMiejscNaMostku-1][0].equals(xNaMostku)){
            if (TablicaPozycjiMostek[liczbaMiejscNaMostku-1][0] > xNaMostku) {
                xNaMostku++;
                this.repaint();
                try {
                    Thread.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        while(i>0&&TablicaPozycjiMostek[i-1][2]==-1){
            for(int j=0;j<40;j++) {
                if (TablicaPozycjiMostek[i - 1][0] > xNaMostku) {
                    xNaMostku++;
                    this.repaint();
                    try {
                        Thread.sleep(2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            i--;

        }


        TablicaPozycjiMostek[i][2]=0;

        listaPozycjiNaMostku.add(nr);
        malujNaMostku= false;

    }
    public  void przesunPasazeraNaZaklepaneMiejsceNaStatku(int miejsce, int nr)throws  InterruptedException{
        xNaStatku=700-40;
        yNaStatku=460;
        TablicaPozycjiMostek[0][2]=-1;
        ostatnieMiejsceNaMostku--;
        malujNaStatku= true;
        listaPozycjiNaMostku.removeFirst();
        while(TablicaPozycjiStatek[miejsce][0]!=xNaStatku||TablicaPozycjiStatek[miejsce][1]!=yNaStatku){
            if (TablicaPozycjiStatek[miejsce][0] > xNaStatku) {
                xNaStatku++;
                this.repaint();
            }
            else if (TablicaPozycjiStatek[miejsce][1] < yNaStatku) {
                yNaStatku--;
                this.repaint();

            }
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        listaPozycjiNaStatku.add(nr);
        malujNaStatku = false;
    }


    public  void wysiadanie(int nr) {

        //dokonczenie animacji wsiadania

        liczbaJuzObecnych++;
        licznik++;
        System.out.println("Bilet skasowany dla pasażera o numerze: " + nr);
        liczbaPasazerow--;
        liczbaPozostalychMiejscNaStatku--;
        this.repaint();

        if (liczbaJuzObecnych == liczbaMiejscNaStatku || licznik == licznikBezpeczenstwa){
            System.out.println("Statek nie będzie miec już więcej pasażerów");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ostatnieMiejsceNaStatku=0;
            if(licznikBezpeczenstwa==licznik){
                System.out.println("Statek zakończył swoje kursy, więcej nie będzie");
            }
            plynie.release();
        }
    }
    public  void kapitanStart() {
            try {

                plynie.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("-----------Statek odpływa-----------");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //animacja odplywu statku
        for(int y = yStatek;y+statekHeight*2>0;y-=10){
            yStatekOffset-=10;
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.repaint();

            yStatek=y;
        }
        //tutaj moze byc petla zwalniajace obecnych jesli chcesz zeby wchodzili jak juz nowy przyplywa


        liczbaPozostalychMiejscNaStatku=liczbaMiejscNaStatku;
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
    public  void kapitanStop() {

        liczbaJuzObecnych = 0;
        następneMiejsce = 0;

        for (int i = 0; i < liczbaMiejscNaStatku; i++) {
            obecni.release();
        }
        listaPozycjiNaStatku.clear();

        yStatekOffset=0;
        stoi.release();


    }

    public  void initTablicaPozycji(){
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
        xOffset=0;
        int xStartMostek= xStatek-40;
        int yStartMostek= 460;
        for(int i=0;i<liczbaMiejscNaMostku;i++){
            TablicaPozycjiMostek[i][0]=xStartMostek-xOffset;
            TablicaPozycjiMostek[i][1]=yStartMostek;
            TablicaPozycjiMostek[i][2]=-1;
            xOffset+=40;
        }
        xOffset=0;
        int xStartStatek1 =xStatek+20;
        int yStartStatek1 = 250;
        yOffset=0;
        for(int i=0;i<liczbaMiejscNaStatku;i++){
            TablicaPozycjiStatek[i][0]=xStartStatek1+xOffset;
            TablicaPozycjiStatek[i][1]=yStartStatek1+yOffset;
            TablicaPozycjiStatek[i][2]=-1;
            xOffset+=40;
            if(i%3==2){
                yOffset+=40;
                xOffset=0;
            }
        }


    }


    public  void przesunKolejke(){
        przesuwamKolejke=true;

        for(int i=0;i<listaPozycjiWKolejce.size();i++){

            for(int j=0;j<40;j++){
                if(i<8){
                    TablicaPozycji[i+1][0]++;
                    this.repaint();
                }
                else{
                    TablicaPozycji[i+1][1]++;
                    this.repaint();
                }
                try {
                    if (listaPozycjiWKolejce.size() > 7){
                        Thread.sleep(0,2);
                    }

                    else{
                        Thread.sleep(1);

                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }




        }
            if(listaPozycjiWKolejce.size()>1){
                TablicaPozycji[listaPozycjiWKolejce.size()][2] = -1;
                TablicaPozycji[0][2]=0;
            }
            else if(listaPozycjiWKolejce.size()==1) {
                TablicaPozycji[1][2] = -1;

                TablicaPozycji[0][2]=0;

            }
            else {
                TablicaPozycji[0][2] = -1;

            }
        this.repaint();

        resetTablica();
        przesuwamKolejke=false;
        this.repaint();

    }

    public  void przesunKolejkeMostek() {
        przesuwamKolejkeMostek = true;

        for (int i = 1; i < TablicaPozycjiMostek.length; i++) {
            for (int j = 0; j < 40; j++) {
                TablicaPozycjiMostek[i][0]++;
                this.repaint();

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


        }
        if(listaPozycjiNaMostku.size()>1){
            TablicaPozycjiMostek[listaPozycjiNaMostku.size()][2] = -1;
            TablicaPozycjiMostek[0][2]=0;
        }
        else if(listaPozycjiNaMostku.size()==1) {
            TablicaPozycjiMostek[1][2] = -1;

            TablicaPozycjiMostek[0][2]=0;

        }
        else {
            TablicaPozycjiMostek[0][2] = -1;

        }
        resetTablicaMostek();
        przesuwamKolejkeMostek = false;
        this.repaint();
    }

    public  void resetTablica()
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
        this.repaint();

    }
    public  void resetTablicaMostek(){
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

