package bgu.spl.net.srv;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.MessagingProtocol;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

public class BlockingConnectionHandler<T> implements Runnable, ConnectionHandler<T> {

    private final int id;
    private final BidiMessagingProtocol<T> protocol;
    private final MessageEncoderDecoder<T> encdec;
    private final Socket sock;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private volatile boolean connected = true;
    private Connections<T> connections;
    //private User user;

    public BlockingConnectionHandler(int _id, Socket sock, MessageEncoderDecoder<T> reader, BidiMessagingProtocol<T> protocol, Connections connections) {
        this.sock = sock;
        this.encdec = reader;
        this.protocol = protocol;
        this.connections = connections;
        id = _id;
        ((ConnectionsImp) connections).setActiveMap(id,this);
        protocol.start(id,connections);

    }

    /*
    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
    */


    @Override
    public void run() {
        try (Socket sock = this.sock) {
            int read;
            in = new BufferedInputStream(sock.getInputStream());
            out = new BufferedOutputStream(sock.getOutputStream());
            while (!protocol.shouldTerminate() && connected && (read = in.read()) >= 0) {
                T nextMessage = encdec.decodeNextByte((byte) read);
                if (nextMessage != null) {
                    protocol.process(nextMessage);
                }
                System.out.println("finished process, moving to next read from socket");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void close() throws IOException {
        connected = false;
        sock.close();
    }

    @Override
    public void send(T msg) {
        try {
            if (msg != null) {
                out.write(encdec.encode(msg));
                out.flush();
                System.out.println(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
