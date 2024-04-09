import java.util.*;
import java.io.*;


public class Main {
    static final BufferedReader BR = new BufferedReader(new InputStreamReader(System.in));
    static final BufferedWriter BW = new BufferedWriter(new OutputStreamWriter(System.out));
    static final int[] DX = {1, -1, 0, 0};
    static final int[] DY = {0, 0, 1, -1};
    int[] inputArray;
    int[][] maze;
    int N, M, K;
    List<Position> players;
    Position exit;
    int time;
    int answer;

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
        N = inputArray[0];
        M = inputArray[1];
        K = inputArray[2];

        maze = new int[N][N];

        for (int i = 0; i < N; i++) {
            maze[i] = getInputArray();
        }

        players = new ArrayList<>();

        for (int i = 0; i < M; i++) {
            inputArray = getInputArray();
            players.add(new Position(inputArray[0] - 1, inputArray[1] - 1));
        }

        inputArray = getInputArray();
        exit = new Position(inputArray[0] - 1, inputArray[1] - 1);

        time = 0;
        answer = 0;
    }


    void solution() throws IOException {
        while (time < K) {
            movePlayer();
            removePlayer();

            if (players.isEmpty()) {
                break;
            }

            rotateMaze();

            time += 1;
        }
        System.out.println(answer);
        System.out.println((exit.x + 1) + " " + (exit.y + 1));
    }


    void movePlayer() {
        for (Position player : players) {
            int shortestPath = getDistance(player.x, player.y, exit.x, exit.y);
            Queue<Integer> canMove = new ArrayDeque<>();
            for (int i = 0; i < 4; i++) {
                int newX = player.x + DX[i];
                int newY = player.y + DY[i];
                int distance = getDistance(newX, newY, exit.x, exit.y);
                if (shortestPath > distance) {
                    canMove.add(i);
                }

            }
            while (!canMove.isEmpty()) {
                int toMove = canMove.remove();
                if (maze[player.x + DX[toMove]][player.y + DY[toMove]] == 0) {
                    answer += 1;
                    player.x += DX[toMove];
                    player.y += DY[toMove];
                    break;
                }
            }

        }
    }

    void removePlayer() {
        List<Position> newPlayers = new ArrayList<>();
        for (Position player : players) {
            if (player.x == exit.x && player.y == exit.y) {
                continue;
            }
            newPlayers.add(player);
        }
        players = newPlayers;
    }

    boolean canGo(int x, int y) {
        return isInner(x, y) && maze[x][y] == 0;
    }

    boolean isInner(int x, int y) {
        return 0 <= x && x < N && 0 <= y && y < N;
    }

    int getDistance(int x, int y, int otherX, int otherY) {
        return Math.abs(x - otherX) + Math.abs(y - otherY);
    }

    void rotateMaze() {
        PriorityQueue<Square> possibleSquares = new PriorityQueue<>(Square::compareWithSideLengthAndPosition);
        for (Position player : players) {
//            System.out.println(player.x + " " + player.y);
            int sideLength = Math.max(Math.abs(exit.x - player.x), Math.abs(exit.y - player.y));
            for (int i = exit.x - sideLength; i <= exit.x; i++) {
                for (int j = exit.y - sideLength; j <= exit.y; j++) {
                    if (canMakeSquare(i, j, sideLength, player)) {
                        possibleSquares.add(new Square(new Position(i, j), sideLength));
                        break;
                    }
                }
            }
        }
//        System.out.println("exit : " + exit.x + " " + exit.y);
        Square square = possibleSquares.remove();
//        System.out.println("square : " + square.leftTop.x + " " + square.leftTop.y + " " + square.sideLength);
        int[][] temp = new int[square.sideLength + 1][square.sideLength + 1];
        boolean[][] rotated = new boolean[N][N];
        for (int i = 0; i <= square.sideLength; i++) {
            for (int j = 0; j <= square.sideLength; j++) {
                rotated[i + square.leftTop.x][j + square.leftTop.y] = true;
                temp[j][square.sideLength - i] = maze[i + square.leftTop.x][j + square.leftTop.y];
                if (temp[j][square.sideLength - i] > 0) {
                    temp[j][square.sideLength - i] -= 1;
                }
            }
        }
        for (int i = 0; i <= square.sideLength; i++) {
            for (int j = 0; j <= square.sideLength; j++) {
                maze[i + square.leftTop.x][j + square.leftTop.y] = temp[i][j];
            }
        }
        for (Position player : players) {
            if (rotated[player.x][player.y]) {
                int i = player.x - square.leftTop.x;
                int j = player.y - square.leftTop.y;

                player.x = square.leftTop.x + j;
                player.y = square.leftTop.y + square.sideLength - i;
            }
        }

        if (rotated[exit.x][exit.y]) {
            int i = exit.x - square.leftTop.x;
            int j = exit.y - square.leftTop.y;

            exit.x = square.leftTop.x + j;
            exit.y = square.leftTop.y + square.sideLength - i;
        }
    }

    boolean canMakeSquare(int x, int y, int sideLength, Position player) {
        return isInner(x, y) && x <= player.x && player.x <= x + sideLength && y <= player.y && player.y <= y + sideLength;
    }

    static class Position {
        int x;
        int y;

        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    static class Square {
        Position leftTop;
        int sideLength;

        public Square(Position leftTop, int sideLength) {
            this.leftTop = leftTop;
            this.sideLength = sideLength;
        }

        int compareWithSideLengthAndPosition(Square compare) {
            if (this.sideLength != compare.sideLength) {
                return Integer.compare(this.sideLength, compare.sideLength);
            }
            if (this.leftTop.x != compare.leftTop.x) {
                return Integer.compare(this.leftTop.x, compare.leftTop.x);
            }
            return Integer.compare(this.leftTop.y, compare.leftTop.y);
        }
    }

}