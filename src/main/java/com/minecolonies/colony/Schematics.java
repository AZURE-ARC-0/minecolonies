package com.minecolonies.colony;

import com.minecolonies.lib.Constants;
import com.minecolonies.util.Log;
import net.minecraft.block.Block;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/**
 * Created by chris on 10/19/15.
 * Schematic class
 */
public class Schematics
{
    //Hut, Styles
    private static Map<String, List<String>> hutStyleMap = new HashMap<>();

    //Decoration, Style
    private static Map<String, List<String>> decorationStyleMap = new HashMap<>();

    private static final String NULL_STYLE            = "schematics";
    private static final String SCHEMATIC_EXTENSION   = ".schematic";
    private static final String SCHEMATICS_ASSET_PATH = "/assets/minecolonies/schematics/";

    /**
     * Calls {@link #loadStyleMaps()}
     */
    public static void init()
    {
        loadStyleMaps();
    }

    /**
     * Loads all styles saved in ["/assets/minecolonies/schematics/"]
     * Puts these in {@link #hutStyleMap}, with key being the name of the hutDec (E.G. Lumberjack)
     * and the value is a list of styles. Puts decorations in {@link #decorationStyleMap}.
     */
    private static void loadStyleMaps()
    {
        try
        {
            URI  uri = ColonyManager.class.getResource(SCHEMATICS_ASSET_PATH).toURI();
            Path basePath;

            if (uri.getScheme().equals("jar"))
            {
                basePath = FileSystems.newFileSystem(uri, Collections.emptyMap()).getPath(SCHEMATICS_ASSET_PATH);
            }
            else
            {
                basePath = Paths.get(uri);
            }

            loadStyleMaps(basePath);

        }
        catch (IOException | URISyntaxException e)
        {
            Log.logger.error("Error loading Schematic directory. Things will break!", e);
        }
    }

    private static void loadStyleMaps(Path basePath) throws IOException
    {
        try (Stream<Path> walk = Files.walk(basePath))
        {
            Iterator<Path> it = walk.iterator();

            while (it.hasNext())
            {
                Path path = it.next();

                String style = path.getParent().getFileName().toString();

                //Don't treat generic schematics as decorations or huts - ex: supply ship
                if(NULL_STYLE.equals(style))
                {
                    continue;
                }

                if (path.toString().endsWith(SCHEMATIC_EXTENSION))
                {
                    String filename = path.getFileName().toString().split("\\.schematic")[0];
                    String hut      = filename.split("\\d+")[0];

                    if (isSchematicHut(hut))
                    {
                        addHutStyle(hut, style);
                    }
                    else
                    {
                        addDecorationStyle(filename, style);
                    }
                }
            }
        }
    }

    private static void addHutStyle(String hut, String style)
    {
        if (!hutStyleMap.containsKey(hut))
        {
            hutStyleMap.put(hut, new ArrayList<>());
        }

        if(!hutStyleMap.get(hut).contains(style))
        {
            hutStyleMap.get(hut).add(style);
        }
    }

    private static void addDecorationStyle(String decoration, String style)
    {
        if (!decorationStyleMap.containsKey(decoration))
        {
            decorationStyleMap.put(decoration, new ArrayList<>());
        }
        decorationStyleMap.get(decoration).add(style);
    }

    private static boolean isSchematicHut(String name)
    {
        return Block.getBlockFromName(Constants.MOD_ID + ":blockHut" + name) != null;
    }

    /**
     * Returns a set of huts.
     * This is the key set of {@link #hutStyleMap}
     *
     * @return Set of huts with a schematic
     */
    public static Set<String> getHuts()
    {
        return hutStyleMap.keySet();
    }

    /**
     * Returns a list of styles for one specific hut
     *
     * @param hut Hut to get styles for
     * @return List of styles
     */
    public static List<String> getStylesForHut(String hut)
    {
        return hutStyleMap.get(hut);
    }

    /**
     * Returns a set of decorations.
     * This is the key set of {@link #decorationStyleMap}
     *
     * @return Set of decorations with a schematic
     */
    public static Set<String> getDecorations()
    {
        return decorationStyleMap.keySet();
    }

    /**
     * Returns a list of styles for one specific decoration
     *
     * @param decoration Decoration to get styles for
     * @return List of styles
     */
    public static List<String> getStylesForDecoration(String decoration)
    {
        return decorationStyleMap.get(decoration);
    }

    /**
     * For use on client side by the ColonyStylesMessage
     *
     * @param hutStyleMap        new hutStyleMap
     * @param decorationStyleMap new decorationStyleMap
     */
    @SideOnly(Side.CLIENT)
    public static void setStyles(Map<String, List<String>> hutStyleMap, Map<String, List<String>> decorationStyleMap)
    {
        Schematics.hutStyleMap = hutStyleMap;
        Schematics.decorationStyleMap = decorationStyleMap;
    }
}
