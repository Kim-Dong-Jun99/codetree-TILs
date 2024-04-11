import java.util.*;
import java.io.*;

public class Main {
    static final BufferedReader BR = new BufferedReader(new InputStreamReader(System.in));
    static final BufferedWriter BW = new BufferedWriter(new OutputStreamWriter(System.out));
    static final int[] DX = {-1, 0, 0, 1};
    static final int[] DY = {0, -1, 1, 0};
    static int n, m;
    static int[][] board;
    static boolean[][] occupied;

    List<Travel> travels;
    Position[] stores;
    int[] inputArray;
    int time;

    public static void main(String[] args) throws IOException {
        Main main = new Main();
        main.init();
        main.solve();
        main.printResult();
    }

    int[] getInputArray() throws IOException {
        return Arrays.stream(BR.readLine().split(" ")).mapToInt(Integer::parseInt).toArray();
    }

    void init() throws IOException {
        inputArray = getInputArray();
        n = inputArray[0];
        m = inputArray[1];

        board = new int[n][n];
        occupied = new boolean[n][n];
        for (int i = 0; i < n; i++) {
            board[i] = getInputArray();
        }

        stores = new Position[m + 1];
        travels = new ArrayList<>();
        for (int i = 1; i <= m; i++) {
            inputArray = getInputArray();
            stores[i] = new Position(inputArray[0] - 1, inputArray[1] - 1);
        }

        time = 1;
    }

    void solve() {
        while (true) {
            movePeople();
            if (checkOccupied()) {
                break;
            }
            addTravel();
            time += 1;
        }
    }

    void movePeople() {
        for (Travel travel : travels) {
            if (travel.arrived) {
                continue;
            }
            travel.move();
        }
    }

    boolean checkOccupied() {
        int arrived = 0;
        for (Travel travel : travels) {
            if (travel.arrived) {
                arrived += 1;
                occupied[travel.storeX][travel.storeY] = true;
            }
        }
        return arrived == m;
    }

    void addTravel() {
        if (!(1 <= time && time <= m)) {
            return;
        }
        Position store = stores[time];
        Position baseCamp = getBaseCamp(store);
        travels.add(new Travel(baseCamp.x, baseCamp.y, store.x, store.y));
        occupied[baseCamp.x][baseCamp.y] = true;
        board[baseCamp.x][baseCamp.y] = 0;

    }

    Position getBaseCamp(Position store) {
        boolean[][] visited = new boolean[n][n];
        PriorityQueue<Position> heap = new PriorityQueue<>(Position::sort);
        List<Position> current = Collections.singletonList(store);
        visited[store.x][store.y] = true;
        boolean find = false;
        while (!current.isEmpty()) {
            List<Position> nextPositions = new ArrayList<>();
            for (Position p : current) {
                for (int d = 0; d < 4; d++) {
                    int newX = p.x + DX[d];
                    int newY = p.y + DY[d];
                    if (isInner(newX, newY) && !visited[newX][newY] && !occupied[newX][newY]) {
                        Position next = new Position(newX, newY);
                        nextPositions.add(next);
                        visited[newX][newY] = true;
                        if (board[newX][newY] == 1) {
                            find = true;
                            heap.add(next);
                        }
                    }
                }
            }
            if (find) {
                break;
            }
            current = nextPositions;
        }
        return heap.remove();
    }

    static boolean isInner(int x, int y) {
        return 0 <= x && x < n && 0 <= y && y < n;
    }

    void printResult() throws IOException {
        BW.write(Integer.toString(time));
        BW.flush();
        BW.close();
        BR.close();
    }

    static class Travel {
        boolean arrived;
        int currentX;
        int currentY;
        int storeX;
        int storeY;

        Travel(int currentX, int currentY, int storeX, int storeY) {
            this.currentX = currentX;
            this.currentY = currentY;
            this.storeX = storeX;
            this.storeY = storeY;
            this.arrived = false;
        }

        void move() {
            int d = getDirection();
            currentX += DX[d];
            currentY += DY[d];
            if (storeX == currentX && storeY == currentY) {
                arrived = true;
            }
        }

        int getDirection() {
            int distance = Integer.MAX_VALUE;
            int direction = -1;
            for (int d = 0; d < 4; d++) {
                int newX = currentX + DX[d];
                int newY = currentY + DY[d];
                if (isInner(newX, newY) && !occupied[newX][newY]) {
                    int newDistance = getDistance(newX, newY);
                    if (newDistance < distance) {
                        distance = newDistance;
                        direction = d;
                    }
                }
            }
            return direction;
        }

        int getDistance(int x, int y) {
            boolean[][] visited = new boolean[n][n];
            int distance = 0;
            visited[x][y] = true;
            List<Position> current = Collections.singletonList(new Position(x, y));
            while (!current.isEmpty()) {
                if (visited[storeX][storeY]) {
                    return distance;
                }
                List<Position> next = new ArrayList<>();
                for (Position p : current) {
                    for (int d = 0; d < 4; d++) {
                        int newX = p.x + DX[d];
                        int newY = p.y + DY[d];
                        if (isInner(newX, newY) && !visited[newX][newY] && !occupied[newX][newY]) {
                            next.add(new Position(newX, newY));
                            visited[newX][newY] = true;
                        }
                    }
                }
                distance += 1;
                current = next;
            }

            return Integer.MAX_VALUE;
        }
    }

    static class Position {
        int x;
        int y;

        Position(int x, int y) {
            this.x = x;
            this.y = y;
        }

        int sort(Position compare) {
            if (this.x != compare.x) {
                return Integer.compare(this.x, compare.x);
            }
            return Integer.compare(this.y, compare.y);
        }
    }
}