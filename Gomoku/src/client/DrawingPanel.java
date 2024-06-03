package client;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;

public class DrawingPanel extends JPanel {

    private final static int intersectionPerRowOrColumn = 16;
    private final static int distanceBetweenIntersections = 40;
    private ArrayList<Point> pointList = new ArrayList<>();
    public static final int radius = 15;
    private Color currentColor = Color.WHITE;
    private Boolean frozen = false;
    private MessagePanel mPanel = null;
    private ArrayList<GomokuPiece> circleList= new ArrayList<GomokuPiece>();

    public void setCurrentColor(Color color) { currentColor = color; }
    public Color getCurrentColor() { return currentColor; }

    DrawingPanel() {
        for(int y = 1; y < intersectionPerRowOrColumn; y++) {
            for(int x = 1; x < intersectionPerRowOrColumn; x++) {
                int ptY = y * distanceBetweenIntersections;
                int ptX = x * distanceBetweenIntersections;
                pointList.add(new Point(ptX, ptY));
            }
        }

        addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                if(!frozen) {

                    Point clickedPoint = e.getPoint();
                    System.out.println("Click is at: X = " + clickedPoint.getX() + " Y: " + clickedPoint.getY());

                    Point intersection = findIntersection(clickedPoint);

                    if ( intersection == null ) {
                        System.out.println("invalid point, nothing to do");
                    } else {
                        System.out.println("Click centered at: x=" + intersection.getX() + " y=" + intersection.getY());
                        drawGomokuPiece(intersection, currentColor);

                        Boolean isBlack = false;
                        if(currentColor.equals(Color.BLACK))
                            isBlack = true;
                        int x = (int)intersection.getX() / distanceBetweenIntersections;
                        int y = (int)intersection.getY() / distanceBetweenIntersections;
                        x = x - 1;
                        y = y - 1;

                        mPanel.sendMessage(Protocol.generatePlayMessage(isBlack, x, y));
                    }
                }
                else {
                    System.out.println("game is currently frozen");
                }
            }
        });

        createAndShow();
    }


    @Override
    protected void paintComponent(Graphics g) {
        System.out.println("Entered paint component in DrawingPanel");

        g.clearRect(0, 0, getWidth(), getHeight());

        for(int i = 1; i <= intersectionPerRowOrColumn; i++) {
            int x = i * distanceBetweenIntersections;
            g.drawLine(x, 0, x, 640);

        }

        for(int i = 1; i < intersectionPerRowOrColumn; i++) {
            int y = i * distanceBetweenIntersections;
            g.drawLine(0, y, 600, y);
        }

        for(GomokuPiece p : circleList) {
            g.setColor(p.color);
            g.fillOval(p.x, p.y, 2 * p.radius, 2 * p.radius);
            g.setColor(Color.GRAY);
            g.drawOval(p.x, p.y, 2 * p.radius, 2 * p.radius);
        }
    }


    public void drawGomokuPiece(Point intersect, Color color) {
        Point intersection = new Point((int)intersect.getX(), (int)intersect.getY());
        System.out.println("intersection  x:" + intersection.getX() + " y: " + intersection.getY());

        int minX = (int)(intersection.getX()) - radius;
        int minY = (int) (intersection.getY()) - radius;
        System.out.println("drawing point at: x=" + minX + " y=" + minY);
        System.out.println("double of point: x=" + (intersection.getX() - radius) + " y=" + (intersection.getY() - ( radius )) );
        GomokuPiece gp = new GomokuPiece(minX, minY, radius, color);
        circleList.add(gp);
        repaint();

        if(color.equals(currentColor)) {
            System.out.println("freezing");
            if(currentColor.equals(Color.black))
                mPanel.getDataPanel().updateDialogueArea(MESSAGETYPE.WHITETURN);
            else
                mPanel.getDataPanel().updateDialogueArea(MESSAGETYPE.BLACKTURN);
            freeze();
        }
        else {
            System.out.println("unfreezing");
            mPanel.getDataPanel().updateDialogueArea(MESSAGETYPE.YOURTURN);
            unfreeze();
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(distanceBetweenIntersections * intersectionPerRowOrColumn, intersectionPerRowOrColumn * distanceBetweenIntersections);
    }

    public static void createAndShow() {
        JPanel draw = new JPanel();
        draw.setBackground(new Color(255, 255 ,240));
    }

    public Point findIntersection(Point p) {
        Point intersection = null;

        for ( int i = 0; i < pointList.size(); i++ ) {
            Point actualIntersection = pointList.get(i);
            double xUpperBound = actualIntersection.getX() + radius;
            double xLowerBound = actualIntersection.getX() - radius;
            double actualX = p.getX();
            double yUpperBound = actualIntersection.getY() + radius;
            double yLowerBound = actualIntersection.getY() - radius;
            double actualY = p.getY();

            if ( actualX < xUpperBound && actualX > xLowerBound &&
                    actualY < yUpperBound && actualY > yLowerBound ) {
                intersection = actualIntersection;
            }
        }

        return intersection;
    }

    public void reset() {
        System.out.println("Resetting in drawingpanel aka this is the real reset okay");
        circleList.clear();
        Graphics g = getGraphics();
        g.clearRect(0, 0, getWidth(), getHeight());
        for(int i = 1; i <= intersectionPerRowOrColumn; i++) {
            int x = i * distanceBetweenIntersections;
            g.drawLine(x, 0, x, 640);

        }

        for(int i = 1; i < intersectionPerRowOrColumn; i++) {
            int y = i * distanceBetweenIntersections;
            g.drawLine(0, y, 600, y);
        }


        unfreeze();
    }

    public void freeze() {
        frozen = true;
    }

    public void unfreeze() {
        frozen = false;
    }

    public void setMessagePanel(MessagePanel m) {mPanel = m;}

    public int getDistanceBetweenIntersections( ) { return distanceBetweenIntersections;}
}
