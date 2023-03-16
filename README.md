资金交易链路追踪算法的设计与实现

![image](https://user-images.githubusercontent.com/13626321/225681518-66de1d9f-8d81-45ba-bb41-b149ca1734e1.png)

![image](https://user-images.githubusercontent.com/13626321/225681163-a76224d5-3f8e-4517-8ea2-564f45be4d4a.png)

![image](https://user-images.githubusercontent.com/13626321/225681669-479c12b7-39db-492e-96ce-5005bbb794d4.png)

![image](https://user-images.githubusercontent.com/13626321/225681727-20e3fd03-c381-49af-939e-891b9e653c7d.png)

![image](https://user-images.githubusercontent.com/13626321/225681803-edbdd4da-8574-480e-9d6c-fac7e31f9381.png)

![image](https://user-images.githubusercontent.com/13626321/225681934-e5f69ea2-fee6-443d-a831-83e3875790bc.png)

![image](https://user-images.githubusercontent.com/13626321/225687993-fe8abbd1-b586-4109-aa19-094ae21d19c9.png)

![image](https://user-images.githubusercontent.com/13626321/225682102-484a8f3b-91d7-4bd5-a055-499e83d9eebe.png)

![image](https://user-images.githubusercontent.com/13626321/225682417-d0cebce2-aee4-4ffc-89c8-5d5f01bcca23.png)

![image](https://user-images.githubusercontent.com/13626321/225682527-64f715a7-345e-4dd2-8099-70579b113869.png)

![image](https://user-images.githubusercontent.com/13626321/225682583-e2268f57-12d7-4908-9c30-99a6eeabf711.png)

![image](https://user-images.githubusercontent.com/13626321/225682650-6d980886-06ff-467e-8ccb-e721e2282ac2.png)

[资金交易链路追踪算法的设计与实现--OLTP.pdf](https://github.com/edward0130/FindTransPath/files/10992969/--OLTP.pdf)

## 测试方法：
### 1.首先往neo4j数据库写入测试数据，程序中读取数据的方法如下：
 MATCH (c:ACCOUNT_TABLE {card_no:$card_no})-[l:trans]->(r) RETURN c.card_no as cardId,r.card_no as toCardId,l.trans_time as dealTime, l.trans_amount as money order by dealTime 
### 2.调用java程序参数如下：
usage: TransMain cardId toCardId dealTime money    
62319000001760*21332 62319000001760*21333 "2023-01-15 00:00:17" 30000
### 3.结果数据写入到neo4j图库中
存储在图库的表名为ACCOUNT%d，多种组合序号自动+1

