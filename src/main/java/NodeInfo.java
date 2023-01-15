import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class NodeInfo {
    int level;
    List<String> cardId =  new ArrayList<String>();
    String toCardId;
    List<Long> dealTime =  new ArrayList<Long>();
    List<Double> money = new ArrayList<Double>();
    long maxDealTime ;
    Double totalMoney;

    public NodeInfo(int level, String cardId, String toCardId, long dealTime, double money) {
        this.level = level;
        this.cardId.add(cardId);
        this.toCardId = toCardId;
        this.dealTime.add(dealTime) ;
        this.money.add(money);
        this.maxDealTime = dealTime;
        this.totalMoney = money;
    }

    public int getLevel() {
        return level;
    }

    public List<String> getCardId() {
        return cardId;
    }

    public void setCardId(List<String> cardId) {
        this.cardId = cardId;
    }

    public String getToCardId() {
        return toCardId;
    }

    public void setToCardId(String toCardId) {
        this.toCardId = toCardId;
    }

    public void setLevel(int level) {
        this.level = level;
    }


    public long getMaxDealTime() {
        return maxDealTime;
    }

    public void setMaxDealTime(long maxDealTime) {
        this.maxDealTime = maxDealTime;
    }

    public double getTotalMoney() {
        return totalMoney;
    }

    public void setTotalMoney(double totalMoney) {
        this.totalMoney = totalMoney;
    }

    public boolean delInfo(String card, Long time, Double money)
    {

        for (int i = 0; i < cardId.size(); i++) {
            if(cardId.get(i).equals(card) &&
               dealTime.get(i).equals(time) &&
               this.money.get(i).equals(money))
            {
                cardId.remove(i);
                dealTime.remove(i);
                this.money.remove(i);
                return true;
            }
        }
        return false;
    }

    public String timeToString(int index)
    {
        if (index > dealTime.size() - 1)
        {
            return null;
        }
        Date date = new Date();
        date.setTime(dealTime.get(index));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return simpleDateFormat.format(date);
    }

    @Override
    public String toString() {
        Date date = new Date();
        date.setTime(maxDealTime);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return "NodeInfo{" +
                "level=" + level +
                ", cardId=" + cardId +
                ", toCardId='" + toCardId + '\'' +
                ", dealTime=" + dealTime +
                ", money=" + money +
                ", maxDealTime='" + simpleDateFormat.format(date) + '\'' +
                ", totalMoney=" + totalMoney +
                '}';
    }
}
