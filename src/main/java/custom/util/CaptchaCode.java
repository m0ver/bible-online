/*******************************************************************************
 * Copyright  (c) 2017 James Mover Zhou
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package custom.util;

import org.tinystruct.ApplicationException;
import org.tinystruct.http.Request;
import org.tinystruct.http.Response;
import org.tinystruct.http.Session;
import org.tinystruct.system.util.Matrix;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

public class CaptchaCode {
    private String sessionName = "", sessionValue = "", formName = "";
    private String temp = "";
    public boolean beRemoved = false;
    private int width = 66, height = 19;
    private Security.Mode mode = Security.Mode.OnlyNumber;
    private boolean LineCase = false;
    private Font font = new Font("Georgia", Font.BOLD, 20);
    private Request request = null;
    private Response response = null;
    private Session session = null;
    private BufferedImage image = null;

    public CaptchaCode(Request request) {
        this.request = request;
        this.session = request.getSession();
    }

    public CaptchaCode(Request request, Response response) throws IOException {
        temp = formName;
        this.request = request;
        this.response = response;
        this.session = request.getSession();

//			if(sessionName!=null&&sessionName.trim().length()!=0)	this.session.removeAttribute(sessionName);
//			if(session.getAttribute(sessionName)==null)this.beRemoved=true;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public void setMode(Security.Mode mode) {
        this.mode = mode;
    }

    public void setLineCase(boolean lcase) {
        this.LineCase = lcase;
    }

    Color getRandColor(int fc, int bc) {
        Random random = new Random();
        if (fc > 255) fc = 255;
        if (bc > 255) bc = 255;
        int r = fc + random.nextInt(bc - fc);
        int g = fc + random.nextInt(bc - fc);
        int b = fc + random.nextInt(bc - fc);
        return new Color(r, g, b);
    }

    public String getEstablishedCode() {
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics graphic = image.getGraphics();
        Random random = new Random();
        graphic.setColor(getRandColor(200, 250));
        graphic.fillRect(0, 0, width, height);
        graphic.setFont(font);
        graphic.setColor(getRandColor(160, 200));
        for (int i = 0; LineCase && i < 155; i++) {
            int x = random.nextInt(width),
                    y = random.nextInt(height),
                    xl = random.nextInt(12),
                    yl = random.nextInt(12);
            graphic.drawLine(x, y, x + xl, y + yl);
        }
        String rand = "", string = "";
        switch (mode.ordinal()) {
            case 0:
                string = "0123456789";
                break;
            case 1:
                string = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
                break;
            case 2:
                string = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
                break;
            default:
                string = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
                break;
        }
        char[] character = string.toCharArray();
        sessionName = "";
        for (int i = 0; i < 4; i++) {
            rand = String.valueOf(character[random.nextInt(character.length)]);
            sessionName += rand;

            graphic.setColor(new Color(20 + random.nextInt(110), 20 + random.nextInt(110), 20 + random.nextInt(110)));//���ú����4����ɫ��ͬ����������Ϊ����̫�ӽ�����ֻ��ֱ�����
            if (this.mode.ordinal() > 2)
                graphic.drawString(new Security(sessionName).encodePassword(rand), 13 * i + 6, 16);
            else
                graphic.drawString(rand, 13 * i + 6, 16);
        }

        formName = "";
        for (int j = 0; j < 4; j++) {
            formName += String.valueOf(character[random.nextInt(character.length)]);
        }

        sessionValue = (this.mode.ordinal() > 2) ? new Security(sessionName).encodePassword(sessionName) : sessionName;
        session.setAttribute(sessionName, sessionValue);
        graphic.dispose();

		try {
			return "data:image/png;base64," + Matrix.getBase64Image(this.image);
		} catch (ApplicationException e) {
			e.printStackTrace();
		}

		return "";
	}

    public String authorizedCode() {
        return (String) session.getAttribute("authorizedCode");
    }
}