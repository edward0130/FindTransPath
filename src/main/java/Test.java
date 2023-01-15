import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.net.URI;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.neo4j.driver.Values.parameters;

public class Test {


    public static void main(String args[])
    {
//        Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "123456"));
//        Session session = driver.session();
//        session.run("create ( n: domain {ip: $ip, port: $port})", parameters("ip", "192.168.1.1", "port", "8080"));
//
//        Result result = session.run("match(n:domain) where n.ip=$ip return n.ip as ip ,n.port as port", parameters("ip", "192.168.1.1", "port", "8080"));
//
//        while(result.hasNext())
//        {
//            Record record = result.next();
//            System.out.println(record.get("ip") +":"+record.get("port"));
//        }
//        session.close();
//        driver.close();


        /*
        List<List<TransInfo>> info= new ArrayList<List<TransInfo>>();

        List<TransInfo> l = new ArrayList<TransInfo>();

        TransInfo t1 = new TransInfo(1,"11111", "22222", Utils.getTimestamp("2023-01-01 14:50:10", "yyyy-MM-dd HH:mm:ss"), 100);
        TransInfo t2 = new TransInfo(2,"22222", "33333", Utils.getTimestamp("2023-01-01 15:50:10", "yyyy-MM-dd HH:mm:ss"), 200);
        TransInfo t3 = new TransInfo(2,"22222", "33333", Utils.getTimestamp("2023-01-01 12:50:10", "yyyy-MM-dd HH:mm:ss"), 200);

        l.add(t1);
        l.add(t2);
        l.add(t3);
        Collections.sort(l);

        info.add(l);
        System.out.println(info);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        */



        ArrayList<Integer> arr = new ArrayList<Integer>(5);
        arr.add(220);
        arr.add(115);
        arr.add(320);
        arr.add(145);

        System.out.println("Size of list: " + arr.size());
        for (Integer number : arr) {
            System.out.println("Number = " + number);
        }
        arr.remove(2);

        System.out.println("After remove, Size of list: " + arr.size());

        int i=0;
        for (Integer number : arr) {
            System.out.println("Number = " + number);
            if(i++==0)
                arr.remove(0);
        }

        //long t =
        //System.out.println(Test.getTimestamp("2023-01-01 14:50:10", "yyyy-MM-dd HH:mm:ss"));

    }


    /*
class Solution {
    public List<List<Integer>> levelOrder(Node root) {
        if (root == null) {
            return new ArrayList<List<Integer>>();
        }

        List<List<Integer>> ans = new ArrayList<List<Integer>>();
        Queue<Node> queue = new ArrayDeque<Node>();
        queue.offer(root);

        while (!queue.isEmpty()) {
            int cnt = queue.size();
            List<Integer> level = new ArrayList<Integer>();
            for (int i = 0; i < cnt; ++i) {
                Node cur = queue.poll();
                level.add(cur.val);
                for (Node child : cur.children) {
                    queue.offer(child);
                }
            }
            ans.add(level);
        }

        return ans;
    }
}
*/





}
