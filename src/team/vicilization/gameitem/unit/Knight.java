package team.vicilization.gameitem.unit;

import team.vicilization.country.Country;
import team.vicilization.util.Position;

public class Knight extends Fighter {
    public Knight(Position position, Country country) {
        super(position, country, UnitSubType.KNIGHT);
        setSubType(UnitSubType.KNIGHT);
    }
}
