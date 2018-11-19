package team.vicilization.gamemap;

enum TerrainType {
    PLAIN,
    HILL,
    RIDGE,
    LAKE,
    RIVER_ROW,
    RIVER_COL,
    RIVER_NE,
    RIVER_NW,
    RIVER_SE,
    RIVER_SW,
    ;

    public int getMobilityCost() {
        return GameMapConfig.TERRAIN_MOBILITY_COST.get(this);
    }

    public int getDefenceBuff() {
        return GameMapConfig.TERRAIN_DEFENCE_BUFF.get(this);
    }

    public int getProductivityYield() {
        return GameMapConfig.TERRAIN_PRODUCTIVITY.get(this);
    }

    public int getMoneyYield() {
        return GameMapConfig.TERRAIN_MONEY.get(this);
    }

    public int getFoodYield() {
        return GameMapConfig.TERRAIN_FOOD.get(this);
    }

    public int getScienceYield() {
        return GameMapConfig.TERRAIN_SCIENCE.get(this);
    }
}
