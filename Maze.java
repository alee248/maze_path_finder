// Do not edit this for homework.

// See the comments near "main()" about various ways to create and
// print a Maze from the command line.

// A Maze is an N by N array of bits (0 or 1).  Each '1' is a wall,
// and each '0' is open space.  Two bits are "4-adjacent" if they are
// separated by one step in one of the four cardinal directions: (up,
// down, left, or right).  An "open path" in the maze is a sequence of
// 4-adjacent open spaces.  In PathFinder, we are interested in
// whether there is an open path from the top-left corner S (position
// (0, 0)) to the bottom-right corner T (position (N-1, N-1)).

// This class provides methods to read a maze from a text file,
// to print one out, and to generate a maze at random.

import java.util.Random;
import java.util.Scanner;
import java.util.Arrays;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;

public class Maze
{
    // We represent a maze as a square (N by N) char array.  A char
    // is either '0' or '1', meaning open space or a wall.
    final private int N;
    final private char[][] maze;

    // This string tries to describe how the maze was constructed.
    private String args;        // summary of command-line arguments
    public String desc() { return "java Maze " + args; }

    // This constructor reads a maze from an input text stream, such
    // as an open file.  We scan the text as a sequence of tokens
    // separated by whitespace (spaces or newlines).  The first token
    // must be the integer N (in decimal).  The next N*N tokens should
    // each be a bit: 0 or 1.  The first N bits define the first row
    // of the maze (maze[0][j], for j from 0 to N-1), the next N bits
    // are the second row of the maze, and so on.
    public Maze(InputStream in)
    {
        args = "file?";        // replaced later, if we have fileName
        Scanner s = new Scanner(in);
        N = s.nextInt();
        if (N < 1)
            throw new RuntimeException("N must be positive: " + N);
        maze = new char[N][N];
        int i=0, j=0;
        while(i < N)
        {
            String bit = s.next();
            if (!(bit.equals("0") || bit.equals("1")))
                throw new RuntimeException("bit must be 0 or 1: " + bit);
            maze[i][j] = bit.charAt(0);
            if (++j==N) { j=0; ++i; }
        }
    }

    // Construct maze by reading from the named file.
    public Maze(String fileName) throws IOException {
        this(new FileInputStream(fileName));
        args = fileName;
    }

    // Accessors.
    public int size() { return maze.length; }

    // Is Position p in range (as row/column indices) for this maze?
    public boolean inRange(Position p) { return inRange(p.i, p.j); }
    public boolean inRange(int x, int y) {
        return x>=0 && x<N && y>=0 && y<N;
    }

    // Low level: get the char at a Position.
    public char get(Position p) { return maze[p.i][p.j]; }

    // Is Position p a wall in this maze?
    public boolean isWall(Position p) { return inRange(p) && get(p)=='1'; }

    // Is Position p open space in this maze?
    public boolean isOpen(Position p) { return inRange(p) && get(p)=='0'; }

    // Return a copy of our maze array.
    // The user can modify this array without changing ours.
    public char[][] copyArray() {
        char[][] copy = new char[N][];
        for (int i=0; i<N; ++i)
            copy[i] = Arrays.copyOf(maze[i], N);
        return copy;
    }

    // This constructor builds a random N by N maze (for odd N).  When
    // i and j are even, position (i,j) is a "room", and all rooms are
    // open.  When (i+j) is odd, (i,j) is a potential "passage"
    // between two rooms, and we open some subset of these passages.
    //
    // If C and D are 0, the maze is a random tree on the rooms
    // (there is exactly one open path between any two rooms).
    //
    // If C is positive, we add C random open passages, possibly
    // creating some cycles in the maze.
    //
    // If D is positive, we remove D random open passages, possibly
    // disconnecting the maze into several open pieces.
    //
    // The final argument S controls our random number generator:
    // if S is -1 we use "new Random()", else "new Random(S)".
    public Maze(int N, int C, int D, long S)
    {
        args = N+" "+C+" "+D+" "+S;
        // This random maze constructor is long and
        // difficult, you do not have to study this!
        this.N = N;
        if (N<1 || N%2==0)
            throw new RuntimeException("N must be odd and positive: " + N);
        Random gen = (S==-1) ? new Random() : new Random(S);
        // Create empty maze.
        maze = new char[N][N];
        // Initially walls everywhere.
        for (int i=0; i<N; ++i)
            for (int j=0; j<N; ++j)
                maze[i][j] = '1';
        // Now make each room open ('0' marks it as unvisited).
        for (int i=0; i<N; i+=2)
            for (int j=0; j<N; j+=2)
                maze[i][j] = '0';
        // Take a random walk on rooms, until every room is visited.
        // The last visited room becomes the "root" of a random tree.
        // Until we find the root, to take a random step, we pick a
        // random direction (saving it as a letter in the maze), and
        // step to the next room in that direction.
        int unvisited = (N+1)*(N+1)/4;     // rooms not yet visited
        Position here = new Position(0,0); // next room to consider
        while (true)
        {
            if (get(here) == '0') // not visited before, count it!
                --unvisited;
            if (unvisited == 0) {
                // Found root (here)!  It does not need a direction.
                break;
            }
            // Not done. We need to step from here to a random valid
            // neighbor room, in one of the four cardinal directions.
            int dir = gen.nextInt(4);
            Position next = here.neighbor(dir).neighbor(dir);
            while (!inRange(next)) {
                dir = gen.nextInt(4); // try another
                next = here.neighbor(dir).neighbor(dir);
            }
            // Encode our dir choice as a char ('A', 'B', 'C', or 'D').
            char dirChar = (char)('A'+dir);
            // Save dirChar in maze, replacing '0' as value of get(here).
            // This also marks "here" as a visited position.
            maze[here.i][here.j] = dirChar;
            // Step to the next room.
            here = next;
        }
        // Now every room (except root) has a direction char.   For each
        // such room, cut a passage to its neighbor room in that direction.
        // Also erase the direction chars, leaving just '0' for open space.
        for (int i=0; i<N; i+=2)
            for (int j=0; j<N; j+=2)
            {
                Position room = new Position(i,j);
                char dirChar = get(room);
                if (dirChar == '0') continue;  // root
                int dir = dirChar - 'A'; // 0, 1, 2, or 3
                Position passage = room.neighbor(dir);
                maze[passage.i][passage.j] = '0'; // open the passage
                maze[i][j] = '0'; // forget direction, open the room
            }
        // Count the open passages, to catch excessive values of C and D.
        // Note a passage is at a position (i,j) where (i+j)%2==1.
        int pass=(N+1)*(N+1)/4-1, maxPass = (N+1)*(N-1)/2;
        if (C < 0) throw new RuntimeException("C is negative");
        if (pass+C > maxPass) throw new RuntimeException("C too big");
        // Add C open passages (connections, '1' to '0').
        while (C > 0) {
            int i = gen.nextInt(N), j = gen.nextInt(N);
            if ((i+j)%2==1 && maze[i][j]=='1') { maze[i][j]='0'; --C; }
        }
        pass += C;
        if (D < 0) throw new RuntimeException("D is negative");
        if (D > pass) throw new RuntimeException("D too big");
        // Remove D open passages (disconnections, '0' to '1').
        while (D > 0) {
            int i = gen.nextInt(N), j = gen.nextInt(N);
            if ((i+j)%2==0) continue; // not a passage?
            if ((i+j)%2==1 && maze[i][j]=='0') { maze[i][j]='1'; --D; }
        }
        pass -= D;
        // all done!
    }

    // Simple constructor (a random tree-like N by N maze, for odd N).
    public Maze(int N) { this(N, 0, 0, -1); }


    // Construct maze where each bit is chosen at random.
    // pWall is the probability that each bit is a wall (1).
    // S controls our random number generator:
    // if S is -1 we use "new Random()", else "new Random(S)".
    public Maze(int N, double pWall, long S)
    {
        args = N + " " + pWall + " " + S;
        this.N = N;
        if (N < 1)
            throw new RuntimeException("N must be positive: " + N);
        Random gen = (S==-1) ? new Random() : new Random(S);
        maze = new char[N][N];
        for (int i=0; i<N; ++i)
            for (int j=0; j<N; ++j)
                if (gen.nextDouble() < pWall)
                    maze[i][j] = '1';
                else
                    maze[i][j] = '0';
    }

    // Print this Maze as a String (with multiple lines).
    // Our InputStream constructor can read back this format.
    public String toString() { return toString(maze); }

    // As a convenience, we provide this static method that
    // prints any square char array in the same way.
    public static String toString(char[][] map)
    {
        int N = map.length;
        StringBuilder sb = new StringBuilder();
        sb.append(N).append("\n");
        for (int i=0; i<N; ++i) {
            for (int j=0; j<N; ++j) {
                if (j>0) sb.append(" ");
                sb.append(map[i][j]);
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    // main() generates a maze from the command-line arguments, and
    // prints it.  See mazeFromArgs, described just below.
    public static void main(String[] args) {
        Maze maze = mazeFromArgs(args);
        System.out.println(maze);
    }

    // mazeFromArgs(args) generates a maze as specified by the given
    // command-line arguments.  There are several different usages,
    // corresponding to our three Maze constructors.
    //
    // 1.  java Maze FILENAME
    //
    // If there is one argument and it does not look like an integer,
    // then we assume it is a filename, and we try to read the maze
    // as text from the file, using the Maze(FILENAME) constructor.
    //
    //
    // 2.  java Maze N P [S]
    //
    // If there are at least two arguments and the second looks like
    // floating point (containing '.'), then the first argument and
    // optional third argument should both be integers.  For example:
    //
    //     java Maze 10 0.2 5
    //
    // The arguments are:
    //    N = the maze size (int, an N by N grid of bits)
    //    P = probability that each maze bit is a wall (double)
    //    S = seed for random number generator (default -1 means no seed)
    //
    // If you fix N and S, you can observe that the set of wall bits
    // increases "montonically" as you increase P.
    //
    // 3.  java Maze [N C D S]
    //
    // Otherwise, there are 0-4 arguments, all integers.  For example:
    //
    //     java Maze 17
    //   or
    //     java Maze 5 3 0 415
    //
    // We use them as arguments to our "random tree maze" constructor.
    // They are:
    //
    //    N = the maze size (an N by N grid, must be odd, default 11)
    //    C = the number of passages added (connections, default 0)
    //    D = the number of passages removed (disconnections, default 0)
    //    S = seed for random number generator (default -1 means no seed)
    //
    // When C=D=0, the open part of the maze is a "random tree".
    //
    //
    // If these methods fail somehow (with an exception), mazeFromArgs
    // prints a message and returns null.

    public static Maze mazeFromArgs(String[] args)
    {
        // Defaults:
        int N = 11;
        int C = 0;
        int D = 0;
        long S = -1;            // -1 means no seed
        double P = 0.2;         // unused default
        Maze maze;

        try
        {
            // If args[0] is given, try to parse as integer N.
            if (args.length > 0)
            {
                try {
                    N = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    // Not an integer?
                    // Use Maze(fileName) constructor.
                    return new Maze(args[0]);
                }
            }
            // If args[1] contains '.', second case.
            if (args.length > 1 && args[1].indexOf('.')>=0)
            {
                // Use the Maze(N,P,S) constructor.
                P = Double.parseDouble(args[1]);
                if (args.length > 2)
                    S = Long.parseLong(args[2]);
                return new Maze(N, P, S);
            }
            else
            {
                // Use the Maze(N,C,D,S) constructor.
                // We already parsed args[0] if available.
                if (args.length > 1)
                    C = Integer.parseInt(args[1]);
                if (args.length > 2)
                    D = Integer.parseInt(args[2]);
                if (args.length > 3)
                    S = Long.parseLong(args[3]);
                return new Maze(N, C, D, S);
            }
        } catch (Exception e) {
            System.err.println("mazeFromArgs caught " + e);
            return null;
        }
    }
}
