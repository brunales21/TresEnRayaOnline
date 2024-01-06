import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Server {
    private ServerSocket serverSocket;
    private int port;
    private List<Client> clientes;
    private List<Socket> sockets;
    private final int MAX_CAP = 2;
    private String[][] tablero;


    public Server(int port) {
        this.port = port;
        this.sockets = new ArrayList<>();
        this.clientes = new ArrayList<>();
        initBoard();
    }

    public void initNames(List<Client> clientes) {
        for (Client client : clientes) {
            try {
                PrintStream out = new PrintStream(client.getSocket().getOutputStream());
                out.println("Introduce tu nombre: ");
                Scanner in = new Scanner(client.getSocket().getInputStream());
                client.setName(in.nextLine());
                out.println("Tu simbolo: ");
                client.setSymbol(in.nextLine().charAt(0)+" ");
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    public void start() {
        try {
            this.serverSocket = new ServerSocket(port);
            for (int i = 0; i < MAX_CAP; i++) {
                System.out.println("Esperando cliente " + i + "..");
                Socket socket = serverSocket.accept();
                sockets.add(socket);
                clientes.add(new Client(socket));
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        initNames(clientes);
        initTurn();
        process(clientes);
    }

    private void process(List<Client> clients) {
        printBoard(clients);
        boolean existsWinner = false;
        do {
            do {
                try {
                    for (Client client : clients) {
                        if (client.isTurno()) {
                            Scanner in = new Scanner(client.getSocket().getInputStream());
                            String msg = in.nextLine();
                            int x = Integer.parseInt(String.valueOf(msg.charAt(0))) - 1;
                            int y = Integer.parseInt(String.valueOf(msg.charAt(1))) - 1;
                            updateBoard(x, y, client.getSymbol());
                            if (won(client.getSymbol())) {
                                client.setWinner(true);
                                notifyClients("Winner: "+client.getName());
                                existsWinner = true;
                                break;
                            }
                            setNextTurn();
                            printBoard(clients);
                            clientes.stream().filter(a -> a.isTurno()).forEach(a -> System.out.println(a.getName()));
                        }
                    }
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }
            } while (!existsWinner);
        } while (seguirJugando());
        notifyClients("El juego termino..");

    }

    static final String RESPUESTAS_POSITIVAS = "YSys";
    static final String RESPUESTAS_NEGATIVAS = "Nn";

    public boolean seguirJugando() {
        try {
            for (Client client : clientes) {
                PrintStream out = null;
                out = new PrintStream(client.getSocket().getOutputStream());
                String input;
                do {
                    out.println("DESEA JUGAR OTRA PARTIDA? (y/n)");
                    Scanner in = new Scanner(client.getSocket().getInputStream());
                    input = in.nextLine();
                } while (RESPUESTAS_POSITIVAS.indexOf(input.charAt(0)) == -1 && RESPUESTAS_NEGATIVAS.indexOf(input.charAt(0)) == -1);

                if (RESPUESTAS_POSITIVAS.indexOf(input.charAt(0)) != -1) {
                    client.setSeguirJugando(true);
                } else {
                    client.setSeguirJugando(false);
                }
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return clientes.stream().allMatch(Client::isSeguirJugando);
    }

    public void notifyClients(String mensaje) {
        for (Client client : clientes) {
            PrintStream out = null;
            try {
                out = new PrintStream(client.getSocket().getOutputStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            out.println(mensaje);
        }
    }

    public boolean won(String playerSymbol) {

        boolean line = false;

        //Checks horizontales
        for (int x = 0; x < tablero.length; x++) {
            for (int y = 0; y < tablero[0].length; y++) {
                if (!tablero[x][y].equals(playerSymbol)) {
                    line = false;
                    break;
                } else {
                    line = true;
                }
            }
            if (line) {
                return true;
            }
        }


        //Checks verticales
        for (int x = 0; x < tablero.length; x++) {
            for (int y = 0; y < tablero[0].length; y++) {
                if (!tablero[y][x].equals(playerSymbol)) {
                    line = false;
                    break;
                } else {
                    line = true;
                }
            }
            if (line) {
                return true;
            }
        }

        //Checks diagonales
        // (desc)
        for (int x = 0; x < 3; x++) {
            if (!tablero[x][x].equals(playerSymbol)) {
                line = false;
                break;
            } else {
                line = true;
            }
        }
        if (line) {
            return true;
        }

        // (asc)
        for (int x = 0; x < 3; x++) {
            line = tablero[x][2 - x].equals(playerSymbol);
            if (!line) {
                break;
            }
        }
        return line;
    }

    private Client getWinner() {
        return clientes.stream().filter(c -> c.isWinner()).toList().get(0);
    }

    private void setNextTurn() {
        /*
        if (clientes.get(0).isTurno()) {
            clientes.get(0).setTurno(false);
            clientes.get(1).setTurno(true);
        } else {
            clientes.get(1).setTurno(false);
            clientes.get(0).setTurno(true);
        }

         */
        for (int i = 0; i < clientes.size() - 1; i++) {
            Client client = clientes.get(i);
            if (client.isTurno()) {
                clientes.get(i + 1).setTurno(true);
                client.setTurno(false);
                break;
            }
            clientes.get(clientes.size() - 1).setTurno(false);
            clientes.get(0).setTurno(true);
        }
    }

    private void initTurn() {
        clientes.get(0).setTurno(true);
        System.out.println(clientes.get(0).getName());
    }

    public void updateBoard(int x, int y, String symbol) {
        for (int i = 0; i < tablero.length; i++) {
            for (int j = 0; j < tablero[i].length; j++) {
                tablero[x][y] = symbol.concat(" ");
            }
        }
    }

    public Client getPlayerTurno() {
        return clientes.stream().filter(c -> c.isTurno()).toList().get(0);
    }

    public void printBoard(List<Client> clientes) {
        try {
            for (Client client : clientes) {
                cleanConsole(client.getSocket());
                PrintStream out = new PrintStream(client.getSocket().getOutputStream());
                out.println("Turno de " + getPlayerTurno().getName());
                for (int i = 0; i < tablero.length; i++) {
                    for (int j = 0; j < tablero[i].length; j++) {
                        out.print(tablero[i][j]);
                    }
                    out.println();
                }
            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

    }

    public void cleanConsole(Socket socket) {
        try {
            PrintStream out = new PrintStream(socket.getOutputStream());
            out.print("\033[H\033[2J");
            out.flush();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public void initBoard() {
        this.tablero = new String[3][3];
        for (int i = 0; i < tablero.length; i++) {
            for (int j = 0; j < tablero[i].length; j++) {
                tablero[i][j] = "# ";
            }
        }
    }

    public static void main(String[] args) {
        Server s = new Server(23);
        s.start();
    }
}
