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

import java.util.Date;
import java.util.Locale;

import static org.tinystruct.http.Constants.HTTP_REQUEST;
import static org.tinystruct.http.Constants.HTTP_RESPONSE;

public class register extends AbstractApplication {
    private User user;
    private Session session;
    private Request request;

    @Override
    public void init() {
        // TODO Auto-generated method stub
        this.setAction("user/register", "post");

        this.setSharedVariable("error", "");
        this.setSharedVariable("lastname", "");
        this.setSharedVariable("firstname", "");
        this.setSharedVariable("city", "");
        this.setSharedVariable("postcode", "");

        this.setSharedVariable("gender.male", "");
        this.setSharedVariable("gender.female", "");
        this.setSharedVariable("gender.security", "");

        this.setSharedVariable("telephone", "");

        this.setSharedVariable("nickname", "");
        this.setSharedVariable("email", "");
        this.setSharedVariable("password", "");
        this.setSharedVariable("info", "");
        this.setSharedVariable("display", "block");
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

        this.setText("page.welcome.hello", (username == null || username.trim()
                .length() == 0) ? "" : username + "，");
    }

    public Object post() {
        this.setText("register.tips", this.getLink("help"), this.getLink("help/condition"));

        this.request = (Request) this.context.getAttribute(HTTP_REQUEST);
        try {
            if (this.append()) {
                this.setVariable("info", "<div class=\"info\">" + this.setText("register.success") + "</div>");
                this.setVariable("display", "none");
            }
        } catch (ApplicationException e) {
            this.setVariable("error", "<div class=\"error\">" + e.getMessage() + "</div>");
        }

        this.setVariable("action", this.config.get("default.base_url") + this.context.getAttribute("REQUEST_ACTION").toString());

        Session session = request.getSession();
        if (session.getAttribute("usr") != null) {
            this.user = (User) session.getAttribute("usr");

            this.setVariable("user.status", "");
            this.setVariable("user.profile", "<a href=\"javascript:void(0)\" onmousedown=\"profileMenu.show(event,'1')\">" + this.user.getEmail() + "</a>");
        } else {
            this.setVariable("user.status", "<a href=\"" + this.getLink("user/login") + "\">" + this.getProperty("page.login.caption") + "</a>");
            this.setVariable("user.profile", "");
        }

        return this;
    }

    public String approve() {
        return null;
    }

    public Cookie getCookieByName(String name) {

        if (this.request.cookies() != null) {
            Cookie[] cookies = this.request.cookies();
            int i = 0;
            while (cookies.length > i) {
                if (cookies[i].name().equalsIgnoreCase(name))
                    return cookies[i];
                i++;
            }
        }

        return null;
    }

    public Object post(String keyValue) {
        this.setText("register.tips", this.getLink("help"), this.getLink("help/condition"));

        Response response = (Response) this.context.getAttribute(HTTP_RESPONSE);
        Cookie key = new CookieImpl("key");
        key.setMaxAge(24 * 3600 * 7);
        key.setValue(keyValue);
        response.addHeader(Header.SET_COOKIE.toString(), key);

        this.setVariable("action", String.valueOf(this.context.getAttribute("HTTP_HOST")) + this.context.getAttribute("REQUEST_ACTION").toString());

        Session session = request.getSession();
        if (null != session.getAttribute("usr")) {
            this.user = (User) session.getAttribute("usr");

            this.setVariable("user.status", "");
            this.setVariable("user.profile", "<a href=\"javascript:void(0)\" onmousedown=\"profileMenu.show(event,'1')\">" + this.user.getEmail() + "</a>");
        } else {
            this.setVariable("user.status", "<a href=\"" + this.getLink("user/login") + "\">" + this.getProperty("page.login.caption") + "</a>");
            this.setVariable("user.profile", "");
        }

        return this;
    }

    public boolean append() throws ApplicationException {

        Cookie cookie = this.getCookieByName("key");

        if (cookie == null) {
            throw new ApplicationException(this.getProperty("register.status"));
        }

        ActivationKey key = new ActivationKey();
        String number = cookie.value();

        serial serial = new serial();
        Table t = serial.findWith("WHERE number like ?", new Object[]{number});
        if (t.size() > 0) {
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

        if (this.request.getParameter("nickname") == null || this.request.getParameter("nickname").trim().length() == 0) {
            throw new ApplicationException(this.getProperty("register.invalid.nickname"));
        } else {
            this.user = new User();
            this.user.setNickname(this.request.getParameter("nickname"));
            this.user.setUsername(this.user.getNickname());

            this.setVariable("nickname", this.user.getNickname());

            this.session = this.request.getSession();
            this.session.setAttribute("usr", this.user);
        }

        if (this.request.getParameter("email") == null || this.request.getParameter("email").trim().length() == 0) {
            throw new ApplicationException(this.getProperty("register.invalid.email"));
        } else {
            this.user.setEmail(this.request.getParameter("email"));
            this.setVariable("email", this.user.getEmail());
            this.session.setAttribute("usr", this.user);
        }

        if (this.request.getParameter("password") == null || this.request.getParameter("password").trim().length() == 0) {
            throw new ApplicationException(this.getProperty("register.invalid.password"));
        } else {
            this.user.setPassword(this.request.getParameter("password"));

            this.session.setAttribute("usr", this.user);
        }

        if (user.setRequestFields("count(*) as p").findWith("WHERE email=?", new Object[]{this.user.getEmail()}).get(0).getFieldInfo("p").intValue() == 0) {
            user.setPassword(new Security(user.getEmail()).encodePassword(this.user.getPassword()));
            user.setUsername(this.user.getEmail());

            user.setLastloginIP("");
            user.setLastloginTime(new Date());
            user.setRegistrationTime(new Date());

            if (this.request.getParameter("last-name") == null || this.request.getParameter("last-name").trim().length() == 0 || this.request.getParameter("first-name") == null || this.request.getParameter("first-name").trim().length() == 0) {
                throw new ApplicationException(this.getProperty("register.invalid.gender"));
            } else {
                this.user.setLastName(this.request.getParameter("last-name"));
                this.user.setFirstName(this.request.getParameter("first-name"));

                this.setVariable("lastname", this.user.getLastName());
                this.setVariable("firstname", this.user.getFirstName());

                this.session.setAttribute("usr", this.user);
            }

            if (this.request.getParameter("gender") == null || this.request.getParameter("gender").trim().length() == 0) {
                throw new ApplicationException(this.getProperty("register.invalid.gender"));
            } else {
                this.user.setGender(Integer.parseInt(this.request.getParameter("gender")));

                switch (this.user.getGender()) {
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

                this.session.setAttribute("usr", this.user);
            }

            if (this.request.getParameter("country") == null || this.request.getParameter("country").trim().length() == 0 || this.request.getParameter("city") == null || this.request.getParameter("city").trim().length() == 0) {
                throw new ApplicationException(this.getProperty("register.invalid.country"));
            } else {
                this.user.setCountry(this.request.getParameter("country"));
                this.user.setCity(this.request.getParameter("city"));

                this.setVariable("city", this.user.getCity());
                this.setVariable("postcode", this.user.getPostcode());


                this.session.setAttribute("usr", this.user);
            }

            if (this.request.getParameter("zip-postal-code") == null || this.request.getParameter("zip-postal-code").trim().length() == 0) {
                throw new ApplicationException(this.getProperty("register.invalid.postcode"));
            } else {
                this.user.setPostcode(this.request.getParameter("zip-postal-code"));

                this.setVariable("telephone", this.user.getTelephone());

                this.session.setAttribute("usr", this.user);
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