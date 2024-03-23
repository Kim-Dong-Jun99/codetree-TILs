import java.util.*;
import java.io.*;

/*
P명의 산타, 루돌프가 반란을 일으킴,

루돌프는 산타들에 박치기를 해서 선물 배달을 방해하려고함,

N * N  보드, 좌상단 1,1

M 개의 턴, 매턴마다 루돌프와 산타들이 한번씩 움직임
루돌프가 한번 움직이고, 1번부터 P번 산타가 움직임,
    기절해있거나, 탈락한 산타는 움직임 X

두점 사이 거리 공식있음

루돌프 움직임
    가장 가까운 산타를 향해 1칸 돌진,
    가장 가까운 산타가 두명이상이면, r,c 좌표가 큰 산타로 돌진,
    8방향중 하나로 돌진, 가장 우선순위가 높은 산타를 향해 8방향중 가장 가까워지는 방향으로 돌진

산타 움직임
    루돌프에게 거리가 가장 가까워지는 방향으로 1칸 움직임
    다른 산타가 있는 칸으로 무빙 X,
    움직일 수 있는 칸이 있더라도 가까워지지 않는 다면 무빙 X
    상우하좌 순으로 움직임

충돌
    루돌프가 움직여서 충돌
        해당 산타는 C 만큼 점수를 얻고, 루돌프가 이동해온 방향으로 C만큼 밀려남
    산타가 움직여서 충돌
        D 만큼 점수를 얻고, 이동해온 반대방향으로 D만큼 밀려남
    밀려나는 동안에는 충돌 X, 밀려난 칸이 외부면 탈락, 산타가있음 상호작용

상호작용
    이동한 칸에 다른 산타가 있다면, 연쇄적으로 밀림,

기절
    루돌프랑 충돌 후 기절함, k 턴에 기절하면, k+2부터 정상 활동 가능
    기절한 산타도 충돌 가능함

게임 종료
    M 번의 턴에서 루돌프, 산타가 움직이고, 게임 종료
    산타가 모두 탈락하면 그 즉시 게임 종료
    매턴 아직 탈락하지 않은 산타는 1점 추가 획득

각 산타가 얻은 최종 점수를 구해야함

N <= 50,
M <= 1000,
P <= 30,

 */

public class Main {
    static final BufferedReader BR = new BufferedReader(new InputStreamReader(System.in));
    static final int EMPTY = 0;
    static final int RUDOLF = -1;
    static final int[] DX = {-1, 0, 1, 0, 1, -1, 1, -1};
    static final int[] DY = {0, 1, 0, -1, 1, 1, -1, -1};
    static final int[] OPPOSITE = {2, 3, 0, 1};
    int N, M, P, C, D;
    int[] inputArray;
    Santa[] santa;
    int round;

    int[][] board;
    Rudolf rudolf;


    public static void main(String[] args) {

        Main main = new Main();
        try {
            main.init();
            main.solution();
            main.printResult();
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
        P = inputArray[2];
        C = inputArray[3];
        D = inputArray[4];
        board = new int[N][N];
        santa = new Santa[P + 1];
        inputArray = getInputArray();
        rudolf = new Rudolf(inputArray[0] - 1, inputArray[1] - 1);
        board[rudolf.x][rudolf.y] = RUDOLF;

        for (int i = 0; i < P; i++) {
            inputArray = getInputArray();
            santa[inputArray[0]] = new Santa(inputArray[0], inputArray[1] - 1, inputArray[2] - 1);
            board[inputArray[1] - 1][inputArray[2] - 1] = inputArray[0];
        }

        round = 1;

    }

    void printBoard() {
        System.out.println("board");
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                System.out.print(board[i][j]+" ");
            }
            System.out.println();
        }
    }

    void solution() {

        while (round <= M) {
            if (gameOver()) {
                break;
            }
//            System.out.println("before round " + round);
//            printBoard();
            moveRudolf();
            moveSanta();
            updateSantaScore();
//            System.out.println("after round " + round);
//            printBoard();
            round += 1;
        }
    }

    void moveRudolf() {
        Position closestSantaPosition = getClosestSantaPosition();
        Position nextRudolfPosition = getNextRudolfPosition(closestSantaPosition);
        int rudolfDirection = getDirection(rudolf.x, rudolf.y, nextRudolfPosition.x, nextRudolfPosition.y);
        board[rudolf.x][rudolf.y] = EMPTY;
        rudolf.x = nextRudolfPosition.x;
        rudolf.y = nextRudolfPosition.y;
        if (board[nextRudolfPosition.x][nextRudolfPosition.y] != EMPTY) {
            crashWithSanta(rudolfDirection);
        }
        board[rudolf.x][rudolf.y] = RUDOLF;
    }

    void crashWithSanta(int rudolfDirection) {
        if (rudolfDirection == -1) {
            throw new RuntimeException("invalid situation");
        }
        Santa crashedSanta = santa[board[rudolf.x][rudolf.y]];
        crashedSanta.score += C;
        crashedSanta.knockedOut = round + 1;
        crashedSanta.x += DX[rudolfDirection] * C;
        crashedSanta.y += DY[rudolfDirection] * C;
        if (isInner(crashedSanta.x, crashedSanta.y)) {
            if (board[crashedSanta.x][crashedSanta.y] == EMPTY) {
                board[crashedSanta.x][crashedSanta.y] = crashedSanta.index;
            } else {
                Santa pushSanta = crashedSanta;
                while (pushSanta != null) {
                    Santa nextSanta;
                    if (!isInner(pushSanta.x, pushSanta.y) || board[pushSanta.x][pushSanta.y] == EMPTY) {
                        if (isInner(pushSanta.x, pushSanta.y)) {
                            board[pushSanta.x][pushSanta.y] = pushSanta.index;
                        }
                        nextSanta = null;
                    } else {
                        nextSanta = santa[board[pushSanta.x][pushSanta.y]];
                        board[pushSanta.x][pushSanta.y] = pushSanta.index;
                        nextSanta.x += DX[rudolfDirection];
                        nextSanta.y += DY[rudolfDirection];
                    }
                    pushSanta = nextSanta;
                }
            }
        }


    }


    int getDirection(int fromX, int fromY, int toX, int toY) {
        for (int i = 0; i < 8; i++) {
            int newX = fromX + DX[i];
            int newY = fromY + DY[i];
            if (newX == toX && newY == toY) {
                return i;
            }
        }
        return -1;
    }

    Position getNextRudolfPosition(Position santaPosition) {
        PriorityQueue<Position> rudolfPositions = new PriorityQueue<>(Position::compareWithDistanceAndXAndY);
        for (int i = 0; i < 8; i++) {
            int newX = rudolf.x + DX[i];
            int newY = rudolf.y + DY[i];
            if (isInner(newX, newY)) {
                rudolfPositions.add(new Position(newX, newY, calculateDistance(newX, newY, santaPosition.x, santaPosition.y)));
            }
        }
        return rudolfPositions.peek();
    }

    Position getClosestSantaPosition() {
        PriorityQueue<Position> santaPositions = new PriorityQueue<>(Position::compareWithDistanceAndXAndY);
        for (int i = 1; i <= P; i++) {
            if (isInner(santa[i].x, santa[i].y)) {
                santaPositions.add(new Position(santa[i].x, santa[i].y, calculateDistance(rudolf.x, rudolf.y, santa[i].x, santa[i].y)));
            }
        }
        return santaPositions.peek();
    }

    void moveSanta() {
        for (int i = 1; i <= P; i++) {
            if (isInner(santa[i].x, santa[i].y) && round > santa[i].knockedOut) {
                int nextSantaDirection = getNextSantaDirection(santa[i]);
                if (nextSantaDirection == -1) {
                    continue;
                }

                board[santa[i].x][santa[i].y] = EMPTY;
                santa[i].x += DX[nextSantaDirection];
                santa[i].y += DY[nextSantaDirection];
                if (board[santa[i].x][santa[i].y] == RUDOLF) {
                    crashWithRudolf(santa[i], nextSantaDirection);
                } else {

                    board[santa[i].x][santa[i].y] = santa[i].index;
                }
            }
        }
    }

    void crashWithRudolf(Santa santa, int direction) {
        int oppositeDirection = OPPOSITE[direction];
        santa.score += D;
        santa.knockedOut = round + 1;
        santa.x += DX[oppositeDirection] * D;
        santa.y += DY[oppositeDirection] * D;
        if (isInner(santa.x, santa.y)) {
            if (board[santa.x][santa.y] == EMPTY) {
                board[santa.x][santa.y] = santa.index;
            } else {
                Santa pushSanta = santa;
                while (pushSanta != null) {
                    Santa nextSanta;
                    if (!isInner(pushSanta.x, pushSanta.y) || board[pushSanta.x][pushSanta.y] == EMPTY) {
                        if (isInner(pushSanta.x, pushSanta.y)) {
                            board[pushSanta.x][pushSanta.y] = pushSanta.index;
                        }
                        nextSanta = null;
                    } else {
                        nextSanta = this.santa[board[pushSanta.x][pushSanta.y]];
                        board[pushSanta.x][pushSanta.y] = pushSanta.index;
                        nextSanta.x += DX[oppositeDirection];
                        nextSanta.y += DY[oppositeDirection];
                    }
                    pushSanta = nextSanta;
                }
            }
        }
    }

    int getNextSantaDirection(Santa santa) {
        int direction = -1;
        int currentDistance = calculateDistance(rudolf.x, rudolf.y, santa.x, santa.y);
        for (int i = 0; i < 4; i++) {
            int newX = santa.x + DX[i];
            int newY = santa.y + DY[i];

            if (isInner(newX, newY) && currentDistance > calculateDistance(rudolf.x, rudolf.y, newX, newY) && board[newX][newY] <= 0) {
                currentDistance = calculateDistance(rudolf.x, rudolf.y, newX, newY);
                direction = i;
            }
        }


        return direction;
    }

    int calculateDistance(int x1, int y1, int x2, int y2) {
        return (int) (Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    void updateSantaScore() {
        for (int i = 1; i <= P; i++) {
            if (isInner(santa[i].x, santa[i].y)) {
                santa[i].score += 1;
            }
        }
    }

    boolean gameOver() {
        for (int i = 1; i <= P; i++) {
            if (isInner(santa[i].x, santa[i].y)) {
                return false;
            }
        }
        return true;
    }

    boolean isInner(int x, int y) {
        return 0 <= x && x < N && 0 <= y && y < N;
    }

    void printResult() {
        for (int i = 1; i <= P; i++) {
            System.out.print(santa[i].score+" ");
        }
    }

    static class Position {
        int x;
        int y;
        int distance;

        public Position(int x, int y, int distance) {
            this.x = x;
            this.y = y;
            this.distance = distance;
        }

        int compareWithDistanceAndXAndY(Position compare) {
            if (this.distance != compare.distance) {
                return Integer.compare(this.distance, compare.distance);
            }
            if (this.x != compare.x) {
                return Integer.compare(compare.x, this.x);
            }
            return Integer.compare(compare.y, this.y);
        }
    }

    static class Rudolf {
        int x;
        int y;

        public Rudolf(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    static class Santa {
        int index;
        int score;
        int x;
        int y;
        int knockedOut;

        public Santa(int index, int x, int y) {
            this.index = index;
            this.x = x;
            this.y = y;
            this.knockedOut = 0;
        }
    }





}