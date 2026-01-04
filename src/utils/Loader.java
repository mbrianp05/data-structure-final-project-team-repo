package utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class Loader { // para cargar imágenes y sonidos

    public static BufferedImage CargadorImagenes(String ruta) {
        if (ruta == null) {
            System.err.println("[Loader] Ruta nula pasada a CargadorImagenes");
            return null;
        }

        // Intentar obtener el recurso como stream
        InputStream is = Loader.class.getResourceAsStream(ruta);
        if (is == null) {
            // Mensaje claro si no se encuentra el recurso en el classpath
            System.err.println("[Loader] Recurso no encontrado en classpath: " + ruta);
            return null;
        }

        try {
            BufferedImage img = ImageIO.read(is);
            if (img == null) {
                // ImageIO.read puede devolver null si el formato no es reconocido
                System.err.println("[Loader] ImageIO no pudo decodificar la imagen: " + ruta);
            }
            return img;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        } finally {
            try {
                is.close();
            } catch (IOException ignored) { }
        }
    }
}
