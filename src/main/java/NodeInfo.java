import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class NodeInfo  implements Serializable{
    int level;
    List<String> cardId ;
    String toCardId;
    List<Long> dealTime ;
    List<Double> money ;
    long maxDealTime ;
    Double totalMoney;

    public NodeInfo(int level, String cardId, String toCardId, long dealTime, double money) {
        this.level = level;
        this.cardId =  new ArrayList<String>();
        this.cardId.add(cardId);
        this.toCardId = toCardId;
        this.dealTime = new ArrayList<Long>();
        this.dealTime.add(dealTime) ;
        this.money = new ArrayList<Double>();
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


    public NodeInfo deepCopy() throws Exception
    {
        // 将该对象序列化成流,因为写在流里的是对象的一个拷贝，而原对象仍然存在于JVM里面。所以利用这个特性可以实现对象的深拷贝
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        ObjectOutputStream oos = new ObjectOutputStream(bos);
        //this很关键，引用当前对象，当然，这是值传递
        oos.writeObject(this);

        // 将流序列化成对象
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());

        ObjectInputStream ois = new ObjectInputStream(bis);

        return (NodeInfo) ois.readObject();
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
