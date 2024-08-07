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

import custom.objects.*;
import custom.util.Security;
import org.tinystruct.ApplicationException;
import org.tinystruct.data.component.Row;
import org.tinystruct.data.component.Table;
import org.tinystruct.http.*;
import org.tinystruct.system.Language;
import org.tinystruct.system.Resource;
import org.tinystruct.system.util.StringUtilities;

import java.util.Date;
import java.util.Vector;

public class passport {
    private String username;
    private String password;
    private String email = "";
    private String sessionName = "";
    private String where = "";
    private Resource resource;
    private Language lang;

    private User currentUser;
    private boolean recognized = false;

    private Session session;
    private Request request;
    private Response response;

    public passport(Request request, Response response, String sessionName) throws ApplicationException {
        this.request = request;
        this.response = response;
        this.currentUser = new User();

        this.session = this.request.getSession();

        this.sessionName = sessionName;
        if (this.session.getAttribute(sessionName) != null)
            this.recognized = (Boolean) this.session.getAttribute(sessionName);

        Cookie language = StringUtilities.getCookieByName(request.cookies(), "language");
        if (language != null) {
            this.setLanguage(Language.valueOf(language.value()));
        } else
            this.setLanguage(Language.zh_CN);

        this.resource = Resource.getInstance(this.lang.toString());
    }

    public passport(String username, String password) {
        this.currentUser = new User();
        this.currentUser.setUsername(username);
        this.currentUser.setPassword(password);
    }

    private void setLanguage(Language valueOf) {
        this.lang = valueOf;
    }

    public boolean recognized() {
        return this.recognized;
    }

    public void setLoginAsUser(Object userId) throws ApplicationException {
        this.currentUser.setId(userId);
        this.currentUser.findOneById();

        this.setLogin();
    }

    private void setLogin() throws ApplicationException {
        this.recognized = true;
        this.session.setAttribute(sessionName, Boolean.TRUE);
        this.session.setAttribute("usr", this.currentUser);

        Cookie username = new CookieImpl("username");
        username.setValue(this.currentUser.getUsername());
        username.setMaxAge(24 * 3600);
        username.setHttpOnly(true);
        username.setSecure(true);
        this.response.addHeader(Header.SET_COOKIE.name(), username);
        this.response.addHeader(Header.SET_COOKIE.name(), "JSESSIONID = " + this.session.getId());

        Member member = new Member();
        Table members = member.findWith("WHERE user_id=?", new Object[]{this.currentUser.getId()});
        if (!members.isEmpty()) {
            member.setData(members.get(0));
        } else {
            member.setUserId(currentUser.getId());
            member.setGroupId("386e27c2-5db6-4f63-b28d-68a4adec2fd6");
            member.append();
        }

        Group group = new Group();
        group.setId(member.getGroupId());
        group.findOneById();

        String[] roles = group.getRoles().split(",");

        Vector<String> rights = new Vector<String>();
        for (String roleId : roles) {
            Role role = new Role();
            role.setId(roleId);
            role.findOneById();

            String[] _rights = role.getRights().split(",");

            for (String rightId : _rights) {
                if (!rights.contains(rightId))
                    rights.add(rightId);
            }
        }

        this.session.setAttribute("rights", rights);

        Log log = new Log();
        Table logs = log.findWith("WHERE user_id=?", new Object[]{this.currentUser.getId()});
        if (!logs.isEmpty()) {
            log.setData(logs.get(0));
            log.setDate(new Date());
            log.update();
        } else {
            log.setUserId(this.currentUser.getId());
            log.setAction("Logined Successful");
            log.setActionType(0);
            log.setDate(new Date());
            log.append();
        }

    }

    public Session getSession() {
        return this.session;
    }

    public void logout() {
        this.recognized = false;

        this.session.removeAttribute(this.sessionName);
        this.session.removeAttribute("usr");
        this.session.removeAttribute("rights");
    }

    public boolean checkUser() throws ApplicationException {
        Object[] parameters = new Object[]{};
        if (currentUser.getUsername() != null && currentUser.getUsername().trim().length() != 0 && new StringUtilities(currentUser.getUsername()).safe()) {
            this.username = currentUser.getUsername();
            parameters = new Object[]{this.username};
            where = "WHERE username=? and status='1'";
        } else if (currentUser.getEmail() != null && new StringUtilities(currentUser.getEmail()).safe()) {
            this.email = currentUser.getEmail();
            parameters = new Object[]{this.email};
            where = "WHERE email=? and status='1'";
        } else {
            return false;
        }

        User u = new User();
        Table list = u.findWith(where, parameters);

        if (list.size() > 0) {
            Row found = list.get(0);
            this.email = found.getFieldInfo("email").stringValue();

            if (this.email != null && this.email.trim().length() > 0) {
                if (found.getFieldInfo("username") != null && found.getFieldInfo("username").stringValue().trim().length() > 0)
                    currentUser.setUsername(found.getFieldInfo("username").stringValue());
                else
                    currentUser.setUsername(this.email);
            } else {
                return false;
            }

            this.password = currentUser.getPassword();
            if (this.password.equals(String.valueOf(found.get(0).get("password").value()))) {
                currentUser.setData(found);

                currentUser.setLastloginTime(new Date());
//                currentUser.setLastloginIP(this.request.getRemoteAddr());

                if (this.request.getParameter("autologin") != null) {
                    Cookie autologin = new CookieImpl("autologin");
                    autologin.setValue(currentUser.getId());
                    autologin.setMaxAge(24 * 3600 * 30);
                }

                currentUser.update();

                return true;
            }
        }

        return false;
    }

    public boolean login() throws ApplicationException {
//		String lastformName=org.mover.system.Security.ValidateCode.getLastFormName();

        if (this.request.getParameter("username") == null) {
            throw new ApplicationException(this.resource.getLocaleString("login.username.invalid"));
        } else {
            if (this.request.getParameter("username").indexOf('@') != -1) {
                this.currentUser.setEmail(this.request.getParameter("username"));
            } else {
                this.currentUser.setUsername(this.request.getParameter("username"));
            }
        }

        if (this.request.getParameter("password") == null) {
            throw new ApplicationException(this.resource.getLocaleString("login.password.invalid"));
        } else {
            this.currentUser.setPassword(new Security(this.currentUser.getUsername()).encode(this.request.getParameter("password")));
        }
		
/*		if(this.communicator.getParameter(lastformName)==null||this.communicator.getParameter(lastformName).trim().length()==0)
		{
			throw new ApplicationException(this.resource.getLocaleString("login.authorized.invalid"));
		}*/

//		if(!validateCode())
//		{
//			throw new ApplicationException(this.resource.getLocaleString("login.authorized.failed"));
//		}

        if (!checkUser()) {
            throw new ApplicationException(this.resource.getLocaleString("login.usernotexists"));
        }

        this.setLogin();

        return true;
    }

}