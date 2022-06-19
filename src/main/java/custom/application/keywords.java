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

import custom.objects.keyword;
import org.tinystruct.AbstractApplication;
import org.tinystruct.ApplicationException;
import org.tinystruct.data.component.Row;
import org.tinystruct.data.component.Table;
import org.tinystruct.http.Method;
import org.tinystruct.http.Request;
import org.tinystruct.system.util.StringUtilities;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;

import static org.tinystruct.handler.DefaultHandler.HTTP_REQUEST;

public class keywords extends AbstractApplication {

    private Request request;

    @Override
    public void init() {
        // TODO Auto-generated method stub
        this.setAction("bible/keywords", "list");
    }

    @Override
    public String version() {
        // TODO Auto-generated method stub
        return null;
    }

    public String list() throws ApplicationException {
        String keyword;
        this.request = (Request) this.context.getAttribute(HTTP_REQUEST);

        if (this.request.getParameter("keyword") != null && this.request.getParameter("keyword").equals("") == false && new StringUtilities(this.request.getParameter("keyword").toString()).safe()) {
            if (this.request.method() == Method.GET) {
                keyword = this.request.getParameter("keyword").toString();
                try {
                    keyword = new String(keyword.getBytes("ISO8859-1"), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else
                keyword = this.request.getParameter("keyword").toString();
        } else {
            keyword = null;
        }

        String javascript_block = "";
        String keywordlist = "", keywordvisit = "";

        keyword _keyword = new keyword();
        Table findTable = _keyword.findWith(
                "WHERE keyword LIKE ? ORDER BY visit LIMIT 0,7",
                new Object[]{"%" + keyword + "%"});

        Row currentRow;
        String k = new String(""), v = new String(""), k_item = new String(""), v_item = new String(
                "");

        Iterator<Row> iterator = findTable.iterator();
        while (iterator.hasNext()) {
            currentRow = iterator.next();
            k_item = currentRow.getFieldInfo("keyword").stringValue();
            if (k != null && k.trim().length() > 0) {
                k += ",\"" + k_item + "\"";
            } else {
                k = "\"" + k_item + "\"";
            }

            v_item = currentRow.getFieldInfo("visit").stringValue();
            if (v != null && v.trim().length() > 0) {
                v += ",\"" + v_item + "\"";
            } else {
                v = "\"" + v_item + "\"";
            }
        }

        keywordlist = "new Array(";
        keywordlist += k;
        keywordlist += ")";

        keywordvisit = "new Array(";
        keywordvisit += v;
        keywordvisit += ")";
        javascript_block = "window.listener.sendRPCDone(frameElement,\""
                + keyword + "\"," + keywordlist + "," + keywordvisit
                + ",new Array(\"\"));";

        return javascript_block;
    }

}