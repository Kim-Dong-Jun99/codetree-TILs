import java.util.*;
import java.io.*;

public class Main {
    static final BufferedReader BR = new BufferedReader(new InputStreamReader(System.in));
    static final BufferedWriter BW = new BufferedWriter(new OutputStreamWriter(System.out));
    static final int[] DX = {0, -1, 0, 1};
    static final int[] DY = {1, 0, -1, 0};

    int[] inputArray;
    int n, m, k;
    int[][] board, newBoard;
    Team[] teams;
    int answer;
    int round;

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
        k = inputArray[2];

        board = new int[n][n];
        for (int i = 0; i < n; i++) {
            board[i] = getInputArray();
        }

        answer = 0;
        teams = new Team[m+1];
        initTeams();
    }

    void initTeams() {
        boolean[][] visited = new boolean[n][n];
        newBoard = new int[n][n];
        int teamIndex = 1;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (board[i][j] == 4 && !visited[i][j]) {
                    List<Position> tracks = getTracks(i, j, visited);
                    List<Position> people = getPeople(tracks);
                    markPeopleToBoard(people, teamIndex);
                    teams[teamIndex] = new Team(teamIndex, people, tracks);
                    teamIndex += 1;
                }
            }
        }
        board = newBoard;
    }

    List<Position> getTracks(int x, int y, boolean[][] visited) {
        visited[x][y] = true;
        int index = 0;
        List<Position> tracks = new ArrayList<>();
        Queue<Position> current = new LinkedList<>();
        current.add(new Position(index, x, y));
        while (!current.isEmpty()) {
            Position curr = current.poll();
            tracks.add(curr);
            for (int d = 0; d < 4; d++) {
                int newX = curr.x + DX[d];
                int newY = curr.y + DY[d];
                if (isInner(newX, newY) && board[newX][newY] != 0 && !visited[newX][newY]) {
                    index += 1;
                    current.add(new Position(index, newX, newY));
                    visited[newX][newY] = true;
                    break;
                }
            }
        }
        return tracks;
    }

    List<Position> getPeople(List<Position> tracks) {
        List<Position> people = new ArrayList<>();
        int foundHeadAndTail = 0;
        boolean mustFlip = false;
        for (Position p : tracks) {
            if (board[p.x][p.y] == 4) {
                continue;
            }
            if (board[p.x][p.y] == 1 || board[p.x][p.y] == 3) {
                foundHeadAndTail += 1;
                if (foundHeadAndTail == 1) {
                    if (board[p.x][p.y] == 3) {
                        mustFlip = true;
                    }
                }
            }
            people.add(p);
            if (foundHeadAndTail == 2) {
                break;
            }
        }
        if (mustFlip) {
            List<Position> newPeople = new ArrayList<>();
            for (int i = people.size() - 1; i >= 0; i--) {
                newPeople.add(people.get(i));
            }
            people = newPeople;
        }
        return people;
    }

    void markPeopleToBoard(List<Position> people, int teamIndex) {
        for (Position p : people) {
            newBoard[p.x][p.y] = teamIndex;
        }
    }

    void solve() {
        while (round < k) {
            moveTeams();
            shootBall();
            round += 1;
        }
    }

    void moveTeams() {
        newBoard = new int[n][n];
        for (int i = 1; i <= m; i++) {
            Team team = teams[i];
            team.move(newBoard);
        }
        board = newBoard;
    }

    void shootBall() {
        int d = getBallDirection();
        Position p = getBallPosition();
        HashSet<Integer> caught = new HashSet<>();
        while (isInner(p)) {
            if (board[p.x][p.y] != 0 && !caught.contains(board[p.x][p.y])) {
                Team hit = teams[board[p.x][p.y]];
                int distance = hit.getDistance(p.x, p.y);
                answer += (distance * distance);
                hit.flip();
                caught.add(board[p.x][p.y]);
            }
            p.x += DX[d];
            p.y += DY[d];
        }
    }

    boolean isInner(Position p) {
        return 0 <= p.x && p.x < n && 0 <= p.y && p.y < n;
    }

    boolean isInner(int x, int y) {
        return 0 <= x && x < n && 0 <= y && y < n;
    }

    Position getBallPosition() {
        int mod = round % (n * 4);
        int div = mod / n;
        int diff = mod % n;
        if (div == 0) {
            return new Position(diff, 0);
        } else if (div == 1) {
            return new Position(n - 1, diff);
        } else if (div == 2) {
            return new Position(diff, n - 1);
        } else {
            return new Position(0, n - 1 - diff);
        }
    }

    int getBallDirection() {
        int mod = round % (n * 4);
        return mod / n;
    }

    void printResult() throws IOException {
        BW.write(Integer.toString(answer));
        BW.flush();
        BW.close();
        BR.close();
    }

    static class Position {
        int index;
        int x;
        int y;

        Position(int index, int x, int y) {
            this.index = index;
            this.x = x;
            this.y = y;
        }

        Position(int x, int y) {
            this.index = 0;
            this.x = x;
            this.y = y;
        }
    }

    static class Team {
        int index;
        List<Position> people;
        List<Position> tracks;
        boolean moveLeft;

        Team(int index, List<Position> people, List<Position> tracks) {
            this.index = index;
            this.people = people;
            this.tracks = tracks;
            this.moveLeft = people.get(0).index < people.get(people.size() - 1).index;
        }

        void move(int[][] newBoard) {
            List<Position> newPeople = new ArrayList<>();
            if (this.moveLeft) {
                for (Position p : people) {
                    Position newPosition = tracks.get(getLeftIndex(p.index));
                    newPeople.add(newPosition);
                    newBoard[newPosition.x][newPosition.y] = index;
                }
            } else {
                for (Position p : people) {
                    Position newPosition = tracks.get(getRightIndex(p.index));
                    newPeople.add(newPosition);
                    newBoard[newPosition.x][newPosition.y] = index;
                }
            }
            this.people = newPeople;
        }

        int getLeftIndex(int index) {
            return (index + tracks.size() - 1) % tracks.size();
        }

        int getRightIndex(int index) {
            return (index + 1) % tracks.size();
        }

        void flip() {
            List<Position> newPeople = new ArrayList<>();
            for (int i = people.size()-1; i >= 0; i--) {
                newPeople.add(people.get(i));
            }
            this.moveLeft = !this.moveLeft;
            this.people = newPeople;

        }

        int getDistance(int x, int y) {
            int distance = 1;
            for (Position p : people) {
                if (p.x == x && p.y == y) {
                    return distance;
                }
                distance += 1;
            }
            return distance;
        }
    }
}