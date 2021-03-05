/*
THIS CODE IS MY OWN WORK, IT WAS WRITTEN WITHOUT CONSULTING
A TUTOR OR CODE WRITTEN BY OTHER STUDENTS.  Asuka Li
*/

// Given a maze of size N, the method findPath(maze) finds an open
// path from (0,0) to (N-1,N-1), if such a path exists.

// TODO(EC): define method findWallPath (it currently returns null).
// When findPath fails to find a path, there should be a "blocking"
// path of walls separating S and T.  This path can start at any wall
// on the top row or right column of the maze (i==0 or j==N-1), and
// end at any wall at the bottom row or left column of the maze
// (i==N-1 or j==0).  Two walls can be adjacent by a cardinal step OR
// a diagonal step (so, each wall has 8 potential neighbors).  Again,
// recursion is not allowed here.

// TODO(EC): Finding a wall path is good, finding a shortest wall path
// is even better.  Note that if S (or T) is a wall, it is a wall path
// of size one, the smallest possible.

public class PathFinder
{
    // Any data fields here should be private and static.  They exist
    // only as a convenient way to share search context between your
    // static methods here.   It should be possible to call your
    // findPath() method more than once, on different mazes, and
    // to get valid results for each maze.

    // The maze we are currently searching, and its size.
    private static Maze m;      // current maze
    private static int N;       // its size (N by N)

    // The parent array:
    private static Position[][] parent;
    // In the path-finding routines: for each position p, as soon as
    // we find a route to p, we set parent[p.i][p.j] to be a position
    // one step closer to the start (i.e., the parent discovered p).

    // Get parent of p (assumes p in range)
    static Position getParent(Position p) { return parent[p.i][p.j]; }

    // Set parent of p, if not yet set.  Value indicates success.
    static boolean setParent(Position p, Position par) {
        if (getParent(p) != null)
            return false;       // p already has a parent
        parent[p.i][p.j] = par;
        return true;
    }

    public static Deque<Position> findPath(Maze maze) {
        m = maze;                           // save the maze
        N = m.size();                       // save size (maze is N by N)
        parent = new Position[N][N];        // initially all null
        Position S = new Position(0,0);     // start of open path
        Position T = new Position(N-1,N-1); // end of open path

        // If either of these is a wall, there is no open path.
        if (!m.isOpen(S)) return null;
        if (!m.isOpen(T)) return null;

        Deque<Position> queue = new LinkedDeque<Position>();
        queue.addLast(S); // child
        queue.addLast(S); // parent
        while (!queue.isEmpty()) {
            Position p = queue.removeFirst(); // child
            Position from = queue.removeFirst(); // parent

            if (m.inRange(p) && m.isOpen(p) && getParent(p) == null) {
                setParent(p, from);

                for(int dir=0; dir<4; ++dir) {
                    queue.addLast(p.neighbor(dir)); // child
                    queue.addLast(p); // parent
                }
            }
        }
        // fall out of the while loop when we get to the end
        // If T has no parent, it is not reachable, so no path.
        if (getParent(T)==null)
            return null;
        // Otherwise, we can reconstruct a path from S to T.
        Deque<Position> path = new LinkedDeque<Position>();
        for (Position u=T; !u.equals(S); u=getParent(u))
            path.addFirst(u);
        path.addFirst(S);
        return path;
    }


    public static Deque<Position> findPathdfs(Maze maze)
    {
        m = maze;                           // save the maze
        N = m.size();                       // save size (maze is N by N)
        parent = new Position[N][N];        // initially all null
        Position S = new Position(0,0);     // start of open path
        Position T = new Position(N-1,N-1); // end of open path

        // If either of these is a wall, there is no open path.
        if (!m.isOpen(S)) return null;
        if (!m.isOpen(T)) return null;

        // GOAL: for each reachable open position p, parent[p.i][p.j]
        // should be an open position one step closer to S.  That is,
        // it is the position that first discovered a route to p.
        // These parent links will form a tree, rooted at S (the starting point).

        // Compute parent for each position reachable from S.
        // Since S is the root, we will let S be its own parent.

        // Compute parent links, by recursive depth-first-search!
        dfs(S, S);

        // If T has no parent, it is not reachable, so no path.
        if (getParent(T)==null)
            return null;
        // Otherwise, we can reconstruct a path from S to T.
        Deque<Position> path = new LinkedDeque<Position>();
        for (Position u=T; !u.equals(S); u=getParent(u))
            path.addFirst(u);
        path.addFirst(S);
        return path;
    }

    // depth-first-search: set parent for each newly reachable p.
    private static void dfs(Position p, Position from)
    {
        if (!m.inRange(p) || !m.isOpen(p) || getParent(p) != null)
            return;
        // System.out.println("found " + p + " via parent " + from);
        setParent(p, from);
        // Now recursively try the four neighbors of p.
        for (int dir=0; dir<4; ++dir)
            dfs(p.neighbor(dir), p);
    }


    // Return a wall path separating S and T, or null.
    public static Deque<Position> findWallPath(Maze maze)
    {
        //System.out.println("Trying to find wall path");
        m = maze;                           // save the maze
        N = m.size();                       // save size (maze is N by N)
        parent = new Position[N][N];        // initially all null

        Position S = new Position(0,0);
        Position T = null;
        Deque<Position> wall = new LinkedDeque<Position>();

        // looking for starting point on the top row
        // i == 0
        for (int j = 0; j < N; j++) {
            //System.out.println("entered the j loop");
            if (m.isWall(new Position(0, j))) {
                //System.out.println("S is " + S.toString());
                S = new Position(0, j);
                wall.addLast(S); // current
                wall.addLast(S); // parent

                while (!wall.isEmpty()) {
                    Position p = wall.removeFirst(); // current
                    Position from = wall.removeFirst(); // parent

                    if (m.inRange(p) && m.isWall(p) && getParent(p) == null) {
                        setParent(p, from);

                        if (p.i == N-1 || p.j == 0) {
                            T = p;
                            Deque<Position> wpath = new LinkedDeque<Position>();
                            for (Position u = T; !u.equals(S); u = getParent(u))
                                wpath.addFirst(u);
                            //System.out.println("didn't have any parents so adding S");
                            wpath.addFirst(S);
                            return wpath;
                        }
                        for (int dir = 0; dir < 8; ++dir) {
                            wall.addLast(p.neighbor(dir));
                            wall.addLast(p);
                        }
                    }
                }
            }
        }

        // when the starting point is on the right column
        // j == N-1
        for (int i = 0; i < N; i++) {
            if (m.isWall(new Position(i, N-1))) {
                S = new Position(i, N-1);
                wall.addLast(S); // current
                wall.addLast(S); // parent

                while (!wall.isEmpty()) {
                    Position p = wall.removeFirst(); // current
                    Position from = wall.removeFirst(); // parent

                    if (m.inRange(p) && m.isWall(p) && getParent(p) == null) {
                        setParent(p, from);

                        if (p.i == N-1 || p.j == 0) {
                            T = p;
                            Deque<Position> wpath = new LinkedDeque<Position>();
                            for (Position u = T; !u.equals(S); u = getParent(u))
                                wpath.addFirst(u);
                            wpath.addFirst(S);
                            return wpath;
                        }
                        for (int dir = 0; dir < 8; ++dir) {
                            wall.addLast(p.neighbor(dir));
                            wall.addLast(p);
                        }
                    }
                }
            }
        }
        return null;
    }

    // Command-line usage:
    //
    //    java PathFinder ARGS...
    //
    // Constructs maze (using same rules as Maze.main()), prints it,
    // finds the paths (open path and/or wall path), and reprints the
    // maze with the path highlighted.
    public static void main(String[] args)
    {
        Maze m = Maze.mazeFromArgs(args);
        System.out.println(m);
        Deque<Position> oPath = findPath(m);
        if (oPath != null)
            System.out.println("findPath() found an open path of size "
                               + oPath.size());
        Deque<Position> wPath = findWallPath(m);
        if (wPath != null)
            System.out.println("findWallPath() found a wall path of size "
                               + wPath.size());
        if (oPath==null && wPath==null)  {
            System.out.println("WARNING: neither path was found");
            // This may be OK, if you are not doing findWallPath (EC).
            // No point in reprinting the map.
            return;
        }
        if (oPath != null && wPath != null) // crossing?
            System.out.println("WARNING: cannot have both paths!");

        // Copy map of maze, and mark oPath with 'o', wPath with 'w'.
        char[][] map = m.copyArray();
        if (oPath != null)
            for (Position p: oPath)
                map[p.i][p.j] = 'o';
        if (wPath != null)
            for (Position p: wPath)
                map[p.i][p.j] = 'w';
        // Now print the marked map.
        System.out.println(Maze.toString(map));
    }

    // Java "defensive programming": we should not instantiate this
    // class.  To enforce that, we give it a private constructor.
    private PathFinder() {}
}
