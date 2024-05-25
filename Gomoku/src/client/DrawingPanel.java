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
    //number of gridlines going in each direction
    private final static int distanceBetweenIntersections = 40;
    //distance between each grid in pixels
    private ArrayList<Point> pointList = new ArrayList<>();
    public static final int radius = 15;
    private Color currentColor = Color.WHITE;
    private Boolean frozen = false;
    private MessagePanel mPanel = null;
    private ArrayList<GomokuPiece> circleList= new ArrayList<GomokuPiece>();

    // this is called when we receive from the server - it's easier for this to just be a string
    // server will send these messages:
    // game start: A vs B
    // game finished: A won
    // game finished: A won because B left
    // turn: 10 15
    public void setCurrentColor(Color color) { currentColor = color; }
    public Color getCurrentColor() {return currentColor;}

    //Don't need a main here since we use the one in gomoku
/*    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShow();
            }
        });
    }*/

    // CONSTRUCTOR
    DrawingPanel() {
        //create arraylist of valid intersections to check against

        // logic: going across rows, then going across columns to store points in pointList
        for(int y = 1; y < intersectionPerRowOrColumn; y++) {
            for(int x = 1; x < intersectionPerRowOrColumn; x++) {
                int ptY = y * distanceBetweenIntersections;
                int ptX = x * distanceBetweenIntersections;
                pointList.add(new Point(ptX, ptY));
            }
        }

        //add mouse listener
        addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                if(!frozen) {

                    Point clickedPoint = e.getPoint();
                    System.out.println("Click is at: X = " + clickedPoint.getX() + " Y: " + clickedPoint.getY());

                    // pass intersection to the point array finder
                    Point intersection = findIntersection(clickedPoint);

                    if ( intersection == null ) {
                        System.out.println("invalid point, nothing to do");
                    } else { // point is in an intersection
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
                        //send to server
                    }
                }
                else {
                    System.out.println("game is currently frozen");
                }
            }
        });

        createAndShow();
    }


    // ENTER FUNCTION PAINT COMPONENT
    @Override
    protected void paintComponent(Graphics g) {
        System.out.println("Entered paint component in DrawingPanel");

        g.clearRect(0, 0, getWidth(), getHeight());

        //draw grid lines going horizontally i.e. x is changing
        for(int i = 1; i <= intersectionPerRowOrColumn; i++) {
            int x = i * distanceBetweenIntersections;
            g.drawLine(x, 0, x, 640);

            // TODO: get rid of the extra line on the side - I don't know why it is there
        }

        //draw grid lines going vertically i.e. y is changing
        for(int i = 1; i < intersectionPerRowOrColumn; i++) {
            int y = i * distanceBetweenIntersections;
            g.drawLine(0, y, 600, y);
//            System.out.println("Vertical line number: " + i);
        }

        for(GomokuPiece p : circleList) {
            g.setColor(p.color);
            g.fillOval(p.x, p.y, 2 * p.radius, 2 * p.radius);
            g.setColor(Color.GRAY); //outline makes white piece more readable
            g.drawOval(p.x, p.y, 2 * p.radius, 2 * p.radius);
        }
    }


    // ENTER FUNCTION DRAW GOMOKU PIECE
    public void drawGomokuPiece(Point intersect, Color color) {
        Point intersection = new Point((int)intersect.getX(), (int)intersect.getY());
        // make a circle with the intersection at the center
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
        // boolean winner = GomokuLogic.checkWinCondition();
        //System.out.println("Winner is: " + winner);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(distanceBetweenIntersections * intersectionPerRowOrColumn, intersectionPerRowOrColumn * distanceBetweenIntersections);
    }

    //temp testing
    public static void createAndShow() {
        JPanel draw = new JPanel();
        draw.setBackground(new Color(255, 255 ,240));
    }

    // ENTER FUNCTION FIND INTERSECTION
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
                // if it is in the radius of the point - aka if the intersection is clicked accurately
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
        //draw grid lines going horizontally i.e. x is changing
        for(int i = 1; i <= intersectionPerRowOrColumn; i++) {
            int x = i * distanceBetweenIntersections;
            g.drawLine(x, 0, x, 640);

            // TODO: get rid of the extra line on the side - I don't know why it is there
        }

        //draw grid lines going vertically i.e. y is changing
        for(int i = 1; i < intersectionPerRowOrColumn; i++) {
            int y = i * distanceBetweenIntersections;
            g.drawLine(0, y, 600, y);
//            System.out.println("Vertical line number: " + i);
        }


        unfreeze();
    }

    public void freeze() {
        //freeze panel when game ends
        frozen = true;
    }

    public void unfreeze() {
        frozen = false;
    }

    public void setMessagePanel(MessagePanel m) {mPanel = m;}

    public int getDistanceBetweenIntersections( ) { return distanceBetweenIntersections;}
}
