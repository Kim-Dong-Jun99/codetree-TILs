import java.util.*;
import java.io.*;

/*
5 3 8
0 0 0 0 1
9 2 2 0 0
0 1 0 1 0
0 0 0 1 0
0 0 0 0 0
1 3
3 1
3 5
3 3

7
1 1
 */

public class Main {
    static final BufferedReader BR = new BufferedReader(new InputStreamReader(System.in));
    static final BufferedWriter BW = new BufferedWriter(new OutputStreamWriter(System.out));
    static final int EMPTY = 0;
    static final int[] DX = {-1, 1, 0, 0};
    static final int[] DY = {0, 0, -1, 1};
    static final int[] dx = {0, 1, 0, -1};
    static final int[] dy = {1, 0, -1, 0};

    int[] inputArray;
    int N, M, K;
    int[][] board;
    boolean[][] hasMember;
    Member[] members;
    int ex, ey;
    int outCount;
    boolean exitMoved;


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
        K = inputArray[2];

        board = new int[N][N];
        for (int i = 0; i < N; i++) {
            board[i] = getInputArray();
        }

        members = new Member[M];
        for (int i = 0; i < M; i++) {
            inputArray = getInputArray();
            members[i] = new Member(inputArray[0] - 1, inputArray[1] - 1);
        }

        inputArray = getInputArray();
        ex = inputArray[0] - 1;
        ey = inputArray[1] - 1;
        outCount = 0;
    }

    void solve() {
        while (K-- > 0 && outCount < M) {
            exitMoved = false;
            moveMember();
            markMemberToBoard();
            Square s = getSquare();
            rotateSquare(s.x, s.y, s.size);
        }
    }

    void moveMember() {
        for (Member m : members) {
            if (m.isOut) {
                continue;
            }
            int currentDistance = getDistance(ex, ey, m.x, m.y);
            int direction = -1;
            for (int d = 0; d < 4; d++) {
                int newX = m.x + DX[d];
                int newY = m.y + DY[d];
                int newDistance = getDistance(ex, ey, newX, newY);
                if (isInner(newX, newY) && board[newX][newY] == EMPTY && newDistance < currentDistance) {
                    currentDistance = newDistance;
                    direction = d;
                }
            }
            if (direction == -1) {
                continue;
            }
            m.x += DX[direction];
            m.y += DY[direction];
            m.distance += 1;
            if (m.x == ex && m.y == ey) {
                m.isOut = true;
                outCount += 1;
            }
        }
    }

    int getDistance(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    boolean isInner(int x, int y) {
        return 0 <= x && x < N && 0 <= y && y < N;
    }

    void markMemberToBoard() {
        hasMember = new boolean[N][N];
        for (Member m : members) {
            if (m.isOut) {
                continue;
            }
            hasMember[m.x][m.y] = true;
        }
    }

    Square getSquare() {
        int size = getSquareSize();
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (!squareContainsExit(i, j, size)) {
                    continue;
                }
                if (squareContainsMember(i, j, size)) {
                    return new Square(i, j, size);
                }
            }
        }
        return null;
    }

    int getSquareSize() {
        int size = N;
        for (Member m : members) {
            if (m.isOut) {
                continue;
            }
            int xDiff = Math.abs(m.x - ex);
            int yDiff = Math.abs(m.y - ey);
            size = Math.min(size, Math.max(xDiff, yDiff) + 1);
        }
        return size;
    }

    boolean squareContainsExit(int x, int y, int size) {
        return x <= ex && ex < x + size && y <= ey && ey < y + size;
    }

    boolean squareContainsMember(int x, int y, int size) {
        for (Member m : members) {
            if (m.isOut) {
                continue;
            }
            if (x <= m.x && m.x < x + size && y <= m.y && m.y < y + size) {
                return true;
            }
        }
        return false;
    }

    void rotateSquare(int x, int y, int size) {
        if (size < 2) {
            return;
        }
        List<Position> positionList = getPositionList(x, y, size);
        int[][] edge = new int[N][N];
        for (int i = 0; i < positionList.size(); i++) {
            Position p = positionList.get(i);
            Position left = positionList.get(getLeftPosition(i, size - 1, positionList.size()));
            edge[p.x][p.y] = board[left.x][left.y];
        }

        for (int i = 0; i < positionList.size(); i++) {
            Position p = positionList.get(i);
            Position right = positionList.get(getRightPosition(i, size - 1, positionList.size()));
            board[p.x][p.y] = edge[p.x][p.y];
            if (board[p.x][p.y] > 0) {
                board[p.x][p.y] -= 1;
            }
            if (ex == p.x && ey == p.y && !exitMoved) {
                ex = right.x;
                ey = right.y;
                exitMoved = true;
            }
            if (hasMember[p.x][p.y]) {
                moveMember(p.x, p.y, right.x, right.y);
            }
        }

        rotateSquare(x + 1, y + 1, size - 2);
    }

    void moveMember(int x, int y, int toX, int toY) {
        for (Member m : members) {
            if (m.isOut) {
                continue;
            }
            if (m.x == x && m.y == y) {
                m.x = toX;
                m.y = toY;
            }
        }
    }

    List<Position> getPositionList(int x, int y, int size) {
        List<Position> positions = new ArrayList<>();
        int curX = x;
        int curY = y;
        int d = 0;
        while (positions.size() < size * 4 - 4) {
            positions.add(new Position(curX, curY));
            int newX = curX + dx[d];
            int newY = curY + dy[d];
            if (isInSquare(x, y, size, newX, newY)) {
                curX = newX;
                curY = newY;
            } else {
                d = getNextDirection(d);
                curX += dx[d];
                curY += dy[d];

            }
        }
        return positions;
    }

    boolean isInSquare(int x, int y, int size, int i, int j) {
        return x <= i && i < x + size && y <= j && j < y + size;
    }

    int getNextDirection(int d) {
        return (d + 1) % 4;
    }

    int getLeftPosition(int index, int diff, int size) {
        return (index + size - diff) % size;
    }

    int getRightPosition(int index, int diff, int size) {
        return (index + diff) % size;
    }

    void printResult() throws IOException {
        calculateDistanceSum();
        BW.flush();
        BW.close();
        BR.close();
    }

    void calculateDistanceSum() throws IOException{
        int distanceSum = 0;
        for (Member m : members) {
            distanceSum += m.distance;
        }
        BW.write(Integer.toString(distanceSum) + "\n");
        BW.write(Integer.toString(ex + 1) + " " + Integer.toString(ey + 1) + "\n");
    }

    static class Square {
        int x;
        int y;
        int size;

        Square(int x, int y, int size) {
            this.x = x;
            this.y = y;
            this.size = size;
        }

    }

    static class Member {
        int x;
        int y;
        int distance;
        boolean isOut;

        Member(int x, int y) {
            this.x = x;
            this.y = y;
            this.distance = 0;
            this.isOut = false;
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