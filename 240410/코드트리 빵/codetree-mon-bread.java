import java.util.*;
import java.io.*;

public class Main {
    static final BufferedReader BR = new BufferedReader(new InputStreamReader(System.in));
    static final BufferedWriter BW = new BufferedWriter(new OutputStreamWriter(System.out));
    static final int[] DX = {0, 1, 0, -1};
    static final int[] DY = {1, 0, -1, 0};
    static boolean[][] occupied;
    static int n, m;
    static int[][] board;
    static int arriveCount;

    int[] inputArray;
    Store[] stores;
    List<People> peopleList;
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

        stores = new Store[m + 1];
        for (int i = 1; i <= m; i++) {
            inputArray = getInputArray();
            stores[i] = new Store(inputArray[0] - 1, inputArray[1] - 1);
        }
        peopleList = new ArrayList<>();
        time = 1;
        arriveCount = 0;
    }

    void solve() {
        while (true) {
            movePeople();
            if (arriveCount == m) {
                break;
            }
            checkArrival();
            addPeople();
            time += 1;
        }
    }

    void movePeople() {
        for (People people : peopleList) {
            people.move();
        }
    }

    void checkArrival() {
        List<People> newPeople = new ArrayList<>();
        for (People people : peopleList) {
            if (people.arrived) {
                occupied[people.storeX][people.storeY] = true;
            } else {
                newPeople.add(people);
            }
        }
        peopleList = newPeople;
    }

    void addPeople() {
        if (time <= m) {
            Store store = stores[time];
            Position baseCamp = findBaseCamp(store);
            peopleList.add(new People(baseCamp.x, baseCamp.y, store.x, store.y));
        }
    }

    Position findBaseCamp(Store store) {
        List<Position> candidate = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (board[i][j] != 0) {
                    candidate.add(new Position(i, j));
                }
            }
        }
        PriorityQueue<BaseCamp> baseCamps = new PriorityQueue<>(BaseCamp::sort);
        for (Position p : candidate) {
            int distance = calculateDistance(p, store);
            baseCamps.add(new BaseCamp(distance, p.x, p.y));
        }
        BaseCamp baseCamp = baseCamps.remove();
        return new Position(baseCamp.x, baseCamp.y);
    }

    int calculateDistance(Position position, Store store) {
        int distance = 0;
        boolean[][] visited = new boolean[n][n];
        visited[position.x][position.y] = true;
        List<Position> current = new ArrayList<>();
        current.add(position);
        while (!current.isEmpty()) {
            List<Position> temp = new ArrayList<>();
            for (Position p : current) {
                for (int d = 0; d < 4; d++) {
                    int newX = p.x + DX[d];
                    int newY = p.y + DY[d];
                    if (isInner(newX, newY) && !visited[newX][newY] && !occupied[newX][newY]) {
                        temp.add(new Position(newX, newY));
                        visited[newX][newY] = true;
                    }
                }
            }
            distance += 1;
            if (visited[store.x][store.y]) {
                break;
            }
            current = temp;
        }
        return distance;
    }

    boolean isInner(int x, int y) {
        return 0 <= x && x < n && 0 <= y && y < n;
    }

    void printResult() throws IOException {
        BW.write(Integer.toString(time));
        BW.flush();
        BW.close();
        BR.close();
    }

    static class Store {
        int x;
        int y;

        Store(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    static class People {
        boolean arrived;
        boolean[][] visited;
        List<Position> positions;
        int storeX;
        int storeY;

        People(int baseCampX, int baseCampY, int storeX, int storeY) {
            this.arrived = false;
            occupied[baseCampX][baseCampY] = true;
            board[baseCampX][baseCampY] = 0;
            this.positions = new ArrayList<>();
            this.positions.add(new Position(baseCampX, baseCampY));
            this.visited = new boolean[n][n];
            this.storeX = storeX;
            this.storeY = storeY;
        }

        void move() {
            List<Position> temp = new ArrayList<>();
            for (Position p : positions) {
                for (int d = 0; d < 4; d++) {
                    int newX = p.x + DX[d];
                    int newY = p.y + DY[d];
                    if (isInner(newX, newY) && !occupied[newX][newY] && !visited[newX][newY]) {
                        temp.add(new Position(newX, newY));
                        visited[newX][newY] = true;
                    }
                }
            }
            if (visited[storeX][storeY]) {
                this.visited = null;
                this.positions = null;
                arriveCount += 1;
                arrived = true;
                return;
            }
            positions = temp;
        }

        boolean isInner(int x, int y) {
            return 0 <= x && x < n && 0 <= y && y < n;
        }
    }

    static class BaseCamp {
        int distance;
        int x;
        int y;

        BaseCamp(int distance, int x, int y) {
            this.distance = distance;
            this.x = x;
            this.y = y;
        }

        int sort(BaseCamp compare) {
            if (this.distance != compare.distance) {
                return Integer.compare(this.distance, compare.distance);
            }
            if (this.x != compare.x) {
                return Integer.compare(this.x, compare.x);
            }
            return Integer.compare(this.y, compare.y);
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