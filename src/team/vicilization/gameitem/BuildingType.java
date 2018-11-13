package team.vicilization.gameitem;

public enum BuildingType {

    ACADEMY,
    COMMERCIALCERTER,
    INDUSTRIALPARK;

    @Override
    public String toString() {
        String name=super.toString().toLowerCase();
        char[] cs=name.toCharArray();
        cs[0]-=32;
        return String.valueOf(cs);
    }
    //TODO 多单词枚举值的toString方法
}
