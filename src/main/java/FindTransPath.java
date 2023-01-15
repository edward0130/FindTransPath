import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;

import static org.neo4j.driver.Values.parameters;


public class FindTransPath {

    public static List<NodeInfo> endTransList= new ArrayList<NodeInfo>();

    Driver driver;
    Session session;
    LimitElem limit;
    String cardId;
    String toCardId;
    String dealTime;
    Double money;
    public static void main(String args[])
    {
        if(args.length != 4) {
            System.out.println("usage: cardId toCardId dealTime money");
            return ;
        }

        FindTransPath trans = new FindTransPath();

        trans.cardId = args[0];
        trans.toCardId = args[1];
        trans.dealTime = args[2];
        trans.money = Double.valueOf(args[3]);

        trans.limit = new LimitElem();

        //删除节点：MATCH (n:ACCOUNT_TABLE) detach delete n;
        //样例1： 62319000001760*2131 62319000001760*2132 "2023-01-14 00:00:01" 30000
        //样例2： 62319000001760*21311  62319000001760*21312 "2023-01-15 00:00:01" 30000
        //样例3： 62170039700023*8300  62170039700012*4861 "2020-12-03 18:48:53" 50000
        //样例4： 62284814504318*4218  62284833586016*5677  "2023-01-01 12:50:10" 300000
        //样例5： 62284800869378*3679  62305233500121*0278  "2017-08-16 17:29:16" 300000

        //获取初始节点信息
        NodeInfo init = new NodeInfo(0, trans.cardId, trans.toCardId , Utils.getTimestamp( trans.dealTime, "yyyy-MM-dd HH:mm:ss"), trans.money);

        //初始化 消息列表，用于存储结果数据，每一层一个列表
        List<List<NodeInfo>> info= new ArrayList<List<NodeInfo>>();

        //创建队列存储交易层次信息，对数据进行广度优先搜索
        Queue<NodeInfo> queue = new ArrayDeque<NodeInfo>();

        //链接neo4j创建session
        trans.initDB();

        //把初始节点加入到队列中
        queue.offer(init);

        int n = 0;

        while (!queue.isEmpty()) {
            //获取当前层级的节点数量
            int count = queue.size();
            //创建层级列表，存储这个层级的交易记录
            List<NodeInfo> level = new ArrayList<NodeInfo>();

            //循环处理当前层次的节点
            for (int i = 0; i < count; i++) {

                //从队列中取出一个节点
                NodeInfo node= queue.poll();

                //将取出的节点的起始账号与所有层级的尾部节点的目标账号进行比较，如果账号相同，将交易金额累加到当前节点
                //解决场景如下：
                //      1 -> 2 -> 3
                //      1    ->   3
                trans.sumEndTransInfo(node, endTransList);

                //将取出的节点与历史节点进行比较，如果有相同的交易，则不进行记录
                Boolean check = trans.checkSameTransInfo(node, info);
                if(check==true)
                {
                    continue;
                }
                //把从队列中取出的信息存储到这个层级的列表中
                level.add(node);

                //基于取出的节点查找有关系的交易节点
                List<NodeInfo> nodeList = trans.getTransInfo(node, n, queue);

                //把交易节点依次插入到队列中，下一个层级计算会获取到此数据
                for(int j = 0; j <nodeList.size(); j++ )
                {
                    queue.add(nodeList.get(j));
                }
            }
            n++;

            //filterTransInfo(endTransNode, info);
            //把层级交易信息以列表方式存储，然后加入到列表结果集中
            info.add(level);
        }
        System.out.println(info);
        trans.saveResult(info);

        //关闭链接
        trans.close();
    }

    public void initDB() {
        //访问neo4j获取账号交易信息
        this.driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "123456"));
        this.session = driver.session();
    }

    public void close(){
        session.close();
        driver.close();
    }

    public void saveResult(List<List<NodeInfo>> result){

        //先删除结果数据 MATCH (n:ACCOUNT) detach delete n;
        session.run("MATCH (n:ACCOUNT) detach delete n ");

        for( List<NodeInfo> level: result ) {
            for(NodeInfo node: level) {
                session.run("MERGE (n:ACCOUNT {card_no:$card_no}) ", parameters("card_no", node.toCardId));
                for (int i = 0; i < node.cardId.size(); i++) {
                    session.run("MERGE (n:ACCOUNT {card_no:$card_no}) ", parameters("card_no", node.cardId.get(i)));
                    session.run("MATCH (a:ACCOUNT),(b:ACCOUNT) where " +
                            "             a.card_no=$card_no " +
                            "             and b.card_no=$to_card_no " +
                            "            create (a)-[r:RES{trans_time:$trans_time,trans_amount:$trans_amount}]->(b)",
                            parameters("card_no", node.cardId.get(i), "to_card_no", node.toCardId, "trans_time", node.timeToString(i), "trans_amount", node.money.get(i)));
                }
            }
        }
    }

    /**
     * 有效交易数据筛选的过滤原则：
     * 1.多笔交易金额累加值，在一定百分比范围内 如 80%~120%;
     * 2.组合交易笔数，不能超过限定值 如 3;
     * 3.组合交易单笔金额占比， 不能少于指定百分比 如 10%;
     * 4.交易有效的最小金额， 少于指定值的交易不计入统计， 如 10000;
     * 5.追踪交易的跳数， 限制追踪的跳数，如 0 不限制;
     * 6.上下级交易间隔的有效时间， 设置追踪的有效时间， 如 2 两小时内发生的交易
    **/


    /**
     * 相同层级的数据对交易目标相同的账号进行合并
     *
     * @param nodeList
     * @param node
     * @return
    **/
    public List<NodeInfo> unionTransInfo(List<NodeInfo> nodeList, NodeInfo node, Queue<NodeInfo> queue)
    {

        for (NodeInfo qNode: queue) {
            //此层级的节点，与先插入到此层级的列表进行比较，如果目标相同对数据进行合并；
            if (qNode.toCardId.equals(node.toCardId)) {
                qNode.totalMoney = qNode.totalMoney + node.totalMoney;
                qNode.cardId.add(node.cardId.get(0));
                qNode.money.add(node.money.get(0));
                qNode.dealTime.add(node.dealTime.get(0));
                return nodeList;
            }
        }
        nodeList.add(node);

        return nodeList;
    }


    /**
     * 检查是否有相同的交易，如果有则过滤
     * @param node
     * @param info
     * @return
     */
    public Boolean checkSameTransInfo(NodeInfo node, List<List<NodeInfo>> info)
    {
        //节点与历史层级进行比较
        for (int j = 0; j < info.size(); j++) {
            List<NodeInfo> infoList = info.get(j);

            for (int k = 0; k < infoList.size(); k++) {
                NodeInfo p = infoList.get(k);

                for (int l = 0; l < p.cardId.size(); l++) {

                    //与历史节点相同  账号、目标账号、时间 相同
                    node.delInfo(p.cardId.get(l), p.dealTime.get(l), p.money.get(l));
                }
            }
        }
        if(node.cardId.size()==0)
        {
            node = null;
            return true;
        }

        return false;
    }

    /**
     * 对目标相同的账号交易总和进行合并
     *
     * @param node
     * @param endTransList
     * @return
     */
    public NodeInfo sumEndTransInfo(NodeInfo node, List<NodeInfo> endTransList)
    {
        Iterator<NodeInfo> iterator = endTransList.iterator();
        while (iterator.hasNext()){
            NodeInfo nInfo=iterator.next();

            if(node.toCardId.equals(nInfo.toCardId))
            {
                double money = node.getTotalMoney() + nInfo.getTotalMoney();
                //设置当前节点交易总额
                node.setTotalMoney(money);
                //移除当前尾部节点
                iterator.remove();
            }
        }
        return node;
    }

    /**
     * 简单条件过滤
     * 3.组合交易单笔金额占比， 不能少于指定百分比 如 10%;
     * 4.交易有效的最小金额， 少于指定值的交易不计入统计， 如 10000;
     * 6.交易间隔的有效时间， 设置追踪的有效时间， 如 2 两小时内发生的交易
     * @param node
     * @param n
     * @return
     */
    public Boolean filterTransInfo(NodeInfo node, NodeInfo n)
    {
        //时间条件过滤
        // 起始节点 > 目标节点   ||  起始节点 + 时间间隔 < 目标节点  过滤掉
        if( node.dealTime.get(0) > n.dealTime.get(0) || node.dealTime.get(0) + limit.getMillisecond() < n.dealTime.get(0))
        {
            return true;
        }

        //组合中单笔金额不能少于指定百分比
        // 起始节点 * 百分比 > 目标节点  过滤掉
        if( node.totalMoney * limit.getSingleScale() > n.totalMoney )
        {
            return true;
        }

        //单笔交易不能少于指定金额
        // 目标节点 < 最小金额    过滤掉
        if( n.totalMoney < limit.getMinMoney())
        {
            return true;
        }

        return false;
    }


    /**
     * 根据当前交易信息获取下游交易信息
     * 可筛选的数据原则：
     * 3.组合交易单笔金额占比， 不能少于指定百分比 如 10%;
     * 4.交易有效的最小金额， 少于指定值的交易不计入统计， 如 10000;
     * 6.交易间隔的有效时间， 设置追踪的有效时间， 如 2 两小时内发生的交易
     *
     * @param nodeInfo
     * @param layer
     * @return
     */
    public List<NodeInfo> getTransInfo(NodeInfo nodeInfo, int layer, Queue<NodeInfo> queue)
    {

        List<NodeInfo> nodeList = new ArrayList<NodeInfo>();

        //System.out.println("Node:"+nodeInfo+", Layer:"+layer);

        //访问neo4j获取账号交易信息
        //Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "123456"));
        //Session session = driver.session();

        Result result = session.run("MATCH (c:ACCOUNT_TABLE {card_no:$card_no})-[l:trans]->(r) RETURN c.card_no as cardId,r.card_no as toCardId,l.trans_time as dealTime, l.trans_amount as money ", parameters("card_no", nodeInfo.toCardId));

        while(result.hasNext())
        {
            Record record = result.next();
            //System.out.println(record.get("cardId") +":"+record.get("toCardId"));

            NodeInfo n = new NodeInfo( layer, record.get("cardId").asString(), record.get("toCardId").asString(), Utils.getTimestamp(record.get("dealTime").asString(), "yyyy-MM-dd HH:mm:ss"), Double.valueOf(record.get("money").asString()));

            //简单条件过滤
            Boolean r = filterTransInfo(nodeInfo, n);
            if(r == true)
            {
                continue;
            }

            unionTransInfo(nodeList, n, queue);

        }

        if(nodeList.size() == 0)
        {
            endTransList.add(nodeInfo);
        }

        return nodeList;
    }

}
