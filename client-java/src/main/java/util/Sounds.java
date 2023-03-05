package util;
;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Sounds {
  static final Map<String, Clip> clipCollection = new HashMap<>();
  private static boolean soundsEnabled = true;

  static final Map<String, File> soundCollection =
      Map.ofEntries(
          new AbstractMap.SimpleEntry<>(
              "soft_ding", getFileFromResource("./sounds/58731_wipe-soft.wav")),
          new AbstractMap.SimpleEntry<>(
              "door_knock", getFileFromResource("./sounds/193859_door-knock.wav")),
          new AbstractMap.SimpleEntry<>(
              "footstep", getFileFromResource("./sounds/336598_footstep.wav")),
          new AbstractMap.SimpleEntry<>(
              "metallic", getFileFromResource("./sounds/406489_metallic-pipe.wav")),
          new AbstractMap.SimpleEntry<>(
              "eat", getFileFromResource("./sounds/412068_chewing-carrot.wav")),
          new AbstractMap.SimpleEntry<>("door", getFileFromResource("./sounds/424496_door.wav")),
          new AbstractMap.SimpleEntry<>(
              "sword", getFileFromResource("./sounds/441666_sword-slash.wav")),
          new AbstractMap.SimpleEntry<>(
              "bowl", getFileFromResource("./sounds/448073_ceramic-bowl.wav")),
          new AbstractMap.SimpleEntry<>(
              "door_kick", getFileFromResource("./sounds/452600_door-wood-kick-open.wav")),
          new AbstractMap.SimpleEntry<>("ding", getFileFromResource("./sounds/454612_ding.wav")),
          new AbstractMap.SimpleEntry<>("piano", getFileFromResource("./sounds/587634_piano.wav")));

  static {
    try {
      for (Map.Entry<String, File> entry : soundCollection.entrySet()) {
        Clip clip = AudioSystem.getClip();

        clip.open(AudioSystem.getAudioInputStream(entry.getValue()));
        clipCollection.put(entry.getKey(), clip);
      }
    } catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
      throw new RuntimeException(e);
    }
  }

  public static void enableSound() {
    setSound(true);
  }

  public static void disableSound() {
    setSound(false);
  }

  public static void setSound(boolean enabled) {
    soundsEnabled = enabled;
  }

  public static void attack() {
    playSound("sword");
  }

  public static void door() {
    playSound("door");
  }

  public static void door_kick() {
    playSound("door_kick");
  }

  public static void eat() {
    playSound("eat");
  }

  public static void explore() {
    playSound("footstep");
  }

  public static void search() {
    playSound("door_knock");
  }

  private static void playSound(String soundName) {
    if (!soundsEnabled) {
      return;
    }

    Loggers.SoundLogger.debug(soundName);
    Clip clip = clipCollection.get(soundName);
    // To prevent having to reset the audio resource, just set the start time at 0
    clip.setMicrosecondPosition(0);
    clip.start();
  }

  private static File getFileFromResource(String path) {
    String resourcePath =
        Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource(path)).getFile();
    return new File(resourcePath);
  }

  public static void main(String[] args) throws Exception {
    for (String key : soundCollection.keySet()) {
      System.out.println("playing sound named: " + key);
      playSound(key);
      TimeUnit.SECONDS.sleep(2);
    }
  }
}
