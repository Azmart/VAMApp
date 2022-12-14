package Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;


public class ClientServer {
    //    ArrayList<Socket> socketList = new ArrayList<Socket>();
    //users socket hashmap
    HashMap<String, Socket> socketList = new HashMap<>();
    HashMap<Socket, String> socketListReverse = new HashMap<>();
    //user's hashmap -> DB
    static HashMap<String, String> userDB = new HashMap<>();

    //form userReceiver: [userSender,fileName]
    HashMap<String, String[]> offlineMessages = new HashMap<>();

    public void print(String str, Object... o) {
        System.out.printf(str, o);
    }

    public ClientServer(int port) throws IOException {
        ServerSocket srvSocket = new ServerSocket(port);

        while(true) {
            print("Listening at port %d...\n", port);
            Socket clientSocket = srvSocket.accept();

            Thread t = new Thread(()-> {
                try {
                    serve(clientSocket);
                } catch (IOException ex) {
                    print("Connection drop ", socketListReverse.get(clientSocket));
                }

                synchronized (socketList) {
                    socketList.remove(socketListReverse.get(clientSocket));
                    socketListReverse.remove(clientSocket);
                }
            });
            t.start();
        }
    }

    private void serve(Socket clientSocket) throws IOException {
        byte[] buffer = new byte[1024];
        print("Established a connection to host %s:%d\n\n",
                clientSocket.getInetAddress(), clientSocket.getPort());

        DataInputStream in = new DataInputStream(clientSocket.getInputStream());
        DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
        if(authenticate(in, out, clientSocket)){
            //send list of users in database
            sendListOfUsers(getListOfUsers(), out);
            //check if we store offlineMessages for this user
            if(offlineMessages.containsKey(socketListReverse.get(clientSocket))){
                print(socketListReverse.get(clientSocket)+" online; has unread message\n");
                sendOfflineMessage(clientSocket);
            }
            while(true){
                //read from buffer receiver name and message
                String userReceiver = readingFromBufferUsernMessage(in, buffer);
                String msg = readingFromBufferUsernMessage(in, buffer);;
                //check if user online
                if(socketList.containsKey(userReceiver)){
                    //user online -> send messages to receiver
                    forward(socketListReverse.get(clientSocket), userReceiver);
                    forward(msg, userReceiver);
                }else{
                    //user offline -> store message in file
                    print(userReceiver+" offline: started writing to file\n");
                    offlineStoreInFile(msg.length(), buffer, userReceiver, socketListReverse.get(clientSocket));
                }


            }
        }
    }

    private String readingFromBufferUsernMessage(DataInputStream in, byte[] buffer) throws IOException {
        int size = in.readInt();
        String msg = "";
        while(size > 0) {
            int len = in.read(buffer, 0, Math.min(size, buffer.length));
            msg += new String(buffer, 0, len);
            size -= len;
        }
        return msg;
    }

    private void sendOfflineMessage(Socket clientSocket) throws IOException {
        String userReceiver = socketListReverse.get(clientSocket);
        String userSender = offlineMessages.get(userReceiver)[0];
        String fileName = offlineMessages.get(userReceiver)[1];
        String text = readFromFile(fileName);
        //delete file and from hashmap
        new File(fileName).delete();
        offlineMessages.remove(userReceiver);
        //send message to receiver
        forward(userSender, userReceiver);
        forward(text, userReceiver);
    }

    public boolean authenticate(DataInputStream in, DataOutputStream out, Socket clientSocket) throws IOException {
        //getting user
        String user = "";
        String password = "";
        byte[] buffer = new byte[1024];
        int size = in.readInt();
        while(size > 0) {
            int len = in.read(buffer, 0, Math.min(size, buffer.length));
            user += new String(buffer, 0, len);
            size -= len;
        }
        buffer = new byte[1024];
        size = in.readInt();
        while(size > 0) {
            int len = in.read(buffer, 0, Math.min(size, buffer.length));
            password += new String(buffer, 0, len);
            size -= len;
        }
        //check if there's user in DB and check password of user
        String msg = "";
        Boolean inDB = false;
        if(userDB.containsKey(user) && userDB.get(user).equals(password)){
            inDB = true;
            msg = "Y";
            synchronized (socketList) {
                socketList.put(user,clientSocket);
                socketListReverse.put(clientSocket, user);
            }
        }else{
            msg = "N";
        }
        try {
            out.writeInt(msg.length());
            out.write(msg.getBytes(), 0, msg.length());
        } catch (IOException ex) {
            print("Unable to forward message\n");
        }
        return inDB;
    }

    private void offlineStoreInFile(int len, byte[] buffer,String userReceiver, String userSender) throws IOException {
        String fileName = new SimpleDateFormat("yyyyMMddHHmm'.txt'").format(new Date());
        File file = new File(userReceiver+fileName);
        FileOutputStream out = new FileOutputStream(file, false);
        out.write(buffer, 0, len);
        out.flush();
        out.close();
        //store filename in hashmap
        String[] insideArray = {userSender, userReceiver+fileName};
        offlineMessages.put(userReceiver,insideArray);
    }

    private void forward(String msg, String user){
        synchronized (socketList) {
            Socket socket = socketList.get(user);
            try {
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                out.writeInt(msg.length());
                out.write(msg.getBytes(), 0, msg.length());
            } catch (IOException ex) {
                print("Unable to forward message to %s:%d\n",
                        socket.getInetAddress().getHostName(), socket.getPort());
            }
        }
    }

    public String readFromFile(String filename) throws IOException {
        byte[] buffer = new byte[1024];
        String str = "";
        File file = new File(filename);
        long size = file.length();
        FileInputStream in = new FileInputStream(file);
        while (size > 0) {
            int len = in.read(buffer, 0, buffer.length);
            size -= len;
            str += new String(buffer, 0, len);
        }
        in.close();
        return str;
    }

    private void sendListOfUsers(String msg, DataOutputStream out) throws IOException {
        out.writeInt(msg.length());
        out.write(msg.getBytes(), 0, msg.length());
    }

    private String getListOfUsers(){
        String list = "";
        for ( String key : userDB.keySet() ) {
            list+=key+",";
        }
        StringBuffer sb= new StringBuffer(list);
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }



    public static void main(String[] args) throws IOException {
        userDB.put("ali","12345");
        userDB.put("bula","qwerty");
        userDB.put("vrus","qwerty");
        userDB.put("atom","qwerty");
        userDB.put("sand","qwerty");
        userDB.put("page","qwerty");
        int port = Integer.parseInt("12345");
        new ClientServer(port);
    }
}