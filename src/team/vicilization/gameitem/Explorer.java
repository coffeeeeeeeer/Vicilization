package team.vicilization.gameitem;

import team.vicilization.country.Country;
import team.vicilization.gamemap.ResourceType;
import team.vicilization.mechanics.ScienceName;
import team.vicilization.util.Position;

public class Explorer extends Folk{

    public Explorer(Position position, Country country) {
        super(position, country, UnitSubType.EXPLORER);
        setSubType(UnitSubType.EXPLORER);
    }
}
