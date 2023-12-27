package ru.gb.lesson5;

import lombok.Getter;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

public class Server {

  public static final int PORT = 8181;

  private static long clientIdCounter = 1L;
  private static Map<Long, SocketWrapper> clients = new HashMap<>();

  public static void main(String[] args) throws IOException {
    try (ServerSocket server = new ServerSocket(PORT)) {
      System.out.println("Сервер запущен на порту " + PORT);
      while (true) {
        final Socket client = server.accept();
        final long clientId = clientIdCounter++;


        SocketWrapper wrapper = new SocketWrapper(clientId, client);
        System.out.println("Подключился новый клиент[" + wrapper + "]");
        clients.put(clientId, wrapper);

        new Thread(() -> {
          try (Scanner input = wrapper.getInput(); PrintWriter output = wrapper.getOutput()) {
            output.println("Подключение успешно. Список всех клиентов: " + clients);

            while (true) {
              String clientInput = input.nextLine();
              //при сообщении admin сервер поймет, что подлкючился админ
              if (Objects.equals("admin", clientInput)){
                wrapper.setAdmin(true);
                output.println("Вы подключились как админ");
              }
              //формат kick id
              if (clientInput.substring(0, 4).equals("kick")){
                long destinationId = Long.parseLong(clientInput.substring(5, 6));
                SocketWrapper destination = clients.get(destinationId);
                clients.remove(destinationId);
                clients.values().forEach(it -> it.getOutput().println("Клиент[" + destinationId + "] отключился"));
              }
              if (Objects.equals("q", clientInput)) {
                // todo разослать это сообщение всем остальным клиентам
                clients.remove(clientId);
                clients.values().forEach(it -> it.getOutput().println("Клиент[" + clientId + "] отключился"));
                break;
              }
              if (clientInput.substring(0, 1).equals("@")) {
                long destinationId = Long.parseLong(clientInput.substring(1, 2));
                SocketWrapper destination = clients.get(destinationId);
                destination.getOutput().println(clientInput);
              }else {
                if (!Objects.equals("admin", clientInput))
                clients.values().stream().filter(it -> !it.equals(clients.get(clientId))).forEach(it -> it.getOutput().println(clientInput));
              }
            }
          }
        }).start();
      }
    }
  }

}

@Getter
class SocketWrapper implements AutoCloseable {

  private final long id;
  private final Socket socket;
  private final Scanner input;
  private final PrintWriter output;

  private boolean admin = false;

  SocketWrapper(long id, Socket socket) throws IOException {
    this.id = id;
    this.socket = socket;
    this.input = new Scanner(socket.getInputStream());
    this.output = new PrintWriter(socket.getOutputStream(), true);
  }

  @Override
  public void close() throws Exception {
    socket.close();
  }

  @Override
  public String toString() {
    return String.format("%s, %s", socket.getInetAddress().toString(), id);
  }

  public void setAdmin(boolean admin) {
    this.admin = admin;
  }
}
