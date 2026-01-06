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

import custom.objects.Member;
import custom.objects.User;
import custom.objects.serial;
import custom.util.ActivationKey;
import custom.util.Security;
import org.tinystruct.AbstractApplication;
import org.tinystruct.ApplicationException;
import org.tinystruct.data.component.Table;
import org.tinystruct.http.*;
import org.tinystruct.system.annotation.Action;

import java.util.Date;
import java.util.Locale;

public class register extends AbstractApplication {

    @Override
    public void init() {
    }

    @Override
    public void setLocale(Locale locale) {
        super.setLocale(locale);

        this.setText("page.register.title");
        this.setText("register.account-details");
        this.setText("register.required-fields");
        this.setText("register.screen-name.caption");
        this.setText("register.password.caption");
        this.setText("register.confirm-password.caption");
        this.setText("register.email.caption");
        this.setText("register.first-name.caption");
        this.setText("register.last-name.caption");
        this.setText("register.city.caption");
        this.setText("register.country-region.caption");
        this.setText("register.postal-code.caption");
        this.setText("register.phone.caption");
        this.setText("register.select.country");
        this.setText("register.gender.caption");
        this.setText("user.gender.MALE");
        this.setText("user.gender.FEMALE");
        this.setText("user.gender.SECURITY");
        this.setText("register");

        this.setText("application.title");
        this.setText("application.language.name");
        this.setText("page.welcome.caption");
        this.setText("page.language-setting.title");
        this.setText("page.logout.caption");

        this.setText("navigator.bible.caption");
        this.setText("navigator.video.caption");
        this.setText("navigator.document.caption");
        this.setText("navigator.reader.caption");
        this.setText("navigator.controller.caption");
        this.setText("navigator.help.caption");

        this.setText("footer.report-a-site-bug");
        this.setText("footer.privacy");
        this.setText("footer.register");
        this.setText("footer.api");
        this.setText("footer.updates-rss");

        String username = "";
        if (this.getVariable("username") != null) {
            username = String.valueOf(this.getVariable("username").getValue());
        }

        this.setVariable("TEMPLATES_DIR", "/themes");

        this.setText("page.welcome.hello", (username == null || username.trim()
                .length() == 0) ? "" : username + "，");

        this.setVariable("error", "");
        this.setVariable("lastname", "");
        this.setVariable("firstname", "");
        this.setVariable("city", "");
        this.setVariable("postcode", "");

        this.setVariable("gender.male", "");
        this.setVariable("gender.female", "");
        this.setVariable("gender.security", "");

        this.setVariable("telephone", "");

        this.setVariable("nickname", "");
        this.setVariable("email", "");
        this.setVariable("password", "");
        this.setVariable("info", "");
        this.setVariable("display", "block");
    }

    @Action(value = "user/register", mode = Action.Mode.HTTP_POST)
    public Object post(Request request) {
        this.setText("register.tips", this.getLink("help"), this.getLink("help/condition"));

        try {
            if (this.append(request)) {
                this.setVariable("info", "<div class=\"info\">" + this.setText("register.success") + "</div>");
                this.setVariable("display", "none");
            }
        } catch (ApplicationException e) {
            this.setVariable("error", "<div class=\"error\">" + e.getMessage() + "</div>");
        }

        this.setVariable("action", getConfiguration().get("default.base_url") + getContext().getAttribute("REQUEST_PATH").toString());

        Session session = request.getSession();
        if (session.getAttribute("usr") != null) {
            User user = (User) session.getAttribute("usr");

            this.setVariable("user.status", "");
            this.setVariable("user.profile", "<a href=\"javascript:void(0)\" onmousedown=\"profileMenu.show(event,'1')\">" + user.getEmail() + "</a>");
        } else {
            this.setVariable("user.status", "<a href=\"" + this.getLink("user/login") + "\">" + this.getProperty("page.login.caption") + "</a>");
            this.setVariable("user.profile", "");
        }

        return this;
    }

    public String approve() {
        return null;
    }

    public Cookie getCookieByName(Request request, String name) {

        if (request.cookies() != null) {
            Cookie[] cookies = request.cookies();
            int i = 0;
            while (cookies.length > i) {
                if (cookies[i].name().equalsIgnoreCase(name))
                    return cookies[i];
                i++;
            }
        }

        return null;
    }

    @Action(value = "user/register", mode = Action.Mode.HTTP_POST)
    public Object post(Request request, Response response, String keyValue) {
        this.setText("register.tips", this.getLink("help"), this.getLink("help/condition"));

        Cookie key = new CookieImpl("key");
        key.setMaxAge(24 * 3600 * 7);
        key.setValue(keyValue);
        response.addHeader(Header.SET_COOKIE.toString(), key);

        this.setVariable("action", getContext().getAttribute("HTTP_HOST") + getContext().getAttribute("REQUEST_PATH").toString());

        Session session = request.getSession();
        if (null != session.getAttribute("usr")) {
            User user = (User) session.getAttribute("usr");

            this.setVariable("user.status", "");
            this.setVariable("user.profile", "<a href=\"javascript:void(0)\" onmousedown=\"profileMenu.show(event,'1')\">" + user.getEmail() + "</a>");
        } else {
            this.setVariable("user.status", "<a href=\"" + this.getLink("user/login") + "\">" + this.getProperty("page.login.caption") + "</a>");
            this.setVariable("user.profile", "");
        }

        return this;
    }

    public boolean append(Request request) throws ApplicationException {
        Cookie cookie = this.getCookieByName(request,"key");
        if (cookie == null) {
            throw new ApplicationException(this.getProperty("register.status"));
        }

        ActivationKey key = new ActivationKey();
        String number = cookie.value();

        serial serial = new serial();
        Table t = serial.findWith("WHERE number like ?", new Object[]{number});
        if (!t.isEmpty()) {
            throw new ApplicationException(this.getProperty("register.code.used"));
        }

        try {
            if (key.expired(number)) {
                throw new ApplicationException(this.getProperty("register.code.expired"));
            }
        } catch (ApplicationException e) {
            // TODO Auto-generated catch block
            throw new ApplicationException(this.getProperty("register.code.expired"));
        }

        User user;
        Session session;
        if (request.getParameter("nickname") == null || request.getParameter("nickname").trim().isEmpty()) {
            throw new ApplicationException(this.getProperty("register.invalid.nickname"));
        } else {
            user = new User();
            user.setNickname(request.getParameter("nickname"));
            user.setUsername(user.getNickname());

            this.setVariable("nickname", user.getNickname());

            session = request.getSession();
            session.setAttribute("usr", user);
        }

        if (request.getParameter("email") == null || request.getParameter("email").trim().isEmpty()) {
            throw new ApplicationException(this.getProperty("register.invalid.email"));
        } else {
            user.setEmail(request.getParameter("email"));
            this.setVariable("email", user.getEmail());
            session.setAttribute("usr", user);
        }

        if (request.getParameter("password") == null || request.getParameter("password").trim().isEmpty()) {
            throw new ApplicationException(this.getProperty("register.invalid.password"));
        } else {
            user.setPassword(request.getParameter("password"));

            session.setAttribute("usr", user);
        }

        if (user.setRequestFields("count(*) as p").findWith("WHERE email=?", new Object[]{user.getEmail()}).get(0).getFieldInfo("p").intValue() == 0) {
            user.setPassword(new Security(user.getEmail()).encodePassword(user.getPassword()));
            user.setUsername(user.getEmail());

            user.setLastloginIP("");
            user.setLastloginTime(new Date());
            user.setRegistrationTime(new Date());

            if (request.getParameter("last-name") == null || request.getParameter("last-name").trim().isEmpty() || request.getParameter("first-name") == null || request.getParameter("first-name").trim().length() == 0) {
                throw new ApplicationException(this.getProperty("register.invalid.gender"));
            } else {
                user.setLastName(request.getParameter("last-name"));
                user.setFirstName(request.getParameter("first-name"));

                this.setVariable("lastname", user.getLastName());
                this.setVariable("firstname", user.getFirstName());

                session.setAttribute("usr", user);
            }

            if (request.getParameter("gender") == null || request.getParameter("gender").trim().isEmpty()) {
                throw new ApplicationException(this.getProperty("register.invalid.gender"));
            } else {
                user.setGender(Integer.parseInt(request.getParameter("gender")));

                switch (user.getGender()) {
                    case 0:
                        this.setVariable("gender.male", "checked");
                        break;
                    case 1:
                        this.setVariable("gender.female", "checked");
                        break;
                    case 2:
                        this.setVariable("gender.security", "checked");
                        break;
                    default:
                        break;
                }

                session.setAttribute("usr", user);
            }

            if (request.getParameter("country") == null || request.getParameter("country").trim().length() == 0 || request.getParameter("city") == null || request.getParameter("city").trim().length() == 0) {
                throw new ApplicationException(this.getProperty("register.invalid.country"));
            } else {
                user.setCountry(request.getParameter("country"));
                user.setCity(request.getParameter("city"));

                this.setVariable("city", user.getCity());
                this.setVariable("postcode", user.getPostcode());


                session.setAttribute("usr", user);
            }

            if (request.getParameter("zip-postal-code") == null || request.getParameter("zip-postal-code").trim().length() == 0) {
                throw new ApplicationException(this.getProperty("register.invalid.postcode"));
            } else {
                user.setPostcode(request.getParameter("zip-postal-code"));

                this.setVariable("telephone", user.getTelephone());

                session.setAttribute("usr", user);
            }

            user.append();

            Member member = new Member();
            member.setUserId(user.getId());
            member.setGroupId("386e27c2-5db6-4f63-b28d-68a4adec2fd6");
            member.append();

            serial.setUserId(user.getId());
            serial.setNumber(number);
            serial.append();

            return true;
        } else {
            throw new ApplicationException(this.getProperty("register.email.used"));
        }
    }

    @Override
    public String version() {
        // TODO Auto-generated method stub
        return null;
    }

}