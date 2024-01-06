import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private String name;
    private Socket socket;
    private String hostName;
    private int port;
    private Scanner sc = new Scanner(System.in);
    private boolean turno;
    private String symbol;
    private boolean isWinner = false;
    private boolean seguirJugando;

    public Client(String hostName, int port) {
        this.hostName = hostName;
        this.port = port;
    }

    public Client(Socket socket) {
        this.socket = socket;
        this.turno = false;
    }

    public void connect() {
        try {
            this.socket = new Socket(hostName, port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void send() {
        try {
            PrintStream out = new PrintStream(socket.getOutputStream());
            out.println(sc.nextLine());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void receive() {
        try {
            Scanner in = new Scanner(socket.getInputStream());
            String line;
            while ((line = in.nextLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public boolean isWinner() {
        return isWinner;
    }

    public void setWinner(boolean winner) {
        isWinner = winner;
    }

    public static void main(String[] args) {
        Client c = new Client("localhost", 23);
        c.connect();
        do {
            c.send();
            c.receive();
        } while (true);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Scanner getSc() {
        return sc;
    }

    public void setSc(Scanner sc) {
        this.sc = sc;
    }

    public boolean isTurno() {
        return turno;
    }

    public void setTurno(boolean turno) {
        this.turno = turno;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public boolean isSeguirJugando() {
        return seguirJugando;
    }

    public void setSeguirJugando(boolean seguirJugando) {
        this.seguirJugando = seguirJugando;
    }
}
