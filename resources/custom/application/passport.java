package custom.application;

import java.util.Date;
import java.util.Vector;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.tinystruct.ApplicationException;
import org.tinystruct.data.component.Row;
import org.tinystruct.data.component.Table;
import org.tinystruct.handle.Report;
import org.tinystruct.system.Language;
import org.tinystruct.system.Resource;
import org.tinystruct.system.util.IString;
import org.tinystruct.system.util.Security;
import org.tinystruct.system.util.StringUtilities;

import custom.objects.Group;
import custom.objects.Log;
import custom.objects.Member;
import custom.objects.Role;
import custom.objects.User;

public class passport
{
	private String username;
	private String password;
	private String email="";
	private String sessionname="";
	private String where="";
	private Resource resource;
	private Language lang;

	private User currentUser;
	private Report event;
	private boolean recognized=false;

	private HttpSession session;
	private HttpServletRequest request;
	private HttpServletResponse response;
	
	public passport(HttpServletRequest request,HttpServletResponse response, String sessionname) throws ApplicationException
	{
		this.request=request;
		this.response=response;
		this.currentUser=new User();
		
		this.event=Report.getInstance();
		this.event.setClassName(getClass().getName());
		
		this.session=this.request.getSession();
		
		this.sessionname=sessionname;
		if(this.session.getAttribute(sessionname)!=null)
		this.recognized=((Boolean)this.session.getAttribute(sessionname)).booleanValue();
		
		Cookie language=StringUtilities.getCookieByName(request.getCookies(), "language");
		if(language!=null)
		{
			this.setLanguage(Language.valueOf(language.getValue()));
		}
		else
			this.setLanguage(Language.zh_CN);
		
		this.resource=Resource.getInstance(this.lang.toString());
	}
	
	private void setLanguage(Language valueOf) {
		this.lang = valueOf;
	}

	public passport(String username,String password, String api)
	{
		this.currentUser=new User();
		this.currentUser.setUsername(username);
		this.currentUser.setPassword(password);
	}

	public boolean recognized()
	{
		return this.recognized;
	}
	
	public void setLoginAsUser(Object userId) throws ApplicationException
	{
			this.currentUser.setId(userId);
			this.currentUser.findOneById();
			
			this.setlogin();
	}
	
	private void setlogin() throws ApplicationException
	{
		this.recognized=true;
		this.session.setAttribute(sessionname,new Boolean("true"));
		this.session.setAttribute("usr", this.currentUser);
		
		Cookie username=new Cookie("username",this.currentUser.getUsername());		
		username.setMaxAge(24*3600);
		this.response.addCookie(username);	
		
		Member member=new Member();
		Table members=member.findWith("WHERE user_id=?",new Object[]{this.currentUser.getId()});
		
		if(members.size()>0)
		{
			member.setData(members.get(0));
			
			Group group=new Group();
			group.setId(member.getGroupId());
			group.findOneById();
			
			String[] roles=group.getRoles().split(",");
			
			Vector<String> rights=new Vector<String>();
			for(String roleId:roles)
			{
				Role role=new Role();
				role.setId(roleId);
				role.findOneById();
				
				String[] _rights=role.getRights().split(",");
				
				for(String rightId:_rights)
				{
					if(!rights.contains(rightId))
					rights.add(rightId);
				}
			}
			
			this.session.setAttribute("rights", rights);
			
			Log log=new Log();
			Table logs=log.findWith("WHERE user_id=?",new Object[]{this.currentUser.getId()});
			if(logs.size()>0)
			{
				log.setData(logs.firstElement());
				log.setDate(new Date());
				log.update();
			}
			else
			{
				log.setUserId(this.currentUser.getId());
				log.setAction("Logined Successful");
				log.setActionType(0);
				log.setDate(new Date());
				log.append();
			}
		}

	}
	
	public HttpSession getSession()
	{
		return this.session;
	}
	
	public void logout()
	{
		this.recognized=false;
		
			this.session.removeAttribute(this.sessionname);
			this.session.removeAttribute("usr");
			this.session.removeAttribute("rights");
			
//			this.communicator.addCookie(new Cookie("autologin","false"));
	}
//	
//	public boolean validateCode()
//	{
//		String  CodeSessionName=org.mover.system.Security.ValidateCode.getSessionName(this.communicator.getRequest()),
//				getSessionValue=(String)session.getAttribute(CodeSessionName),
//
//				lastformName=org.mover.system.Security.ValidateCode.getLastFormName(),
//				getParameterValue=this.communicator.getParameter(lastformName);
//
//		if(getParameterValue!=null&&getParameterValue.equals(getSessionValue))
//		this.authorized=true;
//		
//		return this.authorized;
//	}
	
	public boolean checkUser() throws ApplicationException
	{
		Object[] parameters=new Object[]{};
		if(currentUser.getUsername()!=null && currentUser.getUsername().trim().length()!=0 && new IString(currentUser.getUsername()).safe())
		{
			this.username=currentUser.getUsername();
			parameters=new Object[]{this.username};
			where="WHERE username=? and status='1'";
		}
		else
		if(currentUser.getEmail()!=null&&new IString(currentUser.getEmail()).safe())
		{
			this.email=currentUser.getEmail();
			parameters=new Object[]{this.email};
			where="WHERE email=? and status='1'";
		}
		else
		{
			return false;
		}

		User u=new User();
		Table list=u.findWith(where,parameters);
		
		if(list.size()>0)
		{
			Row found=list.get(0);
			this.email=found.getFieldInfo("email").stringValue();
			
			if(this.email!=null && this.email.trim().length()>0)
			{
				if(found.getFieldInfo("username")!=null&&found.getFieldInfo("username").stringValue().trim().length()>0)
					currentUser.setUsername(found.getFieldInfo("username").stringValue());
				else
					currentUser.setUsername(this.email);
			}
			else
			{
				return false;
			}
			
			this.password=currentUser.getPassword();
			if(this.password.equals(new Security(this.email).decodePassword(String.valueOf(found.get(0).get("password").value()))))
			{
				currentUser.setData(found);
				this.event.println(currentUser.toString());
				
				currentUser.setLastloginTime(new Date());
				currentUser.setLastloginIP(this.request.getRemoteAddr());
				
				if(this.request.getParameter("autologin")!=null)
				{
					Cookie autologin=new Cookie("autologin",String.valueOf(currentUser.getId()));
					autologin.setMaxAge(24*3600*30);
//					this.communicator.addCookie(autologin);
				}
				
				currentUser.update();
				
				return true;
			}
		}
		
		return false;
	}
	
	public boolean login() throws ApplicationException
	{
//		String lastformName=org.mover.system.Security.ValidateCode.getLastFormName();
		
		if(this.request.getParameter("username")==null)
		{
			throw new ApplicationException(this.resource.getLocaleString("login.username.invalid"));
		}
		else
		{
			if(this.request.getParameter("username").indexOf('@')!=-1)
			{
				this.currentUser.setEmail(this.request.getParameter("username"));
			}
			else
			{
				this.currentUser.setUsername(this.request.getParameter("username"));
			}
		}
		
		if(this.request.getParameter("password")==null)
		{
			throw new ApplicationException(this.resource.getLocaleString("login.password.invalid"));
		}
		else
		{
			this.currentUser.setPassword(this.request.getParameter("password"));
		}
		
/*		if(this.communicator.getParameter(lastformName)==null||this.communicator.getParameter(lastformName).trim().length()==0)
		{
			throw new ApplicationException(this.resource.getLocaleString("login.authorized.invalid"));
		}*/
		
//		if(!validateCode())
//		{
//			throw new ApplicationException(this.resource.getLocaleString("login.authorized.failed"));
//		}
		
		if(!checkUser())
		{
			throw new ApplicationException(this.resource.getLocaleString("login.usernotexists"));
		}
		
		this.setlogin();
		
		return true;
	}

}