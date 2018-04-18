package com.droid.sxbot;

import com.droid.sxbot.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
/**
 * Created by lisongting on 2018/4/9.
 */
public class UpLoadTask implements Runnable{

    private Socket serverSocket;
    private String file;
    private OutputStream outputStream;
    private OnCompleteListener listener;
    private String ip;
    private int port;

    public interface OnCompleteListener{
        void onComplete();

        void onError(String string);
    }

    public UpLoadTask(String ip,int port,String file) {
        serverSocket = new Socket();
        this.file = file;
        this.ip = ip;
        this.port = port;
    }

    public UpLoadTask(String ip,int port,String file,OnCompleteListener listener) {
        this.listener = listener;
        serverSocket = new Socket();
        this.file = file;
        this.ip = ip;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            serverSocket.connect(new InetSocketAddress(ip, port), 3000);
        }catch (SocketTimeoutException e) {
            if (listener != null) {
                listener.onError("timeout");
            }
        } catch (IOException e) {
            if (listener != null) {
                listener.onError("IOException");
            }
            e.printStackTrace();
        }
        File fileResource = new File(file);
        String fileName = null;
        FileInputStream fis = null;

        if(!fileResource.exists()) {
            log("file is not exists");
            return;
        }
        try {
            outputStream = serverSocket.getOutputStream();
            fileName = URLEncoder.encode(fileResource.getName(), "utf-8");
            String fileNameLenBinary = Integer.toBinaryString(fileName.length());
            StringBuilder sb = new StringBuilder();
            int numZero = 16 - fileNameLenBinary.length();
            for(int i=0;i<numZero;i++) {
                sb.append('0');
            }
            sb.append(fileNameLenBinary);
            byte[] fileNameLenBytes = sb.toString().getBytes("utf-8");
            outputStream.write(fileNameLenBytes);
//			log("fileNameLengthBinary bytes :"+Arrays.toString(fileNameLenBytes));
            byte[] fileNameBytes = fileName.getBytes("utf-8");
            outputStream.write(fileNameBytes);
//			log("fileNameBytes:"+Arrays.toString(fileNameBytes));

            if(!fileResource.canRead()) {
                log("read file failed");
                return;
            }

            fis = new FileInputStream(file);
            byte[] bytes = new byte[1024];
            long total = file.length();
            int len;
            long current = 0;
            while((len=fis.read(bytes))!=-1) {
                outputStream.write(bytes,0,len);
                current +=len;
            }
            Util.close(serverSocket,outputStream,fis);
            if (listener != null) {
                listener.onComplete();
            }
            log("upload: "+fileResource.getName()+" -- success !");
        } catch (Exception e1) {
            Util.close(serverSocket,outputStream,fis);
            if (listener != null) {
                listener.onError("Exception");

            }
            e1.printStackTrace();
        }

    }

    private static void log(String s) {
        System.out.println(s);
    }
}
