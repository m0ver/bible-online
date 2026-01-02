/*******************************************************************************
 * Copyright  (c) 2013 Mover Zhou
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
package custom.application;

import custom.objects.User;
import org.tinystruct.AbstractApplication;
import org.tinystruct.ApplicationException;
import org.tinystruct.data.component.Table;
import org.tinystruct.http.Request;
import org.tinystruct.http.Session;
import org.tinystruct.mail.SimpleMail;
import org.tinystruct.system.annotation.Action;

import java.util.Locale;

import static org.tinystruct.http.Constants.HTTP_REQUEST;

public class password extends AbstractApplication {

    @Override
    public void init() {
        // TODO Auto-generated method stub
    }

    @Override
    public void setLocale(Locale locale) {
        super.setLocale(locale);

        this.setText("page.findlostpassword.title", locale);
        this.setText("login.user.email", locale);
        this.setText("login.verifycode.caption", locale);
        this.setText("login.lost-password", locale);
        this.setText("login.find-password.tips", locale);
        this.setText("login.get-password", locale);
        this.setText("page.condition.title", locale);
        this.setText("application.title", locale);
        this.setText("application.language.name", locale);

        this.setText("page.welcome.caption", locale);
        this.setText("page.language-setting.title", locale);
        this.setText("page.logout.caption", locale);

        this.setText("navigator.bible.caption", locale);
        this.setText("navigator.video.caption", locale);
        this.setText("navigator.document.caption", locale);
        this.setText("navigator.reader.caption", locale);
        this.setText("navigator.controller.caption", locale);
        this.setText("navigator.help.caption", locale);

        this.setText("footer.report-a-site-bug", locale);
        this.setText("footer.privacy", locale);
        this.setText("footer.register", locale);
        this.setText("footer.api", locale);
        this.setText("footer.updates-rss", locale);

        this.setVariable("TEMPLATES_DIR", "/themes");
        String username = "";
        if (this.getVariable("username") != null) {
            username = String.valueOf(this.getVariable("username").getValue());
        }

        this.setText("page.welcome.hello", (username == null || username.trim().isEmpty()) ? "" : username + "，");
    }

    @Action("user/password")
    public Object send(Request request) throws ApplicationException {
        this.setVariable("action", getConfiguration().get("default.base_url") + getContext().getAttribute("REQUEST_PATH").toString());

        Session session = request.getSession();
        if (session.getAttribute("usr") != null) {
            User usr = (User) session.getAttribute("usr");

            this.setVariable("user.status", "");
            this.setVariable("user.profile", "<a href=\"javascript:void(0)\" onmousedown=\"profileMenu.show(event,'1')\">" + usr.getEmail() + "</a>");
        } else {
            this.setVariable("user.status", "<a href=\"" + this.getLink("user/login") + "\">" + this.getProperty("page.login.caption") + "</a>");
            this.setVariable("user.profile", "");
        }

        return this;
    }

    @Action("user/password")
    public boolean send(String mailto) throws ApplicationException {
        User user = new User();
        Table table = user.findWith("WHERE email=?", new Object[]{mailto});

        if (!table.isEmpty()) {
            org.tinystruct.data.component.Row row = table.get(0);
            try {
                SimpleMail email = new SimpleMail();
                email.setFrom(this.getProperty("mail.default.from"));
                email.setSubject("密码重置邮件");
                email.setBody("亲爱的" + row.getFieldInfo("username").stringValue() + "用户，我们刚刚收到您的密码找回请求。为了保证您能及时使用我们提供的服务，请您于24小时内点击此链接重置您的密码。");
                email.setTo(mailto);

                return email.send();
            } catch (Exception ex) {
                throw new ApplicationException(ex.getMessage(), ex.getCause());
            }

        }

        return false;
    }

    @Override
    public String version() {
        // TODO Auto-generated method stub
        return null;
    }

}
