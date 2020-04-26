package astarpathfinder;

public class Node {
    public int[] pos;
    public Node parent=null;
    public int g_cost = 0;
    public int h_cost = 0;
    public int cost = g_cost + h_cost;

    public Node(int[] pos) {
        this.pos = pos;
    }

    public void update_cost() {
        this.cost = this.h_cost + this.g_cost;
    }
}
