package bgu.spl.net.srv;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.api.ConnectionHandler;
import bgu.spl.net.api.Connections;
import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.impl.BGSServer.ConnectionsImp;

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

    public BlockingConnectionHandler(int _id, Socket sock, MessageEncoderDecoder<T> reader, BidiMessagingProtocol<T> protocol, Connections connections) {
        this.sock = sock;
        this.encdec = reader;
        this.protocol = protocol;
        this.connections = connections;
        id = _id;
        ((ConnectionsImp) connections).setActiveMap(id,this);
        protocol.start(id,connections);

    }


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
            }
            close();
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
        if (msg != null)
            try {
                out.write(encdec.encode(msg));
                out.flush();
                if (msg.equals("1003"))
                    close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public void setIn(BufferedInputStream in) {
        this.in = in;
    }

    public void setOut(BufferedOutputStream out) {
        this.out = out;
    }
}
