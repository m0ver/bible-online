package custom.application;

import custom.objects.subscription;
import org.tinystruct.AbstractApplication;
import org.tinystruct.ApplicationException;
import org.tinystruct.data.component.Row;
import org.tinystruct.http.Request;
import org.tinystruct.system.annotation.Action;

import static org.tinystruct.http.Constants.HTTP_REQUEST;

public class subscriber extends AbstractApplication {

    @Override
    public void init() {
        this.setTemplateRequired(false);
    }

    @Action("services/subscribe")
    public String subscribe(Request request) throws ApplicationException {

        String mailto = "moverinfo@gmail.com";
        if (request.getParameter("toemail") != null
                && !request.getParameter("toemail").trim().isEmpty()) {
            mailto = request.getParameter("toemail");
        } else
            return "false";

        String list = null;
        if (request.getParameter("bible") != null) {
            list = "bible";
        }

        if (request.getParameter("article") != null) {
            if (list == null)
                list = "article";
            else
                list += ",article";
        }

        if (list == null)
            return "empty";

        String language = this.getLocale().toString();
        String bibleVersion = null;
        if (request.getParameter("version") != null && !request.getParameter("version").isEmpty()) {
            switch (request.getParameter("version")) {
                case "NIV":
                    bibleVersion = "NIV";
                    break;
                case "ESV":
                    bibleVersion = "ESV";
                    break;
                case "KJV":
                    bibleVersion = "KJV";
                    break;
                default:
                    break;
            }
        }

        String[] addresses = mailto.split(";");
        for (String address : addresses) {
            if (address.indexOf('@') < 1
                    || address.indexOf('@') >= address
                    .lastIndexOf('.') + 1) {
                return "invalid";
            } else {
                subscription subscription = new subscription();
                subscription.setAvailable(true);

                Row row = subscription.findOneByKey("email", address);
                if (row.isEmpty()) {
                    subscription.setEmail(address);
                    subscription.setList(list);
                    subscription.setLanguage(language);
                    subscription.setBibleVersion(bibleVersion != null ? bibleVersion : "");
                    subscription.appendAndGetId();
                } else {
                    subscription.setData(row);
                    subscription.setList(list);
                    subscription.setLanguage(language);
                    subscription.setBibleVersion(bibleVersion != null ? bibleVersion : "");
                    subscription.update();
                }
            }
        }

        return "true";
    }

    @Action("services/unsubscribe")
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
