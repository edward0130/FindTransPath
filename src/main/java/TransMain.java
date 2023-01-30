import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.util.*;

import static org.neo4j.driver.Values.parameters;

public class TransMain {


    //neo4j 链接
    Driver driver;
    Session session;

    //输入参数及配置要求
    LimitElem limit;
    String cardId;
    String toCardId;
    String dealTime;
    Double money;

    //路径数量
    int pathCount=0;

    //路径堆栈
    Deque<FindTransPathBT> stack = new LinkedList<FindTransPathBT>();

    public  static void main(String[] args) throws Exception {
        if(args.length != 4) {
            System.out.println("usage: cardId toCardId dealTime money");
            return ;
        }


        //删除节点：MATCH (n:ACCOUNT_TABLE) detach delete n;
        //样例1： 62319000001760*2131 62319000001760*2132 "2023-01-14 00:00:01" 30000
        //样例2： 62319000001760*21311 62319000001760*21312 "2023-01-15 00:00:01" 30000
        //样例3： 62319000001760*21321 62319000001760*21322 "2023-01-15 00:00:01" 30000
        //样例4： 62170039700023*8300 62170039700012*4861 "2020-12-03 18:48:53" 50000
        //样例5： 62319000001760*21332 62319000001760*21333 "2023-01-15 00:00:17" 30000

        //测试数据：
        // 62284833584614*2976 62284833586037*7278 "2017-08-04 15:33:01" 46057
        // 62305201000224*2970 62284833581971*8772 "2017-08-18 00:21:42" 300000
        // 62148312025968*2 62284801286897*7277 "2020-03-30 23:37:38" 200000
        // 62284833586014*1571 62284800833506*1010 "2018-03-30 23:37:38" 300000
        // 62284833586014*1571 62284800833506*1010 "2019-03-30 23:37:38" 300000
        // 62284833586014*1571 62284800833506*1010 "2021-03-30 23:37:38" 400000


        TransMain tm = new TransMain();

        tm.cardId = args[0];
        tm.toCardId = args[1];
        tm.dealTime = args[2];
        tm.money = Double.valueOf(args[3]);

        tm.limit = new LimitElem();

        //获取初始节点信息
        NodeInfo init = new NodeInfo(0, tm.cardId, tm.toCardId , Utils.getTimestamp( tm.dealTime, "yyyy-MM-dd HH:mm:ss"), tm.money);

        FindTransPathBT first = new FindTransPathBT();
        first.queue.offer(init);


        tm.stack.push(first);

        //链接neo4j创建session
        tm.initDB();
        while(!tm.stack.isEmpty())
        {
            FindTransPathBT path = tm.stack.peek();
            tm.stack.pop();
            tm.findPath(path);
        }

        //关闭数据链接
        tm.closeDB();

    }
    public void findPath(FindTransPathBT path) throws Exception {

        while (!path.queue.isEmpty()) {
            //获取当前层级的节点数量
            int count = path.queue.size();

            //创建层级列表，存储这个层级的交易记录
            List<NodeInfo> levelList = new ArrayList<NodeInfo>();

            //组合路径的开始，复原处理状态
            if(path.combineFlag == true)
            {
                count = path.queueNum;
                levelList = path.levelList;
                path.combineFlag = false;
            }

            //循环处理当前层次的节点
            for (int i = 0; i < count ; i++) {

                //从队列中取出一个节点
                NodeInfo node= path.queue.poll();

                //将取出的节点的起始账号与所有层级的尾部节点的目标账号进行比较，如果账号相同，将交易金额累加到当前节点
                //解决场景如下：
                //      1 -> 2 -> 3
                //      1    ->   3
                sumEndTransInfo(node, path.endTransList);

                //将取出的节点与历史节点进行比较，如果有相同的交易，则不进行记录
                Boolean check = checkSameTransInfo(node, path.info);
                if(check==true)
                {
                    continue;
                }

                //把从队列中取出的信息存储到这个层级的列表中
                levelList.add(node);

                //基于取出的节点查找有关系的交易节点
                List<NodeInfo> nodeList = getTransInfo(node, path.levelNum, path.queue, path.endTransList);

                //单笔交易
                if(nodeList.size()==1)
                {
                    path.queue.add(nodeList.get(0));
                }

                //组合交易
                else if(nodeList.size()>1)
                {
                    //组合序号列表
                    List<Integer> combine = new ArrayList<>();
                    //组合序号列表结果集
                    List<List<Integer>> allCombine = new ArrayList<>();
                    double target = node.totalMoney;

                    //通过回溯算法获取组合信息
                    findCombineTrans(allCombine, combine, nodeList, target, 0, 0);
                    System.out.println(allCombine);

                    //没有组合
                    if(allCombine.size()==0)
                        nodeList = null;

                    //多种组合把组合记录下来，放到堆栈中
                    for (int j = 1; j < allCombine.size(); j++) {
                            //路径拷贝，拷贝后放入到堆栈中
                            path.levelList = levelList;
                            FindTransPathBT bt = path.deepCopy();
                            //设置组合标记
                            bt.combineFlag = true;
                            //记录当前层级剩余的队列数量
                            bt.queueNum = count - i - 1;
                            for (int k = 0; k < allCombine.get(j).size(); k++) {
                                bt.queue.add(nodeList.get(allCombine.get(j).get(k)));
                            }
                            //把组合路径放到堆栈中，
                            stack.push(bt);
                            System.out.println("stack:"+bt);
                    }
                    // 程序位置顺序不能调整，前面用到了节点的深度拷贝，不能先添加数据到节点
                    if(allCombine.size() > 0) {
                        for (int k = 0; k < allCombine.get(0).size(); k++) {
                            path.queue.add(nodeList.get(allCombine.get(0).get(k)));
                        }
                    }
                }
            }
            path.levelNum++;

            //把层级交易信息以列表方式存储，然后加入到列表结果集中
            path.info.add(levelList);
        }

        // 保存结果集数据到数据库中
        pathCount++;
        saveResult(path.info, pathCount);

        System.out.println("result:"+path);

    }

    public void initDB() {
        //访问neo4j获取账号交易信息
        this.driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "123456"));
        this.session = driver.session();
    }

    public void closeDB(){
        session.close();
        driver.close();
    }


    public void saveResult(List<List<NodeInfo>> result, int num){

        //先删除结果数据 MATCH (n:ACCOUNT) detach delete n;
        String sql = String.format("MATCH (n:ACCOUNT%d) detach delete n ", num);
        session.run(sql,parameters("num", String.valueOf(num)));

        for( List<NodeInfo> level: result ) {
            for(NodeInfo node: level) {
                sql = String.format("MERGE (n:ACCOUNT%d {card_no:$card_no}) ", num);
                session.run( sql, parameters("num", String.valueOf(num), "card_no", node.toCardId));
                for (int i = 0; i < node.cardId.size(); i++) {
                    sql = String.format("MERGE (n:ACCOUNT%d {card_no:$card_no})", num) ;
                    session.run( sql, parameters("num", String.valueOf(num), "card_no", node.cardId.get(i)));
                    sql = String.format("MATCH (a:ACCOUNT%d),(b:ACCOUNT%d) where " +
                            "           a.card_no=$card_no  " +
                            "           and b.card_no=$to_card_no  " +
                            "          create (a)-[r:RES{trans_time:$trans_time,trans_amount:$trans_amount}]->(b)", num, num);
                    session.run( sql,
                            parameters("num", String.valueOf(num), "card_no", node.cardId.get(i), "to_card_no", node.toCardId, "trans_time", node.timeToString(i), "trans_amount", node.money.get(i)));
                }
            }
        }
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
     * 从起始账号的所有交易数据中查找满足指定金额的所有组合数据， 返回所有组合信息，
     * 1.通过配置信息限制组合深度
     * 2.通过配置信息限制交易金额的范围
     * 3.将所有符合交易金额范围的结果都获取到
     *
     * @param allCombine   保存所有可能的交易组合，保存的是交易列表的索引
     * @param combine      每一个交易组合
     * @param candidates   交易列表
     * @param target       目标金额
     * @param sum          计算总金额
     * @param startIndex   查询的索引
     */
    public void findCombineTrans(List<List<Integer>> allCombine, List<Integer> combine, List<NodeInfo> candidates, double target,  double sum, int startIndex)
    {
        if (sum > target * limit.getMinScale() && sum < target * limit.getMaxScale()) {
            allCombine.add(new ArrayList(combine));
            //获取所有满足交易的组合，如果后续组合满足继续查找，不退出
            //return;
        }
        for (int i = startIndex; i < candidates.size(); i++) {
            //超出限制总额，停止向树枝寻找，进行剪枝
            if (sum + candidates.get(i).getTotalMoney() > target * limit.getMaxScale()) {
                continue;
            }

            //超出指定组合笔数就跳过，进行剪枝
            if(combine.size()>=limit.getCombineNum())
            {
                continue;
            }

            sum += candidates.get(i).getTotalMoney();
            combine.add(i);

            // 从树枝节点继续往下搜索
            findCombineTrans(allCombine, combine, candidates, target, sum,i + 1);

            // 回溯，弹出一个节点后，继续临近遍历
            sum -= candidates.get(i).getTotalMoney();
            combine.remove(combine.size()-1);
        }
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
    public List<NodeInfo> getTransInfo(NodeInfo nodeInfo, int layer, Queue<NodeInfo> queue, List<NodeInfo> endTransList)
    {

        List<NodeInfo> nodeList = new ArrayList<NodeInfo>();
        List<NodeInfo> combineList = new ArrayList<NodeInfo>();

        //System.out.println("Node:"+nodeInfo+", Layer:"+layer);

        //访问neo4j获取账号交易信息
        //Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "123456"));
        //Session session = driver.session();

        Result result = session.run("MATCH (c:ACCOUNT_TABLE {card_no:$card_no})-[l:trans]->(r) RETURN c.card_no as cardId,r.card_no as toCardId,l.trans_time as dealTime, l.trans_amount as money order by dealTime ", parameters("card_no", nodeInfo.toCardId));

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


//        if(nodeList.size()>1)
//        {
//            List<Integer> combine = new ArrayList<>();
//            List<List<Integer>> allCombine = new ArrayList<>();
//            double target = nodeInfo.totalMoney;
//            System.out.println("target="+target);
//            System.out.println("NodeList="+nodeList);
//            findCombineTrans(allCombine, combine, nodeList, target, 0, 0);
//            System.out.println(allCombine);
//
//
//            if(allCombine.size()==0)
//                return null;
//
//            for (int i = 0; i < allCombine.get(0).size(); i++) {
//                combineList.add(nodeList.get(allCombine.get(0).get(i)));
//            }
//
//            nodeList = combineList;
//            System.out.println("FinalNodeList="+nodeList);
//        }

        //尾节点，插入到尾部列表，后续用于跨层级的合并
        if(nodeList.size() == 0)
        {
            endTransList.add(nodeInfo);
        }

        return nodeList;
    }

}
