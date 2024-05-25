package client;
import java.awt.Color;

public class GomokuPiece {
    public int radius;
    public int x;
    public int y;
    public Color color;

    public GomokuPiece(int i, int j, int r, Color c) {
        x = i;
        y = j;
        radius = r;
        color = c;
    }
}
