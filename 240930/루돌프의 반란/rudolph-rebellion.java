import java.util.*;
import java.io.*;

public class Main {
    static final BufferedReader BR = new BufferedReader(new InputStreamReader(System.in));
    static final BufferedWriter BW = new BufferedWriter(new OutputStreamWriter(System.out));
    static final int[] DX = {-1, -1, 0, 1, 1, 1, 0, -1};
    static final int[] DY = {0, 1, 1, 1, 0, -1, -1, -1};
    static final int[] OPPOSITE = {4, 5, 6, 7, 0, 1, 2, 3};

    int[] inputArray;
    int N, M, P, C, D;
    int rx, ry;
    int[][] santaBoard;
    Santa[] santas;
    int outCount, turn;

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
        N = inputArray[0];
        M = inputArray[1];
        P = inputArray[2];
        C = inputArray[3];
        D = inputArray[4];

        inputArray = getInputArray();
        rx = inputArray[0]-1;
        ry = inputArray[1]-1;

        santas = new Santa[P+1];
        santaBoard = new int[N][N];
        for (int i = 0; i < P; i++) {
            inputArray = getInputArray();
            int index = inputArray[0];
            int x = inputArray[1]-1;
            int y = inputArray[2]-1;
            Santa santa = new Santa(index, x, y);
            santaBoard[x][y] = index;
            santas[index] = santa;
        }

        outCount = 0;
        turn = 1;
    }

    void solve() throws IOException {
        while (turn <= M && outCount < P) {
            rudolfTurn();
            santaTurn();
            updateScore();
            turn += 1;
        }
        printScore();
    }

    void rudolfTurn() {
        ClosestSanta closestSanta = getClosestSanta();
        int direction = getRudolfDirection(closestSanta);
        rx += DX[direction];
        ry += DY[direction];
        if (santaBoard[rx][ry] != 0) {
            rudolfCrashSanta(direction);
        }
    }

    void rudolfCrashSanta(int direction) {
        Santa santa = santas[santaBoard[rx][ry]];
        santa.score += C;
        santa.crashed = turn + 1;
        unmarkSanta(santa);
        santa.x += DX[direction] * C;
        santa.y += DY[direction] * C;
        if (!isInner(santa.x, santa.y)) {
            santa.isOut = true;
            outCount += 1;
            return;
        }
        if (santaBoard[santa.x][santa.y] != 0) {
            interaction(santas[santaBoard[santa.x][santa.y]], direction);
        }
        markSanta(santa);
    }

    void interaction(Santa santa, int direction) {
        unmarkSanta(santa);
        santa.x += DX[direction];
        santa.y += DY[direction];
        if (!isInner(santa.x, santa.y)) {
            santa.isOut = true;
            outCount += 1;
            return;
        }
        if (santaBoard[santa.x][santa.y] != 0) {
            interaction(santas[santaBoard[santa.x][santa.y]], direction);
        }
        markSanta(santa);

    }

    void unmarkSanta(Santa santa) {
        santaBoard[santa.x][santa.y] = 0;
    }

    void markSanta(Santa santa) {
        santaBoard[santa.x][santa.y] = santa.index;
    }

    ClosestSanta getClosestSanta() {
        PriorityQueue<ClosestSanta> heap = new PriorityQueue<>(ClosestSanta::sort);
        for (int i = 1; i <= P; i++) {
            Santa santa = santas[i];
            if (!santa.isOut) {
                int d = getDistance(rx, ry, santa.x, santa.y);
                heap.add(new ClosestSanta(d, santa.x, santa.y));
            }
        }
        return heap.remove();
    }

    int getRudolfDirection(ClosestSanta closestSanta) {
        int distance = 2500;
        int direction = -1;
        for (int d = 0; d < 8; d++) {
            int newX = rx + DX[d];
            int newY = ry + DY[d];
            int newDistance = getDistance(newX, newY, closestSanta.x, closestSanta.y);
            if (newDistance < distance) {
                distance = newDistance;
                direction = d;
            }
        }
        return direction;

    }

    int getDistance(int x1, int y1, int x2, int y2) {
        return (int) (Math.pow(x1-x2,2) + Math.pow(y1-y2,2));
    }

    boolean isInner(int x, int y) {
        return 0 <= x && x < N && 0 <= y && y < N;
    }

    void santaTurn() {
        for (int i = 1; i <= P; i++) {
            Santa santa = santas[i];
            if (!santa.isOut && turn > santa.crashed) {
                int direction = getNextDirection(santa);
                if (direction != -1) {
                    unmarkSanta(santa);
                    santa.x += DX[direction];
                    santa.y += DY[direction];
                    if (santa.x == rx && santa.y == ry) {
                        santaCrashWithRudolf(santa, direction);
                    } else {
                        markSanta(santa);
                    }
                }
            }
        }
    }

    void santaCrashWithRudolf(Santa santa, int direction) {
        santa.score += D;
        santa.crashed = turn + 1;
        int opposite = OPPOSITE[direction];
        santa.x += DX[opposite] * D;
        santa.y += DY[opposite] * D;
        if (!isInner(santa.x, santa.y)) {
            santa.isOut = true;
            outCount += 1;
            return;
        }

        if (santaBoard[santa.x][santa.y] != 0) {
            interaction(santas[santaBoard[santa.x][santa.y]], opposite);
        }

        markSanta(santa);
    }

    int getNextDirection(Santa santa) {
        int distance = getDistance(rx, ry, santa.x, santa.y);
        int direction = -1;
        for (int d = 0; d < 8; d += 2) {
            int newX = santa.x + DX[d];
            int newY = santa.y + DY[d];
            int newDistance = getDistance(rx, ry, newX, newY);
            if (isInner(newX, newY) && santaBoard[newX][newY] == 0 && newDistance < distance) {
                distance = newDistance;
                direction = d;
            }
        }
        return direction;
    }

    void updateScore() {
        for (int i = 1; i <= P; i++) {
            Santa santa = santas[i];
            if (!santa.isOut) {
                santa.score += 1;
            }
        }
    }

    void printScore() throws IOException {
        for (int i = 1; i <= P; i++) {
            BW.write(Integer.toString(santas[i].score) +" ");
        }
    }

    void printResult() throws IOException {
        BW.flush();
        BW.close();
        BR.close();
    }

    static class Santa {
        int index;
        int x;
        int y;
        int score;
        boolean isOut;
        int crashed;

        Santa(int index, int x, int y) {
            this.index = index;
            this.x = x;
            this.y = y;
            this.score = 0;
            this.isOut = false;
            this.crashed = 0;
        }
    }

    static class ClosestSanta {
        int distance;
        int x;
        int y;

        ClosestSanta(int distance, int x, int y) {
            this.distance = distance;
            this.x = x;
            this.y = y;
        }

        int sort(ClosestSanta compare) {
            if (this.distance != compare.distance) {
                return Integer.compare(this.distance, compare.distance);
            }
            if (this.x != compare.x) {
                return Integer.compare(compare.x, this.x);
            }
            return Integer.compare(compare.y, this.y);
        }
    }


}