package com.imbuedfart;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.SoundEffectPlayed;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;
import javax.sound.sampled.*;
import java.io.*;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@PluginDescriptor(
        name = "Imbued Fart",
        description = "Plays a fart noise instead of the imbued heart sound @Hooti_osrs"
)
public class ImbuedFartPlugin extends Plugin
{
    @Inject
    private Client client;
    @Inject
    private ImbuedFartConfig config;

    private Clip clip;

    private static final int FART_FILE_COUNT = 17;

    @Provides
    ImbuedFartConfig provideConfig(final ConfigManager configManager)
    {
        return configManager.getConfig(ImbuedFartConfig.class);
    }

    @Override
    protected void shutDown() throws Exception
    {
        if (clip != null && clip.isOpen())
        {
            clip.close();
        }
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event)
    {
        if (!config.menuEntryTrigger())
        {
            return;
        }

        if (event.getMenuOption().toLowerCase().contains("invigorate")
                && event.getMenuTarget().toLowerCase().contains("heart")
                && event.getMenuAction().equals(MenuAction.CC_OP))
        {
            playRandomFart();
        }
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event)
    {
        if (!config.fartMenuEntry())
        {
            return;
        }

        if (event.getMenuEntry().getOption().toLowerCase().contains("invigorate")
                && event.getMenuEntry().getTarget().toLowerCase().contains("heart")
                && event.getMenuEntry().getType().equals(MenuAction.CC_OP))
        {
            client.createMenuEntry(-1)
                    .setOption("Fart")
                    .setTarget(event.getTarget())
                    .setIdentifier(event.getIdentifier())
                    .setParam0(event.getActionParam0())
                    .setParam1(event.getActionParam1())
                    .setType(MenuAction.RUNELITE)
                    .onClick(this::playRandomFart);
        }
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
        if (event.getSoundId() == 3887 || event.getSoundId() == 6847)
        {
            event.consume();

            if (!config.menuEntryTrigger())
            {
                playRandomFart();
            }
        }
    }

    public void playRandomFart(MenuEntry menuEntry)
    {
        playRandomFart();
    }

    public void playRandomFart()
    {
        int random = ThreadLocalRandom.current().nextInt(1, FART_FILE_COUNT);

        playFart(random);
    }

    public void playSequentialFarts()
    {
        for (int i = 1; i <= FART_FILE_COUNT; i++)
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

            AudioInputStream stream = null;
            InputStream is;
            String filename = String.format("/%s.wav", index);

            is = getClass().getResourceAsStream(filename);

            if (is == null) {
                log.debug(String.format("Resource not found: %s", filename));
                return;
            }

            BufferedInputStream bis = new BufferedInputStream(is);

            stream = AudioSystem.getAudioInputStream(bis);
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

}