package com.imbuedfart;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.SoundEffectPlayed;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;
import javax.sound.sampled.*;
import java.io.*;
import java.net.URL;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@PluginDescriptor(
        name = "Imbued Fart",
        enabledByDefault = false,
        description = "Plays a fart noise instead of the imbued heart sound"
)
public class ImbuedFartPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private ImbuedFartConfig config;

    private Clip clip;

    @Provides
    ImbuedFartConfig provideConfig(final ConfigManager configManager)
    {
        return configManager.getConfig(ImbuedFartConfig.class);
    }

    @Subscribe
    public void onCommandExecuted(CommandExecuted event)
    {
        if (event.getCommand().equals("fart"))
        {
            playRandomFart();
        }

        if (event.getCommand().equals("allfarts"))
        {
            playSequentialFarts();
        }
    }

    @Subscribe
    public void onSoundEffectPlayed(SoundEffectPlayed event)
    {
        if (event.getSoundId() == 3887)
        {
            event.consume();
            playRandomFart();
        }
    }

    public void playRandomFart()
    {
        int random = ThreadLocalRandom.current().nextInt(1, 17);

        playFart(random);
    }

    public void playSequentialFarts()
    {
        for (int i = 1; i <= 17; i++)
        {
            playFart(i);

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void playFart(int index)
    {
        try {
            if (clip != null)
            {
                clip.close();
            }

            URL url = null;
            AudioInputStream stream = null;
            log.debug(index + ".wav");

            url = getResourceURL(index + ".wav");
            stream = AudioSystem.getAudioInputStream(url);
            AudioFormat format = stream.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            clip = (Clip) AudioSystem.getLine(info);

            clip.open(stream);

            FloatControl volume = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float volumeValue = volume.getMinimum() + ((50 + (config.volumeLevel()*5)) * ((volume.getMaximum() - volume.getMinimum()) / 100));

            volume.setValue(volumeValue);

            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    private URL getResourceURL(String path)
    {
        return getContextClassLoader().getResource(path);
    }

    private ClassLoader getContextClassLoader()
    {
        return Thread.currentThread().getContextClassLoader();
    }

}