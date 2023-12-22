import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.Random;

public class ServerOrClient {
    private static final int BOARD_SIZE = 10;
    private static final String CLIENT = "client";
    private static final String SERVER = "server";
    private static final String END = "end";
    private static String mode;
    private static String serverAddress;
    private static int port;
    private static Path map;
    private static int remainingShips = 10;
    private static final BattleshipGenerator bg = BattleshipGenerator.defaultInstance();
    private static String shotCoordinates = " ";
    private static String enemyMap = "??????????" +
            "??????????" +
            "??????????" +
            "??????????" +
            "??????????" +
            "??????????" +
            "??????????" +
            "??????????" +
            "??????????" +
            "??????????";
    public static void main(String[] args) {
        fillArgs(args);
        if (SERVER.equals(mode)) {
            runServer(port);
        } else if (CLIENT.equals(mode)) {
            runClient(serverAddress, port);
        } else {
            System.out.println("Nieprawidłowy mode.");
            System.out.println("mode: " + mode);
        }
    }
    private static void runServer(int port) {
        try {
            String stringMap = bg.generateMap();
            writeStringToFile(map, stringMap);

            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Oczekiwanie na połączenie...");
            Socket clientSocket = serverSocket.accept();
            System.out.println("Połączono z klientem.");

            BufferedReader clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter clientOut = new PrintWriter(clientSocket.getOutputStream(), true);

            while (true) {
                // Odczytanie komunikatu od serwera
                String clientMessage = clientIn.readLine();
                if (clientMessage == null) {
                    // Połączenie z serwerem zostało przerwane
                    break;
                }

                // Obsługa otrzymanego komunikatu od serwera
                String messageToSend = handleServerMessage(clientMessage);
                boolean newShot = false;
                while(!newShot){
                    shotCoordinates = generateRandomCoordinate();
                    if(enemyMap.charAt(coordinatesToIndex(shotCoordinates)) == '?'){
                        newShot = true;
                    }
                }
                System.out.println("REMAIN: " + remainingShips);
                System.out.println(shotCoordinates);
                clientOut.println(messageToSend + ";" + shotCoordinates);
            }

        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println("End of the program.");
        }
    }
    private static void runClient(String serverAddress, int port) {
        try {
            String stringMap = bg.generateMap();
            writeStringToFile(map, stringMap);

            Socket socket = new Socket(serverAddress, port);
            BufferedReader serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter serverOut = new PrintWriter(socket.getOutputStream(), true);

            // Wysłanie komendy start do serwera
            shotCoordinates = generateRandomCoordinate();
            serverOut.println("start;" + shotCoordinates);

            // Główna pętla gry klienta
            while (true) {
                // Odczytanie komunikatu od serwera
                String serverMessage = serverIn.readLine();
                if (serverMessage == null) {
                    // Połączenie z serwerem zostało przerwane
                    System.out.println("no message");
                    break;
                }

                // Obsługa otrzymanego komunikatu od serwera

                String messageToSend = handleServerMessage(serverMessage);
                if(Objects.equals(messageToSend, END)) return;

                boolean newShot = false;
                while(!newShot){
                    shotCoordinates = generateRandomCoordinate();
                    if(enemyMap.charAt(coordinatesToIndex(shotCoordinates)) == '?'){
                        newShot = true;
                    }
                }
                System.out.println("REMAIN: " + remainingShips);
                System.out.println(shotCoordinates);
                serverOut.println(messageToSend + ";" + shotCoordinates);
            }

        } catch (IOException e) {
            //e.printStackTrace();

            System.out.println("End of the program.");
        }
    }
    private static int coordinatesToIndex(String coordinates){
        return (coordinates.charAt(0) - 'A')*BOARD_SIZE + Integer.parseInt(coordinates.substring(1)) - 1;
    }
    private static String generateRandomCoordinate() {
        Random random = new Random();

        // Randomly select a row and column
        int row = random.nextInt(BOARD_SIZE) + 1; // Adding 1 to convert to 1-based index
        int col = random.nextInt(BOARD_SIZE) + 'A'; // Adding 'A' to convert to letter

        // Convert column value to letter representation (A, B, C, ...)
        char colChar = (char) col;

        // Combine row and column to form the coordinate string
        return colChar + Integer.toString(row);
    }
    private static void completeEnemyMapWithDots(Coordinates coordinates, String previous){
        int upperCell = (coordinates.row() - 1) * BOARD_SIZE + coordinates.column();
        int lowerCell = (coordinates.row() + 1) * BOARD_SIZE + coordinates.column();
        int rightCell = coordinates.row() * BOARD_SIZE + coordinates.column() + 1;
        int leftCell = coordinates.row() * BOARD_SIZE + coordinates.column() - 1;

        if(coordinates.row() > 0 && !previous.equals("upper")){
            completeWithDotsHelper(upperCell, "lower");
        }
        if(coordinates.row() < BOARD_SIZE - 1 && !previous.equals("lower")){
            completeWithDotsHelper(lowerCell, "upper");
        }
        if(coordinates.row() > 0 && !previous.equals("left")){
            completeWithDotsHelper(leftCell, "right");
        }
        if(coordinates.column() < BOARD_SIZE - 1 && !previous.equals("right")){
            completeWithDotsHelper(rightCell, "left");
        }
    }
    private static void completeWithDotsHelper(int cell, String previous){
        if(enemyMap.charAt(cell) == '?' || enemyMap.charAt(cell) == '.'){
            enemyMap = replaceChar(enemyMap, cell, '.');
            completeDiagonal(cell, previous);
        } else
        if(enemyMap.charAt(cell) == '#') {
            completeEnemyMapWithDots(new Coordinates(cell), previous);
        }
    }
    private static void completeDiagonal(int cell, String previous){
        switch (previous){
            case "lower":
            case "upper":
                if(cell % 10 != 0) enemyMap = replaceChar(enemyMap, cell - 1, '.');
                if(cell % 10 != 9) enemyMap = replaceChar(enemyMap, cell + 1, '.');
                break;
            case "right":
            case "left":
                if(cell >= 10) enemyMap = replaceChar(enemyMap, cell - 10, '.');
                if(cell < 90) enemyMap = replaceChar(enemyMap, cell + 10, '.');
                break;
        }
    };
    private static String handleServerMessage(String message) throws IOException {
        // Rozdzielenie komendy i współrzędnych
        String[] parts = message.split(";");
        String command = parts[0];
        Coordinates coordinates = null;
        if(parts.length == 2)
            coordinates = new Coordinates(parts[1]);

        int previousShotIndex = 0;
        if(!Objects.equals(command, "start"))
            previousShotIndex = coordinatesToIndex(shotCoordinates);
        // Obsługa różnych komend od serwera
        switch (command) {
            case "pudło":
                System.out.println("Pudło!");
                enemyMap = replaceChar(enemyMap, previousShotIndex, '.');
                printBoard(enemyMap);
                break;
            case "trafiony":
                System.out.println("Trafiony!");
                enemyMap = replaceChar(enemyMap, previousShotIndex, '#');
                printBoard(enemyMap);
                // Tutaj dodaj kod obsługujący trafiony
                break;
            case "trafiony zatopiony":
                System.out.println("Trafiony zatopiony! Ty zatopił statek.");
                enemyMap = replaceChar(enemyMap, previousShotIndex, '#');
                completeEnemyMapWithDots(new Coordinates(previousShotIndex), " ");
                printBoard(enemyMap);
                // Tutaj dodaj kod obsługujący trafiony zatopiony
                break;
            case "ostatni zatopiony":
                System.out.println("Wygrana");
                enemyMap = replaceChar(enemyMap, previousShotIndex, '#');
                enemyMap = enemyMap.replace("?",".");
                printBoard(enemyMap);
                System.out.println();
                printBoard(new String(Files.readAllBytes(map)));
                return END;
                // Tutaj dodaj kod obsługujący ostatni zatopiony
                // Możesz również zakończyć pętlę gry lub podjąć inne działania
            case "start":
                System.out.println("START!!!");
                break;
            default:
                System.out.println("Nieznana komenda od serwera: " + message);
        }
        return handleCoordinates(coordinates);
        // Tutaj można dodać kod obsługujący wprowadzanie ruchu gracza i wysłanie go do serwera
        // Przykład: serverOut.println("strzał;B2");
    }
    private static String handleCoordinates(Coordinates coordinates) throws IOException {
        String messageToServer = "";
        String stringMap = new String(Files.readAllBytes(map));
        int shotIndex = coordinates.row() * BOARD_SIZE + coordinates.column();
        char shotCell = stringMap.charAt(shotIndex);
        switch (shotCell) {
            case '.':
                messageToServer = "pudło";
                stringMap = replaceChar(stringMap, shotIndex, '~');
                break;
            case '#':
                if (checkIfLastPartOfShip(coordinates, stringMap, " ")) {
                    if (remainingShips == 1) {
                        messageToServer = "ostatni zatopiony";
                        System.out.println("Przegrana");
                        stringMap = replaceChar(stringMap, shotIndex, '@');
                        writeStringToFile(map, stringMap);
                        printBoard(enemyMap);
                        System.out.println();
                        printBoard(new String(Files.readAllBytes(map)));
                    } else {
                        messageToServer = "trafiony zatopiony";
                        remainingShips--;
                    }
                } else {
                    messageToServer = "trafiony";
                }
                stringMap = replaceChar(stringMap, shotIndex, '@');
                break;
            case '@':
                if(checkIfLastPartOfShip(coordinates, stringMap, " ")){
                    messageToServer = "trafiony zatopiony";
                } else {
                    messageToServer = "trafiony";
                }
                break;
            case '~':
                messageToServer = "pudło";
                break;
        }
        writeStringToFile(map, stringMap);
        return messageToServer;
    }
    private static boolean checkIfLastPartOfShip(Coordinates coordinates, String stringMap, String previous){
        int upperCell = (coordinates.row() - 1) * BOARD_SIZE + coordinates.column();
        int lowerCell = (coordinates.row() + 1) * BOARD_SIZE + coordinates.column();
        int rightCell = coordinates.row() * BOARD_SIZE + coordinates.column() + 1;
        int leftCell = coordinates.row() * BOARD_SIZE + coordinates.column() - 1;
        String result1 = "DOT";
        String result2 = "DOT";
        String result3 = "DOT";
        String result4 = "DOT";
        System.out.println(coordinates.row() + " " + coordinates.column());
        printBoard(stringMap);
        if(coordinates.row() > 0 && !previous.equals("upper")){
            result1 = cellOfSunkenShip(upperCell, stringMap, "lower");
            if(result1.equals("NOT_LAST"))return false;
        }
        if(coordinates.row() < BOARD_SIZE - 1 && !previous.equals("lower")){
            result2 = cellOfSunkenShip(lowerCell, stringMap, "upper");
            if(result2.equals("NOT_LAST"))return false;
        }
        if(coordinates.column() > 0 && !previous.equals("left")){
            result3 = cellOfSunkenShip(leftCell, stringMap, "right");
            if(result3.equals("NOT_LAST"))return false;
        }
        if(coordinates.column() < BOARD_SIZE - 1 && !previous.equals("right")){
            result4 = cellOfSunkenShip(rightCell, stringMap, "left");
            if(result4.equals("NOT_LAST"))return false;
        }
        if(result1.equals("DOT") && result2.equals("DOT") && result3.equals("DOT") && result4.equals("DOT")){
            return true;
        } else if(result1.equals("LAST") || result2.equals("LAST") || result3.equals("LAST") || result4.equals("LAST")){
            return true;
        } else return false;
    }
    private static String cellOfSunkenShip(int cell, String stringMap, String previous){
        if(stringMap.charAt(cell) != '.' && stringMap.charAt(cell) != '~'){
            if(stringMap.charAt(cell) == '#') {
                return "NOT_LAST";
            }
            if(stringMap.charAt(cell) == '@') {
                if(checkIfLastPartOfShip(new Coordinates(cell), stringMap, previous)) return "DOT";
                else return "NOT_LAST";
            }
        }
        return "DOT";
    }
    private static void fillArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-mode":
                    if (args.length > i + 1) {
                        mode = args[i + 1];
                    }
                    break;
                case "-server":
                    if (args.length > i + 1) {
                        serverAddress = args[i + 1];
                    }
                    break;
                case "-port":
                    if (args.length > i + 1) {
                        port = Integer.parseInt(args[i + 1]);
                    }
                    break;
                case "-map":
                    if (args.length > i + 1) {
                        map = Path.of(args[i + 1]);
                    }
                    break;
            }
        }
    }
    private static String replaceChar(String str, int index, char replacementChar){
        StringBuilder stringBuilder = new StringBuilder(str);
        stringBuilder.setCharAt(index, replacementChar);
        return stringBuilder.toString();
    }
    private static void writeStringToFile(Path filePath, String content) throws IOException {
        Files.write(filePath, content.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
    }
    private static void printBoard(String stringMap) {
        for(int i = 0; i < BOARD_SIZE; ++i){
            for(int j = 0; j < BOARD_SIZE; ++j){
                int charIndex = i * BOARD_SIZE + j;
                System.out.print(stringMap.charAt(charIndex));
            }
            System.out.println();
        }
    }
}
