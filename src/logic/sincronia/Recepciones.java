package logic.sincronia;
// depura los mensajes recibidos

import java.nio.ByteBuffer;
import javafx.stage.Stage;
import logic.interfaz.Adsobalin;
import logic.interfaz.GUIs;
import logic.interfaz.Lobby;
import logic.interfaz.Menu;
import javafx.application.Platform;
import logic.interfaz.Mundo;
import logic.interfaz.Resultado;
import logic.objetos.Arbol;
import logic.objetos.Cadaver;

public class Recepciones {
    
    // nodo base de todo el software
    protected Stage raiz;
    
    // lectura de clientes al ping del servidor
    private static byte server_orden = 0;
    
    public Recepciones(Stage raiz) {
        this.raiz = raiz;
    }
    
    public void depuraMsj(ByteBuffer data, String emisor) {
        try {
            if (data.getInt() == Conector.SOFT_ID) {
                byte orden;
                byte tipo = data.get();
                switch (tipo) {
                    
                    case Envios.MSJ_HOLA:
                        // solo el servidor recibe este tipo de mensaje
                        if (Adsobalin.isServer) {
                            recHola(
                                data.getShort(), // version
                                data.get(), // estilo
                                data.get(), // grupo
                                Conector.buffGetString(data),
                                emisor
                            );
                        }
                        break;
                    
                    case Envios.MSJ_WELCOME:
                        // es cliente y da igual en que estado este
                        if (!Adsobalin.isServer) {
                            recWelcome(
                                data.get(), // ind
                                data.get(), // estilo
                                data.get(), // grupo
                                Conector.buffGetString(data),
                                emisor
                            );
                        }
                        break;
                    
                    case Envios.MSJ_MSJ:
                        // es cliente y da igual en que estado este
                        if (!Adsobalin.isServer) {
                            recMsj(data.get());
                        }
                        break;
                    
                    case Envios.MSJ_LOBBY:
                        // es cliente y tiene un servidor asociado
                        if (!Adsobalin.isServer &&
                                !Conector.myServer.isEmpty()) {
                            Conector.serverPing = Conector.PING;
                            if (apruebaServerPing(data.get())) {
                                byte talla = data.get();
                                byte obstaculos = data.get();
                                byte tiempo = data.get();
                                byte encursable = data.get();
                                byte[] npcs = new byte[18];
                                String[] nombres = new String[18];
                                for (int i = 0; i < 18; i++) {
                                    npcs[i] = data.get();
                                    nombres[i] = Conector.buffGetString(data);
                                }
                                recLobby(
                                        talla, obstaculos, tiempo,
                                        encursable, npcs, nombres);
                            }
                        }
                        break;
                    
                    case Envios.MSJ_NPC:
                        // es cliente y tiene un servidor asociado
                        if (!Adsobalin.isServer &&
                                !Conector.myServer.isEmpty()) {
                            Conector.serverPing = Conector.PING;
                            if (apruebaServerPing(data.get())) {
                                float tiempo = data.getFloat();
                                int radioMundial = data.getInt();
                                Adsobalin.gruPoints[0] = data.getInt();
                                Adsobalin.gruPoints[1] = data.getInt();
                                for (int i = 0; i < 18; i++) {
                                    Adsobalin.userPoints[i] = data.getInt();
                                }
                                byte bestInd = data.get();
                                String bestName = Conector.buffGetString(data);
                                if (bestInd != -1) {
                                    Adsobalin.userName[bestInd] = bestName;
                                }
                                if (recNPC(tiempo, radioMundial)) {
                                    try {
                                        Mundo mun = (Mundo)raiz.getScene();
                                        float[] pos = {0f, 0f};
                                        float ang;
                                        byte hit, inmune;
                                        for (int i = 0; i < 18; i++) {
                                            pos[0] = data.getFloat();
                                            pos[1] = data.getFloat();
                                            ang = data.getFloat();
                                            hit = data.get();
                                            inmune = data.get();
                                            mun.setNPC(i, pos, ang,
                                                    hit, inmune);
                                        }
                                    }
                                    catch (Exception e) {}
                                }
                            }
                        }
                        break;
                    
                    case Envios.MSJ_PLANO:
                        if (Adsobalin.isServer) {
                            Envios.sendMundo(raiz, emisor);
                        }
                        break;
                    
                    case Envios.MSJ_MUNDO:
                        // es cliente y tiene un servidor asociado
                        if (!Adsobalin.isServer &&
                                !Conector.myServer.isEmpty()) {
                            Mundo mun = null;
                            try {
                                mun = (Mundo)raiz.getScene();
                            }
                            catch (Exception e) {
                                mun = null;
                            }
                            if (mun != null) {
                                if (mun.temp_get_mapa != -1) {
                                    mun.temp_get_mapa = -1f;
                                    // poner arboles obtenidos
                                    int tot = data.getInt();
                                    float[] posss = {0f, 0f};
                                    for (int i = 0; i < tot; i++) {
                                        posss[0] = data.getFloat();
                                        posss[1] = data.getFloat();
                                        mun.newObjeto(Arbol.class,
                                                posss.clone());
                                    }
                                    // poner cadaveres obtenidos
                                    tot = data.getInt();
                                    Cadaver cdv;
                                    for (int i = 0; i < tot; i++) {
                                        posss[0] = data.getFloat();
                                        posss[1] = data.getFloat();
                                        cdv = (Cadaver)mun.newObjeto(
                                                Cadaver.class, posss.clone());
                                        posss[0] = data.getFloat(); // angulo
                                        posss[1] = data.getFloat(); // info
                                        cdv.setInfo(posss[1], posss[0], true);
                                    }
                                }
                            }
                        }
                        break;
                    
                    case Envios.MSJ_RESULT:
                        // es cliente y tiene un servidor asociado
                        if (!Adsobalin.isServer &&
                                !Conector.myServer.isEmpty()) {
                            Conector.serverPing = Conector.PING;
                            if (apruebaServerPing(data.get())) {
                                String txt = Conector.buffGetString(data);
                                recResultado(txt);
                            }
                        }
                        break;
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private void recHola(int version, int estilo, int grupo,
            String nombre, String emisor) {
        if (version != Adsobalin.VERSION) {
            Envios.sendMsj(Envios.SUB_VERSION, emisor);
        }
        else if (Adsobalin.userGetInd(emisor) != -1) {
            int ind = Adsobalin.userGetInd(emisor);
            if (Adsobalin.estado == Adsobalin.EST_JUEGO) {
                // no permitira actualizacion, solo le da permiso de ingreso
                Envios.sendWelcome(Adsobalin.userName[ind], emisor,
                        Adsobalin.userStyle[ind],
                        Adsobalin.userGetGrupo(ind), ind);
            }
            else {
                // evitar que se dupliquen estilos o nombres, no modificara
                if (Adsobalin.userContEstilo(estilo)) {
                    estilo = Adsobalin.userStyle[ind];
                }
                if (Adsobalin.userContNombre(nombre)) {
                    nombre = Adsobalin.userName[ind];
                }
                // primero se eliminan los datos del usuario
                Adsobalin.userClean(ind);
                // se agrega el nuevo usuario para actualizarlo
                newUser(estilo, grupo, nombre, emisor);
            }
        }
        else if (Adsobalin.userHayCupo() == -1) {
            Envios.sendMsj(Envios.SUB_CUPO, emisor);
        }
        else if (!(Adsobalin.estado == Adsobalin.EST_LOBBY ||
                (Adsobalin.estado == Adsobalin.EST_JUEGO &&
                Adsobalin.isEncursable))) {
            Envios.sendMsj(Envios.SUB_ENCURSO, emisor);
        }
        else if (Adsobalin.userContEstilo(estilo)) {
            Envios.sendMsj(Envios.SUB_ESTILO, emisor);
        }
        else if (Adsobalin.userContNombre(nombre)) {
            Envios.sendMsj(Envios.SUB_NOMBRE, emisor);
        }
        else {
            // se agrega el nuevo usuario a la tabla de usuarios
            newUser(estilo, grupo, nombre, emisor);
        }
    }
    
    private void newUser(int estilo, int grupo,
            String nombre, String emisor) {
        // el mensaje welcome es para poner al cliente activo y
        // en escucha, pero el servidor en este punto ya comenzara
        // a enviar rafagas de mensajes para la sincronia
        int ind = Adsobalin.userAdd(emisor, nombre, estilo, grupo);
        int newGrupo = Adsobalin.userGetGrupo(ind);
        Envios.sendWelcome(nombre, emisor, estilo, newGrupo, ind);
        // el servidor redibuja la lista de conectados
        if (Adsobalin.estado == Adsobalin.EST_LOBBY) {
            Lobby gui = (Lobby)raiz.getScene();
            Platform.runLater(() -> {
                gui.reDibujar();
            });
        }
    }
    
    private void recWelcome(int ind, int estilo, int grupo,
            String nombre, String emisor) {
        if (Adsobalin.estado == Adsobalin.EST_MENU) {
            // afirma que los datos del jugador son los dados por el servidor
            Adsobalin.indice = ind;
            Adsobalin.estilo = estilo;
            Adsobalin.grupo = grupo;
            Adsobalin.nombre = nombre;
            // al haber servidor asociado el cliente oira y enviara rafagas
            Conector.myServer = emisor;
            // para evitar estar conectado y en el menu principal a la vez
            // pero luego puede que se cambie la interfaz con la sincronia
            Adsobalin.isServer = false;
            Menu gui = (Menu)raiz.getScene();
            gui.guardarDatos();
            Platform.runLater(() -> {
                raiz.setScene(new Lobby(raiz));
            });
        }
    }
    
    private void recMsj(byte submsj) {
        if (Adsobalin.estado == Adsobalin.EST_MENU ||
                Adsobalin.estado == Adsobalin.EST_LOBBY) {
            GUIs gui = (GUIs)raiz.getScene();
            Platform.runLater(() -> {
                switch (submsj) {
                    case Envios.SUB_CUPO:
                        gui.setMensaje("no hay cupo", false);
                        break;
                    case Envios.SUB_ENCURSO:
                        gui.setMensaje("partida en curso", false);
                        break;
                    case Envios.SUB_ESTILO:
                        gui.setMensaje("escoja otro estilo", false);
                        break;
                    case Envios.SUB_NOMBRE:
                        gui.setMensaje("escriba otro nombre", false);
                        break;
                    case Envios.SUB_VERSION:
                        gui.setMensaje("la versión es diferente", false);
                        break;
                }
            });
        }
    }
    
    private void recLobby(byte talla, byte obstaculos, byte tiempo,
            byte encursable, byte[] npcs, String[] nombres) {
        if (Adsobalin.estado != Adsobalin.EST_LOBBY) {
            Platform.runLater(() -> {
                raiz.setScene(new Lobby(raiz));
            });
        }
        else {
            Platform.runLater(() -> {
                Lobby lob = (Lobby)raiz.getScene();
                lob.setDatos(talla, obstaculos, tiempo,
                        encursable, npcs, nombres);
            });
        }
    }
    
    private void recResultado(String txt) {
        if (Adsobalin.estado != Adsobalin.EST_FINAL) {
            Platform.runLater(() -> {
                raiz.setScene(new Resultado(raiz));
            });
        }
        else {
            Platform.runLater(() -> {
                Resultado res = (Resultado)raiz.getScene();
                res.setAllData(txt);
            });
        }
    }
    
    private boolean recNPC(float tiempo, int radioMundial) {
        if (Adsobalin.estado != Adsobalin.EST_JUEGO) {
            Platform.runLater(() -> {
                raiz.setScene(new Mundo(raiz, null, radioMundial, 0, 1));
            });
            return false;
        }
        else {
            Mundo.tiempoRestante = tiempo;
        }
        return true;
    }
    
    private boolean apruebaServerPing(byte newOrden) {
        if (newOrden > server_orden || (server_orden - newOrden) > 127) {
            server_orden = newOrden;
            return true;
        }
        return false;
    }
}
