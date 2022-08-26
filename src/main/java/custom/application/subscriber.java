package custom.application;

import custom.objects.subscription;
import org.tinystruct.AbstractApplication;
import org.tinystruct.ApplicationException;
import org.tinystruct.data.component.Row;
import org.tinystruct.http.Request;

import static org.tinystruct.http.Constants.HTTP_REQUEST;

public class subscriber extends AbstractApplication {

    private Request request;

    @Override
    public void init() {
        this.setAction("services/subscribe", "subscribe");
        this.setAction("services/unsubscribe", "unsubscribe");
    }

    public String subscribe() throws ApplicationException {
        this.request = (Request) this.context.getAttribute(HTTP_REQUEST);

        String mailto = "moverinfo@gmail.com";
        if (this.request.getParameter("toemail") != null
                && this.request.getParameter("toemail").trim().length() > 0) {
            mailto = this.request.getParameter("toemail");
        } else
            return "false";

        String list = null;
        if (this.request.getParameter("bible") != null) {
            list = "bible";
        }

        if (this.request.getParameter("article") != null) {
            if (list == null)
                list = "article";
            else
                list += ",article";
        }

        if (list == null)
            return "empty";

        String[] addresses = mailto.split(";");

        for (int i = 0; i < addresses.length; i++) {
            if (addresses[i].indexOf('@') < 1
                    || addresses[i].indexOf('@') >= addresses[i]
                    .lastIndexOf('.') + 1) {
                return "invalid";
            } else {
                subscription subscription = new subscription();
                subscription.setAvailable(true);

                Row row = subscription.findOneByKey("email", addresses[i]);
                if (row.size() == 0) {
                    subscription.setEmail(addresses[i]);
                    subscription.setList(list);
                    subscription.append();
                }
            }
        }

        return "true";
    }

    public boolean unsubscribe(String id) throws ApplicationException {
        subscription subscription = new subscription();
        subscription.setId(id);
        subscription.findOneById();
        subscription.setAvailable(false);

        return subscription.update();
    }

    @Override
    public String version() {
        // TODO Auto-generated method stub
        return null;
    }

}
