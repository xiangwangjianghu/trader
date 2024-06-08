package com.newtouch.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;

public class Captcha {

    // 验证码
    private String code;

    // 图片
    private BufferedImage bufferedImage;

    // 随机数发生器
    private Random random = new Random();

    public Captcha(int width, int height, int codeCount, int lineCount) {
        // 1.生成图像
        bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // 2.背景色
        Graphics graphics = bufferedImage.getGraphics();
        graphics.setColor(getRandColor(200, 250));
        graphics.fillRect(0, 0, width, height);

        Font font = new Font("Fixedsys", Font.BOLD, height - 5);
        graphics.setFont(font);

        // 3.生成干扰线 噪点
        for (int i = 0; i < lineCount; i++) {
            int xs = random.nextInt(width);
            int ys = random.nextInt(height);
            int xe = xs + random.nextInt(width);
            int ye = ys + random.nextInt(width);

            graphics.setColor(getRandColor(1, 255));
            graphics.drawLine(xs, ys, xe, ye);
        }

        float yawpRate = 0.01f;
        int area = (int) (yawpRate * width * height);
        for (int i = 0; i < area; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int rgb = random.nextInt(255);

            bufferedImage.setRGB(x, y, rgb);
        }

        // 4.添加字符
        this.code = randomStr(codeCount);
        int fontWidth = width / codeCount, fontHeight = height - 5;

        for (int i = 0; i < codeCount; i++) {
            graphics.setColor(getRandColor(1, 255));
            graphics.drawString(this.code.substring(i, i + 1), i * fontWidth + 3, fontHeight - 3);
        }
    }

    private Color getRandColor(int fc, int bc) {
        fc = Math.min(fc, 255);
        bc = Math.min(bc, 255);


        int r = fc + random.nextInt(bc - fc);
        int g = r, b = r;

        return new Color(r, g, b);
    }

    // 随机生成字符
    private String randomStr(int codeCount) {
        String str = "ABCDEFGHJKMNOPQRSTUVWXYZabcdefghjkmnopqrstuvwxyz1234567890";
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < codeCount; i++) {
            int randNum = (int) ((Math.random()) * (str.length() - 1));

            sb.append(str.charAt(randNum));
        }

        return sb.toString();
    }

    public String getBase64ByteStr() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", baos);

        String s = Base64.getEncoder().encodeToString(baos.toByteArray());

        s = s.replaceAll("\n", "").replaceAll("\r", "");

        return "data:image/jpg;base64," + s;
    }

    public String getCode() {
        return code.toLowerCase();
    }
}
