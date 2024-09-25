import java.util.*;
import java.io.*;

public class Main {
    static final BufferedReader BR = new BufferedReader(new InputStreamReader(System.in));
    static final BufferedWriter BW = new BufferedWriter(new OutputStreamWriter(System.out));
    static final int[] DX = {-1, 0, 1, 0};
    static final int[] DY = {0, 1, 0, -1};

    int[] inputArray;
    int R, C, K, answer;
    int[][] board;
    Golem[] golems;
    public static void main(String[] args) throws IOException {
        Main main = new Main();
        main.init();
        main.solve();
        BW.flush();
    }

    int[] getInputArray() throws IOException {
        return Arrays.stream(BR.readLine().split(" ")).mapToInt(Integer::parseInt).toArray();
    }

    void init() throws IOException {
        inputArray = getInputArray();
        R = inputArray[0];
        C = inputArray[1];
        K = inputArray[2];

        board = new int[R][C];
        for (int i = 0; i < R; i++) {
            Arrays.fill(board[i], -1);
        }
        golems = new Golem[K];
        for (int i = 0; i < K; i++) {
            inputArray = getInputArray();
            golems[i] = new Golem(i, new Position(-2, inputArray[0]-1), inputArray[1]);
        }
        answer = 0;
    }

    void solve() throws IOException {
        for (Golem golem : golems) {
            moveGolem(golem);
            if (!isInnerBoard(golem.p.x, golem.p.y)) {
                resetBoard();
                continue;
            }
            markGolemToBoard(golem);
            travelGolem(golem);
        }
        BW.write(Integer.toString(answer));
    }

    boolean isInnerBoard(int x, int y) {
        return 1 <= x && x < R-1 && 1 <= y && y < C-1;
    }

    void moveGolem(Golem g) {
        while (true) {
            if (moveDown(g)) {
                continue;
            }
            if (moveLeft(g)) {
                continue;
            }
            if (moveRight(g)) {
                continue;
            }
            break;
        }
    }

    boolean isInner(int x, int y) {
        return 0 <= x && x < R && 0 <= y && y < C;
    }

    boolean canGo(int x, int y) {
        if (isInner(x, y)) {
            return board[x][y] == -1;
        } else {
            return 0 <= y && y < C && x < R;
        }
    }

    boolean canGoDown(int x, int y) {
        int lx = x;
        int ly = y-1;
        int rx = x;
        int ry = y+1;
        int dx = x+1;
        int dy = y;
        
        return canGo(lx+1, ly) && canGo(rx+1,ry) && canGo(dx+1,dy);
    }

    boolean canGoLeft(int x, int y) {
        int lx = x;
        int ly = y-1;
        int dx = x+1;
        int dy = y;
        int ux = x-1;
        int uy = y;

        return canGo(lx,ly-1) && canGo(dx,dy-1) && canGo(ux, uy-1);
    }

    boolean canGoRight(int x, int y) {
        int rx = x;
        int ry = y+1;
        int dx = x+1;
        int dy = y;
        int ux = x-1;
        int uy = y;

        return canGo(rx, ry+1) && canGo(dx, dy+1) && canGo(ux, uy+1); 
    }

    boolean moveDown(Golem g) {
        if (canGoDown(g.p.x, g.p.y)) {
            g.p.x += 1;
            return true;
        }
        return false;
    }

    boolean moveLeft(Golem g) {
        if (canGoLeft(g.p.x, g.p.y) && canGoDown(g.p.x, g.p.y-1)) {
            g.p.x += 1;
            g.p.y -= 1;
            g.d = ccw(g.d);
            return true;
        }
        return false;
    }

    boolean moveRight(Golem g) {
        if (canGoRight(g.p.x, g.p.y) && canGoDown(g.p.x, g.p.y+1)) {
            g.p.x += 1;
            g.p.y += 1;
            g.d = cw(g.d);
            return true;
        }
        return false;
    }

    int cw(int d) {
        return (d + 1) % 4;
    }

    int ccw(int d) {
        return (d + 3) % 4;
    }

    void resetBoard() {
        board = new int[R][C];
        for (int i = 0; i < R; i++) {
            Arrays.fill(board[i], -1);
        }
    }

    void markGolemToBoard(Golem g) {
        board[g.p.x][g.p.y] = g.id;
        for (int d = 0; d < 4; d++) {
            board[g.p.x+DX[d]][g.p.y+DY[d]] = g.id;
        }
    }

    void travelGolem(Golem g) {
        boolean[] visited = new boolean[K];
        int lowest = 0;
        Queue<Integer> q = new LinkedList<>();
        q.add(g.id);
        visited[g.id] = true;
        while (!q.isEmpty()) {
            Golem cur = golems[q.remove()];
            lowest = Math.max(lowest, cur.p.x+2);
            int ex = cur.p.x + DX[cur.d];
            int ey = cur.p.y + DY[cur.d];
            for (int d = 0; d < 4; d++) {
                int nx = ex + DX[d];
                int ny = ey + DY[d];
                if (isInner(nx, ny) && board[nx][ny] >= 0 && !visited[board[nx][ny]]) {
                    visited[board[nx][ny]] = true;
                    q.add(board[nx][ny]);
                }
            }
        }
        answer += lowest;
    }

    static class Golem {
        int id;
        Position p;
        int d;

        Golem(int id, Position p, int d) {
            this.id = id;
            this.p = p;
            this.d = d;
        }
    }

    static class Position {
        int x;
        int y;

        Position(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}