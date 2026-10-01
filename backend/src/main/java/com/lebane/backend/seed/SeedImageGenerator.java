package com.lebane.backend.seed;

import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
public class SeedImageGenerator {
    private static final int WIDTH = 640;
    private static final int HEIGHT = 360;

    public byte[] generate(String title, int variant) {

        BufferedImage image = new BufferedImage(WIDTH,HEIGHT,BufferedImage.TYPE_INT_RGB);

        Graphics2D graphics = image.createGraphics();

        try {
            drawBackground(graphics, variant);

            graphics.setColor(Color.WHITE);
            graphics.setFont(new Font(Font.SANS_SERIF,Font.BOLD,28));

            graphics.drawString(title,40,170);

            graphics.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,18));

            graphics.drawString("Lebane Real Estate",40,210);

            ByteArrayOutputStream output = new ByteArrayOutputStream();

            ImageIO.write(image,"png",output);

            return output.toByteArray();

        } catch (IOException exception) {
            throw new IllegalStateException("Could not generate seed image",exception);

        } finally {
            graphics.dispose();
        }
    }

    private void drawBackground(Graphics2D graphics,int variant) {

        Color[] colors = {
                new Color(52, 73, 94),
                new Color(44, 62, 80),
                new Color(39, 174, 96),
                new Color(41, 128, 185),
                new Color(142, 68, 173),
                new Color(211, 84, 0),
                new Color(127, 140, 141),
                new Color(22, 160, 133),
                new Color(192, 57, 43)
        };

        Color background = colors[variant % colors.length];

        graphics.setColor(background);

        graphics.fillRect(0,0,WIDTH,HEIGHT);

        graphics.setColor(new Color(255,255,255,35));

        graphics.fillRect(30,120,580,130);
    }
}
