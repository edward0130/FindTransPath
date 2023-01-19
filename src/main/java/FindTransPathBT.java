import org.neo4j.driver.Record;
import org.neo4j.driver.*;

import java.io.*;
import java.util.*;

import static org.neo4j.driver.Values.parameters;


/**
 *  资金交易链路追踪
 *
 */
public class FindTransPathBT implements Serializable {

    private static final long serialVersionUID = 12345L;

    //创建队列存储交易层次信息，对数据进行广度优先搜索
    Queue<NodeInfo> queue =  new ArrayDeque<>();

    //初始化 消息列表，用于存储结果数据，每一层一个列表
    List<List<NodeInfo>> info= new ArrayList<List<NodeInfo>>();

    //交易尾部节点信息
    List<NodeInfo> endTransList= new ArrayList<NodeInfo>();

    //记录层级列表信息
    List<NodeInfo> levelList = new ArrayList<NodeInfo>();

    //层级号
    int levelNum = 1;

    //记录当前层级队列弹出了多少
    int usedNum = 0;

    //是否是组合标记
    Boolean combineFlag = false;


    public Queue<NodeInfo> getQueue() {
        return queue;
    }

    public void setQueue(Queue<NodeInfo> queue) {
        this.queue = queue;
    }

    public List<List<NodeInfo>> getInfo() {
        return info;
    }

    public void setInfo(List<List<NodeInfo>> info) {
        this.info = info;
    }

    public List<NodeInfo> getEndTransList() {
        return endTransList;
    }

    public void setEndTransList(List<NodeInfo> endTransList) {
        this.endTransList = endTransList;
    }

    public List<NodeInfo> getLevelList() {
        return levelList;
    }

    public void setLevelList(List<NodeInfo> levelList) {
        this.levelList = levelList;
    }

    public int getLevelNum() {
        return levelNum;
    }

    public void setLevelNum(int levelNum) {
        this.levelNum = levelNum;
    }

    public int getUsedNum() {
        return usedNum;
    }

    public void setUsedNum(int usedNum) {
        this.usedNum = usedNum;
    }


    @Override
    public String toString() {
        return "FindTransPathBT{" +
                "queue=" + queue +
                ", info=" + info +
                ", endTransList=" + endTransList +
                ", levelList=" + levelList +
                ", levelNum=" + levelNum +
                ", usedNum=" + usedNum +
                '}';
    }

    public FindTransPathBT deepCopy() throws Exception
    {
        // 将该对象序列化成流,因为写在流里的是对象的一个拷贝，而原对象仍然存在于JVM里面。所以利用这个特性可以实现对象的深拷贝
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        ObjectOutputStream oos = new ObjectOutputStream(bos);
        //this很关键，引用当前对象，当然，这是值传递
        oos.writeObject(this);

        // 将流序列化成对象
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());

        ObjectInputStream ois = new ObjectInputStream(bis);

        return (FindTransPathBT) ois.readObject();
    }


}
