public class LimitElem {
    /**
     * 1.��ʽ��׽���ۼ�ֵ����һ���ٷֱȷ�Χ�� �� 80%~120%;
     * 2.��Ͻ��ױ��������ܳ����޶�ֵ �� 3;
     * 3.��Ͻ��׵��ʽ��ռ�ȣ� ��������ָ���ٷֱ� �� 10%;
     * 4.������Ч����С�� ����ָ��ֵ�Ľ��ײ�����ͳ�ƣ� �� 10000;
     * 5.׷�ٽ��׵������� ����׷�ٵ��������� 0 ������;
     * 6.���¼����׼������Чʱ�䣬 ����׷�ٵ���Чʱ�䣬 �� 2 ��Сʱ�ڷ����Ľ���
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
