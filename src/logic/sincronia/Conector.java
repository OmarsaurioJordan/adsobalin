package logic.sincronia;
// maneja las comunicaciones y sincronizacion

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import javafx.stage.Stage;
import logic.interfaz.Adsobalin;

public class Conector {
    
    // identificador unico del software para sus mensajes UDP
    public static final int SOFT_ID = 69750244;
    // este debug es para enviar datos a puerto + 1
    private static final boolean PORT_DEBUG = true;
    // talla del buffer de recepcion, ajustar al minimo necesario
    private static final int LIM_LEC_BUF = 2048;
    // tiempo tras el cual desconectar usuarios inactivos
    public static final float PING = 5f;
    
    // informacion de usuarios conectados si es servidor
    // el id es el indice del arreglo
    // el grupo depende de si es id < 9 o no
    public static String[] userIP = new String[18];
    public static String[] userName = new String[18];
    public static int[] userStyle = new int[18];
    public static float[] userPing = new float[18];
    
    // nodo base de todo el software
    private Stage raiz;
    // conexion donde se envian y reciben datos
    private static DatagramSocket socket;
    // todos los jugadores usaran el mismo puerto
    private static int puerto;
    
    public Conector(int puerto) {
        this.puerto = puerto;
        this.raiz = raiz;
        try {
            socket = new DatagramSocket(puerto);
        }
        catch (Exception ex) {
            socket = null;
        }
        // inicializar los datos de conexiones a servidor
        Adsobalin.userClean();
    }
    
    public void setEscuchar(Stage raiz) {
        // si pudo abrir el socket, creara un hilo para
        // recibir los mensajes entrantes, el demon lo auto cierra
        this.raiz = raiz;
        if (isSocketOk()) {
            Thread receptor = new Thread(this::recibeMsj);
            receptor.setDaemon(true);
            receptor.start();
        }
    }
    
    public boolean isSocketOk() {
        return socket != null;
    }
    
    public static boolean enviaMsj(byte[] data, String destino) {
        try {
            InetAddress dir = InetAddress.getByName(destino);
            DatagramPacket pack;
            if (PORT_DEBUG) {
                pack = new DatagramPacket(
                    data, data.length, dir, puerto + 1);
            }
            else {
                pack = new DatagramPacket(
                    data, data.length, dir, puerto);
            }
            socket.send(pack);
        }
        catch (Exception ex) {
            return false;
        }
        return true;
    }
    
    private void recibeMsj() {
        Recepciones recividor = new Recepciones(raiz);
        byte[] buff = new byte[LIM_LEC_BUF];
        while (true) {
            try {
                DatagramPacket pack = new DatagramPacket(
                        buff, LIM_LEC_BUF);
                socket.receive(pack);
                String ip = pack.getAddress().toString();
                recividor.depuraMsj(arr2buf(pack.getData()), ip);
            }
            catch (Exception ex) {}
        }
    }
    
    public static byte[] buf2arr(ByteBuffer buff) {
        // la posicion del buffer debe estar al final de los datos
        // finalmente posicion y limite quedaran igual que posicion inicial
        byte[] data = new byte[buff.position()];
        buff.flip();
        buff.get(data);
        return data;
    }
    
    public static ByteBuffer arr2buf(byte[] data) {
        // el nuevo buffer estara listo para leerse en posicion 0
        return ByteBuffer.wrap(data);
    }
    
    public static void buffPutString(ByteBuffer buff, String texto) {
        char c;
        for (int i = 0; i < texto.length(); i++) {
            c = texto.charAt(i);
            buff.put((byte)c);
        }
        buff.put((byte)0);
    }
    
    public static String buffGetString(ByteBuffer buff) {
        String txt = "";
        try {
            byte b = buff.get();
            while (b != 0) {
                txt += (char)b;
                b = buff.get();
            }
        }
        catch (Exception ex) {
            txt = "";
        }
        return txt;
    }
    
    public static ByteBuffer newBuffer(byte idMsj, int talla) {
        // la talla involucra solo a los datos externos, no el header
        ByteBuffer buff = ByteBuffer.allocate(
                Integer.BYTES + 1 + talla);
        buff.putInt(SOFT_ID);
        buff.put(idMsj);
        return buff;
    }
}
