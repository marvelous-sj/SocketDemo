package udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.UUID;

/**
 * @Description: UDP提供者
 * @Author: Marsj
 * @Create: 2020/5/6 22:11
 */
public class UdpProvider {
    public static void main(String[] args) throws IOException {

        String sn = UUID.randomUUID().toString();
        Provider provider = new Provider(sn);
        provider.start();

        // 读取任意信息则退出
        System.in.read();
        provider.exit();
    }

    public static class Provider extends Thread {
        private final String sn;
        private boolean done = false;
        private DatagramSocket ds = null;

        public Provider(String sn) {
            super();
            this.sn = sn;
        }

        @Override
        public void run() {
            System.out.println("UdpProvider Started!");
            try {
                ds = new DatagramSocket(20000);
                while (!done) {

                    final byte[] buffer = new byte[512];
                    DatagramPacket receiveDatagramPacket = new DatagramPacket(buffer, buffer.length);
                    // 接受回发数据
                    ds.receive(receiveDatagramPacket);

                    String senderIp = receiveDatagramPacket.getAddress().getHostAddress();
                    int senderDataLength = receiveDatagramPacket.getLength();
                    String senderData = new String(receiveDatagramPacket.getData(), 0, senderDataLength);
                    // 此时端口号为指定的端口号
                    int senderPort = MessageCreator.parsePort(senderData);
                    System.out.println(senderPort);
                    if (senderPort == -1) {
                        return;
                    }
                    System.out.println("senderIp = " + senderIp + " senderPort = " + senderPort + " senderData = " + senderData);
                    // 回送消息
                    String responseData = "receive data length:" + senderDataLength;
                    responseData = MessageCreator.buildWithSn(sn);
                    final byte[] responseBuffer = responseData.getBytes();
                    DatagramPacket responseDatagramPacket = new DatagramPacket(
                            responseBuffer, responseBuffer.length, receiveDatagramPacket.getAddress(), senderPort);

                    ds.send(responseDatagramPacket);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                close();
            }
            System.out.println("UdpProvider Finished!");
        }

        public void exit() {
            done = true;
            close();
        }

        public void close() {
            if (ds != null) {
                ds.close();
                ds = null;
            }
        }
    }
}
