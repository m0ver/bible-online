package custom.application;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tinystruct.AbstractApplication;
import org.tinystruct.ApplicationException;
import org.tinystruct.data.component.Builder;
import org.tinystruct.data.component.Struct;
import org.tinystruct.system.util.StringUtilities;

public class communicator extends AbstractApplication {

	Map<String, String> map = Collections.synchronizedMap(new HashMap<String, String>());
	
	@Override
	public void init() {
		// TODO Auto-generated method stub
		this.setAction("communicator", "index");
		this.setAction("communicator/update", "update");
		this.setAction("communicator/save", "save");
		this.setAction("communicator/version", "version");
		
		System.out.println("COMMUNICATOR:Thread["+Thread.currentThread().getId()+"]"+Thread.currentThread().getName());
	}
	
	public communicator index(){
		return this;
	}

	public synchronized void update() throws InterruptedException, IOException, ApplicationException {
		HttpServletResponse response = (HttpServletResponse) this.context
				.getAttribute("HTTP_RESPONSE");
		
		Builder struct;
		while (true) {
			
			wait();
			
			if(this.map.containsKey("message")) {
				struct = new Builder();
				struct.parse(this.map.get("message"));
				
				System.out.println(struct.get("browser")+":"+struct.get("textvalue"));
				response.getWriter().println(
						"<script charset=\"utf-8\"> var message = '" + new StringUtilities(struct.get("textvalue").toString()).replace('\n', "\\n")
								+ "';parent.update(message);</script>");
				response.getWriter().flush();
				
				this.map.remove("message");
			}

		}
	}

	public synchronized boolean save() {
		HttpServletRequest request = (HttpServletRequest) this.context
		.getAttribute("HTTP_REQUEST");
		
		String[] agent = request.getHeader("User-Agent").split(" ");
		
		Builder builder = new Builder();
		builder.put("textvalue", request.getParameter("text"));
		builder.put("browser", agent[agent.length-1]);
		this.map.put("message", builder.toString());
		
		System.out.println("It's ready now!");
		notifyAll();
		return true;
	}

	@Override
	public String version() {
		// TODO Auto-generated method stub
		return null;
	}

}
