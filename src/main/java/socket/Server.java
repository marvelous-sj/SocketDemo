package socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @Description:
 * @Author: sj
 * @Create: 2018-12-01 11:23
 **/
public class Server {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket =new ServerSocket(5555);
        System.out.println("服务器已经启动");
        System.out.println("服务端信息:"+serverSocket.getInetAddress()+"端口:"+serverSocket.getLocalPort());
        System.out.println("服务端信息:"+serverSocket.getLocalSocketAddress()+"端口:"+serverSocket.getLocalPort());
        while (true) {
            //客户端连接
            Socket accept = serverSocket.accept();
            //多线程处理客户端请求
            ClientHandler clientHandler = new ClientHandler(accept);
            clientHandler.start();
        }

    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private boolean flag=true;
        ClientHandler(Socket socket){
            this.socket=socket;
        }

        @Override
        public void run() {
            System.out.println("新客户端连接了~ 信息:"+socket.getInetAddress()+"端口:"+socket.getPort());
            try {
                //输出数据，返回客户端
                PrintStream printStream = new PrintStream(socket.getOutputStream());
                //接受客户端数据
                BufferedReader socketBufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                do {
                    //读取一行数据，发送到服务器
                    String string = socketBufferedReader.readLine();
                    if ("bye".equalsIgnoreCase(string)){
                        flag = false;
                        printStream.println("bye");
                    }
                    else{
                        System.out.println(string);
                        printStream.println("回送"+string.length());
                    }

                }while (flag);
                printStream.close();
                socketBufferedReader.close();
            } catch (IOException e) {
                System.out.println("连接异常！");
            }finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("客户端已经退出！");
        }
    }
}
