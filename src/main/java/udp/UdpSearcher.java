package udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;


/**
 * @Description: UDP搜索者
 * @Author: Marsj
 * @Create: 2020/5/6 22:11
 */
public class UdpSearcher {
    private static final int LISTEN_PORT = 30000;
    public static Listener listen() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Listener listener = new Listener(LISTEN_PORT, countDownLatch);
        listener.start();
        countDownLatch.await();
        return listener;
    }
    public static void sendBroadcast() throws IOException{
        System.out.println("UdpSearcher SendBroadcast start");
        // 搜索方无需指定端口，系统分配
        DatagramSocket datagramSocket = new DatagramSocket();
        // 构建数据
        final byte[] buffer = MessageCreator.buildWithPort(LISTEN_PORT).getBytes();
        // 广播发送
        DatagramPacket receiveDatagramPacket = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("255.255.255.255"), 20000);
        datagramSocket.send(receiveDatagramPacket);

        System.out.println("UdpSearcher SendBroadcast end");
        // 关闭
        datagramSocket.close();
    }
    public static void main(String[] args) throws Exception {
        Listener listen = listen();
        sendBroadcast();

        // 读取任意信息则退出
        System.in.read();
        List<Device> devices = listen.getDeviceAndClose();
        for (Device device: devices) {
            System.out.println(device.toString());
        }
    }
    private static class Device {
        final int port;
        final String ip;
        final String sn;

        public Device(int port, String ip, String sn) {
            this.port = port;
            this.ip = ip;
            this.sn = sn;
        }

        @Override
        public String toString() {
            return "Device{" +
                    "port=" + port +
                    ", ip='" + ip + '\'' +
                    ", sn='" + sn + '\'' +
                    '}';
        }

    }
    private static class Listener extends Thread{
        private final int listenPort;
        private final CountDownLatch countDownLatch;
        private final List<Device> list = new ArrayList<>();
        private boolean done = false;
        private DatagramSocket ds = null;
        public Listener(int listenPort,CountDownLatch countDownLatch){
            super();
            this.listenPort = listenPort;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            System.out.println("UdpSearcher Started!");
            countDownLatch.countDown();
            try {
                ds = new DatagramSocket(listenPort);
                while(!done){
                    final byte[] buffer = new byte[512];
                    DatagramPacket receiveDatagramPacket = new DatagramPacket(buffer, buffer.length);
                    // 接收
                    ds.receive(receiveDatagramPacket);

                    String ip = receiveDatagramPacket.getAddress().getHostAddress();
                    int port = receiveDatagramPacket.getPort();
                    int dataLength = receiveDatagramPacket.getLength();
                    String data = new String(receiveDatagramPacket.getData(), 0, dataLength);
                    System.out.println("UdpSearcher receive From ip = " + ip + " port = " + port + " data = " + data);

                    String sn = MessageCreator.parseSn(data);
                    if(sn!= null){
                        Device device = new Device(port, ip, sn);
                        list.add(device);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                close();
            }
            System.out.println("UdpSearcher End!");
        }

        public List<Device> getDeviceAndClose(){
            done = true;
            close();
            return list;
        }

        public void close() {
            if (ds != null) {
                ds.close();
                ds = null;
            }
        }
    }
}
