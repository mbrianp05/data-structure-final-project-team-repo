package utils;

import java.awt.image.BufferedImage;

public class ResourceManager {
    public static BufferedImage player;// imagen del jugador
    // jugador diablo
    public static BufferedImage devil1D;
    public static BufferedImage devil2D;
    public static BufferedImage devil3D;
    public static BufferedImage devil1I;
    public static BufferedImage devil2I;
    public static BufferedImage devil3I;

    // slime
    public static BufferedImage slime1;
    public static BufferedImage slime2;
    public static BufferedImage slime3;
    public static BufferedImage slime4;
    public static BufferedImage slime5;
    // esqueleto
    public static BufferedImage esq1;
    public static BufferedImage esq2;
    public static BufferedImage esq3;

    // Goblin
    public static BufferedImage gob1;
    public static BufferedImage gob2;
    public static BufferedImage gob3;

    // murcielago
    public static BufferedImage bat1;
    public static BufferedImage bat2;
    public static BufferedImage bat3;

    // Enemigo verde
    public static BufferedImage em1;
    public static BufferedImage em2;
    public static BufferedImage em3;

    // cristales
    public static BufferedImage c1;
    public static BufferedImage c2;
    public static BufferedImage c3;
    public static BufferedImage c4;
    public static BufferedImage c5;
    // llave
    public static BufferedImage key;
    // puertafinal
    public static BufferedImage finaldoor;
    // portal
    public static BufferedImage p1;
    public static BufferedImage p2;
    public static BufferedImage p3;
    public static BufferedImage p4;
    public static BufferedImage p5;
    // suelos
    public static BufferedImage floor;
    // parded
    public static BufferedImage wall;

    public static BufferedImage boss1;
    public static BufferedImage boss2;
    public static BufferedImage boss3;

    // poderes
    public static BufferedImage pw3;
    public static BufferedImage pw2;
    public static BufferedImage pw1;
    public static BufferedImage pw4;

    public static BufferedImage power1;
    public static BufferedImage power2;
    public static BufferedImage power3;
    public static BufferedImage power4;
    public static BufferedImage power5;
    public static BufferedImage power6;
    public static BufferedImage power7;
    public static BufferedImage power8;

    // menu
    public static BufferedImage iniciar;
    public static BufferedImage iniciar1;
    public static BufferedImage fondo1;
    public static BufferedImage salir;
    public static BufferedImage salir1;
    public static BufferedImage reanudar;
    public static BufferedImage reanudar1;
    public static BufferedImage reintentar;
    public static BufferedImage reintentar1;
    public static BufferedImage victoryPanel;
    public static BufferedImage lostPanel;
    public static BufferedImage menuPrincipal1;
    public static BufferedImage getMenuPrincipal2;

    public static void init() {
        player = Loader.CargadorImagenes("/Assets/img.png");
        slime2 = Loader.CargadorImagenes("/Assets/slime2.png");
        slime3 = Loader.CargadorImagenes("/Assets/slime3.png");
        slime4 = Loader.CargadorImagenes("/Assets/slime4.png");
        slime5 = Loader.CargadorImagenes("/Assets/slime5.png");
        c1 = Loader.CargadorImagenes("/Assets/c1.png");
        c2 = Loader.CargadorImagenes("/Assets/c2.png");
        c3 = Loader.CargadorImagenes("/Assets/c3.png");
        c4 = Loader.CargadorImagenes("/Assets/c4.png");
        c5 = Loader.CargadorImagenes("/Assets/c5.png");
        key = Loader.CargadorImagenes("/Assets/key.png");
        p1 = Loader.CargadorImagenes("/Assets/door .png");
        p2 = Loader.CargadorImagenes("/Assets/p2.png");
        p3 = Loader.CargadorImagenes("/Assets/p3.png");
        p4 = Loader.CargadorImagenes("/Assets/p4.png");
        p5 = Loader.CargadorImagenes("/Assets/p5.png");
        floor = Loader.CargadorImagenes("/Assets/floor.png");
        finaldoor = Loader.CargadorImagenes("/Assets/finaldoor.png");
        esq1 = Loader.CargadorImagenes("/Assets/esq1.png");
        esq2 = Loader.CargadorImagenes("/Assets/esq2.png");
        esq3 = Loader.CargadorImagenes("/Assets/esq3.png");
        gob1 = Loader.CargadorImagenes("/Assets/gob1.png");
        gob2 = Loader.CargadorImagenes("/Assets/gob2.png");
        gob3 = Loader.CargadorImagenes("/Assets/gob3.png");
        devil1D = Loader.CargadorImagenes("/Assets/devil1D.png");
        devil2D = Loader.CargadorImagenes("/Assets/devil2D.png");
        devil3D = Loader.CargadorImagenes("/Assets/devil3D.png");
        devil1I = Loader.CargadorImagenes("/Assets/devil1I.png");
        devil2I = Loader.CargadorImagenes("/Assets/devil2I.png");
        devil3I = Loader.CargadorImagenes("/Assets/devil3I.png");
        boss1 = Loader.CargadorImagenes("/Assets/boss1.png");
        boss2 = Loader.CargadorImagenes("/Assets/boss2.png");
        boss3 = Loader.CargadorImagenes("/Assets/boss3.png");
        wall = Loader.CargadorImagenes("/Assets/wall.png");
        iniciar = Loader.CargadorImagenes("/Assets/iniciar .png");
        iniciar1 = Loader.CargadorImagenes("/Assets/iniciar1.png");
        salir = Loader.CargadorImagenes("/Assets/salir.png");
        salir1 = Loader.CargadorImagenes("/Assets/salir1.png");
        reanudar = Loader.CargadorImagenes("/Assets/reanudar.png");
        reanudar1 = Loader.CargadorImagenes("/Assets/reanudar1.png");
        reintentar = Loader.CargadorImagenes("/Assets/reintentar .png");
        reintentar1 = Loader.CargadorImagenes("/Assets/reintentar1.png");
        pw1 = Loader.CargadorImagenes("/Assets/pw1.png");
        pw2 = Loader.CargadorImagenes("/Assets/pw2.png");
        pw3 = Loader.CargadorImagenes("/Assets/pw3.png");
        pw4 = Loader.CargadorImagenes("/Assets/pw4.png");
        power1 = Loader.CargadorImagenes("/Assets/power1.png");
        power2 = Loader.CargadorImagenes("/Assets/power2.png");
        power3 = Loader.CargadorImagenes("/Assets/power3.png");
        power4 = Loader.CargadorImagenes("/Assets/power4.png");
        power5 = Loader.CargadorImagenes("/Assets/power5.png");
        power6 = Loader.CargadorImagenes("/Assets/power6.png");
        power7 = Loader.CargadorImagenes("/Assets/power7.png");
        power8 = Loader.CargadorImagenes("/Assets/power8 .png");
        fondo1 = Loader.CargadorImagenes("/Assets/fondo1.png");
        victoryPanel = Loader.CargadorImagenes("/Assets/victoryPanelrezise.png");
        lostPanel = Loader.CargadorImagenes("/Assets/panelDerrota.png");
        menuPrincipal1 = Loader.CargadorImagenes("/Assets/menuPrincipal1.png");
        getMenuPrincipal2 = Loader.CargadorImagenes("/Assets/menuPrincipal2.png");

        em1 = Loader.CargadorImagenes("/Assets/em1.png");
        em2 = Loader.CargadorImagenes("/Assets/em2.png");
        em3 = Loader.CargadorImagenes("/Assets/em3.png");
    }
}
