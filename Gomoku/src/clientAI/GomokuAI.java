package clientAI;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;

public class GomokuAI {
    // String array can hold null (blank), black, and white
    public static String[][] gomArr = new String[15][15];
    private static int turn;
    DrawingPanel dp = null;
    private String currentColor;
    private String opponentColor;


    public void start(DrawingPanel d) {
        dp = d;
        if ( dp.getCurrentColor() == Color.BLACK ) {
            currentColor = "black";
            opponentColor = "white";
        } else if ( dp.getCurrentColor() == Color.WHITE ) {
            currentColor = "white";
            opponentColor = "black";
        }

        for(int x = 0; x < 15; x++) {
            for (int y = 0; y < 15; y++) {
                gomArr[x][y] = "blank";
            }
        }

        playFirstMove();

    }

    public void playFirstMove() {
        if(currentColor.equals("black")) {
            //make our first turn
            int gap = dp.getDistanceBetweenIntersections();
            dp.drawGomokuPiece(new Point((7 * gap) + gap, (7 * gap) + gap), Color.BLACK);
            dp.getMessagePanel().sendMessage(Protocol.generatePlayMessage(true, 7, 7));
            setGomArr(7, 7, "black");
            System.out.println("AI has made the first turn");
        } else {
            System.out.println("We are white and don't have the first turn");
        }
    }

    public void playNextMove() {
        Color c = Color.WHITE;
        boolean isBlack = false;
        if(currentColor.equals("black")) {
            c = Color.BLACK;
            isBlack = true;
        }
        Point pt = findBestPoint();
        System.out.println("Actual point at: ( " + pt.getX() + " , " + pt.getY() + " )");
        int gap = dp.getDistanceBetweenIntersections();
        int x = ((int)pt.getX() * gap) + gap;
        int y = ((int)pt.getY() * gap) + gap;
        Point drawpt = new Point(x, y);

        System.out.println("AI tells us to draw a point at: ( " + x + " , " + y + " )" );
        dp.drawGomokuPiece(drawpt, c);
        dp.getMessagePanel().sendMessage(Protocol.generatePlayMessage(isBlack, (int)pt.getX(), (int)pt.getY()));
        setGomArr((int)pt.getX(), (int)pt.getY(), currentColor);

    }

    // constructor
    GomokuAI() {
        //initialize board to blank
        reset();
    }

    // reset function will call setFirstTurn
    public void setFirstTurn() {turn = 0;}

    // function allows us to update the colors in the gomoku board
    public static void setGomArr(int x, int y, String type) {
        System.out.println("We have entered setGomArr");
        gomArr[x][y] = type;
        System.out.println("x:" + x + "y:" + y + gomArr[x][y]);

        turn++;
    }

    // TODO: this whole function might not be necessary - it isn't called and it works
    // on intersections
    private boolean isValidPlacement(String t, int x, int y) {
        boolean canPlace = false;
        //first check if first turn for either player

        //if its not the first turn then check if the spot is blank
        if(x < 0 || x > 14) {
            canPlace = false;
        }
        else if(y < 0 || y > 14) {
            canPlace = false;
        }
        else if (gomArr[x][y].equals("blank")) {
            canPlace = true;
        }
        return canPlace;
    }

    public void reset() {
        for(int x = 0; x < 15; x++) { // TODO: set these to width or size thing
            for(int y = 0; y < 15; y++) {
                gomArr[x][y] = null;
            }
        }
        turn = 0;
//        winnerExists = false;
    }


    public Point findBestPoint() {
        Point bestPoint = new Point();
        int score = 0;

        for (int x = 0; x < 15; x++) {
            for (int y = 0; y < 15; y++) {
                for ( int z = 0; z < (gomArr.length * gomArr[x].length ); z++ ) {
                    if ( isValidPlacement(currentColor, x, y) || isValidPlacement(opponentColor, x, y) ) {
                        int checkedScore = calcScore(x, y);

                        if ( checkedScore > score ) {
                            bestPoint = new Point(x, y);
                            score = checkedScore;
                            System.out.println("Estimated score for a offensive move is: " + score);
                        }
                    }
                }
            }
        } // end loops to find the best offensive move

        Point biggestthreat = checkThreatCount(); // returns an array of Points where the threat must be blocked

        if ( biggestthreat != null && isValidPlacement(currentColor, (int)biggestthreat.getX(), (int)biggestthreat.getY())) {
            bestPoint = biggestthreat;
            System.out.println("threat at: " + biggestthreat);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return bestPoint;
    }

    public int calcScore(int x, int y) {
        int localScore = 0;

        // check the 8 points around the selected point
        if ( x == 0 && y == 0 ) {
            if ( gomArr[x+1][y] == currentColor ) {
                localScore++;
            } else if ( gomArr[x][y+1] == currentColor ) {
                localScore++;
            } else if ( gomArr[x+1][y+1] == currentColor ) {
                localScore++;
            }
        } else if ( x == 0 && y < gomArr[x].length ) {
            // if x is 0 but y is not, but also is not the boundary
            for ( int j = y-1; j < gomArr[x].length; j++ ) {
                for ( int k = x; k < gomArr.length; k++ ) {
                    if ( gomArr[j][k] == currentColor ) {
                        localScore++;
                    }
                }
            }
        } else if ( x == 0 && y == gomArr[x].length ) {
            if ( gomArr[x+1][y] == currentColor ) {
                localScore++;
            } else if ( gomArr[x][y-1] == currentColor ) {
                localScore++;
            } else if ( gomArr[x+1][y-1] == currentColor ) {
                localScore++;
            }
        } else if ( x < gomArr.length && y == 0 ) {
            // if y is 0 but x is not, but also is not the boundary
            for ( int i = x-1; i < gomArr.length; i++ ) {
                for ( int j = y; j < gomArr[x].length; j++ ) {
                    if ( gomArr[i][j] == currentColor ) {
                        localScore++;
                    }
                }
            }
        } else if ( x == gomArr.length && y == 0 ) {
            if ( gomArr[x][y+1] == currentColor ) {
                localScore++;
            } else if ( gomArr[x-1][y+1] == currentColor ) {
                localScore++;
            } else if ( gomArr[x-1][y+1] == currentColor ) {
                localScore++;
            }
        } else if ( x < gomArr.length && y < gomArr[x].length ) {
            for ( int i = x-1; i < x+1; i++ ) {
                for ( int j = y-1; j < y+1; j++ ) {
                    if (gomArr[i][j] == currentColor) {
                        localScore++;
                    }
                }
            }
        } else if ( x == gomArr.length && y == gomArr[x].length ) {
            if ( gomArr[x-1][y] == currentColor ) {
                localScore++;
            } else if ( gomArr[x][y-1] == currentColor ) {
                localScore++;
            } else if ( gomArr[x-1][y-1] == currentColor ) {
                localScore++;
            }
        } else {
            System.out.println("You have reached an error!");
        }

        return localScore;
    }


    public Point checkThreatCount() {

        Point threats = null;
        int z = 0;
        String opponentColor = "";

        if ( currentColor == "black" ) { opponentColor = "white"; }
        else { opponentColor = "black"; }

        for (int x = 2; x < 12; x++) {
            for (int y = 2; y < 12; y++ ) {
                // need to check if valid
                if ( (gomArr[x][y] == gomArr[x][y+1] && gomArr[x][y] == gomArr[x][y+2] && gomArr[x][y] == opponentColor ) && ( gomArr[x][y+3] != currentColor || gomArr[x][y-1] != currentColor ) ) {
                    threats = new Point(x, y+3);
                } else if ( (gomArr[x][y] == gomArr[x+1][y] && gomArr[x][y] == gomArr[x+2][y] && gomArr[x][y] == opponentColor ) && ( gomArr[x+3][y] != currentColor || gomArr[x-1][y] != currentColor )) {
                    threats = new Point(x+3, y);
                } else if ( (gomArr[x][y] == gomArr[x+1][y+1] && gomArr[x][y] == gomArr[x+2][y+2] && gomArr[x][y] == opponentColor ) && ( gomArr[x+3][y+3] != currentColor || gomArr[x-1][y-1] != currentColor ) ){
                    threats = new Point(x+3, y+3);
                }
            }
        }

        return threats;
//		ArrayList<Point> threats = new ArrayList<Point>();
//		int z = 0;
//
//        for (int x = 0; x < gomArr.length; x++) { // iterate through rows
////          System.out.println("Entered X for loop");
//          for (int y = 0; y < gomArr[x].length; y++) { // iterate through columns
////              System.out.println("Entered Y for loop");
//              // 5 is the number of pieces in a row - so it will check 4 beyond itself
//
//              if ( gomArr[x][y].equals(opponentColor) ) {
////                  System.out.println("You have found a piece that is not blank!");
////                  System.out.println("We will now check the four pieces below it");
//
//                  System.out.println("x:" + x + " y:" + y + " y+1: " + (y+1));
//                  System.out.println("gomArr[x][y]: " + gomArr[x][y]);
//
//                  if((y + 4) < gomArr[y].length) {
//                  	if (gomArr[x][y].equals(gomArr[x][y + 1]) &&
//                      gomArr[x][y].equals(gomArr[x][y + 2])) {
//                  		for(int i = 1; i < 3; i++) {
//                  			if((y+i) < 15) {
//                  				threats.add(new Point(x, y+i));
//                  			}
//                  			if((y-i) >=0) {
//                  				threats.add(new Point(x, y-i));
//                  			}
//                  		}
//
//                      }
//                  }
//                  if((x+4) < gomArr[x].length) {
//                	  if (gomArr[x][y].equals(gomArr[x+1][y]) &&
//                	  gomArr[x][y].equals(gomArr[x+2][y])){
//
//		              	for(int i = 1; i < 3; i++) {
//		              		if((x + i) < 15) {
//		              			threats.add(new Point(x+i, y));
//		              		}
//		              		if((x-i) >= 0) {
//		              			threats.add(new Point(x-i, y));
//		              		}
//		              	}
//
//
//                      }
//                  }
//                  if( ((x + 4) > gomArr[x].length) && ((y + 4) < gomArr[y].length) ) {
//                  	if (gomArr[x][y].equals(gomArr[x+1][y+1]) &&
//                        gomArr[x][y].equals(gomArr[x+2][y+2])) {
//                  		for(int i = 1; i < 3; i ++) {
//                  			if( ((x+i) < 15) && ((y+i) < 15) ) {
//                  				threats.add(new Point(x+i, y+i));
//                  			}
//                  			if( ((x -i ) >=0 ) && ((y-i) >=0)) {
//                  				threats.add(new Point(x-i, y-i));
//                  			}
//                  		}
//                  	}//end positive slope diag
//                  }
//                  if( ((x - 2) >= 0) && ((y + 2) < gomArr[y].length) ) {
//                  	if (gomArr[x][y].equals(gomArr[x-1][y+1]) &&
//                        gomArr[x][y].equals(gomArr[x-2][y+2])) {
//
//                        for(int i = 1; i < 3  ; i++) {
//                        	if( ((x - i) >= 0) && ((y + i) < 15) ) {
//                        		threats.add(new Point(x-i, y+i));
//                        	}
//                        	if( ((x + i ) < 15 && ((y - i) >= 0 ))) {
//
//                            	threats.add(new Point(x+i, y-i));
//                        	}
//                        }
//                      }
//                  }//end negative slope diag
//
//              }//end if this is opp color
//          }//end y
//      }//end x
//
//	     //now check threats and remove invalid threats
//	     ArrayList<Integer> remove = new ArrayList<>();
//	     for (int i = 0; i < threats.size(); i++) {
//	    	 Point p = threats.get(i);
//	    	 if(!isValidPlacement(currentColor, (int)p.getX(), (int)p.getY())) {
//	    		 remove.add(i);
//	    	 }
//	     }
//	     for(int i = 0; i < remove.size(); i++) {
//	    	 threats.remove(remove.get(i));
//	     }
//	     if(threats.size() == 0) {
//	    	 return null;
//	     }
//	     else if (threats.size() == 1) {
//	    	 return threats.get(0);
//	     }
//	     else {
//	    	 Point mostrepeated = threats.get(0);
//		     int repetitions = 0;
//		     for(int i = 0; i < threats.size(); i ++) {
//		    	 //check for repeated points. point repeated the most is the most pressing threat
//		    	 Point p1 = threats.get(i);
//		    	 int c = 0;
//		    	 for(int j = 0; j < threats.size(); j++) {
//		    		 Point p = threats.get(j);
//		    		 if(p.equals(mostrepeated)) {
//		    			 c++;
//		    		 }
//		    	 }
//		    	 if (c > repetitions) {
//		    		 repetitions = c;
//		    		 mostrepeated = p1;
//		    	 }
//
//		     }
//
//		     return mostrepeated;
//	     }


    }

    //
}