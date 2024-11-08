package logic.sincronia;
// depura los mensajes recibidos

import java.nio.ByteBuffer;
import javafx.stage.Stage;
import logic.interfaz.Adsobalin;
import logic.interfaz.GUIs;
import logic.interfaz.Lobby;

public class Recepciones {
    
    // nodo base de todo el software
    protected Stage raiz;
    
    public Recepciones(Stage raiz) {
        this.raiz = raiz;
    }
    
    public void depuraMsj(ByteBuffer data, String emisor) {
        try {
            if (data.getInt() == Conector.SOFT_ID) {
                byte tipo = data.get();
                switch (tipo) {
                    
                    case Envios.MSJ_HOLA:
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
                        if (!Adsobalin.isServer) {
                            recMsj(data.get());
                        }
                        break;
                }
            }
        }
        catch (Exception ex) {}
    }
    
    private void recHola(int version, int estilo, int grupo,
            String nombre, String emisor) {
        if (version != Adsobalin.VERSION) {
            Envios.sendMsj(Envios.SUB_VERSION, emisor);
        }
        else if (Adsobalin.userContIP(emisor)) {
            if (grupo != Adsobalin.userGetGrupo(emisor)) {
                // Tarea
            }
        }
        else if (!Adsobalin.userHayCupo()) {
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
            // el mensaje welcome es para poner al cliente activo y
            // en escucha, pero el servidor en este punto ya comenzara
            // a enviar rafagas de mensajes para la sincronia
            int ind = Adsobalin.userAdd(emisor, nombre, estilo, grupo);
            Envios.sendWelcome(nombre, emisor, estilo, grupo, ind);
        }
    }
    
    private void recWelcome(int ind, int estilo, int grupo,
            String nombre, String emisor) {
        // reafirma que los datos del jugador son los dados por el servidor
        Adsobalin.indice = ind;
        Adsobalin.estilo = estilo;
        Adsobalin.grupo = grupo;
        Adsobalin.nombre = nombre;
        // al haber servidor asociado el cliente oira y enviara rafagas
        Adsobalin.myServer = emisor;
        // esto es para evitar estar conectado y en el menu principal a la vez
        // pero luego puede que se cambie la interfaz con la sincronia
        if (Adsobalin.estado == Adsobalin.EST_MENU) {
            raiz.setScene(new Lobby(raiz, false));
        }
    }
    
    private void recMsj(byte submsj) {
        if (Adsobalin.estado == Adsobalin.EST_MENU ||
                Adsobalin.estado == Adsobalin.EST_LOBBY) {
            GUIs gui = (GUIs)raiz.getScene();
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
        }
    }
}
