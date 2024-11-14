package logic.abstractos;
// clase base para todos los objetos que hay en el mundo

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.transform.Rotate;
import logic.interfaz.Mundo;

public abstract class Objeto {
    
    // define en que punto x,y se encuentra
    public float[] posicion = {0f, 0f};
    // define el circulo de colision, todas son circulares
    public float radio = 0f;
    // la direccion en la que apunta el sprite principal, radianes
    public float angulo = 0f;
    
    // constantes para tipo de objeto y depth
    public static final int OBJ_BALDOZA = 0;
    public static final int OBJ_CADAVER = 1;
    public static final int OBJ_BALIN = 2;
    public static final int OBJ_BALA = 3;
    public static final int OBJ_AUTOMATA = 4;
    public static final int OBJ_SOMBRA = 5;
    public static final int OBJ_PLAYER = 6;
    public static final int OBJ_ARBOL = 7;
    public static final int OBJ_BASE = 8;
    // especifica el orden de dibujado
    public int myTipo = -1;
    
    public Objeto(float[] posicion, int myTipo, float radio) {
        this.posicion = posicion;
        this.myTipo = myTipo;
        this.radio = radio;
    }
    
    private static float[] realPos(float[] posicion) {
        return Tools.vecResta(posicion, Mundo.camaraPos);
    }
    
    private static void rotar(GraphicsContext gc, float angulo,
            float[] pivote) {
        Rotate r = new Rotate((angulo + Math.PI / 2f) * (180f / Math.PI),
                pivote[0], pivote[1]);
        gc.setTransform(r.getMxx(), r.getMyx(), r.getMxy(),
                r.getMyy(), r.getTx(), r.getTy());
    }
    
    protected static void drawImagenRot(GraphicsContext gc,
            Image img, float[] posicion, float angulo) {
        gc.save();
        float[] pos = realPos(posicion);
        rotar(gc, angulo, pos);
        gc.drawImage(img,
                pos[0] - img.getWidth() / 2f,
                pos[1] - img.getHeight() / 2f);
        gc.restore();
    }
    
    public static void drawImagen(GraphicsContext gc,
            Image img, float[] posicion) {
        float[] pos = realPos(posicion);
        gc.drawImage(img,
                pos[0] - img.getWidth() / 2f,
                pos[1] - img.getHeight() / 2f);
    }
    
    public abstract void step(float delta);
    
    public abstract void draw(GraphicsContext gc);
}
