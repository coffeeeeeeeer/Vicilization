package team.vicilization.util;

public class Position {
    private int x; // 列数 从左到右
    private int y; // 行数 从上到下

    public Position() {
        this.x = 0;
        this.y = 0;
    }

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Position(Position position) {
        this.x = position.getX();
        this.y = position.getY();
    }

    public static int distanceSquare(Position p1, Position p2) {
        return ((p1.getX() - p2.getX()) * (p1.getX() - p2.getX())
                + (p1.getY() - p2.getY()) * (p1.getY() - p2.getY()));
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public boolean equals(Object object) {
        if (object.getClass() == this.getClass()) {
            Position pos = (Position) object;
            if (pos.getX() == x && pos.getY() == y) {
                return true;
            }
        }
        return false;
    }
}
