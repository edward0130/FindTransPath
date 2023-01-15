public class LimitElem {
    /**
     * 1.多笔交易金额累加值，在一定百分比范围内 如 80%~120%;
     * 2.组合交易笔数，不能超过限定值 如 3;
     * 3.组合交易单笔金额占比， 不能少于指定百分比 如 10%;
     * 4.交易有效的最小金额， 少于指定值的交易不计入统计， 如 10000;
     * 5.追踪交易的跳数， 限制追踪的跳数，如 0 不限制;
     * 6.上下级交易间隔的有效时间， 设置追踪的有效时间， 如 2 两小时内发生的交易
     **/
    float minScale = 0.8f;
    float maxScale = 1.2f;
    int combineNum = 3;
    float singleScale = 0.1f;
    int steps = 0;
    int hours = 24;
    double minMoney = 1000;

    public double getMinMoney() {
        return minMoney;
    }

    public void setMinMoney(double minMoney) {
        this.minMoney = minMoney;
    }



    public float getMinScale() {
        return minScale;
    }

    public void setMinScale(float minScale) {
        this.minScale = minScale;
    }

    public float getMaxScale() {
        return maxScale;
    }

    public void setMaxScale(float maxScale) {
        this.maxScale = maxScale;
    }

    public int getCombineNum() {
        return combineNum;
    }

    public void setCombineNum(int combineNum) {
        this.combineNum = combineNum;
    }

    public float getSingleScale() {
        return singleScale;
    }

    public void setSingleScale(float singleScale) {
        this.singleScale = singleScale;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public long getMillisecond(){
        return this.hours * 60 * 60 * 1000;
    }
}
