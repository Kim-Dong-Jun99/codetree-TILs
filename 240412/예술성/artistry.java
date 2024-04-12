import java.util.*;
import java.io.*;

public class Main {
    static final BufferedReader BR = new BufferedReader(new InputStreamReader(System.in));
    static final BufferedWriter BW = new BufferedWriter(new OutputStreamWriter(System.out));
    static final int[] DX = {0, 1, 0, -1};
    static final int[] DY = {1, 0, -1, 0};
    int n;
    int[][] board;
    int[][] teamBoard;
    boolean[][] visited;
    int answer;
    int spin;
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
        n = Integer.parseInt(BR.readLine());
        board = new int[n][n];
        for (int i = 0; i < n; i++) {
            board[i] = getInputArray();
        }
        answer = 0;
        spin = 0;
    }

    void solve() {
        answer += calculateArtScore();
        while (spin < 3) {
            rotate();
            answer += calculateArtScore();
            spin += 1;
        }
    }

    int calculateArtScore() {
        teamBoard = new int[n][n];
        visited = new boolean[n][n];
        fillTeamBoard();
        HashMap<Integer, Team> teamMap = getTeamMap();
        int score = 0;
        for (Team team : teamMap.values()) {
            for (Integer neighbourIndex : team.neighbour.keySet()) {
                if (neighbourIndex < team.index) {
                    continue;
                }
                Team neighbour = teamMap.get(neighbourIndex);
                score += getScore(team, neighbour);
            }
        }
        return score;
    }

    int getScore(Team team, Team neighbour) {
        return (team.size + neighbour.size) * team.number * neighbour.number * team.neighbour.get(neighbour.index);
    }

    void fillTeamBoard() {
        int teamIndex = 1;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (!visited[i][j]) {
                    int currentId = board[i][j];
                    List<Position> current = Collections.singletonList(new Position(i, j));
                    while (!current.isEmpty()) {
                        List<Position> next = new ArrayList<>();
                        for (Position p : current) {
                            teamBoard[p.x][p.y] = teamIndex;
                            for (int d = 0; d < 4; d++) {
                                int newX = p.x + DX[d];
                                int newY = p.y + DY[d];
                                if (isInner(newX, newY) && board[newX][newY] == currentId && !visited[newX][newY]) {
                                    next.add(new Position(newX, newY));
                                    visited[newX][newY] = true;
                                }
                            }
                        }
                        current = next;
                    }
                    teamIndex += 1;
                }
            }
        }
    }

    HashMap<Integer, Team> getTeamMap() {
        HashMap<Integer, Team> teamMap = new HashMap<>();
        visited = new boolean[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (!visited[i][j]) {
                    visited[i][j] = true;
                    int number = board[i][j];
                    int index = teamBoard[i][j];
                    Team team = new Team(index, number);
                    List<Position> current = Collections.singletonList(new Position(i, j));
                    while (!current.isEmpty()) {
                        List<Position> next = new ArrayList<>();
                        for (Position p : current) {
                            team.size += 1;
                            for (int d = 0; d < 4; d++) {
                                int newX = p.x + DX[d];
                                int newY = p.y + DY[d];
                                if (isInner(newX, newY) && !visited[newX][newY]) {
                                    if (teamBoard[newX][newY] == index) {
                                        next.add(new Position(newX, newY));
                                        visited[newX][newY] = true;
                                    } else {
                                        int neighbour = teamBoard[newX][newY];
                                        team.neighbour.put(neighbour, team.neighbour.getOrDefault(neighbour, 0) + 1);
                                    }
                                }
                            }
                        }
                        current = next;
                    }
                    teamMap.put(index, team);
                }
            }
        }
        return teamMap;
    }

    boolean isInner(int x, int y) {
        return 0 <= x && x < n && 0 <= y && y < n;
    }

    void rotate() {
        int[][] newBoard = new int[n][n];
        rotateCross(newBoard);
        rotateSquare(newBoard);
        board = newBoard;
    }

    void rotateCross(int[][] newBoard) {
        int center = n / 2;
        newBoard[center][center] = board[center][center];
        int size = (n - 1) / 2;
        Position[] toRotate = new Position[size * 4];
        fillToRotate(toRotate, center);
        for (int i = 0; i < toRotate.length; i++) {
            int rightIndex = (i + size) % toRotate.length;
            Position p = toRotate[i];
            Position r = toRotate[rightIndex];
            newBoard[p.x][p.y] = board[r.x][r.y];
        }
    }

    void fillToRotate(Position[] toRotate, int center) {
        int index = 0;
        for (int i = 0; i < center; i++) {
            toRotate[index] = new Position(i, center);
            index += 1;
        }
        for (int j = n - 1; j > center; j--) {
            toRotate[index] = new Position(center, j);
            index += 1;
        }
        for (int i = n - 1; i > center; i--) {
            toRotate[index] = new Position(i, center);
            index += 1;
        }
        for (int j = 0; j < center; j++) {
            toRotate[index] = new Position(center, j);
            index += 1;
        }
    }

    void rotateSquare(int[][] newBoard) {
        int size = (n - 1) / 2;
        rotateSquare(newBoard, 0, 0, size);
        rotateSquare(newBoard, 0, size + 1, size);
        rotateSquare(newBoard, size + 1, 0, size);
        rotateSquare(newBoard, size + 1, size + 1, size);
    }

    void rotateSquare(int[][] newBoard, int x, int y, int size) {
        if (size == 0) {
            return;
        }
        if (size == 1) {
            newBoard[x][y] = board[x][y];
            return;
        }
        Position[] toRotate = new Position[size * 4 - 4];
        fillToRotate(toRotate, x, y, size);
        for (int i = 0; i < toRotate.length; i++) {
            int rightIndex = (i + size-1) % toRotate.length;
            Position p = toRotate[i];
            Position rightPosition = toRotate[rightIndex];
            newBoard[rightPosition.x][rightPosition.y] = board[p.x][p.y];
        }
        rotateSquare(newBoard, x + 1, y + 1, size - 2);
    }

    void fillToRotate(Position[] toRotate, int x, int y, int size) {
        int index = 0;
        for (int j = y; j < y + size; j++) {
            toRotate[index] = new Position(x, j);
            index += 1;
        }
        for (int i = x + 1; i < x + size; i++) {
            toRotate[index] = new Position(i, y + size - 1);
            index += 1;
        }
        for (int j = y + size - 2; j >= y; j--) {
            toRotate[index] = new Position(x + size - 1, j);
            index += 1;
        }
        for (int i = x + size - 2; i > x; i--) {
            toRotate[index] = new Position(i, y);
            index += 1;
        }
    }

    void printResult() throws IOException {
        BW.write(Integer.toString(answer));
        BW.flush();
        BW.close();
        BR.close();
    }

    static class Position {
        int x;
        int y;

        Position(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    static class Team {
        int index;
        int number;
        int size;
        HashMap<Integer, Integer> neighbour;

        Team(int index, int number) {
            this.index = index;
            this.number = number;
            this.size = 0;
            this.neighbour = new HashMap<>();
        }
    }
}