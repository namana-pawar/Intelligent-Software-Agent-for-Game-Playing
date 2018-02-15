import java.io.File;
import java.util.*;
import java.io.*;
public class homework {
    static int gridSize = 0;
    static int lizardCount = 0;
    static int[][] board = new int[gridSize][gridSize];
    //for SA
    static double T = 20000;
    Position locationRemoved;
    static LinkedList treePositions = new LinkedList();
    LinkedList<Position> selectedPositions = new LinkedList<>();
    int conflict = 0;
    int placed = 0;
    //BFS
    ArrayList<ArrayList<Position>> unsafePositions = new ArrayList<ArrayList<Position>>();

    public static void main(String args[]) throws IOException {


        homework liz = new homework();
        File file = new File("/Users/namanapawar/Documents/AI/input.txt");
        Scanner s = new Scanner(file);
        String sCurrentLine;
        int line = 0;
        int algo = 0;
        while (s.hasNext() && (sCurrentLine = s.nextLine()) != null) {
            line++;
            if (line == 1) {
                if (sCurrentLine.equalsIgnoreCase("BFS")) algo = 1;
                else if (sCurrentLine.equalsIgnoreCase("DFS")) algo = 2;
                else if (sCurrentLine.equalsIgnoreCase("SA")) algo = 3;
            } else if (line == 2) {
                gridSize = Integer.parseInt(sCurrentLine);
                board = new int[gridSize][gridSize];
            } else if (line == 3) {
                lizardCount = Integer.parseInt(sCurrentLine);
            } else {
                for (int i = 0; i < gridSize; i++) {
                    for (int j = 0; j < gridSize; j++) {
                        int val = Character.getNumericValue(sCurrentLine.charAt(j));
                        board[i][j] = val;
                        if (val == 2) treePositions.add(new Position(i, j));
                    }
                    if (s.hasNext())
                        sCurrentLine = s.nextLine();
                    else
                        break;
                }
                if(treePositions.size()==0&&lizardCount>gridSize){
                    liz.outputToFile("FAIL");
                }
                if(treePositions.size()==gridSize*gridSize){
                    liz.outputToFile("FAIL");
                }
                try{
                    if (algo == 1) {

                        liz.BFS(board);
                    }
                    else if (algo == 2) {
                        if (liz.solveBoardDFS(board, new Position(-99, -99)) == false) {

                            liz.outputToFile("FAIL");
                        } else {
                            liz.printLizardPos(board);
                        }
                    } else liz.SA(board);
                }
                catch (OutOfMemoryError e) {
                    liz.outputToFile("FAIL");
                }
            }


        }
    }
    ///Users/namanapawar/Documents/AI/
    private void outputToFile(String s) throws IOException {
        File file = new File("/Users/namanapawar/Documents/AI/output.txt");
        // creates the file
        file.createNewFile();

        // creates a FileWriter Object
        FileWriter writer = new FileWriter(file);

        // Writes the content to the file
        writer.write(s);
        writer.flush();
        writer.close();
        System.exit(0);
    }

    //BFS
    private void BFS(int[][] board) throws IOException {
        Queue<Matrix> q = new LinkedList<>();
        Matrix parent = new Matrix(board, -99, -99, 0);
        double initialTime=System.currentTimeMillis();
        do {
            double currentTime=System.currentTimeMillis();
            if(currentTime-initialTime>=270000)
            {

                outputToFile("FAIL");
                return;
            }
            for (int i = 0; i < gridSize; i++) {
                for (int j = 0; j < gridSize; j++) {
                    if (parent.grid[i][j] == 0) {
                        Matrix child = new Matrix((mark(makeNewMode(parent.grid), new Position(i, j))), i, j, parent.lizardCount + 1);
                        q.add(child);
                        if (child.lizardCount == lizardCount) {
                            printLizardPos(child.grid);
                            return;
                        }
                    }
                }
            }
            parent = q.element();
            q.remove();
        } while (q.size() != 0);
        outputToFile("FAIL");
    }

    public int[][] mark(int b[][], Position location) {
        conflict = 0;
        ArrayList<Position> unsafePosOfCurrent = new ArrayList<>();
        int safetyVal = -1;
        b[location.x][location.y] = 1;

        //Right restrictions
        for (int k = location.y + 1; k < gridSize; k++) {
            if (b[location.x][k] == 2) break;
            Position locationStore = new Position(location.x, k);
            if (b[locationStore.x][locationStore.y] != 1) {
                unsafePosOfCurrent.add(locationStore);
                b[location.x][k] = safetyVal;
            }
            if (b[locationStore.x][locationStore.y] == 1) conflict++;
        }

        //Left Restrictions
        for (int k = location.y - 1; k >= 0; k--) {
            if (b[location.x][k] == 2) break;
            Position locationStore = new Position(location.x, k);
            if (b[locationStore.x][locationStore.y] != 1) {
                unsafePosOfCurrent.add(locationStore);
                b[location.x][k] = safetyVal;
            }
            if (b[locationStore.x][locationStore.y] == 1) conflict++;
        }
        //Down restrictions
        for (int k = location.x + 1; k < gridSize; k++) {
            if (b[k][location.y] == 2) break;
            Position locationStore = new Position(k, location.y);
            if (b[locationStore.x][locationStore.y] != 1) {
                unsafePosOfCurrent.add(locationStore);
                b[k][location.y] = safetyVal;
            }
            if (b[locationStore.x][locationStore.y] == 1) conflict++;
        }
        //Top Restrictions
        for (int k = location.x - 1; k >= 0; k--) {
            if (b[k][location.y] == 2) break;
            Position locationStore = new Position(k, location.y);
            if (b[locationStore.x][locationStore.y] != 1) {
                unsafePosOfCurrent.add(locationStore);
                b[k][location.y] = safetyVal;
            }
            if (b[locationStore.x][locationStore.y] == 1) conflict++;
        }
        //primary diagonal restrictions
        int r1, c1;
        for (r1 = location.x + 1, c1 = location.y + 1; r1 < gridSize && c1 < gridSize; r1++, c1++) {
            if (b[r1][c1] == 2) break;
            Position locationStore = new Position(r1, c1);
            if (b[locationStore.x][locationStore.y] != 1) {
                unsafePosOfCurrent.add(locationStore);
                b[r1][c1] = safetyVal;
            }
            if (b[locationStore.x][locationStore.y] == 1) conflict++;
        }
        for (r1 = location.x - 1, c1 = location.y - 1; r1 >= 0 && c1 >= 0; r1--, c1--) {
            if (b[r1][c1] == 2) break;
            Position locationStore = new Position(r1, c1);
            if (b[locationStore.x][locationStore.y] != 1) {
                unsafePosOfCurrent.add(locationStore);
                b[r1][c1] = safetyVal;
            }
            if (b[locationStore.x][locationStore.y] == 1) conflict++;
        }
        //diagonal restriction
        for (r1 = location.x + 1, c1 = location.y - 1; r1 < gridSize && c1 >= 0; r1++, c1--) {
            if (b[r1][c1] == 2) break;
            Position locationStore = new Position(r1, c1);
            if (b[locationStore.x][locationStore.y] != 1) {
                unsafePosOfCurrent.add(locationStore);
                b[r1][c1] = safetyVal;
            }
            if (b[locationStore.x][locationStore.y] == 1) conflict++;
        }
        for (r1 = location.x - 1, c1 = location.y + 1; r1 >= 0 && c1 < gridSize; r1--, c1++) {
            if (b[r1][c1] == 2) break;
            Position locationStore = new Position(r1, c1);
            if (b[locationStore.x][locationStore.y] != 1) {
                unsafePosOfCurrent.add(locationStore);
                b[r1][c1] = safetyVal;
            }
            if (b[locationStore.x][locationStore.y] == 1) conflict++;
        }
        unsafePositions.add(unsafePosOfCurrent);
        return b;
    }

    private int[][] makeNewMode(int b[][]) {
        int newBoard[][] = new int[gridSize][gridSize];
        for (int i = 0; i < gridSize; i++)
            for (int j = 0; j < gridSize; j++)
                newBoard[i][j] = b[i][j];
        return newBoard;

    }

    //DFS
    boolean solveBoardDFS(int b[][],Position loc) throws IOException {
        double initialTime=System.currentTimeMillis();
        if (placed == gridSize-1) {
            return true;
        }
        loc=findNextFreePosition(b,loc);
        while (loc!=null)
        {
            b = mark(b);
            double currentTime=System.currentTimeMillis();
            if(currentTime-initialTime>=270000)
            {

                try {
                    outputToFile("FAIL");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false;
            }
            if(solveBoardDFS(b,loc))
                return true;
            b=unmark(b);
            loc=findNextFreePosition(b,loc);
        }
        return false;
    }

    public Position findNextFreePosition(int b[][], Position p) {
        if (p.x != -99) {
            for (int r1 = p.x; r1 < gridSize; r1++) {
                for (int c1 = 0; c1 < gridSize; c1++) {
                    if (r1 == p.x && c1 <= p.y)
                        continue;
                    if (b[r1][c1] == 0) {
                        selectedPositions.add(new Position(r1, c1));
                        placed = selectedPositions.size() - 1;
                        return (Position) selectedPositions.get(placed);
                    }
                }
            }
        } else {
            for (int r1 = 0; r1 < gridSize; r1++) {
                for (int c1 = 0; c1 < gridSize; c1++) {
                    if (b[r1][c1] == 0) {
                        selectedPositions.add(new Position(r1, c1));
                        placed = selectedPositions.size() - 1;
                        return (Position) selectedPositions.get(placed);
                    }
                }
            }
        }

        return null;
    }

    public int[][] mark(int b[][]){
        ArrayList unsafePosOfCurrent=new ArrayList<Position>();
        int safetyVal=-1;
        Position location=(Position)selectedPositions.get(placed);
        //System.out.println("Placing"+location.x+" "+location.y);
        b[location.x][location.y] = 1;

        //Right restrictions
        for (int k = location.y + 1; k < gridSize; k++) {
            if (b[location.x][k] == 2) break;
            Position locationStore=new Position(location.x,k);
            if(b[locationStore.x][locationStore.y]!=-1) {
                unsafePosOfCurrent.add(locationStore);
                b[location.x][k] = safetyVal;
            }
        }
        //Left Restrictions
        for (int k = location.y - 1; k >= 0; k--) {
            if (b[location.x][k] == 2) break;
            Position locationStore=new Position(location.x,k);
            if(b[locationStore.x][locationStore.y]!=-1) {
                unsafePosOfCurrent.add(locationStore);
                b[location.x][k] = safetyVal;
            }
        }
        //Down restrictions
        for (int k = location.x + 1; k < gridSize; k++) {
            if (b[k][location.y] == 2) break;
            Position locationStore=new Position(k,location.y);
            if(b[locationStore.x][locationStore.y]!=-1) {
                unsafePosOfCurrent.add(locationStore);
                b[k][location.y] = safetyVal;
            }
        }
        //Top Restrictions
        for (int k = location.x - 1; k >= 0; k--) {
            if (b[k][location.y] == 2) break;
            Position locationStore=new Position(k,location.y);
            if(b[locationStore.x][locationStore.y]!=-1) {
                unsafePosOfCurrent.add(locationStore);
                b[k][location.y] = safetyVal;
            }
        }
        //primary diagonal restrictions
        int r1, c1;
        for (r1 = location.x + 1, c1 = location.y + 1;r1 < gridSize && c1 < gridSize;r1++,c1++) {
            if (b[r1][c1] == 2) break;
            Position locationStore=new Position(r1,c1);
            if(b[locationStore.x][locationStore.y]!=-1) {
                unsafePosOfCurrent.add(locationStore);
                b[r1][c1] = safetyVal;
            }
        }
        for (r1 = location.x - 1,c1 = location.y - 1;r1 >= 0 && c1 >= 0;r1--,c1--) {
            if (b[r1][c1] == 2) break;
            Position locationStore=new Position(r1,c1);
            if(b[locationStore.x][locationStore.y]!=-1) {
                unsafePosOfCurrent.add(locationStore);
                b[r1][c1] = safetyVal;
            }
        }
        //diagonal restriction
        for (r1 = location.x + 1,c1 = location.y - 1;r1 < gridSize && c1 >= 0;r1++,c1--) {
            if (b[r1][c1] == 2) break;
            Position locationStore=new Position(r1,c1);
            if(b[locationStore.x][locationStore.y]!=-1) {
                unsafePosOfCurrent.add(locationStore);
                b[r1][c1] = safetyVal;
            }
        }
        for (r1 = location.x - 1,c1 = location.y + 1;r1 >= 0 && c1 < gridSize; r1--,c1++) {
            if (b[r1][c1] == 2) break;
            Position locationStore=new Position(r1,c1);
            if(b[locationStore.x][locationStore.y]!=-1) {
                unsafePosOfCurrent.add(locationStore);
                b[r1][c1] = safetyVal;
            }
        }
        unsafePositions.add(unsafePosOfCurrent);
        return b;
    }
    public int[][] unmark(int b[][]){

        Position location=(Position)selectedPositions.get(placed);
        //System.out.println("Removing"+location.x+" "+location.y);
        b[location.x][location.y]=0;
        ArrayList<Position> unsafeCurrentPos= (ArrayList<Position>) unsafePositions.get(placed);
        for (Position p:unsafeCurrentPos)
        {
            b[p.x][p.y]=0;
        }
        selectedPositions.remove(placed);
        unsafePositions.remove(placed);
        placed=selectedPositions.size()-1;

        return b;
    }

    /*SIMULATED ANNEALING*/
    private void SA(int b[][]) throws IOException {
        //TimedExit t=new TimedExit();
        int oldConflict = -99, newConflict = -99;
        Position randPosition = new Position(-99, -99);
        placed = 0;
        double initialTime=System.currentTimeMillis();
        while (placed < lizardCount) {
            do {
                randPosition = new Position((int) (Math.random() * 10 % gridSize), (int) (Math.random() * 10 % gridSize));
            } while (b[randPosition.x][randPosition.y]==1||b[randPosition.x][randPosition.y]==2);
            selectedPositions.add(randPosition);
            placed++;
            b[randPosition.x][randPosition.y] = 1;
        }
        while (T > 0) {
            double currentTime=System.currentTimeMillis();
            if(currentTime-initialTime>=270000)
            {
                System.out.println("here");
                try {
                    outputToFile("FAIL");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.exit(0);
            }
            newConflict = getAllConflicts(b);
            if (oldConflict != -99) {
                if (newConflict == 0) {
                    //System.out.println("Solution Found! :)");
                    printLizardPos(b);
                    break;
                }
                if (newConflict > oldConflict) {
                    double probability = Math.exp((double) (oldConflict - newConflict) / T);
                    if (!checkIfOptimalMove(probability)) {
                        selectedPositions.remove(placed - 1);
                        b[randPosition.x][randPosition.y] = 0;
                        b[locationRemoved.x][locationRemoved.y] = 1;
                        selectedPositions.add(placed - 1, locationRemoved);
                        newConflict = oldConflict;
                    }

                }
            }
            placed = (int) (Math.random() * 10 % (lizardCount)) + 1;
            locationRemoved = (Position) selectedPositions.get(placed - 1);
            selectedPositions.remove(placed - 1);
            b[locationRemoved.x][locationRemoved.y] = 0;
            do {
                randPosition = new Position((int) (Math.random() * 10 % gridSize), (int) (Math.random() * 10 % gridSize));
            } while (b[randPosition.x][randPosition.y]==1||b[randPosition.x][randPosition.y]==2);
            selectedPositions.add(placed - 1, randPosition);
            b[randPosition.x][randPosition.y] = 1;
            oldConflict = newConflict;
            T = newConflict / Math.log(gridSize);
        }
        if (T == 0) {
            outputToFile("FAIL");
            //System.out.println("no solution");
        }
    }

    private boolean contains(LinkedList<Position> list, Position p) {
        int index = 0;
        for (index = 0; index < list.size(); index++) {
            Position comparison = (Position) list.get(index);
            if (comparison.x == p.x && comparison.y == p.y)
                return true;
        }
        return false;

    }

    private int getAllConflicts(int b[][]) {
        int conflict = 0;
        for (int i=0;i<selectedPositions.size();i++)
            conflict+=getCurrentConflict(b,selectedPositions.get(i));
        return conflict;

    }

    private int getCurrentConflict(int b[][],Position location){
        int con=0;
        //Right restrictions
        for (int k = location.y + 1; k < gridSize; k++) {
            Position locationStore=new Position(location.x,k);
            if(b[locationStore.x][locationStore.y]==2)break;
            if(b[locationStore.x][locationStore.y]==1)con++;
        }
        //Left Restrictions
        for (int k = location.y - 1; k >= 0; k--) {
            Position locationStore=new Position(location.x,k);
            if(b[locationStore.x][locationStore.y]==2)break;
            if(b[locationStore.x][locationStore.y]==1)con++;
        }
        //Down restrictions
        for (int k = location.x + 1; k < gridSize; k++) {
            Position locationStore=new Position(k,location.y);
            if(b[locationStore.x][locationStore.y]==2)break;
            if(b[locationStore.x][locationStore.y]==1)con++;
        }
        //Top Restrictions
        for (int k = location.x - 1; k >= 0; k--) {
            Position locationStore=new Position(k,location.y);
            if(b[locationStore.x][locationStore.y]==2)break;
            if(b[locationStore.x][locationStore.y]==1)con++;
        }
        //primary diagonal restrictions
        int r1, c1;
        for (r1 = location.x + 1, c1 = location.y + 1;r1 < gridSize && c1 < gridSize;r1++,c1++) {
            Position locationStore=new Position(r1,c1);
            if(b[locationStore.x][locationStore.y]==2)break;
            if(b[locationStore.x][locationStore.y]==1)con++;
        }
        for (r1 = location.x - 1,c1 = location.y - 1;r1 >= 0 && c1 >= 0;r1--,c1--) {
            Position locationStore=new Position(r1,c1);
            if(b[locationStore.x][locationStore.y]==2)break;
            if(b[locationStore.x][locationStore.y]==1)con++;
        }
        //diagonal restriction
        for (r1 = location.x + 1,c1 = location.y - 1;r1 < gridSize && c1 >= 0;r1++,c1--) {
            Position locationStore=new Position(r1,c1);
            if(b[locationStore.x][locationStore.y]==2)break;
            if(b[locationStore.x][locationStore.y]==1)con++;
        }
        for (r1 = location.x - 1,c1 = location.y + 1;r1 >= 0 && c1 < gridSize; r1--,c1++) {
            Position locationStore=new Position(r1,c1);
            if(b[locationStore.x][locationStore.y]==2)break;
            if(b[locationStore.x][locationStore.y]==1)con++;
        }
        return con;
    }
    public void printLizardPos(int b[][]) throws IOException {
        String output=new String("OK\n");
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (b[i][j] == 1) {
                    //System.out.print("L ");
                    output += Integer.toString(1);
                }
                else if (b[i][j] == 0) {
                    //System.out.print("S ");
                    output += Integer.toString(0);
                }
                else if (b[i][j] == 2) {
                    //System.out.print("T ");
                    output += Integer.toString(2);
                }
                else if (b[i][j] < 0) {
                    //System.out.print("X ");
                    output += Integer.toString(0);
                }

            }
            output+="\n";
            //System.out.println();
        }
        output+="\n";
        //System.out.println();
        outputToFile(output);
    }

    private boolean checkIfOptimalMove(double probability) {
        boolean accept = false;
        double randomeNumber = Math.random();
        if (randomeNumber < probability)
            return true;
        return false;

    }

}

class Position {
    public int x, y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }
}

class Matrix {
    int lizardCount;
    int row;
    int col;
    int grid[][];

    Matrix(int[][] g, int r, int c, int l) {
        this.lizardCount = l;
        this.grid = new int[g.length][g.length];
        for (int i = 0; i < g.length; i++)
            for (int j = 0; j < g.length; j++) {
                this.grid[i][j] = g[i][j];
            }
        row = r;
        col = c;
    }
}
