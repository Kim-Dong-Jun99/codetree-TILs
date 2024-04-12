import java.util.*;
import java.io.*;


public class Main {
    static final BufferedReader BR = new BufferedReader(new InputStreamReader(System.in));
    static final int[] DX = {0, -1, 0, 1};
    static final int[] DY = {1, 0, -1, 0};
    int[] inputArray;
    static int[][] gameBoard;
    static int[][] teamBoard;
    Team[] teams;
    Position ballPosition;
    int ballDirection;
    static int n, m, k;
    int round;
    int answer;
    int repeatMod;

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
        k = inputArray[2];

        gameBoard = new int[n][n];
        teamBoard = new int[n][n];
        teams = new Team[m];

        for (int i = 0; i < n; i++) {
            Arrays.fill(teamBoard[i], -1);
            gameBoard[i] = getInputArray();
        }

        round = 1;
        answer = 0;

        initTeams();

        repeatMod = n * 4;

    }

    void initTeams() {
        boolean[][] visited = new boolean[n][n];
        int index = 0;

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (gameBoard[i][j] > 0 && !visited[i][j]) {
                    Team team = new Team();
                    int x = i;
                    int y = j;
                    while (true) {
                        if (gameBoard[x][y] != 4) {
                            team.size += 1;
                        }
                        team.members.add(new Position(x, y));
                        visited[x][y] = true;
                        teamBoard[x][y] = index;
                        if (gameBoard[x][y] == 3) {
                            team.tail = team.members.size() - 1;
                        }
                        if (gameBoard[x][y] == 1) {
                            team.head = team.members.size() - 1;
                        }
                        boolean canGo = false;
                        for (int d = 0; d < 4; d++) {
                            int newX = x + DX[d];
                            int newY = y + DY[d];
                            if (isInner(newX, newY) && gameBoard[newX][newY] > 0 && !visited[newX][newY]) {
                                x = newX;
                                y = newY;
                                canGo = true;
                                break;
                            }

                        }
                        if (!canGo) {
                            break;
                        }
                    }
                    teams[index] = team;

                    index += 1;
                }
            }
        }
    }

    boolean isInner(int x, int y) {
        return 0 <= x && x < n && 0 <= y && y < n;
    }


    void solution() throws IOException {
        while (round <= k) {
            for (Team team : teams) {
                team.move();
            }
            determineBallPositionAndDirection();

            while (isInner(ballPosition.x, ballPosition.y)) {
                if (0 < gameBoard[ballPosition.x][ballPosition.y] && gameBoard[ballPosition.x][ballPosition.y] < 4) {
                    Team team = teams[teamBoard[ballPosition.x][ballPosition.y]];
                    answer += calculateScore(team);
                    team.flip();
                    break;
                }
                ballPosition.x += DX[ballDirection];
                ballPosition.y += DY[ballDirection];
            }


            round += 1;
        }
        System.out.println(answer);
    }

    int calculateScore(Team team) {
        int distance = team.getDistance(ballPosition);
        return distance * distance;

    }

    void determineBallPositionAndDirection() {
        int repeatedRound = (round - 1) % repeatMod;
        int mod = (round - 1) % n;
        ballDirection = repeatedRound / n;
        if (ballDirection == 0) {
            ballPosition = new Position(mod, 0);
        } else if (ballDirection == 1) {
            ballPosition = new Position(n - 1, mod);
        } else if (ballDirection == 2) {
            ballPosition = new Position(n - 1 - mod, n - 1);
        } else {
            ballPosition = new Position(0, n - 1 - mod);
        }


    }

    static class Position {
        int x;
        int y;

        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    static class Team {
        List<Position> members;
        int head;
        int tail;
        int size;

        public Team() {
            members = new ArrayList<>();
            size = 0;
        }

        int getDistance(Position position) {
            int toReturn = 1;
            int leftIndex = getLeftIndex(head);
            Position left = members.get(leftIndex);
            int current = head;
            if (gameBoard[left.x][left.y] == 4 || gameBoard[left.x][left.y] == 3) {

                while (members.get(current).x != position.x || members.get(current).y != position.y) {
                    toReturn += 1;
                    current = getRightIndex(current);
                }
            } else {
                while (members.get(current).x != position.x || members.get(current).y != position.y) {
                    toReturn += 1;
                    current = getLeftIndex(current);
                }
            }

            return toReturn;
        }

        int getLeftIndex(int index) {
            return (index - 1 + members.size()) % members.size();
        }

        int getRightIndex(int index) {
            return (index + 1) % members.size();
        }

        void move() {
            int[][] tempBoard = new int[n][n];
            int leftIndex = getLeftIndex(head);
            int rightIndex = getRightIndex(head);
            Position left = members.get(leftIndex);
            Position right = members.get(rightIndex);
            if (gameBoard[left.x][left.y] == 4 || gameBoard[left.x][left.y] == 3) {
                head = getLeftIndex(head);
                tail = getLeftIndex(tail);
                for (int i = 0; i < size; i++) {
                    int nextIndex = getRightIndex(leftIndex);
                    Position nextPosition = members.get(nextIndex);
                    tempBoard[left.x][left.y] = gameBoard[nextPosition.x][nextPosition.y];
                    leftIndex = nextIndex;
                    left = members.get(leftIndex);
                }
            } else {
                head = getRightIndex(head);
                tail = getRightIndex(tail);
                for (int i = 0; i < size; i++) {
                    int nextIndex = getLeftIndex(rightIndex);
                    Position nextPosition = members.get(nextIndex);
                    tempBoard[right.x][right.y] = gameBoard[nextPosition.x][nextPosition.y];
                    rightIndex = nextIndex;
                    right = members.get(rightIndex);
                }
            }
            for (Position position : members) {
                if (tempBoard[position.x][position.y] != 0) {
                    gameBoard[position.x][position.y] = tempBoard[position.x][position.y];
                } else {
                    gameBoard[position.x][position.y] = 4;
                }
            }

        }

        void flip() {
            int temp = head;

            head = tail;
            tail = temp;
            gameBoard[members.get(head).x][members.get(head).y] = 1;
            gameBoard[members.get(tail).x][members.get(tail).y] = 3;
        }
    }


}