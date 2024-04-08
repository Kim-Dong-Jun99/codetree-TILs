import java.util.*;
import java.io.*;

/*
15:50
L * L 체스판, 좌상단 1,1
빈칸, 함정, 벽

기사들은 상대방 밀치기 가능,
기사 위치 r,c, r,c를 좌상단으로 하여 h * w 직사각형 형태, 체력 k

기사 이동
    상하좌우 중 한칸이동
    이동하려는 위치에 기사기 있다면, 연쇄적으로 한칸 밀려남
    이동하려는 방향 끝에 벽이 있다면 모두 이동 불가,
    체스판에서 사라진 기사는 명령 불가
대결 대미지
    다른 기사를 밀치게 되면 밀려난 기사들은 피해를 받음,
    각 기사들은 기사가 이동한 곳에 w * h 직사각형 내에 놓여있는 함정의 수 만큼 피해를 받음
    피해를 받은 만큼 체력이 깎이며 현재 체력 이상 대미지를 받으면 사라짐
    명령을 받은 기사는 피해 X,
    기사들은 모두 밀린 이후 대미지를 받음,
생존한 기사들이 총 받은 대미지의 합
*/

public class Main {
    static final BufferedReader BR = new BufferedReader(new InputStreamReader(System.in));
    static final BufferedWriter BW = new BufferedWriter(new OutputStreamWriter(System.out));
    static final int TRAP = 1;
    static final int WALL = 2;
    static final int[] DX = {-1, 0, 1, 0};
    static final int[] DY = {0, 1, 0, -1};

    int[] inputArray;
    int L, N, Q;
    int[][] board;
    int[][] knightBoard;
    Knight[] knights;
    Command[] commands;
    List<Knight> movedKnights;

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
        L = inputArray[0];
        N = inputArray[1];
        Q = inputArray[2];

        board = new int[L][L];
        for (int i = 0; i < L; i++) {
            board[i] = getInputArray();
        }

        knights = new Knight[N+1];
        knightBoard = new int[L][L];
        for (int i = 0; i < N; i++) {
            inputArray = getInputArray();
            int r = inputArray[0]-1;
            int c = inputArray[1]-1;
            int h = inputArray[2];
            int w = inputArray[3];
            int k = inputArray[4];
            knights[i+1] = new Knight(i+1, r, c, h, w, k);
            markBoard(knights[i + 1]);
        }

        commands = new Command[Q];
        for (int i = 0; i < Q; i++) {
            inputArray = getInputArray();
            commands[i] = new Command(inputArray[0], inputArray[1]);
        }
    }

    void solve() throws IOException {
        for (Command command : commands) {
            moveKnight(command);
            damageKnight();
        }
        calculateDamage();
    }

    void calculateDamage() throws IOException {
        int damageSum = 0;
        for (int i = 1; i <= N; i++) {
            Knight knight = knights[i];
            if (knight.k > 0) {
                damageSum += knight.damage;
            }
        }
        BW.write(Integer.toString(damageSum));
    }

    void moveKnight(Command command) {
        movedKnights = new ArrayList<>();
        Knight knight = knights[command.i];
        if (knight.k <= 0) {
            return;
        }
        if (canMove(knight, command.d)) {
            pushKnight(knight, command.d, 0);
        }
    }

    boolean canMove(Knight knight, int d) {
        HashSet<Integer> searchNext = new HashSet<>();
        if (d % 2 == 0) {
            if (d == 0) {
                int newX = knight.x - 1;
                for (int i = 0; i < knight.w; i++) {
                    int newY = knight.y + i;
                    if (isWall(newX, newY)) {
                        return false;
                    }
                    if (knightBoard[newX][newY] != 0) {
                        searchNext.add(knightBoard[newX][newY]);
                    }
                }
            } else {
                int newX = knight.x + knight.h;
                for (int i = 0; i < knight.w; i++) {
                    int newY = knight.y + i;
                    if (isWall(newX, newY)) {
                        return false;
                    }
                    if (knightBoard[newX][newY] != 0) {
                        searchNext.add(knightBoard[newX][newY]);
                    }
                }
            }
        } else {
            if (d == 1) {
                int newY = knight.y + knight.w;
                for (int i = 0; i < knight.h; i++) {
                    int newX = knight.x + i;
                    if (isWall(newX, newY)) {
                        return false;
                    }
                    if (knightBoard[newX][newY] != 0) {
                        searchNext.add(knightBoard[newX][newY]);
                    }
                }
            } else {
                int newY = knight.y - 1;
                for (int i = 0; i < knight.h; i++) {
                    int newX = knight.x + i;
                    if (isWall(newX, newY)) {
                        return false;
                    }
                    if (knightBoard[newX][newY] != 0) {
                        searchNext.add(knightBoard[newX][newY]);
                    }
                }
            }
        }
        boolean result = true;
        for (int index : searchNext) {
            result = canMove(knights[index], d);
            if (!result) {
                break;
            }
        }
        return result;
    }

    boolean isWall(int x, int y) {
        return !isInner(x, y) || board[x][y] == WALL;
    }

    boolean isInner(int x, int y) {
        return 0 <= x && x < L && 0 <= y && y < L;
    }

    void pushKnight(Knight knight, int d, int depth) {
        if (depth != 0) {
            movedKnights.add(knight);
        }
        unmarkBoard(knight);
        List<Knight> toMoveNext = getNextKnight(knight, d);
        for (Knight nextKnight : toMoveNext) {
            pushKnight(nextKnight, d, depth + 1);
        }
        knight.x += DX[d];
        knight.y += DY[d];
        markBoard(knight);
    }

    void unmarkBoard(Knight knight) {
        for (int i = knight.x; i < knight.x + knight.h; i++) {
            for (int j = knight.y; j < knight.y + knight.w; j++) {
                knightBoard[i][j] = 0;
            }
        }
    }

    List<Knight> getNextKnight(Knight knight, int d) {
        HashSet<Integer> indexes = new HashSet<>();
        if (d % 2 == 0) {
            if (d == 0) {
                int newX = knight.x - 1;
                for (int i = 0; i < knight.w; i++) {
                    int newY = knight.y + i;
                    if (knightBoard[newX][newY] != 0) {
                        indexes.add(knightBoard[newX][newY]);
                    }
                }
            } else {
                int newX = knight.x + knight.h;
                for (int i = 0; i < knight.w; i++) {
                    int newY = knight.y + i;
                    if (knightBoard[newX][newY] != 0) {
                        indexes.add(knightBoard[newX][newY]);
                    }
                }
            }
        } else {
            if (d == 1) {
                int newY = knight.y + knight.w;
                for (int i = 0; i < knight.h; i++) {
                    int newX = knight.x + i;
                    if (knightBoard[newX][newY] != 0) {
                        indexes.add(knightBoard[newX][newY]);
                    }
                }
            } else {
                int newY = knight.y - 1;
                for (int i = 0; i < knight.h; i++) {
                    int newX = knight.x + i;
                    if (knightBoard[newX][newY] != 0) {
                        indexes.add(knightBoard[newX][newY]);
                    }
                }
            }
        }
        List<Knight> toReturn = new ArrayList<>();
        for (Integer index : indexes) {
            toReturn.add(knights[index]);
        }
        return toReturn;
    }


    void markBoard(Knight knight) {
        for (int i = knight.x; i < knight.x + knight.h; i++) {
            for (int j = knight.y; j < knight.y + knight.w; j++) {
                knightBoard[i][j] = knight.index;
            }
        }
    }

    void damageKnight() {
        for (Knight knight : movedKnights) {
            int damage = 0;
            for (int i = knight.x; i < knight.x + knight.h; i++) {
                for (int j = knight.y; j < knight.y + knight.w; j++) {
                    if (board[i][j] == TRAP) {
                        damage += 1;
                    }
                }
            }
            knight.damage += damage;
            knight.k -= damage;
            if (knight.k <= 0) {
                unmarkBoard(knight);
            }

        }

    }

    void printResult() throws IOException {
        BW.flush();
        BW.close();
        BR.close();
    }

    static class Knight {
        int index;
        int x;
        int y;
        int h;
        int w;
        int k;
        int damage;

        Knight(int index, int x, int y, int h, int w, int k) {
            this.index = index;
            this.x = x;
            this.y = y;
            this.h = h;
            this.w = w;
            this.k = k;
            this.damage = 0;
        }
    }

    static class Command {
        int i;
        int d;

        Command(int i, int d) {
            this.i = i;
            this.d = d;
        }
    }
}