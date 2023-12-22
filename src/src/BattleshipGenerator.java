import java.util.ArrayList;
import java.util.Random;

public interface BattleshipGenerator {

    String generateMap();
    static BattleshipGenerator defaultInstance() {
        return new BattleshipGenerator() {
            private static final int BOARD_SIZE = 10;
            @Override
            public String generateMap() {
                char[][] board = new char[BOARD_SIZE][BOARD_SIZE];
                initializeBoard(board);
                placeShips(board);
                //System.out.println(boardToString(board));
                return boardToString(board);
            }

            private void initializeBoard(char[][] board) {
                for (int i = 0; i < BOARD_SIZE; i++) {
                    for (int j = 0; j < BOARD_SIZE; j++) {
                        board[i][j] = '.';
                    }
                }
            }

            private void placeShips(char[][] board) {
                Random random = new Random();

                for (int shipSize = 4; shipSize >= 1; shipSize--) {
                    for (int shipCount = 0; shipCount < 5 - shipSize; shipCount++) {
                        boolean placed = false;

                        while (!placed) {
                            int x = random.nextInt(BOARD_SIZE);
                            int y = random.nextInt(BOARD_SIZE);
                            boolean horizontal = random.nextBoolean();

                            if (canPlaceShip(board, x, y, shipSize, horizontal)) {
                                placeShip(board, x, y, shipSize, horizontal);
                                placed = true;
                            }
                        }
                    }
                }
            }

            private boolean canPlaceShip(char[][] board, int x, int y, int size, boolean horizontal) {
                if (x < 0 || x >= BOARD_SIZE || y < 0 || y >= BOARD_SIZE) {
                    return false;
                }

                int endX = x + (horizontal ? size - 1 : 0);
                int endY = y + (horizontal ? 0 : size - 1);

                if (endX >= BOARD_SIZE || endY >= BOARD_SIZE) {
                    return false;
                }

                for (int i = x; i <= endX; i++) {
                    for (int j = y; j <= endY; j++) {
                        if (board[i][j] != '.' ||
                            (j != BOARD_SIZE - 1 &&                        board[i][j + 1] != '.') ||
                            (j != 0 &&                                     board[i][j - 1] != '.') ||
                            (i != BOARD_SIZE - 1 &&                        board[i + 1][j] != '.') ||
                            (i != BOARD_SIZE - 1 && j != BOARD_SIZE - 1 && board[i + 1][j + 1] != '.') ||
                            (i != BOARD_SIZE - 1 && j != 0 &&              board[i + 1][j - 1] != '.') ||
                            (i != 0 &&                                     board[i - 1][j] != '.') ||
                            (i != 0 && j != BOARD_SIZE - 1 &&              board[i - 1][j + 1] != '.') ||
                            (i != 0 && j != 0 &&                           board[i - 1][j - 1] != '.')){
                            return false;
                        }
                    }
                }

                return true;
            }

            private void placeShip(char[][] board, int x, int y, int size, boolean horizontal) {
                int endX = x + (horizontal ? size - 1 : 0);
                int endY = y + (horizontal ? 0 : size - 1);



                for (int i = x; i <= endX; i++) {
                    for (int j = y; j <= endY; j++) {

                        board[i][j] = '#';
                    }
                }
            }

            private String boardToString(char[][] board) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < BOARD_SIZE; i++) {
                    for (int j = 0; j < BOARD_SIZE; j++) {
                        sb.append(board[i][j]);
                    }
                }
                return sb.toString();
            }
        };
    }

}
