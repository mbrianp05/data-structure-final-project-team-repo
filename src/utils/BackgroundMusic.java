package utils;
import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class BackgroundMusic {
    private Clip clip;
    private FloatControl volumeControl;
    private boolean isMuted = false;
    private float volume = 0.5f; // Volumen por defecto (50%)
    private boolean autoPlay = true;

    public BackgroundMusic(String filePath) {
        try {
            // Cargar el archivo de música
            URL url = getClass().getClassLoader().getResource(filePath);
            if (url == null) {
                System.err.println("No se pudo encontrar el archivo de música: " + filePath);
                return;
            }

            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            clip = AudioSystem.getClip();
            clip.open(audioIn);

            // Configurar para reproducción en bucle
            clip.loop(Clip.LOOP_CONTINUOUSLY);

            // Obtener control de volumen si está disponible
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                setVolume(volume);
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error al cargar la música: " + e.getMessage());
        }
    }

    public BackgroundMusic(String filePath, boolean autoPlay) {
        this.autoPlay = autoPlay;
        try {
            URL url = getClass().getClassLoader().getResource(filePath);
            if (url == null) {
                System.err.println("No se pudo encontrar el archivo de música: " + filePath);
                return;
            }

            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            clip = AudioSystem.getClip();
            clip.open(audioIn);

            // Configurar para reproducción en bloop pero NO iniciar
            // Solo configuramos el loop, pero no iniciamos la reproducción
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                setVolume(volume);
            }

            if (autoPlay && !isMuted) {
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error al cargar la música: " + e.getMessage());
        }
    }

    public void play() {
        if (clip != null && !clip.isRunning() && !isMuted) {
            clip.start();
        }
    }

    public void playOnce() {
        if (clip != null && !clip.isRunning() && !isMuted) {
            clip.setFramePosition(0); // Asegurar que empieza desde el principio
            clip.start(); // Reproducir solo una vez
        }
    }

    public void stop() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }

    public void setVolume(float volume) {
        this.volume = Math.max(0.0f, Math.min(1.0f, volume));
        if (volumeControl != null) {
            // Convertir volumen lineal a decibelios
            float dB = (float) (Math.log(this.volume) / Math.log(10.0) * 20.0);
            volumeControl.setValue(dB);
        }
    }

    public void mute() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            isMuted = true;
        }
    }

    public void unmute() {
        if (clip != null && !clip.isRunning() && isMuted) {
            clip.start();
            isMuted = false;
        }
    }

    public void close() {
        if (clip != null) {
            clip.close();
        }
    }
}