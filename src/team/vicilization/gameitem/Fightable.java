package team.vicilization.gameitem;

public interface Fightable {
    public int getAttack();

    public int getDefence();

    public int getHealth();

    public void injure(int damage);

    public void die();
}
