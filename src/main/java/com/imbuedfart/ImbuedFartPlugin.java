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
import java.io.IOException;
import java.net.URL;

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

    String wavPath = "fart.wav";

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
            playFart();
        }
    }

    @Subscribe
    public void onSoundEffectPlayed(SoundEffectPlayed event)
    {
        if (event.getSoundId() == 3887)
        {
            event.consume();
            playFart();
        }
    }

    public void playFart()
    {
        try {
            if (clip != null)
            {
                clip.close();
            }

            Class pluginClass = null;
            AudioInputStream stream = null;
            try {
                pluginClass = Class.forName("com.imbuedfart.ImbuedFartPlugin");
                URL url = pluginClass.getClassLoader().getResource(wavPath);
                stream = AudioSystem.getAudioInputStream(url);
            } catch (ClassNotFoundException | UnsupportedAudioFileException | IOException e) {
                e.printStackTrace();
            }

            if (stream == null)
            {
                return;
            }

            AudioFormat format = stream.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            clip = (Clip) AudioSystem.getLine(info);

            clip.open(stream);

            FloatControl volume = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float volumeValue = volume.getMinimum() + ((50 + (config.volumeLevel()*5)) * ((volume.getMaximum() - volume.getMinimum()) / 100));

            volume.setValue(volumeValue);

            clip.start();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}