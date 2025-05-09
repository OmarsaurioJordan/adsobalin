package logic.objetos;
// representa un punto de respawn para entes

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import logic.abstractos.Objeto;
import logic.abstractos.Solido;
import logic.interfaz.Adsobalin;

public class Base extends Solido {
    
    private Image sprite;
    
    // grupo al que pertenece
    public int grupo = Adsobalin.GRU_AZUL;
    
    public Base(float[] posicion) {
        super(posicion, Objeto.OBJ_BASE);
    }
    
    public void setGrupo(int grupo) {
        this.grupo = grupo;
        String bcol = "azules/azul";
        if (grupo == Adsobalin.GRU_ROJO) {
            bcol = "rojos/rojo";
        }
        sprite = new Image("assets/" + bcol + "base.png",
            170f * 0.75f, 170f * 0.75f, false, false);
    }
    
    @Override
    public void step(float delta) {}
    
    @Override
    public void draw(GraphicsContext gc) {
        drawImagen(gc, sprite, posicion);
    }
}
