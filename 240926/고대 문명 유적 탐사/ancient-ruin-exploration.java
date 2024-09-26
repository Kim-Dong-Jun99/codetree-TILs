import java.util.*;
import java.io.*;

public class Main {
    static final BufferedReader BR = new BufferedReader(new InputStreamReader(System.in));
    static final int[] DX = {0, 1, 0, -1};
    static final int[] DY = {1, 0, -1, 0};

    int[] inputArray;
    int K, M;
    int[][] board;
    int[] wall;
    int answer;
    int index;

    public static void main(String[] args) throws IOException {
        Main main = new Main();
        main.init();
        main.solve();
    }

    int[] getInputArray() throws IOException {
        return Arrays.stream(BR.readLine().split(" ")).mapToInt(Integer::parseInt).toArray();
    }

    void init() throws IOException {
        inputArray = getInputArray();
        K = inputArray[0];
        M = inputArray[1];

        board = new int[5][5];
        for (int i = 0; i < 5; i++) {
            board[i] = getInputArray();
        }
        wall = getInputArray();
        answer = 0;
        index = 0;
    }

    void solve() {
        while (K-- > 0) {
            int catched = 0;
            explore();
            while (true) {
                int treasures = takeTreasures();
                if (treasures == 0) { break; }
                catched += treasures;
                fillTreasures();
            }
            if (catched == 0) { break; }
            System.out.print(catched+" ");
        }
    }

    void explore() {
        PriorityQueue<Rotation> heap = new PriorityQueue<>(Rotation::compare);
        for (int i = 0; i + 2 < 5; i++) {
            for (int j = 0; j + 2 < 5; j++) {
                int[][] tempBoard = board;
                for (int angle = 0; angle < 3; angle++) {
                    tempBoard = rotateBoard(tempBoard, i, j);
                    heap.add(new Rotation(getTreasures(tempBoard), angle+1, i, j));
                }
            }
        }
        Rotation r = heap.remove();
        for (int i = 0; i < r.angle; i++) {
            board = rotateBoard(board, r.x, r.y);
        }
    }

    int[][] rotateBoard(int[][] b, int x, int y) {
        int[][] newBoard = new int[5][5];
        copyBoard(b, newBoard);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                newBoard[j+x][2-i+y] = b[i+x][j+y];
            }
        }
        return newBoard;
    }

    void copyBoard(int[][] from, int[][] to) {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                to[i][j] = from[i][j];
            }
        }
    }

    int getTreasures(int[][] b) {
        boolean[][] visited = new boolean[5][5];
        int sum = 0;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (!visited[i][j]) {
                    int ts = getTreasureSize(b, visited, i, j);
                    if (ts >= 3) {
                        sum += ts;
                    }
                }
            }
        }
        return sum;
    }


    int getTreasureSize(int[][] b, boolean[][] visited, int x, int y) {
        int treasure = b[x][y];
        Queue<Position> q = new LinkedList<>();
        visited[x][y] = true;
        int size = 0;
        q.add(new Position(x, y));
        while (!q.isEmpty()) {
            Position p = q.remove();
            size += 1;
            for (int d = 0; d < 4; d++) {
                int nx = p.x + DX[d];
                int ny = p.y + DY[d];

                if (isInner(nx, ny) && b[nx][ny] == treasure && !visited[nx][ny]) {
                    visited[nx][ny] = true;
                    q.add(new Position(nx, ny));
                }
            }
        }
        return size;
    }


    int takeTreasures() {
        boolean[][] visited = new boolean[5][5];
        int sum = 0;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (!visited[i][j]) {
                    int ts = takeTreasure(visited, i, j);
                    if (ts >= 3) {
                        sum += ts;
                    }
                }
            }
        }
        return sum;
    }

    int takeTreasure(boolean[][] visited, int x, int y) {
        int treasure = board[x][y];
        Queue<Position> q = new LinkedList<>();
        visited[x][y] = true;
        List<Position> ps = new ArrayList<>();
        q.add(new Position(x, y));
        while (!q.isEmpty()) {
            Position p = q.remove();
            ps.add(p);
            for (int d = 0; d < 4; d++) {
                int nx = p.x + DX[d];
                int ny = p.y + DY[d];

                if (isInner(nx, ny) && board[nx][ny] == treasure && !visited[nx][ny]) {
                    visited[nx][ny] = true;
                    q.add(new Position(nx, ny));
                }
            }
        }

        if (ps.size() >= 3) {
            for (Position p : ps) {
                board[p.x][p.y] = 0;
            }
        }
        return ps.size();
    }


    boolean isInner(int x, int y) {
        return 0 <= x && x < 5 && 0 <= y && y < 5;
    }

    void fillTreasures() {
        for (int j = 0; j < 5; j++) {
            for (int i = 4; i >= 0; i--) {
                if (board[i][j] == 0) {
                    board[i][j] = wall[index];
                    index++;
                }
            }
        }
    }

    static class Rotation {
        int catched;
        int angle;
        int x;
        int y;

        Rotation(int catched, int angle, int x, int y) {
            this.catched = catched;
            this.angle = angle;
            this.x = x;
            this.y = y;
        }

        int compare(Rotation compare) {
            if (this.catched != compare.catched) {
                return Integer.compare(compare.catched, this.catched);
            }
            if (this.angle != compare.angle) {
                return Integer.compare(this.angle, compare.angle);
            }
            if (this.y != compare.y) {
                return Integer.compare(this.y, compare.y);
            }
            return Integer.compare(this.x, compare.x);
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