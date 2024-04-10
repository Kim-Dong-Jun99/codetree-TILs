import java.util.*;
import java.io.*;

/*

14 28
0 0 0 0 0 0 0 0 0 0 0 0 0 0
0 1 0 0 0 0 0 0 0 0 0 0 0 0
0 0 0 0 0 0 0 0 1 0 0 0 0 0
0 0 0 0 0 0 0 0 0 1 0 0 0 0
0 1 0 0 1 0 0 1 1 1 0 0 0 0
0 1 0 1 0 0 1 0 0 1 0 0 0 0
0 0 0 0 0 0 0 0 0 1 0 0 0 0
0 0 1 0 0 0 0 0 1 1 0 0 0 0
1 0 1 1 0 0 0 0 0 0 0 0 1 0
1 1 1 0 0 0 0 0 0 0 0 0 0 0
0 0 0 0 0 0 1 0 0 0 0 1 0 1
1 0 0 0 0 0 0 1 0 0 0 1 0 0
0 0 0 0 0 1 0 0 1 0 0 0 0 0
0 0 1 0 0 0 0 0 1 1 0 0 0 0
4 12
10 9
13 4
11 8
10 7
8 7
7 3
3 1
4 2
14 8
2 14
12 6
9 9
4 3
10 4
5 1
10 10
3 2
14 11
5 13
4 11
1 3
6 14
5 4
10 13
11 4
5 14
7 5



 */

public class Main {
    static final BufferedReader BR = new BufferedReader(new InputStreamReader(System.in));
    static final int[] DX = {-1, 0, 0, 1};
    static final int[] DY = {0, -1, 1, 0};
    int[] inputArray;
    static int n, m;
    static int[][] board;
    int time;
    HashMap<Integer, Position> storeMap;
    List<Travel> travels;
    public static void main(String[] args) {
        Main main = new Main();
        try {
            main.init();
            main.solution();
        } catch (IOException e) {
            System.out.println("Exception during I/O");
        }
    }

    int[] getInputArray() throws IOException {
        return Arrays.stream(BR.readLine().split(" ")).mapToInt(Integer::parseInt).toArray();
    }

    void init() throws IOException {
        inputArray = getInputArray();
        n = inputArray[0];
        m = inputArray[1];

        board = new int[n][n];

        for (int i = 0; i < n; i++) {
            board[i] = getInputArray();
        }

        time = 1;
        storeMap = new HashMap<>();
        for (int i = 0; i < m; i++) {
            inputArray = getInputArray();
            storeMap.put(time + i, new Position(inputArray[0] - 1, inputArray[1] - 1));
        }

        travels = new ArrayList<>();
    }


    void solution() throws IOException {
        while (true) {
            for (Travel travel : travels) {
                travel.bfs();
            }

            List<Travel> newTravel = new ArrayList<>();
            for (Travel travel : travels) {
                if (travel.isEnd) {
                    board[travel.store.x][travel.store.y] = -1;
                } else {
                    newTravel.add(travel);
                }
            }
            travels = newTravel;

            if (storeMap.containsKey(time)) {
                Position store = storeMap.get(time);
                Position baseCamp = getBaseCamp(store);
                travels.add(new Travel(store, baseCamp));
                board[baseCamp.x][baseCamp.y] = -1;
            }

            if (travels.isEmpty()) {
                break;
            }
            time += 1;
//            System.out.println(time);

        }
        System.out.println(time);
    }

    Position getBaseCamp(Position store) {
        boolean[][] visited = new boolean[n][n];
        List<Position> currentPositions = Collections.singletonList(store);
        PriorityQueue<Position> minHeap = new PriorityQueue<>(Position::getMinPosition);
        visited[store.x][store.y] = true;
        boolean isEnd = false;
        while (!currentPositions.isEmpty()) {
            List<Position> temp = new ArrayList<>();
            for (Position current : currentPositions) {
                for (int i = 0; i < 4; i++) {
                    int newX = current.x + DX[i];
                    int newY = current.y + DY[i];
                    if (canGo(newX, newY) && !visited[newX][newY]) {
                        Position nextPosition = new Position(newX, newY);
                        visited[newX][newY] = true;
                        temp.add(nextPosition);
                        if (board[newX][newY] == 1) {
                            isEnd = true;
                            minHeap.add(nextPosition);
                        }
                    }
                }
            }
            if (isEnd) {
                break;
            }

            currentPositions = temp;
        }

        return minHeap.remove();
    }

    static boolean canGo(int x, int y) {
        return isInner(x, y) && board[x][y] >= 0;
    }

    static boolean isInner(int x, int y) {
        return 0 <= x && x < n && 0 <= y && y < n;
    }


    static class Travel {
        boolean[][] visited;
        Position store;
        boolean isEnd;
        List<Position> currentPositions;
        Position baseCamp;

        public Travel(Position store, Position baseCamp) {
            this.store = store;
            this.baseCamp = baseCamp;
            this.visited = new boolean[n][n];
            isEnd = false;
            currentPositions = Collections.singletonList(baseCamp);
        }

        void bfs() {
            List<Position> temp = new ArrayList<>();
            for (Position current : currentPositions) {
                for (int i = 0; i < 4; i++) {
                    int newX = current.x + DX[i];
                    int newY = current.y + DY[i];

                    if (canGo(newX, newY) && !visited[newX][newY]) {
                        temp.add(new Position(newX, newY));
                        visited[newX][newY] = true;
                    }
                }
            }

            currentPositions = temp;
            if (visited[store.x][store.y]) {
                isEnd = true;
            }
        }
    }

    static class Position {
        int x;
        int y;

        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }

        int getMinPosition(Position compare) {
            if (this.x != compare.x) {
                return Integer.compare(this.x, compare.x);
            }
            return Integer.compare(this.y, compare.y);
        }
    }


}