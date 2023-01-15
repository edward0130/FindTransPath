import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

public class TransInfo implements Comparable<TransInfo> {

    int level;
    String cardId;
    String toCardId;
    long dealTime;
    double money;


    public TransInfo(int level, String cardId, String toCardId, long dealTime, double money) {
        this.level = level;
        this.cardId = cardId;
        this.toCardId = toCardId;
        this.dealTime = dealTime;
        this.money = money;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getToCardId() {
        return toCardId;
    }

    public void setToCardId(String toCardId) {
        this.toCardId = toCardId;
    }

    public long getDealTime() {
        return dealTime;
    }

    public void setDealTime(long dealTime) {
        this.dealTime = dealTime;
    }

    @Override
    public String toString() {
        Date date = new Date();
        date.setTime(dealTime);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return "TransInfo{" +
                "level='" + level + '\'' +
                "cardId='" + cardId + '\'' +
                ", toCardId='" + toCardId + '\'' +
                ", dealTime='" + simpleDateFormat.format(date) + '\'' +
                ", money=" + money +
                '}';
    }

    @Override
    public int compareTo(TransInfo o) {
        long r = this.dealTime - o.dealTime;
        if(r>0)
            return 1;
        else if (r==0)
            return 0;
        else return -1;
    }
}
