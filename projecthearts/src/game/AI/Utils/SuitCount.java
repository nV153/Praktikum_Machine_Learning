package game.AI.Utils;

public class SuitCount {
    public int cntKaro;
    public int cntHearts;
    public int cntPic;
    public int cntKreuz;
    public int cntSuits;
    public int cntCurrentHandCards;


    public SuitCount(int cntKaro, int cntHearts, int cntPic, int cntKreuz, int cntCurrentHandCards) {
        this.cntKaro = cntKaro;
        this.cntHearts = cntHearts;
        this.cntPic = cntPic;
        this.cntKreuz = cntKreuz;
        this.cntCurrentHandCards = cntCurrentHandCards;
        
        this.cntSuits = 0;
        if(cntKaro != 0) this.cntSuits++;
        if(cntHearts != 0) this.cntSuits++;
        if(cntPic != 0) this.cntSuits++;
        if(cntKreuz != 0) this.cntSuits++;
    }
}