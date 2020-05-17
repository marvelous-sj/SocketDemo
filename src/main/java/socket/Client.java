package socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @Description:
 * @Author: sj
 * @Create: 2018-12-01 11:23
 **/
public class Client {
    public static void main(String[] args) throws IOException {
        Socket socket=new Socket();
        socket.setSoTimeout(3000);
        //连接本地
        socket.connect(new InetSocketAddress(InetAddress.getLocalHost(),5555),3000);
        System.out.println("发起服务器连接，等待后序流程");
        System.out.println("客户端信息:"+socket.getLocalAddress()+"端口:"+socket.getLocalPort());
        System.out.println("服务端信息:"+socket.getInetAddress()+"端口:"+socket.getPort());
        try{
            doSomeThing(socket);
        }catch (Exception e){
            System.out.println("客户端连接异常");
        }
        socket.close();
        System.out.println("客户端已退出");
    }

    private static void doSomeThing(Socket socket) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        //得到socket输出流，转换为打印流
        PrintStream printStream = new PrintStream(socket.getOutputStream());
        //得到socket输入流，转换为BufferReader
        BufferedReader socketBufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        boolean flag=true;
        do {
            //读取一行数据，发送到服务器
            String string = bufferedReader.readLine();
            printStream.println(string);
            //获取服务端响应数据
            String echo = socketBufferedReader.readLine();
            if ("bye".equalsIgnoreCase(echo)) {
                flag = false;
            } else {
                System.out.println(echo);
            }
        }while (flag);
        //关闭流
        printStream.close();
        socketBufferedReader.close();
    }
}
